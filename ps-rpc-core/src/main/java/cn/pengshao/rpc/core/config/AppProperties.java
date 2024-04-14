package cn.pengshao.rpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Description:config app properties
 *
 * @Author: yezp
 * @date 2024/4/11 22:26
 */
@Data
@ConfigurationProperties(prefix = "psrpc.app")
public class AppProperties {

    private String id = "psrpc";

    private String namespace = "public";

    private String env = "dev";

    private String version = "1.0.0";

}
