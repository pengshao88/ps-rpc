package cn.pengshao.rpc.core.meta;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashMap;
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
@EqualsAndHashCode(of = {"host", "port", "context"})
public class InstanceMeta {

    private String schema;
    private String host;
    private int port;
    private String context;
    // online or offline
    private boolean status;
    // 可以存储机房、环境、版本等信息
    private Map<String, String> parameters = new HashMap<>();

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
        return new InstanceMeta("http", post, port, "psrpc");
    }

    public InstanceMeta addParams(Map<String, String> params) {
        this.parameters.putAll(params);
        return this;
    }

    public String toMetas(){
        return JSON.toJSONString(this.parameters);
    }

}
