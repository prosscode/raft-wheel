package support;

import wheel.core.support.TaskExecutor;

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
    public void shutdown() throws InterruptedException {

    }
}
