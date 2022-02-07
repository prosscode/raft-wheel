package wheel.core.rpc.message;

import wheel.core.node.NodeId;
import wheel.core.rpc.Channel;

/**
 * @Date 2022/1/3
 * @Author shuang.peng
 * @Description RequestVoteRpcMessage
 */
public class RequestVoteRpcMessage extends AbstractRpcMessage<RequestVoteRpc> {

    public RequestVoteRpcMessage(RequestVoteRpc rpc, NodeId sourceNodeId, Channel channel) {
        super(rpc, sourceNodeId, channel);
    }
}

