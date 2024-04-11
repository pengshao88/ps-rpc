package cn.pengshao.rpc.core.config;

import cn.pengshao.rpc.core.api.*;
import cn.pengshao.rpc.core.cluster.GrayRouter;
import cn.pengshao.rpc.core.cluster.RoundLoadBalancer;
import cn.pengshao.rpc.core.consumer.ConsumerBootstrap;
import cn.pengshao.rpc.core.filter.CacheFilter;
import cn.pengshao.rpc.core.filter.ParameterFilter;
import cn.pengshao.rpc.core.registry.zk.ZkRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
@Import({AppConfigProperties.class, ConsumerConfigProperties.class})
public class ConsumerConfig {

    @Autowired
    AppConfigProperties appConfigProperties;
    @Autowired
    ConsumerConfigProperties consumerConfigProperties;

    @Bean
    ConsumerBootstrap createConsumerBootstrap() {
        return new ConsumerBootstrap();
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
        return new GrayRouter(consumerConfigProperties.getGrayRatio());
    }

    @Bean
    public LoadBalancer loadBalancer() {
        return new RoundLoadBalancer();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public RegistryCenter consumer_registryCenter() {
        return new ZkRegistryCenter();
    }

    @Bean
    public Filter defaultFilter() {
        return new ParameterFilter();
    }

    @Bean
    public RpcContext createContext(@Autowired Router router,
                                    @Autowired LoadBalancer loadBalancer,
                                    @Autowired List<Filter> filters) {
        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);
        context.setFilters(filters);
        context.getParameters().put("app.id", appConfigProperties.getId());
        context.getParameters().put("app.namespace", appConfigProperties.getNamespace());
        context.getParameters().put("app.env", appConfigProperties.getEnv());
        context.getParameters().put("app.version", appConfigProperties.getVersion());
        context.getParameters().put("app.retries", String.valueOf(consumerConfigProperties.getRetries()));
        context.getParameters().put("app.timeout", String.valueOf(consumerConfigProperties.getTimeout()));
        context.getParameters().put("app.halfOpenInitialDelay", String.valueOf(consumerConfigProperties.getHalfOpenInitialDelay()));
        context.getParameters().put("app.faultLimit", String.valueOf(consumerConfigProperties.getFaultLimit()));
        context.getParameters().put("app.halfOpenDelay", String.valueOf(consumerConfigProperties.getHalfOpenDelay()));
        return context;
    }
}
