package wheel.core.node.store;

import wheel.core.node.NodeId;

/**
 * @Date 2022/1/2
 * @Author shuang.peng
 * @Description MemoryNodeStore 测试用
 */
public class MemoryNodeStore implements NodeStore {
    private int term;
    private NodeId voteFor;

    public MemoryNodeStore() {}

    public MemoryNodeStore(int term, NodeId voteFor) {
        this.term = term;
        this.voteFor = voteFor;
    }

    @Override
    public int getTerm() {
        return term;
    }

    @Override
    public void setTerm(int term) {
        this.term = term;
    }

    @Override
    public NodeId getVotedFor() {
        return voteFor;
    }

    @Override
    public void setVotedFor(NodeId votedFor) {
        this.voteFor = votedFor;
    }

    @Override
    public void close() {

    }
}
