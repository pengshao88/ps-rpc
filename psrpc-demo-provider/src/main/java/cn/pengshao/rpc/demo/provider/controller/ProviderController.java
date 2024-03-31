package cn.pengshao.rpc.demo.provider.controller;

import cn.pengshao.rpc.core.api.RpcRequest;
import cn.pengshao.rpc.core.api.RpcResponse;
import cn.pengshao.rpc.core.provider.ProviderInvoker;
import cn.pengshao.rpc.demo.api.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    ProviderInvoker providerInvoker;
    @Autowired
    UserService userService;

    @RequestMapping("/")
    public RpcResponse<Object> invoke(@RequestBody RpcRequest request) {
        return providerInvoker.invoke(request);
    }

    @RequestMapping("ports")
    public RpcResponse<String> ports(@RequestParam("ports") String ports) {
        userService.setTimeoutPorts(ports);
        RpcResponse<String> rpcResponse = new RpcResponse<>();
        rpcResponse.setStatus(true);
        rpcResponse.setData("success:" + ports);
        return rpcResponse;
    }

    @Bean
    ApplicationRunner providerRun() {
        return x -> {
            RpcRequest request = new RpcRequest();
            request.setService("cn.pengshao.rpc.demo.api.UserService");
            request.setMethodSign("findById_Integer");
            request.setArgs(new Object[]{100});

            RpcResponse<Object> rpcResponse = invoke(request);
            log.info("return : "+rpcResponse.getData());
        };
    }

}
