package cn.pengshao.rpc.core.consumer;

import cn.pengshao.rpc.core.api.*;
import cn.pengshao.rpc.core.consumer.http.OkHttpInvoker;
import cn.pengshao.rpc.core.meta.InstanceMeta;
import cn.pengshao.rpc.core.util.MethodUtils;
import cn.pengshao.rpc.core.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.*;
import java.util.*;


/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/11 22:15
 */
@Slf4j
public class PsInvocationHandler implements InvocationHandler {

    private final Class<?> service;
    private final RpcContext context;
    private final List<InstanceMeta> providers;
    private final static OkHttpInvoker HTTP_INVOKER = new OkHttpInvoker();

    public PsInvocationHandler(Class<?> service, RpcContext context, List<InstanceMeta> providers) {
        this.service = service;
        this.context = context;
        this.providers = providers;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (MethodUtils.checkLocalMethod(method.getName())) {
            return null;
        }
        List<InstanceMeta> routedProviders = context.getRouter().route(providers);
        InstanceMeta instanceMeta = (InstanceMeta) context.getLoadBalancer().choose(routedProviders);
        String url = instanceMeta.toUrl();

        RpcRequest request = new RpcRequest();
        request.setService(service.getCanonicalName());
        request.setMethodSign(MethodUtils.getMethodSign(method));
        request.setArgs(args);

        // todo typeReference 无法使用 class,需要知道真实的类型
//        String response = postReturnString(request);
//        Class<? extends Type> actualType = method.getGenericReturnType().getClass();
//        JSON.parseObject(response, new TypeReference<RpcResponse<actualType>>());

        log.debug("loadBalancer.choose(urls) ===> " + url);
        RpcResponse<Object> rpcResponse = HTTP_INVOKER.post(request, url);
        if (rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            return TypeUtils.castMethod(data, method);
        } else {
            throw new RuntimeException(rpcResponse.getMsg());
        }
    }

}
