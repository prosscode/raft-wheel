package wheel.core.node;

import wheel.core.rpc.Address;

import javax.annotation.Nonnull;

/**
 * @Date 2022/1/2
 * @Created by shuang.peng
 * @Description NodeEndpoint
 */
public class NodeEndpoint {

    private final NodeId id;
    private final Address address;

    public NodeEndpoint(NodeId id, Address address) {
        this.id = id;
        this.address = address;
    }

    public NodeEndpoint(@Nonnull String id, @Nonnull String host, int port) {
        this(new NodeId(id), new Address(host, port));
    }

    public Address getAddress() {
        return address;
    }

    public NodeId getId() {
        return id;
    }
}
