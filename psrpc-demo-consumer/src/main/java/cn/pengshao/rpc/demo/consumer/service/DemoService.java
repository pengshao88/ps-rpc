package cn.pengshao.rpc.demo.consumer.service;

import cn.pengshao.rpc.core.annotaion.PsConsumer;
import cn.pengshao.rpc.core.api.RpcContext;
import cn.pengshao.rpc.demo.api.User;
import cn.pengshao.rpc.demo.api.UserService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
public class DemoService {

    @PsConsumer
    UserService userService;

    @Bean
    public ApplicationRunner consumer_runner() {
        return x -> {
            testAll();
        };
    }

    private void testAll() {
        log.info("[1] test case result:{}", userService.findById(1));
        log.info("[2] test case result:{}", userService.getId(200));
        log.info("[3] test case result:{}", userService.getName("tom"));
        log.info("[5] test case result:{}", userService.getId(1000000000000000000L));
        log.info("[6] test case result:{}", userService.getId(new User(99, "stephen")));
        log.info("[7] test case result:{}", userService.getId(100000.0f));
        log.info("[8] test case result:{}", userService.getName());
        log.info("[9] test case result:{}", userService.getName(88));
        log.info("[10] test case result:{}", Arrays.toString(userService.getIds()));
        log.info("[11] test case result:{}", Arrays.toString(userService.getLongIds()));
        log.info("[12] test case result:{}", Arrays.toString(userService.getIds(new int[]{101, 102, 103})));
        log.info("[13] test case result:{}", userService.getList(Collections.singletonList(new User(188888, "getList"))));
        HashMap<String, User> hashMap = new HashMap<>();
        hashMap.put("ps", new User(9999, "ps"));
        log.info("[14] test case result:{}", userService.getMap(hashMap));
        log.info("[15] test case result:{}", userService.getFlag(false));
        log.info("[16] test case >>===[测试参数和返回值都是User[]类型]===");
        User[] users = new User[]{
                new User(100, "ps-100"),
                new User(101, "ps-101")};
        Arrays.stream(userService.findUsers(users)).forEach(user -> log.info("user:{}", user));

        log.info("[17] test case >>===[测试服务端抛出一个RuntimeException异常]===");
        try {
            User userEx = userService.ex(true);
            log.info(" userEx:{}", userEx);
        } catch (RuntimeException e) {
            log.info(" ===> exception: " + e.getMessage());
        }

        log.info("[18] test case >>===[测试服务端抛出一个超时重试后成功的场景]===");
        // 超时设置的【漏斗原则】
        // A 2000 -> B 1500 -> C 1200 -> D 1000
        long start = System.currentTimeMillis();
        userService.find(1100);
        log.info("userService.find take " + (System.currentTimeMillis()-start) + " ms");

        log.info("[19] test case >>===[测试通过Context跨消费者和提供者进行传参]===");
        String Key_Version = "rpc.version";
        RpcContext.setContextParameter(Key_Version, "v8");
        String version = userService.echoParameter(Key_Version);
        log.info(" ===> echo parameter from c->p->c: " + Key_Version + " -> " + version);

        String Key_Message = "rpc.message";
        RpcContext.setContextParameter("rpc.message", "this is a test message");
        String message = userService.echoParameter("rpc.message");
        log.info(" ===> echo parameter from c->p->c: " + Key_Message + " -> " + message);
    }

}
