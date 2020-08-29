package wheel.node.node.role;

/**
 * @describe: follower节点角色
 *   角色字段的不可变（final）：follower选举超时或者接受到来自leader节点服务器的心跳时，必须新建一个角色实例
 *                          保证并发环境下的数据安全，以及进一步简化设计
 *    1。需要去投票
 *    2。需要知道leader节点
 * @author: 彭爽 pross.peng
 * @date: 2020/08/23
 */

public class FollowerNodeRole extends AbstractNodeRole {
    // 投过票的节点（有可能为空，不可能被投票）
    private final NodeId votedFor;
    // 当前leader节点的ID（有可能为空）
    private final NodeId leaderId;
    // 选举超时
    private final ElectionTimeout electionTimeout;

    public FollowerNodeRole(RoleName name,
                            int term, NodeId votedFor,
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
    public void cancelTimeoutTask() {
        electionTimeout.cancel();
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
