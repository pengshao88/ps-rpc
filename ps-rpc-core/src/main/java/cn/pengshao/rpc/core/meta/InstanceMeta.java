package cn.pengshao.rpc.core.meta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/21 22:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstanceMeta {

    private String schema;
    private String host;
    private int port;
    private String context;
    // online or offline
    private boolean status;
    private Map<String, String> parameters;

    public InstanceMeta(String schema, String host, int port, String context) {
        this.schema = schema;
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public String toPath() {
        return String.format("%s_%s", host, port);
    }

    public String toUrl() {
        return String.format("%s://%s:%d/%s", schema, host, port, context);
    }

    public static InstanceMeta http(String post, Integer port) {
        return new InstanceMeta("http", post, port, "");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (o instanceof InstanceMeta instanceMeta) {
            return this.host.equals(instanceMeta.getHost()) && this.port == instanceMeta.getPort();
        }
        return false;
    }
}
