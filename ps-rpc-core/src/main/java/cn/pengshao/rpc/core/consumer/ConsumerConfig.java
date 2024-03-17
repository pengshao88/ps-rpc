package cn.pengshao.rpc.core.consumer;

import cn.pengshao.rpc.core.api.LoadBalancer;
import cn.pengshao.rpc.core.api.RegistryCenter;
import cn.pengshao.rpc.core.api.Router;
import cn.pengshao.rpc.core.cluster.RandomLoadBalancer;
import cn.pengshao.rpc.core.cluster.RoundLoadBalancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/10 22:51
 */
@Configuration
public class ConsumerConfig {

    @Value("${psrpc.providers}")
    String servers;

    @Bean
    ConsumerBootstrap createConsumerBootstrap() {
        return new ConsumerBootstrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner consumerBootstrapRunner(@Autowired ConsumerBootstrap consumerBootstrap) {
        return x -> {
            System.out.println("consumerBootstrapRunner starting");
            consumerBootstrap.start();
            System.out.println("consumerBootstrapRunner end");
        };
    }

    @Bean
    public Router router() {
        return Router.DEFAULT;
    }

    @Bean
    public LoadBalancer loadBalancer() {
        return new RoundLoadBalancer();
//        return new RandomLoadBalancer();
//        return LoadBalancer.DEFAULT;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter consumer_registryCenter() {
        return new RegistryCenter.StaticRegistryCenter(List.of(servers.split(",")));
    }

}
