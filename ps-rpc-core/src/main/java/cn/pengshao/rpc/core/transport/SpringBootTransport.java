package cn.pengshao.rpc.core.transport;

import cn.pengshao.rpc.core.api.RpcRequest;
import cn.pengshao.rpc.core.api.RpcResponse;
import cn.pengshao.rpc.core.provider.ProviderInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/4/10 22:51
 */
@RestController
public class SpringBootTransport {

    @Autowired
    ProviderInvoker providerInvoker;

    @RequestMapping("/psrpc")
    public RpcResponse<Object> invoke(@RequestBody RpcRequest request) {
        return providerInvoker.invoke(request);
    }

}
