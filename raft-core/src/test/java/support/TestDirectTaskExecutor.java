package support;

import com.google.common.util.concurrent.FutureCallback;
import wheel.core.support.TaskExecutor;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @Date 2022/1/2
 * @Created by shuang.peng
 * @Description 对比SingleThreadTaskExecutor构造 直接执行的版本
 */
public class TestDirectTaskExecutor implements TaskExecutor {

    @Override
    public Future<?> submit(Runnable task) {
        FutureTask<?> futureTask = new FutureTask<>(task, null);
        futureTask.run();
        return futureTask;
    }

    @Override
    public <V> Future<V> submit(Callable<V> task) {
        FutureTask<V> futureTask = new FutureTask<V>(task);
        futureTask.run();
        return futureTask;
    }

    @Override
    public void submit(@Nonnull Runnable task, @Nonnull FutureCallback<Object> callback) {

    }

    @Override
    public void submit(@Nonnull Runnable task, @Nonnull Collection<FutureCallback<Object>> callbacks) {

    }

    @Override
    public void shutdown() throws InterruptedException {

    }
}
