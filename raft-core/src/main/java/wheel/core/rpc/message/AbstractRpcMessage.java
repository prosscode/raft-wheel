package wheel.core.rpc.message;

import wheel.core.node.NodeId;
import wheel.core.rpc.Channel;

/**
 * @Date 2022/1/9
 * @Author shuang.peng
 * @Description AbstractRpcMessage
 */
public abstract class AbstractRpcMessage<T> {
    private final T rpc;
    private final NodeId sourceNodeId;
    private Channel channel;

    AbstractRpcMessage(T rpc, NodeId sourceNodeId, Channel channel) {
        this.rpc = rpc;
        this.sourceNodeId = sourceNodeId;
        this.channel = channel;
    }

    AbstractRpcMessage(T rpc, NodeId sourceNodeId) {
        this.rpc = rpc;
        this.sourceNodeId = sourceNodeId;
    }

    public T get() {
        return this.rpc;
    }

    public NodeId getSourceNodeId() {
        return sourceNodeId;
    }

    public Channel getChannel() {
        return channel;
    }
}
