package wheel.core.node;

import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wheel.core.node.role.*;
import wheel.core.node.store.MemoryNodeStore;
import wheel.core.node.store.NodeStore;
import wheel.core.rpc.message.*;
import wheel.core.schedule.ElectionTimeout;
import wheel.core.schedule.LogReplicationTask;

import java.util.Collection;
import java.util.Objects;

import static wheel.core.node.role.RoleName.FOLLOWER;

/**
 * @Date 2022/1/3
 * @Created by shuang.peng
 * @Description NodeImpl 节点实现类
 *
 * 1.处理request vote消息（onReceiveRequestVoteRpc），并回复（onReceiveRequestVoteResult）
 * 2.处理心跳消息append entries（onReceiveAppendEntriesRpc），并回复（onReceiveAppendEntriesResult）
 */
public class NodeImpl implements Node{
    private static final Logger logger = LoggerFactory.getLogger(NodeImpl.class);
    // node信息
    private NodeContext nodeContext;
    // node状态
    private boolean started;
    // node角色
    private AbstractNodeRole nodeRole;

    public NodeImpl(NodeContext nodeContext) {
        this.nodeContext = nodeContext;
    }

    // 获取核心组件上下文
    public NodeContext getNodeContext() {
        return this.nodeContext;
    }

    public AbstractNodeRole getNodeRole(){
        return this.nodeRole;
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
        // store持久化到memory
        nodeContext.setStore(new MemoryNodeStore(0,NodeId.of(null)));

        // 启动时默认时follower角色,获取持久化的node状态
        NodeStore store = nodeContext.getStore();
        changeToRole(new FollowerNodeRole(store.getTerm(),
                store.getVotedFor(),
                null,
                scheduleElectionTimeout()));
        started = true;
    }

