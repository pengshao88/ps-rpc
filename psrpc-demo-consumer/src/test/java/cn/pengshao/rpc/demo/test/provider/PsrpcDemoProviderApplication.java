package cn.pengshao.rpc.demo.test.provider;

import cn.pengshao.rpc.core.api.RpcRequest;
import cn.pengshao.rpc.core.api.RpcResponse;
import cn.pengshao.rpc.core.provider.ProviderConfig;
import cn.pengshao.rpc.core.provider.ProviderInvoker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/26 23:33
 */
@Slf4j
@SpringBootApplication
@RestController
@Import({ProviderConfig.class})
public class PsrpcDemoProviderApplication {

    @Autowired
    ProviderInvoker providerInvoker;

    @RequestMapping("/")
    public RpcResponse<Object> invoke(@RequestBody RpcRequest request) {
        return providerInvoker.invoke(request);
    }

    @Bean
    ApplicationRunner providerRun() {
        return x -> {
            RpcRequest request = new RpcRequest();
            request.setService("cn.pengshao.rpc.demo.api.UserService");
            request.setMethodSign("findById_Integer");
            request.setArgs(new Object[]{100});

            RpcResponse<Object> rpcResponse = invoke(request);
            log.info("findById_Integer result : "+rpcResponse.getData());
        };
    }

}
