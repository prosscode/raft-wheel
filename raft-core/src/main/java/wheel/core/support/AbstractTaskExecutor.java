package wheel.core.support;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;

import javax.annotation.Nonnull;
import java.util.Collections;

/**
 * @Date 2022/1/16
 * @Created by shuang.peng
 * @Description AbstractTaskExecutor
 */
public abstract class AbstractTaskExecutor implements TaskExecutor {

    public void submit(@Nonnull Runnable task, @Nonnull FutureCallback<Object> callback) {
        Preconditions.checkNotNull(task);
        Preconditions.checkNotNull(callback);
        submit(task, Collections.singletonList(callback));
    }
}
