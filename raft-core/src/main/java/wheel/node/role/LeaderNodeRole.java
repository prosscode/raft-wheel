package wheel.node.role;

import wheel.node.RoleName;

/**
 * @describe: leader节点角色功能
 *  1。 需要定时给follower节点发送心跳（日志复制的定时器）
 *  2。 需要把控日志复制进度（不在节点角色中，和集群成员表在一起实现）
 *
 * @author: 彭爽 pross.peng
 * @date: 2020/08/23
 */
public class LeaderNodeRole extends AbstractNodeRole {

    // 日志复制定时器
    private final LogReplicationTask logReplicationTask;

    public LeaderNodeRole(RoleName name, int term, LogReplicationTask logReplicationTask) {
        super(name, term);
        this.logReplicationTask = logReplicationTask;
    }

    // 取消日志复制定时任务
    @Override
    public void cancelTimeoutTask() {
        logReplicationTask.cancel();
    }

    @Override
    public String toString() {
        return "LeaderNodeRole{" +
                "logReplicationTask=" + logReplicationTask +
                ", term=" + term +
                '}';
    }
}
