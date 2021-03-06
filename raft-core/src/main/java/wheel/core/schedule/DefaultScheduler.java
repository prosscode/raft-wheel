package wheel.core.schedule;


import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 * @describe: 默认定时器实现
 * @created by shuang.peng
 * @date: 2020/08/23
 */
public class DefaultScheduler implements Scheduler {

    // 最小/最大的选举超时使劲啊
    private final int minElectionTimeout;
    private final int maxElectionTimeout;
    // 初次日志复制延迟时间
    private final int logReplicationDelay;
    // 日志复制间隔
    private final int logReplicationInterval;
    // 随机数生成器
    private final Random electionTimeoutRandom;

    private final ScheduledExecutorService scheduledExecutorService;

    // 构造函数
    public DefaultScheduler(int minElectionTimeout, int maxElectionTimeout, int logReplicationDelay, int logReplicationInterval) {
        // 判断选举参数是否有效
        if(minElectionTimeout <= 0 || maxElectionTimeout <= 0 || minElectionTimeout > maxElectionTimeout){
            throw new IllegalArgumentException("election timeout should not be 0 or min > max");
        }
        // 初次日志复制延迟以及日志复制间隔
        if(logReplicationDelay < 0 || logReplicationInterval <= 0){
            throw new IllegalArgumentException("log replication delay < 0 or log replication interval <= 0");
        }

        this.minElectionTimeout = minElectionTimeout;
        this.maxElectionTimeout = maxElectionTimeout;
        this.logReplicationDelay = logReplicationDelay;
        this.logReplicationInterval = logReplicationInterval;
        electionTimeoutRandom = new Random();
        scheduledExecutorService = newSingleThreadScheduledExecutor(r->new Thread(r,"scheduler"));
    }


    // 日志复制定时器
    @Override
    public LogReplicationTask scheduleLogReplicationTask(Runnable task) {
        ScheduledFuture<?> scheduledFuture = this.scheduledExecutorService.scheduleAtFixedRate(task, logReplicationDelay,logReplicationInterval, TimeUnit.MILLISECONDS);
        return new LogReplicationTask(scheduledFuture);
    }

    // 选举超时定时器
    @Override
    public ElectionTimeout scheduleElectionTimeout(Runnable task) {
        /**
         * 随机超时时间
         * 为了减少split vote的影响，选举超时区间内随机一个超时时间
         * 通过scheduledExecutorService创建一个一次性的scheduledFuture，构造ElectionTimeout返回
         */
        int timeout = electionTimeoutRandom.nextInt(maxElectionTimeout - minElectionTimeout) + minElectionTimeout;

        ScheduledFuture<?> scheduledFuture = scheduledExecutorService.schedule(task, timeout, TimeUnit.SECONDS);
        return new ElectionTimeout(scheduledFuture);
    }

    @Override
    public void stop() throws InterruptedException {

    }
}
