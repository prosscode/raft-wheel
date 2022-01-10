package wheel.core.rpc;

import wheel.core.node.NodeEndpoint;
import wheel.core.rpc.message.AppendEntriesResult;
import wheel.core.rpc.message.AppendEntriesRpc;
import wheel.core.rpc.message.RequestVoteResult;
import wheel.core.rpc.message.RequestVoteRpc;

import java.util.Collection;

/**
 * @Date 2022/1/2
 * @Created by shuang.peng
 * @Description rpc调用接口，方便测试和切换不同的实现
 */
public interface Connector {
    // 初始化
    void initialize();

    // 发送request vote给节点 (request vote一般是群发)
    void sendRequestVote(RequestVoteRpc requestVoteRpc, Collection<NodeEndpoint> destinationEndpoints);

    // 回复request vote结果
    void replyRequestVote(RequestVoteResult requestVoteResult, NodeEndpoint destinationEndpoint);

    // 发送appendEntries消息给节点
    void sendAppendEntries(AppendEntriesRpc entriesRpc, NodeEndpoint destinationEndpoints);

    // 发送appendEntries结果
    void replyAppendEntries(AppendEntriesResult entriesResult, NodeEndpoint destinationEndpoint);

    // 关闭
    void close();
}
