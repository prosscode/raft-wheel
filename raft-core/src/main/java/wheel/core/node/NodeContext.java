package wheel.core.node;

import com.google.common.eventbus.EventBus;
import wheel.core.node.store.NodeStore;
import wheel.core.rpc.Connector;
import wheel.core.schedule.Scheduler;
import wheel.core.support.TaskExecutor;


/**
 * @Date 2022/1/2
 * @Created by shuang.peng
 * @Description 间接层类，隔绝核心组件和和各个组件之间的直接调用访问
 */
public class NodeContext {
    // 当前节点id
    private NodeId selfId;
    // 成员列表
    private NodeGroup group;
    // 日志
//    private Log log;
    // RPC组件
    private Connector connector;
    // 定时器组件
    private Scheduler scheduler;
    // pub-sub解耦调用
    private EventBus eventBus;
    // 主线程执行器
    private TaskExecutor taskExecutor;
    // 部分角色状态数据存储
    private NodeStore store;

    public NodeId getSelfId() {
        return selfId;
    }

    public void setSelfId(NodeId selfId) {
        this.selfId = selfId;
    }

}
