package node;

import org.junit.Assert;
import org.junit.Test;
import rpc.MockConnector;
import wheel.core.node.*;
import wheel.core.node.role.AbstractNodeRole;
import wheel.core.node.role.CandidateNodeRole;
import wheel.core.node.role.FollowerNodeRole;
import wheel.core.node.role.LeaderNodeRole;
import wheel.core.rpc.Connector;
import wheel.core.rpc.message.*;
import wheel.core.schedule.NullScheduler;
import wheel.core.support.DirectTaskExecutor;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Date 2022/1/16
 * @Author shuang.peng
 * @Description TestNodeImpl构造测试类
 */
public class TestNodeImpl {

    private NodeBuilder newNodeBuilder(NodeId selfId, NodeEndpoint... endpoints){
        // 初始化NodeContext
        return new NodeBuilder(Arrays.asList(endpoints), selfId)
                .setScheduler(new NullScheduler())
                .setConnector(new MockConnector())
                .setTaskExecutor(new DirectTaskExecutor());
    }

    // 测试系统启动
    @Test
    public void testStart(){
        NodeImpl node = newNodeBuilder(
                NodeId.of("A"), new NodeEndpoint("A", "localhost", 2333)).build();
        node.start();
        FollowerNodeRole role = (FollowerNodeRole) node.getNodeRole();
        // 启动后角色为follower term为0,没有投过票
        Assert.assertEquals(0,role.getTerm());
        Assert.assertNotNull(role.getVotedFor());
    }

    // 测试选举超时
    // 要求：follower选举超时后变成candidate角色，并给其它节点发送request vote消息
    @Test
    public void testElectionTimeoutWhenFollower(){
        NodeImpl node = newNodeBuilder(
                NodeId.of("A"),
                new NodeEndpoint("A", "localhost", 2333),
                new NodeEndpoint("B", "localhost", 2334),
                new NodeEndpoint("C", "localhost", 2335)
        ).build();
        // 节点启动，角色为follower
        node.start();
        // 选举超时，自己成为candidate角色
        node.electionTimeout();
        // 选举开始，初始化term为1，自己给自己投一票
        CandidateNodeRole nodeRole = (CandidateNodeRole) node.getNodeRole();
        Assert.assertEquals(1,nodeRole.getTerm());
        Assert.assertEquals(1,nodeRole.getVotesCount());
        // 当前节点向其它节点发送request vote消息
        MockConnector mockConnector = (MockConnector) node.getNodeContext().getConnector();
        RequestVoteRpc rpc =(RequestVoteRpc) mockConnector.getRpc();
        Assert.assertEquals(1,rpc.getTerm());
        Assert.assertEquals(NodeId.of("A"),rpc.getCandidateId());
        Assert.assertEquals(0,rpc.getLastLogIndex());
        Assert.assertEquals(0,rpc.getLastLogTerm());
    }

    // 收到request vote消息
    // 要求：follower节点收到其它节点的request vote消息，投票并设置自己的voteFor
    @Test
    public void testOnReceiveRequestVoteRpcFollower(){
        // node start
        NodeImpl node = newNodeBuilder(
                NodeId.of("A"),
                new NodeEndpoint("A", "localhost", 2333),
                new NodeEndpoint("B", "localhost", 2334),
                new NodeEndpoint("C", "localhost", 2335)
        ).build();
        node.start();
        // 测试节点C发送request vote消息给节点A
        RequestVoteRpc rpc = new RequestVoteRpc();
        rpc.setTerm(1);
        rpc.setCandidateId(NodeId.of("C"));
        rpc.setLastLogIndex(0);
        rpc.setLastLogTerm(0);
        // 收到request vote消息
        node.onReceiveRequestVoteRpc(new RequestVoteRpcMessage(rpc, NodeId.of("C"), null));
        MockConnector mockConnector = (MockConnector) (node.getNodeContext().getConnector());
        // 收到request result消息
        RequestVoteResult result = (RequestVoteResult) mockConnector.getResult();
        Assert.assertEquals(1, result.getTerm());
        Assert.assertTrue(result.isVoteGranted());
        Assert.assertEquals(NodeId.of("C"), ((FollowerNodeRole) node.getNodeRole()).getVotedFor());
    }

