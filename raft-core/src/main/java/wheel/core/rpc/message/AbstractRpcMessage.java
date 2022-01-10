package wheel.core.rpc.message;

import wheel.core.node.NodeId;
import wheel.core.rpc.Channel;

/**
 * @Date 2022/1/9
 * @Created by shuang.peng
 * @Description AbstractRpcMessage
 */
public abstract class AbstractRpcMessage<T> {
    private final T rpc;
    private final NodeId sourceNodeId;
    private final Channel channel;

    AbstractRpcMessage(T rpc, NodeId sourceNodeId, Channel channel) {
        this.rpc = rpc;
        this.sourceNodeId = sourceNodeId;
        this.channel = channel;
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
