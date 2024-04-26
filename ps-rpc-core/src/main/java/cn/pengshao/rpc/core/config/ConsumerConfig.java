package cn.pengshao.rpc.core.config;

import cn.pengshao.rpc.core.api.*;
import cn.pengshao.rpc.core.cluster.GrayRouter;
import cn.pengshao.rpc.core.cluster.RoundLoadBalancer;
import cn.pengshao.rpc.core.consumer.ConsumerBootstrap;
import cn.pengshao.rpc.core.filter.ParameterFilter;
import cn.pengshao.rpc.core.registry.RegistryCenter;
import cn.pengshao.rpc.core.registry.ps.PsRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * Description: Config for consumer
 *
 * @Author: yezp
 * @date 2024/3/10 22:51
 */
@Slf4j
@Configuration
@Import({AppProperties.class, ConsumerProperties.class})
public class ConsumerConfig {

    @Autowired
    AppProperties appProperties;
    @Autowired
    ConsumerProperties consumerProperties;

    @Bean
    ConsumerBootstrap createConsumerBootstrap() {
        return new ConsumerBootstrap();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "apollo.bootstrap", value = "enabled")
    ApolloChangedListener consumer_apolloChangedListener() {
        return new ApolloChangedListener();
    }

    @Bean
    @Order(Integer.MIN_VALUE + 1)
    public ApplicationRunner consumerBootstrapRunner(@Autowired ConsumerBootstrap consumerBootstrap) {
        return x -> {
            log.info("consumerBootstrapRunner starting");
            consumerBootstrap.start();
            log.info("consumerBootstrapRunner end");
        };
    }

    @Bean
    public Router router() {
        return new GrayRouter(consumerProperties.getGrayRatio());
    }

    @Bean
    public LoadBalancer loadBalancer() {
        return new RoundLoadBalancer();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public RegistryCenter consumer_registryCenter() {
        return new PsRegistryCenter();
        // new ZkRegistryCenter();
    }

    @Bean
    public Filter defaultFilter() {
        return new ParameterFilter();
    }

    @Bean
    @RefreshScope // context.refresh
    public RpcContext createContext(@Autowired Router router,
                                    @Autowired LoadBalancer loadBalancer,
                                    @Autowired List<Filter> filters) {
        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);
        context.setFilters(filters);
        context.getParameters().put("app.id", appProperties.getId());
        context.getParameters().put("app.namespace", appProperties.getNamespace());
        context.getParameters().put("app.env", appProperties.getEnv());
        context.getParameters().put("app.version", appProperties.getVersion());
        context.setConsumerProperties(consumerProperties);
        return context;
    }
}
