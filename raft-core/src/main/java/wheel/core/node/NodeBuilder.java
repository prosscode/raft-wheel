package wheel.core.node;

import com.google.common.eventbus.EventBus;
import wheel.core.rpc.Connector;
import wheel.core.schedule.DefaultScheduler;
import wheel.core.schedule.NullScheduledFuture;
import wheel.core.schedule.NullScheduler;
import wheel.core.schedule.Scheduler;
import wheel.core.support.SingleThreadTaskExecutor;
import wheel.core.support.TaskExecutor;

import java.util.Collection;
import java.util.Collections;

/**
 * @Date 2022/1/16
 * @Created by shuang.peng
 * @Description NodeBuilder
 */
public class NodeBuilder {
    // 集群成员
    private final NodeGroup group;
    // 节点ID
    private final NodeId selfId;
    private final EventBus eventBus;
    // 任务定时器
    private Scheduler scheduler = null;
    // rpc通信
    private Connector connector = null;
    // 主线程执行器
    private TaskExecutor taskExecutor = null;

    // 单节点构造函数
    public NodeBuilder(NodeEndpoint endpoint){
        this(Collections.singleton(endpoint),endpoint.getId());
    }
    // 多节点构造
    public NodeBuilder(Collection<NodeEndpoint> endpoints, NodeId selfId){
        this.group = new NodeGroup(endpoints,selfId);
        this.selfId = selfId;
        this.eventBus = new EventBus(selfId.getValue());
    }

    public NodeBuilder setConnector(Connector connector) {
        this.connector = connector;
        return this;
    }

    public NodeBuilder setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        return this;
    }

    public NodeBuilder setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
        return this;
    }

    public NodeImpl build(){
        return new NodeImpl(buildContext());
    }

    private NodeContext buildContext() {
        NodeContext context = new NodeContext();
        context.setGroup(group);
        context.setSelfId(selfId);
        context.setEventBus(eventBus);
        context.setScheduler(scheduler != null ? scheduler : new NullScheduler());
        context.setConnector(connector);
        context.setTaskExecutor(taskExecutor != null ? taskExecutor : new SingleThreadTaskExecutor("node"));
        return context;
    }
}
