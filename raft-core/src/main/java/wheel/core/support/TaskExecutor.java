package wheel.core.support;

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
    // 关闭任务执行器
    void shutdown() throws InterruptedException;
}
