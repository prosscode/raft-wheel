package wheel.core.node.role;

import wheel.core.node.NodeId;
import wheel.core.schedule.LogReplicationTask;

/**
 * @describe: leader节点角色功能
 *  1。 需要定时给follower节点发送心跳
 *  2。 需要把控日志复制进度（不在节点角色中，和集群成员表在一起实现）
 *
 *  leader角色被创建后，除非切换成其它角色，否则不会有修改
 * @created by shuang.peng
 * @date: 2020/08/23
 */
public class LeaderNodeRole extends AbstractNodeRole {

    // 日志复制定时器
    private final LogReplicationTask logReplicationTask;

    public LeaderNodeRole(RoleName name, int term, LogReplicationTask logReplicationTask) {
        super(name, term);
        this.logReplicationTask = logReplicationTask;
    }

    public LeaderNodeRole(int term, LogReplicationTask logReplicationTask) {
        super(RoleName.LEADER, term);
        this.logReplicationTask = logReplicationTask;
    }

    // 取消日志复制定时任务
    @Override
    public void cancelTimeoutOrTask() {
        logReplicationTask.cancel();
    }

    @Override
    public NodeId getLeaderId(NodeId selfId) {
        return selfId;
    }

    @Override
    public String toString() {
        return "LeaderNodeRole{" +
                "logReplicationTask=" + logReplicationTask +
                ", term=" + term +
                '}';
    }
}
