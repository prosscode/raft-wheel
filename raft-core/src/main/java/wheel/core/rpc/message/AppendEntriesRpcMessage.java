package wheel.core.rpc.message;

import wheel.core.node.NodeId;
import wheel.core.rpc.Channel;

/**
 * @Date 2022/1/16
 * @Author shuang.peng
 * @Description AppendEntriesRpcMessage
 */
public class AppendEntriesRpcMessage extends AbstractRpcMessage<AppendEntriesRpc>{

    public AppendEntriesRpcMessage(AppendEntriesRpc rpc, NodeId sourceNodeId, Channel channel) {
        super(rpc, sourceNodeId, channel);
    }

    public AppendEntriesRpcMessage(AppendEntriesRpc rpc, NodeId sourceNodeId) {
        super(rpc, sourceNodeId);
    }
}
