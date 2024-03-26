package cn.pengshao.rpc.core.provider;

import cn.pengshao.rpc.core.api.RegistryCenter;
import cn.pengshao.rpc.core.registry.zk.ZkRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/7 22:36
 */
@Slf4j
@Configuration
public class ProviderConfig {

    @Bean
    public ProviderBootstrap providerBootstrap() {
        return new ProviderBootstrap();
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
    public RegistryCenter provider_rc() {
        return new ZkRegistryCenter();
    }

}
