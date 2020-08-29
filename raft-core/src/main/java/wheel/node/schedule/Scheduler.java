package wheel.node.schedule;

/**
 * @describe: 定时器组件
 *  选举超时和日志复制定时器，前者执行一次，后者可执行多次
 *  选举超时有三种情况：
 *      1。新建选举超时
 *      2。取消选举超时
 *      3。重置选举超时，在follower角色收到leader角色的心跳消息时进行
 *
 * @author: 彭爽 pross.peng
 * @date: 2020/08/23
 */
public interface Scheduler {

    // 创建日志复制定时任务
    LogReplicationTask scheduleLogReplicationTask(Runnable task);

    // 创建选举超时器
    ElectionTimeout scheduleElectionTimeout(Runnable task);

    // 关闭定时器
    void stop() throws InterruptedException;
}
