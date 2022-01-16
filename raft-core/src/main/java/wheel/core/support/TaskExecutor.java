package wheel.core.support;

import com.google.common.util.concurrent.FutureCallback;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @Date 2022/1/2
 * @Created by shuang.peng
 * @Description 任务执行器接口，抽象化出来，方便测试不同线程模式执行情况
 */
public interface TaskExecutor {
    // 提交任务
    Future<?> submit(Runnable task);
    // 提交任务，有回调返回
    <V> Future<V> submit(Callable<V> task);

    void submit(@Nonnull Runnable task, @Nonnull FutureCallback<Object> callback);
    void submit(@Nonnull Runnable task, @Nonnull Collection<FutureCallback<Object>> callbacks);

    // 关闭任务执行器
    void shutdown() throws InterruptedException;
}
