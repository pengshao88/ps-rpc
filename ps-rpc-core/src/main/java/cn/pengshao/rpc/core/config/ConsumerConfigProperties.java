package cn.pengshao.rpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/4/11 22:30
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "psrpc.consumer")
public class ConsumerConfigProperties {

    private int retries = 1;

    private int timeout = 1000;

    private int faultLimit = 10;

    private int halfOpenInitialDelay = 10000;

    private int halfOpenDelay = 60000;

    private int grayRatio = 0;

}
