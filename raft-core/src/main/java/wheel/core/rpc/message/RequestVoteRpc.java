package wheel.core.rpc.message;

import wheel.core.rpc.NodeId;

/**
 * @describe: raft核心算法中主要有两种消息
 *  1。request vote 投票请求
 *  2。append entries 日志复制请求
 * @created by shuang.peng
 * @date: 2020/09/12
 */
public class RequestVoteRpc {

    // 选举term
    private int term;
    // 候选者节点ID，一般都是发送者自己
    private NodeId candidateId;
    // 候选者最后一条日志的索引
    private int lastLogIndex = 0;
    // 候选者最后一条日志的term
    private int lastLogTerm = 0;

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public NodeId getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(NodeId candidateId) {
        this.candidateId = candidateId;
    }

    public int getLastLogIndex() {
        return lastLogIndex;
    }

    public void setLastLogIndex(int lastLogIndex) {
        this.lastLogIndex = lastLogIndex;
    }

    public int getLastLogTerm() {
        return lastLogTerm;
    }

    public void setLastLogTerm(int lastLogTerm) {
        this.lastLogTerm = lastLogTerm;
    }

    @Override
    public String toString() {
        return "RequestVoteRpc{" +
                "term=" + term +
                ", candidateId=" + candidateId +
                ", lastLogIndex=" + lastLogIndex +
                ", lastLogTerm=" + lastLogTerm +
                '}';
    }
}
