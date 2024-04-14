package cn.pengshao.rpc.demo.provider.controller;

import cn.pengshao.rpc.core.api.RpcRequest;
import cn.pengshao.rpc.core.api.RpcResponse;
import cn.pengshao.rpc.core.config.ProviderProperties;
import cn.pengshao.rpc.core.transport.SpringBootTransport;
import cn.pengshao.rpc.demo.api.User;
import cn.pengshao.rpc.demo.api.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.context.properties.ConfigurationPropertiesRebinder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/7 22:44
 */
@Slf4j
@RestController
public class ProviderController {

    @Autowired
    ProviderProperties providerProperties;
    @Autowired
    UserService userService;
    @Autowired
    SpringBootTransport transport;

    @RequestMapping("ports")
    public RpcResponse<String> ports(@RequestParam("ports") String ports) {
        userService.setTimeoutPorts(ports);
        RpcResponse<String> rpcResponse = new RpcResponse<>();
        rpcResponse.setStatus(true);
        rpcResponse.setData("success:" + ports);
        return rpcResponse;
    }

    @RequestMapping("/metas")
    public String meta() {
        log.info("metas:{}", providerProperties.getMetas());
        return providerProperties.getMetas().toString();
    }

    @Bean
    ApplicationRunner providerRun(@Autowired ApplicationContext context) {
        return x -> {
            ConfigurationPropertiesRebinder rebinder = context.getBean(ConfigurationPropertiesRebinder.class);
            log.info("rebinder:{}", rebinder);

            // testAll();
        };
    }

    private void testAll() {
        RpcRequest request = new RpcRequest();
        request.setService("cn.pengshao.rpc.demo.api.UserService");
        request.setMethodSign("findById_Integer");
        request.setArgs(new Object[]{100});

        RpcResponse<Object> rpcResponse = transport.invoke(request);
        log.info("[1] case findById_Integer return : " + rpcResponse.getData());

        request = new RpcRequest();
        request.setService("cn.pengshao.rpc.demo.api.UserService");
        request.setMethodSign("getList_List");
        List<User> userList = new ArrayList<>();
        userList.add(new User(100, "pp100"));
        userList.add(new User(101, "pp100"));
        request.setArgs(new Object[]{userList});
        rpcResponse = transport.invoke(request);
        log.info("[2] case getList_List return : " + rpcResponse.getData());

        request = new RpcRequest();
        request.setService("cn.pengshao.rpc.demo.api.UserService");
        request.setMethodSign("getMap_Map");
        Map<String, User> userMap = new HashMap<>();
        userMap.put("P100", new User(100, "PP100"));
        userMap.put("P101", new User(101, "PP101"));
        request.setArgs(new Object[]{ userMap });
        rpcResponse = transport.invoke(request);
        log.info("[3] case getMap_Map return : " + rpcResponse.getData());
    }

}
