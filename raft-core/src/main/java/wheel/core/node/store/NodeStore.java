package wheel.core.node.store;

import wheel.core.node.NodeId;

/**
 * @Date 2022/1/2
 * @Created by shuang.peng
 * @Description NodeStore;节点投票后的状态需要持久化,保证一个节点在一个选举范围内只投票一次
 */
public interface NodeStore {
    // 获取current term
    int getTerm();
    // 设置current term
    void setTerm(int term);
    // 获取votedFor
    NodeId getVotedFor();
    // 设置voteFor
    void setVotedFor(NodeId votedFor);
    // 关闭文件
    void close();
}
