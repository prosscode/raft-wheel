package wheel.core.node;

import afu.org.checkerframework.checker.nullness.qual.NonNull;
import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.Objects;

/**
 * @describe:
 * @created by shuang.peng
 * @date: 2021/12/26
 */
public class NodeId implements Serializable {

    private final String value;

    public String getValue() {
        return value;
    }

    public NodeId(String value) {
//        Preconditions.checkNotNull(value);
        this.value = value;
    }

    public static NodeId of(@NonNull String value) {
        return new NodeId(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeId)) return false;
        NodeId id = (NodeId) o;
        return Objects.equals(value, id.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "NodeId{" +
                "value='" + value + '\'' +
                '}';
    }
}
