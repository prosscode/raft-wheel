package wheel.core.node;

/**
 * @Date 2022/1/3
 * @Created by shuang.peng
 * @Description GroupMember
 */
public class GroupMember {

    private final NodeEndpoint endpoint;
    private ReplicatingState replicatingState;
    private boolean major;
    private boolean removing = false;

    public GroupMember(NodeEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public GroupMember(NodeEndpoint endpoint, ReplicatingState replicatingState, boolean major) {
        this.endpoint = endpoint;
        this.replicatingState = replicatingState;
        this.major = major;
    }

    NodeId getId() {
        return endpoint.getId();
    }

    public NodeEndpoint getEndpoint() {
        return endpoint;
    }

    public ReplicatingState getReplicatingState() {
        return replicatingState;
    }

    public void setReplicatingState(ReplicatingState replicatingState) {
        this.replicatingState = replicatingState;
    }

    public boolean isMajor() {
        return major;
    }

    public void setMajor(boolean major) {
        this.major = major;
    }

    public boolean isRemoving() {
        return removing;
    }

    public void setRemoving(boolean removing) {
        this.removing = removing;
    }

    boolean idEquals(NodeId id) {
        return endpoint.getId().equals(id);
    }
}
