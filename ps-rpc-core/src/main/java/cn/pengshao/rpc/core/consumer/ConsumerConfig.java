package cn.pengshao.rpc.core.consumer;

import cn.pengshao.rpc.core.api.*;
import cn.pengshao.rpc.core.cluster.GrayRouter;
import cn.pengshao.rpc.core.cluster.RoundLoadBalancer;
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
public class ConsumerConfig {

    @Value("${app.grayRatio:10}")
    private int grayRatio;
    @Value("${app.id:app}")
    private String app;
    @Value("${app.namespace:public}")
    private String namespace;
    @Value("${app.env:dev}")
    private String env;
    @Value("${app.version:1.0.0}")
    private String version;
    @Value("${app.retries:1}")
    private String retries;
    @Value("${app.timeout:1000}")
    private String timeout;
    @Value("${app.faultLimit:10}")
    private int faultLimit;
    @Value("${app.halfOpenInitialDelay:10000}")
    private int halfOpenInitialDelay;
    @Value("${app.halfOpenDelay:60000}")
    private int halfOpenDelay;

    @Bean
    ConsumerBootstrap createConsumerBootstrap() {
        return new ConsumerBootstrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE + 1)
    public ApplicationRunner consumerBootstrapRunner(@Autowired ConsumerBootstrap consumerBootstrap,
                                                     @Autowired RegistryCenter registryCenter) {
        return x -> {
            log.info("consumerBootstrapRunner starting");
            consumerBootstrap.start();
            log.info("consumerBootstrapRunner end");
        };
    }

    @Bean
    public Router router() {
        return new GrayRouter(grayRatio);
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
        context.getParameters().put("app.id", app);
        context.getParameters().put("app.namespace", namespace);
        context.getParameters().put("app.env", env);
        context.getParameters().put("app.version", version);
        context.getParameters().put("app.retries", String.valueOf(retries));
        context.getParameters().put("app.timeout", String.valueOf(timeout));
        context.getParameters().put("app.halfOpenInitialDelay", String.valueOf(halfOpenInitialDelay));
        context.getParameters().put("app.faultLimit", String.valueOf(faultLimit));
        context.getParameters().put("app.halfOpenDelay", String.valueOf(halfOpenDelay));
        return context;
    }
}
