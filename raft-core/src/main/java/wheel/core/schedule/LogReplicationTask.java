package wheel.core.schedule;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author shuang.peng
 * @Date 2021/12/29
 */
public class LogReplicationTask {

    private final ScheduledFuture<?> scheduledFuture;

    // 不设置日志复制定时器，测试用
    public static final LogReplicationTask NONE = new LogReplicationTask(new NullScheduledFuture());

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
