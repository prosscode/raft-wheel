package wheel.core.node;

import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.FutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wheel.core.node.role.*;
import wheel.core.node.store.NodeStore;
import wheel.core.rpc.message.RequestVoteResult;
import wheel.core.rpc.message.RequestVoteRpc;
import wheel.core.rpc.message.RequestVoteRpcMessage;
import wheel.core.schedule.ElectionTimeout;
import wheel.core.schedule.LogReplicationTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Objects;

import static wheel.core.node.role.RoleName.CANDIDATE;
import static wheel.core.node.role.RoleName.FOLLOWER;

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
        if(newRole.getName() == FOLLOWER){
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
        nodeRole.cancelTimeoutOrTask();
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
     * node收到request vote，比较消息中的term和本地的term大小，根据日志决定是否投票
     * @param rpcMessage rpc过来的对象信息
     */
    @Subscribe
    void onReceiveRequestVoteRpc(RequestVoteRpcMessage rpcMessage) {
        nodeContext.getTaskExecutor().submit(()->{
            nodeContext.getConnector().replyRequestVote(
                    doProcessRequestVoteRpc(rpcMessage),
                    // 发送消息的节点
                    // todo check endpoints
                    nodeContext.getGroup().findMember(rpcMessage.getSourceNodeId()).getEndpoint()
            );
        });

    }

    /**
     * 节点收到request vote响应后返回结果处理
     * @param result
     */
    @Subscribe
    void onReceiveRequestVoteResult(RequestVoteResult result){
        nodeContext.getTaskExecutor().submit(()->{
            doProcessRequestVoteResult(result);
        });
    }

    private void doProcessRequestVoteResult(RequestVoteResult result) {
        // 如果对象的term对自己大，则退化为follower角色
        if (result.getTerm() > nodeRole.getTerm()) {
            becomeFollower(result.getTerm(), null, null, true);
            return;
        }
        // 如果自己不是candidate角色，则忽略
        if(nodeRole.getName()!=RoleName.CANDIDATE){
            logger.debug("receive request vote result and current role is not candidate,ignore.");
        }
        // 如果对方的term比自己小或者对象没有给自己投票，则忽略
        if(result.getTerm() < nodeRole.getTerm() || !result.isVoteGranted()){
            return;
        }
        // 当前票数
        int currentvVoteCount = ((CandidateNodeRole) nodeRole).getVotesCount();
        // 节点数
        int countOfMajor = nodeContext.getGroup().getCountOfMajor();
        logger.debug("votes count {}, node count {}",currentvVoteCount,countOfMajor);
        // 取消选举超时
        nodeRole.cancelTimeoutOrTask();
        // 判断票数
        if (currentvVoteCount > countOfMajor / 2) {
            // 成为Leader角色
            logger.info("become leader, term {}", nodeRole.getTerm());
            changeToRole(new LeaderNodeRole(nodeRole.getTerm(), scheduleLogReplicationTask()));
        } else {
            // 修改收到的投票数，并重置创建选举超时
            changeToRole(new CandidateNodeRole(nodeRole.getTerm(),
                    currentvVoteCount,
                    scheduleElectionTimeout()));
        }
    }

    // 重置日志复制进度(nextIndex和matchIndex重置为0)
    private LogReplicationTask scheduleLogReplicationTask() {
        return nodeContext.getScheduler().scheduleLogReplicationTask(this::replicateLog);
    }

    // 日志复制入口方法
    void replicateLog() {
        nodeContext.getTaskExecutor().submit(this::doReplicateLog);
    }



    private void doReplicateLog() {

    }

    /**
     * 处理收到远程rpc信息的方法
     * @param rpcMessage
     * @return 返回回去的结果
     */
    RequestVoteResult doProcessRequestVoteRpc(RequestVoteRpcMessage rpcMessage){
        // 如果对方的term比自己小，则不投票并且返回自己的term给对象
        RequestVoteRpc rpc = rpcMessage.get();
        if (rpc.getTerm() < nodeRole.getTerm()) {
            logger.debug("term from rpc < current term ,don't vote ({} < {})", rpc.getTerm(), nodeRole.getTerm());
            return new RequestVoteResult(nodeRole.getTerm(),false);
        }

        // 否则,无条件投票
        boolean voteForCandidate = true;

        // 如果对象的term比自己大 则切换为Follower角色
        if (rpc.getTerm() > nodeRole.getTerm()) {
            becomeFollower(rpc.getTerm(), (voteForCandidate ? rpc.getCandidateId() : null), null, true);
            return new RequestVoteResult(rpc.getTerm(), voteForCandidate);
        }

        // 如果对方的term和自己本地的term一样大
        switch (nodeRole.getName()){
            case FOLLOWER:
                FollowerNodeRole follower = (FollowerNodeRole) nodeRole;
                NodeId votedFor = follower.getVotedFor();
                /**
                 * case 1:自己尚未投票，并且对方的日志比自己新
                 * case 2:自己已经给对方投过票
                 * 投票后需要切换为Follower角色
                 */
                if ((votedFor == null && voteForCandidate) || Objects.equals(votedFor, rpc.getCandidateId())) {
                    becomeFollower(
                            nodeRole.getTerm(),
                            rpc.getCandidateId(),
                            null,
                            true);
                    return new RequestVoteResult(rpc.getTerm(), true);
                }
                return new RequestVoteResult(nodeRole.getTerm(), false);
            case CANDIDATE: // 已经给自己投过票，所以不会给其它节点投票
            case LEADER: // Leader不会给其它节点投票
                return new RequestVoteResult(nodeRole.getTerm(), false);
            default:
                throw new IllegalStateException("unexpected node role [" + nodeRole.getName() + "]");
        }

    }

    private void becomeFollower(int term, NodeId votedFor, NodeId leaderId, boolean scheduleElectionTimeout) {
        // 先取消选举超时
        nodeRole.cancelTimeoutOrTask();
        if (leaderId != null && !leaderId.equals(nodeRole.getLeaderId(nodeContext.getSelfId()))) {
            logger.info("current leader is {},term {}", leaderId, term);
        }
        // 重新创建选举超时定时器
        ElectionTimeout electionTimeout =
                scheduleElectionTimeout ? scheduleElectionTimeout() : ElectionTimeout.NONE;
        changeToRole(new FollowerNodeRole(term, votedFor, leaderId, electionTimeout));
    }




}
