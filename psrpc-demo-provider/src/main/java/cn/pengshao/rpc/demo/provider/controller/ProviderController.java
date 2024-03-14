package cn.pengshao.rpc.demo.provider.controller;

import cn.pengshao.rpc.core.api.RpcRequest;
import cn.pengshao.rpc.core.api.RpcResponse;
import cn.pengshao.rpc.core.provider.ProviderBootstrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/7 22:44
 */
@RestController
public class ProviderController {

    @Autowired
    ProviderBootstrap providerBootstrap;

    @RequestMapping("/")
    public RpcResponse invoke(@RequestBody RpcRequest request) {
        return providerBootstrap.invoke(request);
    }

    @Bean
    ApplicationRunner providerRun() {
        return x -> {
            RpcRequest request = new RpcRequest();
            request.setService("cn.pengshao.rpc.demo.api.UserService");
            request.setMethodSign("findById_Integer");
            request.setArgs(new Object[]{100});

            RpcResponse rpcResponse = invoke(request);
            System.out.println("return : "+rpcResponse.getData());

        };
    }

}
