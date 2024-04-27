package cn.pengshao.rpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/4/27 8:41
 */
@Data
@ConfigurationProperties(prefix = "psregistry")
public class PsRegistryProperties {

    List<String> servers;
}