    // 收到request vote响应
    // 要求：节点A变成candidate角色后收到投票的request vote响应，然后变成leader角色
    @Test
    public void testOnReceiveRequestVoteResultLeader(){
        // node start
        NodeImpl node = newNodeBuilder(
                NodeId.of("A"),
                new NodeEndpoint("A", "localhost", 2333),
                new NodeEndpoint("B", "localhost", 2334),
                new NodeEndpoint("C", "localhost", 2335)
        ).build();
        node.start();
        // 选举超时，自己成为candidate role
        node.electionTimeout();
        // 模拟返回结果
        node.onReceiveRequestVoteResult(new RequestVoteResult(1,true));
        LeaderNodeRole nodeRole = (LeaderNodeRole) node.getNodeRole();
        Assert.assertEquals(1,nodeRole.getTerm());
    }

    // 成为leader节点后的发送心跳消息
    // 要求：节点A成为leader节点后，向B和C发送心跳消息
    @Test
    public void testReplicateLog(){
        // node start
        NodeImpl node = newNodeBuilder(
                NodeId.of("A"),
                new NodeEndpoint("A", "localhost", 2333),
                new NodeEndpoint("B", "localhost", 2334),
                new NodeEndpoint("C", "localhost", 2335)
        ).build();
        node.start();
        // 成为candidate role，发送request vote消息，收到投票
        node.electionTimeout();
        node.onReceiveRequestVoteResult(new RequestVoteResult(1,true));
        // 成为leader，开始发送append entries
        node.replicateLog();
        MockConnector mockConnector = (MockConnector) node.getNodeContext().getConnector();
        // request算1条消息
        Assert.assertEquals(3, mockConnector.getMessageCount());
        // 检查目标节点
        List<MockConnector.Message> messages = mockConnector.getMessages();
        Set<NodeId> nodeIds = messages.subList(1, 3).stream().map(MockConnector.Message::getDestinationNodeId)
                .collect(Collectors.toSet());
        Assert.assertEquals(2, nodeIds.size());
        Assert.assertTrue(nodeIds.contains(NodeId.of("B")));
        Assert.assertTrue(nodeIds.contains(NodeId.of("C")));
        AppendEntriesRpc rpc = (AppendEntriesRpc) mockConnector.getLastMessage().getRpc();
        Assert.assertEquals(1, rpc.getTerm());
    }

    // 收到来自leader的心跳消息
    // 要求：节点A启动后收到来自leader节点B的心跳消息，设置自己的term和leaderId
    @Test
    public void testOnReceiveAppendEntriesRpcFollower(){
        // node start
        NodeImpl node = newNodeBuilder(
                NodeId.of("A"),
                new NodeEndpoint("A", "localhost", 2333),
                new NodeEndpoint("B", "localhost", 2334),
                new NodeEndpoint("C", "localhost", 2335)
        ).build();
        node.start();
        AppendEntriesRpc entriesRpc = new AppendEntriesRpc();
        entriesRpc.setTerm(1);
        entriesRpc.setLeaderId(NodeId.of("B"));
        // 收到心跳消息
        node.onReceiveAppendEntriesRpc(new AppendEntriesRpcMessage(entriesRpc, NodeId.of("B")));
        MockConnector mockConnector = (MockConnector) node.getNodeContext().getConnector();
        AppendEntriesResult result = (AppendEntriesResult) mockConnector.getResult();
        // 断言回复心跳消息中term
        Assert.assertEquals(1, result.getTerm());
        Assert.assertTrue(result.isSuccess());
        // 自己应该是follower role, leaderId应该是Node.of("B")
        FollowerNodeRole nodeRole = (FollowerNodeRole) node.getNodeRole();
        Assert.assertEquals(1, nodeRole.getTerm());
        Assert.assertEquals(NodeId.of("B"), nodeRole.getLeaderId());
    }

    // leader收到其它节点的回复
    // 要求：节点A成为Leader节点，发送心跳消息后，接受到其它节点的回复
    @Test
    public void testOnReceiveAppendEntriesResultLeader(){
        // node start
        NodeImpl node = newNodeBuilder(
                NodeId.of("A"),
                new NodeEndpoint("A", "localhost", 2333),
                new NodeEndpoint("B", "localhost", 2334),
                new NodeEndpoint("C", "localhost", 2335)
        ).build();
        node.start();
        node.electionTimeout();
        node.onReceiveRequestVoteResult(new RequestVoteResult(1,true));
        node.replicateLog();
        node.onReceiveAppendEntriesResult(
                new AppendEntriesResultMessage(new AppendEntriesResult(1,true)
                        ,NodeId.of("B")
                ,new AppendEntriesRpc())
        );
        MockConnector connector =(MockConnector) node.getNodeContext().getConnector();
        for (MockConnector.Message message : connector.getMessages()) {
            System.out.println("message:"+message);
        }
    }
}
