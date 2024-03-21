package cn.pengshao.rpc.core.meta;

import lombok.Builder;
import lombok.Data;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/21 23:20
 */
@Data
@Builder
public class ServiceMeta {

    private String app;
    private String namespace;
    private String env;
    private String name;
    private String version;

    public String toPath() {
        return String.format("%s_%s_%s_%s_%s", app, namespace, env, name, version);
    }
}
