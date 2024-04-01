package cn.pengshao.rpc.core.consumer;

import cn.pengshao.rpc.core.api.Filter;
import cn.pengshao.rpc.core.api.LoadBalancer;
import cn.pengshao.rpc.core.api.RegistryCenter;
import cn.pengshao.rpc.core.api.Router;
import cn.pengshao.rpc.core.cluster.GrayRouter;
import cn.pengshao.rpc.core.cluster.RoundLoadBalancer;
import cn.pengshao.rpc.core.filter.CacheFilter;
import cn.pengshao.rpc.core.registry.zk.ZkRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/10 22:51
 */
@Slf4j
@Configuration
public class ConsumerConfig {

    @Value("${app.grayRatio}")
    private int grayRatio;

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

//    @Bean
//    public Filter cacheFilter() {
//        return new CacheFilter();
//    }

//    @Bean
//    public Filter mockFilter() {
//        return new MockFilter();
//    }
}
