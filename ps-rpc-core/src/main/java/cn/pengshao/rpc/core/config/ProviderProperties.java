package cn.pengshao.rpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/4/11 22:34
 */
@Data
@ConfigurationProperties(prefix = "psrpc.provider")
public class ProviderProperties {

    Map<String, String> metas = new HashMap<>();

}
