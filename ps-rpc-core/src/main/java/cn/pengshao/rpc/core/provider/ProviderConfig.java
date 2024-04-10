package cn.pengshao.rpc.core.provider;

import cn.pengshao.rpc.core.api.RegistryCenter;
import cn.pengshao.rpc.core.meta.InstanceMeta;
import cn.pengshao.rpc.core.registry.zk.ZkRegistryCenter;
import cn.pengshao.rpc.core.transport.SpringBootTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import java.util.Map;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/7 22:36
 */
@Slf4j
@Configuration
@Import({SpringBootTransport.class})
public class ProviderConfig {

    @Value("${server.port:8081}")
    private String port;
    private InstanceMeta instance;
    @Value("${app.id:psrpc}")
    private String app;
    @Value("${app.namespace:public}")
    private String namespace;
    @Value("${app.env:dev}")
    private String env;
    @Value("${app.version:1.0.0}")
    private String version;
    // Spel spring language 可以直接将 json 转成 map
    @Value("#{${app.metas:{dc: 'bj', gray: 'false', unit: 'B001'}}}")
    Map<String, String> metas;

    @Bean
    public ProviderBootstrap providerBootstrap() {
        return new ProviderBootstrap(port, app, namespace, env, version, metas);
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
        return new ZkRegistryCenter();
    }

}
