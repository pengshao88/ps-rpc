package cn.pengshao.rpc.core.config;

import cn.pengshao.rpc.core.registry.RegistryCenter;
import cn.pengshao.rpc.core.provider.ProviderBootstrap;
import cn.pengshao.rpc.core.provider.ProviderInvoker;
import cn.pengshao.rpc.core.registry.ps.PsRegistryCenter;
import cn.pengshao.rpc.core.transport.SpringBootTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/7 22:36
 */
@Slf4j
@Configuration
@Import({SpringBootTransport.class, AppProperties.class, ProviderProperties.class})
public class ProviderConfig {

    @Value("${server.port:8080}")
    private String port;

    @Bean
    public ProviderBootstrap providerBootstrap(@Autowired AppProperties appProperties,
                                               @Autowired ProviderProperties providerProperties) {
        return new ProviderBootstrap(port, appProperties, providerProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "apollo.bootstrap", value = "enabled")
    ApolloChangedListener provider_apolloChangedListener() {
        return new ApolloChangedListener();
    }

    @Bean
    ProviderInvoker providerInvoker(@Autowired ProviderBootstrap providerBootstrap) {
        return new ProviderInvoker(providerBootstrap);
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner providerBootstrap_runner(@Autowired ProviderBootstrap providerBootstrap) {
        return x -> {
            log.info("providerBootstrap starting ...");
            providerBootstrap.start();
            log.info("providerBootstrap started ...");
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public RegistryCenter provider_rc() {
        return new PsRegistryCenter();
        // new ZkRegistryCenter();
    }

}
