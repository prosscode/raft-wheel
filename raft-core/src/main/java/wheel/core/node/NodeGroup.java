package wheel.core.node;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Date 2022/1/2
 * @Created by shuang.peng
 * @Description NodeGroup 集群成员组
 */
public class NodeGroup {
    private final NodeId selfId;
    private HashMap<NodeId,GroupMember> memberMap;

    public NodeGroup(NodeId selfId) {
        this.selfId = selfId;
    }

    Set<NodeEndpoint> listEndpointExceptSelf(){
        HashSet<NodeEndpoint> endpoints = new HashSet<>();
        for (GroupMember value : memberMap.values()) {
            if(!value.getId().equals(selfId)){
                endpoints.add(value.getEndpoint());
            }
        }
        return endpoints;
    }

    int getCountOfMajor() {
        return (int) memberMap.values().stream().filter(GroupMember::isMajor).count();
    }

    @Nullable
    GroupMember getMember(NodeId id) {
        return memberMap.get(id);
    }

    @Nonnull
    GroupMember findMember(NodeId id) {
        GroupMember member = getMember(id);
        if (member == null) {
            throw new IllegalArgumentException("no such node " + id);
        }
        return member;
    }

    /**
     * List replication target
     * @return
     */
    Collection<GroupMember> listReplicationTarget(){
        return memberMap.values().stream().filter(node->!node.equals(selfId)).collect(Collectors.toList());
    }

}
