package wheel.core.schedule;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @describe: 选举超时类
 * 对ScheduledFuture进行一个封装，公开了取消的方法，理论上来说，重复取消或取消已完成的任务不会有问题
 * @created by shuang.peng
 * @date: 2020/08/29
 */
public class ElectionTimeout {

    private final ScheduledFuture<?> scheduledFuture;

    public ElectionTimeout(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    // 取消选举
    public void cancel(){
        this.scheduledFuture.cancel(false);
    }

    @Override
    public String toString() {
        // 选举超时已取消
        if(this.scheduledFuture.isCancelled()){
            return "ElectionTimeout(state=canceled)";
        }
        // 选举超时已执行
        if(this.scheduledFuture.isDone()){
            return "ElectionTimeout(state=done)";
        }
        // 选举超时未执行，在多少毫秒后执行
        return "ElectionTimeout{delay=" + scheduledFuture.getDelay(TimeUnit.MILLISECONDS) +"ms}";
    }
}
