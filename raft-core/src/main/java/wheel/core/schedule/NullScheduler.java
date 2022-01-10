package wheel.core.schedule;

/**
 * @Date 2022/1/9
 * @Created by shuang.peng
 * @Description NullScheduler
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
