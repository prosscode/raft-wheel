package wheel.core.rpc;

import javax.annotation.Nonnull;

/**
 * @Date 2022/1/3
 * @Created by shuang.peng
 * @Description Address
 */
public class Address {
    private final String host;
    private final int port;

    public Address(@Nonnull String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "Address{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
