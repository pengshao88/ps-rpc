package cn.pengshao.rpc.demo.consumer.service;

import cn.pengshao.rpc.core.annotaion.PsConsumer;
import cn.pengshao.rpc.demo.api.OrderService;
import cn.pengshao.rpc.demo.api.UserService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/11 22:37
 */
@Component
public class DemoService {

    @PsConsumer
    UserService userService;

    @PsConsumer
    OrderService orderService;

    @Bean
    public ApplicationRunner consumer_runner() {
        return x -> {
            System.out.println(userService.findById(1));
            System.out.println(userService.getId(200));
            System.out.println(userService.getName("tom"));
            System.out.println(orderService.findById(20));
            System.out.println(orderService.findById(404));
        };
    }

}
