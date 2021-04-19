package wheel.core.schedule;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @describe:
 * @created by shuang.peng
 * @date: 2020/08/29
 */
public class LogReplicationTask {

    private final ScheduledFuture<?> scheduledFuture;

    public LogReplicationTask(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    // 取消日志复制定时器
    public void cancel(){
        this.scheduledFuture.cancel(false);
    }

    @Override
    public String toString() {
        return "LogReplicationTask{delay=" + scheduledFuture.getDelay(TimeUnit.MILLISECONDS)+"ms}";
    }
}
