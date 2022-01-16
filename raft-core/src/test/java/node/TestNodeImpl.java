package node;

import org.junit.Assert;
import org.junit.Test;
import rpc.MockConnector;
import wheel.core.node.*;
import wheel.core.node.role.CandidateNodeRole;
import wheel.core.node.role.FollowerNodeRole;
import wheel.core.rpc.message.RequestVoteResult;
import wheel.core.rpc.message.RequestVoteRpc;
import wheel.core.rpc.message.RequestVoteRpcMessage;
import wheel.core.schedule.NullScheduler;
import wheel.core.support.DirectTaskExecutor;

import java.util.Arrays;

/**
 * @Date 2022/1/16
 * @Created by shuang.peng
 * @Description TestNodeImpl Node构造测试类
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

}
