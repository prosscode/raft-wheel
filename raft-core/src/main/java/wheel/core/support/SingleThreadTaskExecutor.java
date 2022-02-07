package wheel.core.support;

import com.google.common.util.concurrent.FutureCallback;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.concurrent.*;

/**
 * @Date 2022/1/2
 * @Author shuang.peng
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
    public void submit(@Nonnull Runnable task, @Nonnull FutureCallback<Object> callback) {

    }

    @Override
    public void submit(@Nonnull Runnable task, @Nonnull Collection<FutureCallback<Object>> callbacks) {

    }

    @Override
    public void shutdown() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);
    }
}
