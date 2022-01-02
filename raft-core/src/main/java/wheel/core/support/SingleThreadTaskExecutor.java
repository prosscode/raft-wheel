package wheel.core.support;

import java.util.concurrent.*;

/**
 * @Date 2022/1/2
 * @Created by shuang.peng
 * @Description 异步单线程的实现
 */
public class SingleThreadTaskExecutor implements TaskExecutor {
    private final ExecutorService executorService;

    public SingleThreadTaskExecutor(String name) {
        this(r -> new Thread(r, name));
    }

    public SingleThreadTaskExecutor(ThreadFactory threadFactory) {
        executorService = Executors.newSingleThreadExecutor(threadFactory);
    }

    public SingleThreadTaskExecutor() {
        this(Executors.defaultThreadFactory());
    }

    @Override
    public Future<?> submit(Runnable task) {
        return executorService.submit(task);
    }

    @Override
    public <V> Future<V> submit(Callable<V> task) {
        return executorService.submit(task);
    }

    @Override
    public void shutdown() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);
    }
}
