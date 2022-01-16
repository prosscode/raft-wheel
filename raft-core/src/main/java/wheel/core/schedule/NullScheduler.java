package wheel.core.schedule;

/**
 * @Date 2022/1/9
 * @Created by shuang.peng
 * @Description NullScheduler 测试定时器组件
 */
public class NullScheduler implements Scheduler{

    @Override
    public LogReplicationTask scheduleLogReplicationTask(Runnable task) {
        return null;
    }

    @Override
    public ElectionTimeout scheduleElectionTimeout(Runnable task) {
        return null;
    }

    @Override
    public void stop() throws InterruptedException {

    }
}
