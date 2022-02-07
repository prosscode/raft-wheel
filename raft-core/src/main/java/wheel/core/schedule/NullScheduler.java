package wheel.core.schedule;

import afu.org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Date 2022/1/9
 * @Author shuang.peng
 * @Description NullScheduler 用于测试的定时器组件
 */
public class NullScheduler implements Scheduler {

    private static final Logger logger = LoggerFactory.getLogger(NullScheduler.class);

    @Override
    public LogReplicationTask scheduleLogReplicationTask(@NonNull Runnable task) {
        logger.debug("scheduler log replication task");
        return LogReplicationTask.NONE;
    }

    @Override
    public ElectionTimeout scheduleElectionTimeout(Runnable task) {
        logger.debug("schedule election timeout");
        return ElectionTimeout.NONE;
    }

    @Override
    public void stop() {

    }
}
