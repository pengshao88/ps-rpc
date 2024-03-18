package cn.pengshao.rpc.core.provider;

import cn.pengshao.rpc.core.api.RegistryCenter;
import cn.pengshao.rpc.core.registry.ZkRegistryCenter;
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
@Configuration
public class ProviderConfig {

    @Bean
    public ProviderBootstrap providerBootstrap() {
        return new ProviderBootstrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner providerBootstrap_runner(@Autowired ProviderBootstrap providerBootstrap) {
        return x -> {
            System.out.println("providerBootstrap starting ...");
            providerBootstrap.start();
            System.out.println("providerBootstrap started ...");
        };
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter provider_rc() {
        return new ZkRegistryCenter();
    }
}
