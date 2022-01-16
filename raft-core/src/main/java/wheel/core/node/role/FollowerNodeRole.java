package wheel.core.node.role;

import wheel.core.node.NodeId;
import wheel.core.schedule.ElectionTimeout;

/**
 * @describe: follower节点角色
 *    1。需要去投票
 *    2。需要知道leader节点
 * @created by shuang.peng
 * @date: 2021/12/23
 */
public class FollowerNodeRole extends AbstractNodeRole {
    // 投过票的节点（有可能为空，不可能被投票）
    private final NodeId votedFor;
    // 当前leader节点的ID（有可能为空）
    private final NodeId leaderId;
    // 选举超时
    private final ElectionTimeout electionTimeout;

    public FollowerNodeRole(int term,
                            NodeId votedFor,
                            NodeId leaderId,
                            ElectionTimeout electionTimeout) {
        super(RoleName.FOLLOWER, term);
        this.votedFor = votedFor;
        this.leaderId = leaderId;
        this.electionTimeout = electionTimeout;
    }

    public NodeId getVotedFor() {
        return votedFor;
    }

    public NodeId getLeaderId() {
        return leaderId;
    }

    // 取消选举定时器
    @Override
    public void cancelTimeoutOrTask() {
        electionTimeout.cancel();
    }

    @Override
    public NodeId getLeaderId(NodeId selfId) {
        return null;
    }

    @Override
    public String toString() {
        return "FollowerNodeRole{" +
                "votedFor=" + votedFor +
                ", leaderId=" + leaderId +
                ", electionTimeout=" + electionTimeout +
                ", term=" + term +
                '}';
    }
}
