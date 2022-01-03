package wheel.core.node;

import com.google.common.util.concurrent.FutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wheel.core.node.role.AbstractNodeRole;
import wheel.core.node.role.CandidateNodeRole;
import wheel.core.node.role.FollowerNodeRole;
import wheel.core.node.role.RoleName;
import wheel.core.node.store.NodeStore;
import wheel.core.rpc.message.RequestVoteRpc;
import wheel.core.rpc.message.RequestVoteRpcMessage;
import wheel.core.schedule.ElectionTimeout;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @Date 2022/1/3
 * @Created by shuang.peng
 * @Description NodeImpl 节点实现类
 */
public class NodeImpl implements Node{
    private static final Logger logger = LoggerFactory.getLogger(NodeImpl.class);

    // node信息
    private NodeContext nodeContext;
    // node状态
    private boolean started;
    // node角色
    private AbstractNodeRole nodeRole;

    // callback for async tasks.
    private static final FutureCallback<Object> LOGGING_FUTURE_CALLBACK = new FutureCallback<Object>() {
        @Override
        public void onSuccess(@Nullable Object result) {}

        @Override
        public void onFailure(@Nonnull Throwable t) {
            logger.warn("failure", t);
        }
    };

    public NodeImpl(NodeContext nodeContext) {
        this.nodeContext = nodeContext;
    }

    @Override
    public void start() {
        // 如果已经启动，则跳过
        if(started){
            return;
        }

        // 注册到EventBus
        nodeContext.getEventBus().register(this);
        // 初始化Connector
        nodeContext.getConnector().initialize();

        // 启动时默认时follower角色,获取持久化的node状态
        NodeStore store = nodeContext.getStore();
        changeToRole(new FollowerNodeRole(store.getTerm(),
                store.getVotedFor(),
//                NodeId.of(null),
                null,
                scheduleElectionTimeout()));
        started = true;
    }

    @Override
    public void stop() throws InterruptedException {
        // state check
        if(!started){
            throw new IllegalStateException("node not started");
        }
        // 关闭定时器
        nodeContext.getScheduler().stop();
        // 关闭Connector
        nodeContext.getConnector().close();
        // 关闭任务执行器
        nodeContext.getTaskExecutor().shutdown();
        started = false;
    }

    // 角色变更,状态同步
    void changeToRole(AbstractNodeRole newRole){
        logger.debug("node {}, role state changed -> {}",nodeContext.getSelfId(),newRole);
        NodeStore store = nodeContext.getStore();
        store.setTerm(newRole.getTerm());
        if(newRole.getName() == RoleName.FOLLOWER){
            store.setVotedFor(((FollowerNodeRole) newRole).getVotedFor());
        }
        nodeRole = newRole;
    }

    // schedule election timeout.
    private ElectionTimeout scheduleElectionTimeout() {
        return nodeContext.getScheduler().scheduleElectionTimeout(this::electionTimeout);
    }

    // election timeout
    void electionTimeout() {
        nodeContext.getTaskExecutor().submit(this::doProcessElectionTimeout);
    }

    private void doProcessElectionTimeout() {
        // leader不会选举超时
        if(nodeRole.getName() == RoleName.LEADER){
            logger.warn("node {}, current role is leader, ignore election timeout",nodeContext.getSelfId());
            return;
        }
        // follower节点需要发起选举
        // candidate节点需要再次发起选举
        int newTerm = nodeRole.getTerm() + 1;
        nodeRole.cancelTimeoutTask();
        logger.info("start election, role {}",nodeRole.getName());
        // 变成candidate角色
        changeToRole(new CandidateNodeRole(newTerm,scheduleElectionTimeout()));
        // 发送request vote消息
        RequestVoteRpc voteRpc = new RequestVoteRpc();
        voteRpc.setTerm(newTerm);
        voteRpc.setCandidateId(nodeContext.getSelfId());
        voteRpc.setLastLogIndex(0);
        voteRpc.setLastLogTerm(0);
        nodeContext.getConnector().sendRequestVote(voteRpc,nodeContext.getGroup().listEndpointExceptSelf());
    }


    /**
     * node收到request vote，比较消息中的term和本地的term大小，之后根据日志决定是否投票
     * 1.如果消息中的term比本地term大。则切换为follower角色
     */
    void onReceiveRequestVoteRpc(RequestVoteRpcMessage rpcMessage) {
        nodeContext.getTaskExecutor().submit(new Runnable() {
            @Override
            public void run() {

            }
        });

    }

    void doProcessRequestVoteRpc(RequestVoteRpcMessage rpcMessage){

    }

}
