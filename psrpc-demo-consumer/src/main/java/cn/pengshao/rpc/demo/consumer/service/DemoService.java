package cn.pengshao.rpc.demo.consumer.service;

import cn.pengshao.rpc.core.annotaion.PsConsumer;
import cn.pengshao.rpc.demo.api.OrderService;
import cn.pengshao.rpc.demo.api.User;
import cn.pengshao.rpc.demo.api.UserService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

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

            System.out.println(userService.getId(1000000000000000000L));
            System.out.println(userService.getId(new User(99, "stephen")));
            System.out.println(userService.getId(100000.0f));
            System.out.println(userService.getName());
            System.out.println(userService.getName(88));
            System.out.println(Arrays.toString(userService.getIds()));
            System.out.println(Arrays.toString(userService.getLongIds()));
            System.out.println(Arrays.toString(userService.getIds(new int[]{101, 102, 103})));
            System.out.println(userService.getList(Collections.singletonList(new User(188888, "getList"))));
            HashMap<String, User> hashMap = new HashMap<>();
            hashMap.put("ps", new User(9999, "ps"));
            System.out.println(userService.getMap(hashMap));
            System.out.println(userService.getFlag(false));

            System.out.println("Case 17. >>===[测试参数和返回值都是User[]类型]===");
            User[] users = new User[]{
                    new User(100, "ps-100"),
                    new User(101, "ps-101")};
            Arrays.stream(userService.findUsers(users)).forEach(System.out::println);

//            System.out.println(orderService.findById(404));
        };
    }

}
