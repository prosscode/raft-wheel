package wheel.core.node;

/**
 * @Date 2022/1/3
 * @Created by shuang.peng
 * @Description Node 暴露给上层节点服务接口
 */
public interface Node {
    // node启动
    void start();
    // node关闭
    void stop() throws InterruptedException;
}
