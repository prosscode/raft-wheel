package wheel.core.rpc.message;

import wheel.core.node.NodeId;

import java.util.Collections;
import java.util.List;

/**
 * @describe:
 * @created by shuang.peng
 * @date: 2021/12/26
 */
public class AppendEntriesRpc {

    // 选举term
    private int term;
    // leader节点id
    private NodeId leaderId;
    // 前一条日志的索引
    private int prevLogIndex = 0;
    // 前一条日志的term
    private int prevLogTerm = 0;
    // 复制的日志条目
    private List<Object> entries = Collections.emptyList();
    // leader的commitIndex
    private int leaderCommit;

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public NodeId getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(NodeId leaderId) {
        this.leaderId = leaderId;
    }

    public int getPrevLogIndex() {
        return prevLogIndex;
    }

    public void setPrevLogIndex(int prevLogIndex) {
        this.prevLogIndex = prevLogIndex;
    }

    public int getPrevLogTerm() {
        return prevLogTerm;
    }

    public void setPrevLogTerm(int prevLogTerm) {
        this.prevLogTerm = prevLogTerm;
    }

    public List<Object> getEntries() {
        return entries;
    }

    public void setEntries(List<Object> entries) {
        this.entries = entries;
    }

    public int getLeaderCommit() {
        return leaderCommit;
    }

    public void setLeaderCommit(int leaderCommit) {
        this.leaderCommit = leaderCommit;
    }

    @Override
    public String toString() {
        return "AppendEntriesRpc{" +
                "term=" + term +
                ", leaderId=" + leaderId +
                ", prevLogIndex=" + prevLogIndex +
                ", prevLogTerm=" + prevLogTerm +
                ", entries.size=" + entries.size() +
                ", leaderCommit=" + leaderCommit +
                '}';
    }
}
