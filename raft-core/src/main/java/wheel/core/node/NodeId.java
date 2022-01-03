package wheel.core.node;

import afu.org.checkerframework.checker.nullness.qual.NonNull;
import com.google.common.base.Preconditions;

import java.io.Serializable;

/**
 * @describe:
 * @created by shuang.peng
 * @date: 2020/09/12
 */
public class NodeId implements Serializable {

    private final String value;

    public String getValue() {
        return value;
    }

    public NodeId(@NonNull String value) {
        Preconditions.checkNotNull(value);
        this.value = value;
    }

    public static NodeId of(@NonNull String value){
        return new NodeId(value);
    }

    @Override
    public String toString() {
        return "NodeId{" +
                "value='" + value + '\'' +
                '}';
    }
}
