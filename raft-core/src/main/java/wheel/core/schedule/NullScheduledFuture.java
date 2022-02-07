package wheel.core.schedule;

import java.util.concurrent.*;

/**
 * @Date 2022/1/9
 * @Author shuang.peng
 * @Description NullScheduledFuture, 构造ElectionTimeout和LogReplicationTask的NONE实例
 */
public class NullScheduledFuture implements ScheduledFuture<Object> {
    @Override
    public long getDelay(TimeUnit unit) {
        return 0;
    }

    @Override
    public int compareTo(Delayed o) {
        return 0;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public Object get(){
        return null;
    }

    @Override
    public Object get(long timeout, TimeUnit unit) {
        return null;
    }
}
