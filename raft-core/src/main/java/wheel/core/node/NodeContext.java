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
    // 主线程执行器
    private TaskExecutor taskExecutor;
    // 部分角色状态数据存储
    private NodeStore store;
    // pub-sub解耦调用
    private EventBus eventBus;

    public NodeId getSelfId() {
        return selfId;
    }

    public void setSelfId(NodeId selfId) {
        this.selfId = selfId;
    }

    public NodeGroup getGroup() {
        return group;
    }

    public void setGroup(NodeGroup group) {
        this.group = group;
    }

    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public TaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public NodeStore getStore() {
        return store;
    }

    public void setStore(NodeStore store) {
        this.store = store;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }
}
