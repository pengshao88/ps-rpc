package cn.pengshao.rpc.core.consumer;

import cn.pengshao.rpc.core.api.*;
import cn.pengshao.rpc.core.consumer.http.OkHttpInvoker;
import cn.pengshao.rpc.core.enums.ErrorCodeEnum;
import cn.pengshao.rpc.core.meta.InstanceMeta;
import cn.pengshao.rpc.core.util.MethodUtils;
import cn.pengshao.rpc.core.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.*;
import java.net.SocketTimeoutException;
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
    private OkHttpInvoker httpInvoker;

    public PsInvocationHandler(Class<?> service, RpcContext context, List<InstanceMeta> providers) {
        this.service = service;
        this.context = context;
        this.providers = providers;
        int timeout = Integer.parseInt(context.getParameters()
                .getOrDefault("app.timeout", "1000"));
        this.httpInvoker = new OkHttpInvoker(timeout);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (MethodUtils.checkLocalMethod(method.getName())) {
            return null;
        }

        RpcRequest request = new RpcRequest();
        request.setService(service.getCanonicalName());
        request.setMethodSign(MethodUtils.getMethodSign(method));
        request.setArgs(args);

        // todo typeReference 无法使用 class,需要知道真实的类型
//        String response = postReturnString(request);
//        Class<? extends Type> actualType = method.getGenericReturnType().getClass();
//        JSON.parseObject(response, new TypeReference<RpcResponse<actualType>>());

        int retries = Integer.parseInt(context.getParameters()
                .getOrDefault("app.retries", "1"));
        while (retries-- > 0) {
            try {
                for (Filter filter : this.context.getFilters()) {
                    Object preResult = filter.preFilter(request);
                    if(preResult != null) {
                        log.debug(filter.getClass().getName() + " ==> prefilter: " + preResult);
                        return preResult;
                    }
                }

                List<InstanceMeta> routedProviders = context.getRouter().route(providers);
                InstanceMeta instanceMeta = context.getLoadBalancer().choose(routedProviders);
                String url = instanceMeta.toUrl();

                log.info("loadBalancer.choose(urls) ===> " + url);
                RpcResponse<Object> rpcResponse = httpInvoker.post(request, url);
                Object result = castReturnResult(method, rpcResponse);
                for (Filter filter : this.context.getFilters()) {
                    Object filterResult = filter.postFilter(request, rpcResponse, result);
                    if(filterResult != null) {
                        return filterResult;
                    }
                }
                return result;
            } catch (Exception e) {
                if (!(e.getCause() instanceof SocketTimeoutException)) {
                    throw new RpcException(e, ErrorCodeEnum.UNKNOWN_ERROR.getErrorMsg());
                }

                log.warn("invoke fail", e);
            }
        }
        return null;
    }

    private static Object castReturnResult(Method method, RpcResponse<?> rpcResponse) {
        if (rpcResponse.isStatus()) {
            return TypeUtils.castMethod(method, rpcResponse.getData());
        } else {
            Exception exception = rpcResponse.getEx();
            if(exception instanceof RpcException ex) {
                throw ex;
            } else {
                throw new RpcException(exception, ErrorCodeEnum.UNKNOWN_ERROR.getErrorMsg());
            }
        }
    }
}