    @Override
    public void stop() throws InterruptedException {
        // node state check
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

    // schedule election timeout.
    private ElectionTimeout scheduleElectionTimeout() {
        return nodeContext.getScheduler().scheduleElectionTimeout(this::electionTimeout);
    }

    public void electionTimeout() {
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
     * 收到request vote的消息
     * @param rpcMessage rpc过来的对象信息
     */
    @Subscribe
    public void onReceiveRequestVoteRpc(RequestVoteRpcMessage rpcMessage) {
        nodeContext.getTaskExecutor().submit(() -> {
            nodeContext.getConnector().replyRequestVote(
                    doProcessRequestVoteRpc(rpcMessage),
                    // 发送消息的节点
                    // todo check endpoints
                    nodeContext.getGroup().findMember(rpcMessage.getSourceNodeId()).getEndpoint()
            );
        });

    }

    // 收到rpc信息处理
    RequestVoteResult doProcessRequestVoteRpc(RequestVoteRpcMessage rpcMessage) {
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

    /**
     * 节点收到request vote响应后返回结果处理
     * @param result
     */
    @Subscribe
    public void onReceiveRequestVoteResult(RequestVoteResult result){
        nodeContext.getTaskExecutor().submit(()->{
            doProcessRequestVoteResult(result);
        });
    }

    // 返回结果处理
    private void doProcessRequestVoteResult(RequestVoteResult result) {
        // 如果对象的term对自己大，则退化为follower角色
        if (result.getTerm() > nodeRole.getTerm()) {
            becomeFollower(result.getTerm(), null, null, true);
            return;
        }
        // 如果自己不是candidate角色，则忽略
        if(nodeRole.getName()!=RoleName.CANDIDATE){
            logger.debug("receive request vote result and current role is not candidate,ignore.");
            return;
        }
        // 如果对方的term比自己小或者对象没有给自己投票，则忽略
        if(result.getTerm() < nodeRole.getTerm() || !result.isVoteGranted()){
            return;
        }
        // 当前票数
        int currentVoteCount = ((CandidateNodeRole) nodeRole).getVotesCount();
        // 节点数
        int countOfMajor = nodeContext.getGroup().getCountOfMajor();
        logger.debug("votes count {}, node count {}",currentVoteCount,countOfMajor);
        // 取消选举超时
        nodeRole.cancelTimeoutOrTask();
        // 判断票数
        if (currentVoteCount > countOfMajor / 2) {
            // 成为Leader角色
            logger.info("become leader, term {}", nodeRole.getTerm());
            changeToRole(new LeaderNodeRole(nodeRole.getTerm(), scheduleLogReplicationTask()));
        } else {
            // 修改收到的投票数，并重置创建选举超时
            changeToRole(new CandidateNodeRole(nodeRole.getTerm(),
                    currentVoteCount,
                    scheduleElectionTimeout()));
        }
    }

    // 重置日志复制进度(nextIndex和matchIndex重置为0)
    private LogReplicationTask scheduleLogReplicationTask() {
        return nodeContext.getScheduler().scheduleLogReplicationTask(this::replicateLog);
    }

    // 日志复制入口方法
    public void replicateLog() {
        nodeContext.getTaskExecutor().submit(this::doReplicateLog);
    }

    private void doReplicateLog() {
        logger.debug("replicate log");
        // 给日志复制对象节点发送append entries消息
        Collection<GroupMember> members = nodeContext.getGroup().listReplicationTarget();
        for (GroupMember groupMember : members) {
            doReplicateLogRpc(groupMember);
        }
    }

    private void doReplicateLogRpc(GroupMember groupMember) {
        AppendEntriesRpc appendEntriesRpc = new AppendEntriesRpc();
        appendEntriesRpc.setTerm(nodeRole.getTerm());
        appendEntriesRpc.setLeaderId(nodeContext.getSelfId());
        appendEntriesRpc.setPrevLogIndex(0);
        appendEntriesRpc.setPrevLogTerm(0);
        appendEntriesRpc.setLeaderCommit(0);
        nodeContext.getConnector().sendAppendEntries(appendEntriesRpc, groupMember.getEndpoint());

    }



    /**
     * 收到来自Leader节点的心跳信息
     * @param rpcMessage
     */
    @Subscribe
    public void onReceiveAppendEntriesRpc(AppendEntriesRpcMessage rpcMessage){
        nodeContext.getTaskExecutor().submit(()->{
            nodeContext.getConnector().replyAppendEntries(
                    doProcessAppendEntriesRpc(rpcMessage),
                    // 发送消息的节点
                    nodeContext.getGroup().findMember(rpcMessage.getSourceNodeId()).getEndpoint()
            );
        });
    }

    private AppendEntriesResult doProcessAppendEntriesRpc(AppendEntriesRpcMessage rpcMessage) {
        AppendEntriesRpc entriesRpc = rpcMessage.get();
        // 如果对方的term比自己的小，则回复自己的term
        if(entriesRpc.getTerm() < nodeRole.getTerm()){
            return new AppendEntriesResult(nodeRole.getTerm(), false);
        }

        // 如果对象的term比自己的大，则自己成为follower角色
        if(entriesRpc.getTerm() > nodeRole.getTerm()){
            becomeFollower(entriesRpc.getTerm(),null,entriesRpc.getLeaderId(),true);
            // 追加日志
            return new AppendEntriesResult(entriesRpc.getTerm(),appendEntries(entriesRpc));
        }


        switch (nodeRole.getName()) {
            case FOLLOWER:
                // 设置leader并重置选举定时器
                becomeFollower(entriesRpc.getTerm(),
                        ((FollowerNodeRole) nodeRole).getVotedFor(),
                        entriesRpc.getLeaderId(),
                        true);
                // 追加日志
                return new AppendEntriesResult(entriesRpc.getTerm(), appendEntries(entriesRpc));
            case CANDIDATE:
                // 如果有两个Candidate角色，并且另外一个candidate已经成为了leader，则当前节点成为follower，并重置选举定时器
                becomeFollower(entriesRpc.getTerm(),
                        null,
                        entriesRpc.getLeaderId(),
                        true);
                // 追加日志
                return new AppendEntriesResult(entriesRpc.getTerm(), appendEntries(entriesRpc));
            case LEADER:
                // leader角色收到appendentries消息，打印日志
                logger.warn("receive append entries rpc from another leader {}, ignore.", entriesRpc.getLeaderId());
                return new AppendEntriesResult(entriesRpc.getTerm(), false);
            default:
                throw new IllegalStateException("unexpected node role [" + nodeRole.getName() + "]");
        }

    }

    private boolean appendEntries(AppendEntriesRpc rpc) {
        return true;
    }

    /**
     *  leader节点收到其它节点的响应
     * @param resultMessage
     */
    @Subscribe
    public void onReceiveAppendEntriesResult(AppendEntriesResultMessage resultMessage){
        nodeContext.getTaskExecutor().submit(()->doProcessAppendEntriesResult(resultMessage));

    }
    private void doProcessAppendEntriesResult(AppendEntriesResultMessage resultMessage) {
        AppendEntriesResult entriesResult = resultMessage.get();
        // 如果对方的term比自己大 则自己成为follower角色
        if (entriesResult.getTerm() > nodeRole.getTerm()) {
            becomeFollower(entriesResult.getTerm(),null,null,true);
            return;
        }
        // check role
        if(nodeRole.getName() != RoleName.LEADER){
            logger.warn("receive append entries result from node {}, but current node is not leader, ignore",resultMessage.getSourceNodeId());
        }
    }

}
