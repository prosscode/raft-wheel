package rpc;

import wheel.core.node.NodeEndpoint;
import wheel.core.node.NodeId;
import wheel.core.rpc.Connector;
import wheel.core.rpc.message.AppendEntriesResult;
import wheel.core.rpc.message.AppendEntriesRpc;
import wheel.core.rpc.message.RequestVoteResult;
import wheel.core.rpc.message.RequestVoteRpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @Date 2022/1/16
 * @Created by shuang.peng
 * @Description MockConnector测试，设计一个存放消息的链表，暴露读取发送后的消息
 */
public class MockConnector implements Connector {

    // 存放消息的链表
    private LinkedList<Message> messages = new LinkedList<>();

    public Message getLastMessage() {
        return messages.isEmpty() ? null : messages.getLast();
    }

    public Message getLastMessageOrDefault() {
        return messages.isEmpty() ? new Message() : messages.getLast();
    }

    public Object getRpc(){
        return getLastMessageOrDefault().getRpc();
    }

    public Object getResult(){
        return getLastMessageOrDefault().getResult();
    }

    public Object getDestinationNodeId(){
        return getLastMessageOrDefault().getDestinationNodeId();
    }

    public Object getMessageCount(){
        return messages.size();
    }

    public List<Message> getMessages(){
        return new ArrayList<>(messages);
    }

    public void clearMessage(){
        messages.clear();
    }

    @Override
    public void initialize() {

    }

    @Override
    public void sendRequestVote(RequestVoteRpc requestVoteRpc,
                                Collection<NodeEndpoint> destinationEndpoints) {
        Message m = new Message();
        m.setRpc(requestVoteRpc);
        messages.add(m);
    }

    @Override
    public void replyRequestVote(RequestVoteResult requestVoteResult,
                                 NodeEndpoint destinationEndpoint) {
        Message m = new Message();
        m.setResult(requestVoteResult);
        m.setDestinationNodeId(destinationEndpoint.getId());
        messages.add(m);
    }

    @Override
    public void sendAppendEntries(AppendEntriesRpc entriesRpc, NodeEndpoint destinationEndpoints) {
        Message m = new Message();
        m.setRpc(entriesRpc);
        m.setDestinationNodeId(destinationEndpoints.getId());
        messages.add(m);
    }

    @Override
    public void replyAppendEntries(AppendEntriesResult entriesResult, NodeEndpoint destinationEndpoint) {
        Message m = new Message();
        m.setResult(entriesResult);
        m.setDestinationNodeId(destinationEndpoint.getId());
        messages.add(m);
    }

    @Override
    public void close() {

    }


    public static class Message{
        private Object rpc;
        private NodeId destinationNodeId;
        private Object result;

        public Object getRpc() {
            return rpc;
        }

        public void setRpc(Object rpc) {
            this.rpc = rpc;
        }

        public NodeId getDestinationNodeId() {
            return destinationNodeId;
        }

        public void setDestinationNodeId(NodeId destinationNodeId) {
            this.destinationNodeId = destinationNodeId;
        }

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
        }

        @Override
        public String toString() {
            return "message{" +
                    "rpc=" + rpc +
                    ", destinationNodeId=" + destinationNodeId +
                    ", result=" + result +
                    '}';
        }
    }
}
