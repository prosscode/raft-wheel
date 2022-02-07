package wheel.core.rpc.message;

import com.google.common.base.Preconditions;
import wheel.core.node.NodeId;

import javax.annotation.Nonnull;

/**
 * @Date 2022/1/16
 * @Author shuang.peng
 * @Description AppendEntriesResultMessage
 */
public class AppendEntriesResultMessage {

    private final AppendEntriesResult result;
    private final NodeId sourceNodeId;
    // TODO remove rpc, just lastEntryIndex required, or move to replicating state?
    private final AppendEntriesRpc rpc;

    public AppendEntriesResultMessage(AppendEntriesResult result, NodeId sourceNodeId, @Nonnull AppendEntriesRpc rpc) {
        Preconditions.checkNotNull(rpc);
        this.result = result;
        this.sourceNodeId = sourceNodeId;
        this.rpc = rpc;
    }

    public AppendEntriesResult get() {
        return result;
    }

    public NodeId getSourceNodeId() {
        return sourceNodeId;
    }

    public AppendEntriesRpc getRpc() {
        return rpc;
    }
}
