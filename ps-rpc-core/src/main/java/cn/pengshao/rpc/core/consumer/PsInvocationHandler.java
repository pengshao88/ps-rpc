package cn.pengshao.rpc.core.consumer;

import cn.pengshao.rpc.core.api.*;
import cn.pengshao.rpc.core.consumer.http.OkHttpInvoker;
import cn.pengshao.rpc.core.enums.ErrorCodeEnum;
import cn.pengshao.rpc.core.governance.SlidingTimeWindow;
import cn.pengshao.rpc.core.meta.InstanceMeta;
import cn.pengshao.rpc.core.util.MethodUtils;
import cn.pengshao.rpc.core.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.*;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


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
    private final OkHttpInvoker httpInvoker;

    /**
     * 滑动窗口 记录统计 实例调用失败记录 30s内调用失败10次  ===> 隔离实例列表
     */
    private final Map<String, SlidingTimeWindow> windows = new HashMap<>();

    /**
     * 隔离实例列表
     */
    private final List<InstanceMeta> isolatedProviders = new ArrayList<>();

    /**
     * 半开实例列表 定时从隔离列表中获取 被隔离的实例 用于尝试调用
     */
    private  final List<InstanceMeta> halfOpenProviders = new ArrayList<>();

    public PsInvocationHandler(Class<?> service, RpcContext context, List<InstanceMeta> providers) {
        this.service = service;
        this.context = context;
        this.providers = providers;
        int timeout = Integer.parseInt(context.getParameters()
                .getOrDefault("app.timeout", "1000"));
        this.httpInvoker = new OkHttpInvoker(timeout);
        ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);
        int halfOpenInitialDelay = Integer.parseInt(context.getParameters()
                .getOrDefault("app.halfOpenInitialDelay", "10000"));
        int halfOpenDelay = Integer.parseInt(context.getParameters()
                .getOrDefault("app.halfOpenDelay", "60000"));
        // 延迟10s，每隔60s执行一次
        scheduledExecutor.scheduleWithFixedDelay(this::halfOpen, halfOpenInitialDelay, halfOpenDelay,
                TimeUnit.MILLISECONDS);
    }

    private void halfOpen() {
        log.debug(" ======> half open isolatedProviders:{}", isolatedProviders);
        halfOpenProviders.clear();
        halfOpenProviders.addAll(isolatedProviders);
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

        int retries = Integer.parseInt(context.getParameters()
                .getOrDefault("app.retries", "1"));
        int faultLimit = Integer.parseInt(context.getParameters()
                .getOrDefault("app.faultLimit", "10"));

        while (retries-- > 0) {
            try {
                for (Filter filter : this.context.getFilters()) {
                    Object preResult = filter.preFilter(request);
                    if(preResult != null) {
                        log.debug(filter.getClass().getName() + " ==> prefilter: " + preResult);
                        return preResult;
                    }
                }

                InstanceMeta instanceMeta;
                synchronized (halfOpenProviders) {
                    if (halfOpenProviders.isEmpty()) {
                        List<InstanceMeta> routedProviders = context.getRouter().route(providers);
                        instanceMeta = context.getLoadBalancer().choose(routedProviders);
                        log.info("loadBalancer.choose(urls) ===> " + instanceMeta);
                    } else {
                        // 尝试调用半开实例
                        instanceMeta = halfOpenProviders.remove(0);
                        log.info("check alive instance ===> " + instanceMeta);
                    }
                }

                String url = instanceMeta.toUrl();
                RpcResponse<Object> rpcResponse;
                Object result;
                try {
                    rpcResponse = httpInvoker.post(request, url);
                    result = castReturnResult(method, rpcResponse);
                } catch (Exception e) {
                    // 故障的规则统计和隔离，
                    // 每一次异常，记录一次，统计30s的异常数。
                    synchronized (windows) {
                        SlidingTimeWindow window = windows.computeIfAbsent(url, v -> new SlidingTimeWindow());
                        window.record(System.currentTimeMillis());
                        log.debug("instance {} in window with {}", url, window.getSum());
                        if (window.getSum() >= faultLimit) {
                            isolate(instanceMeta);
                        }
                    }

                    throw e;
                }

                synchronized (providers) {
                    if (!providers.contains(instanceMeta)) {
                        // 尝试调用成功，从隔离列表中移除，并且添加到可用列表中
                        isolatedProviders.remove(instanceMeta);
                        providers.add(instanceMeta);
                        log.debug("instance {} is recovered, isolatedProviders={}, providers={}",
                                instanceMeta, isolatedProviders, providers);
                    }
                }

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

//                log.warn("invoke fail", e);
            }
        }
        return null;
    }

    /**
     * 故障的规则统计和隔离，
     * 每一次异常，记录一次，统计30s的异常数。
     * 如果异常数大于10，则将实例放入隔离列表。
     *
     * @param instanceMeta 实例
     */
    private void isolate(InstanceMeta instanceMeta) {
        log.debug(" ==> isolate instance: " + instanceMeta);
        providers.remove(instanceMeta);
        log.debug(" ==> providers = {}", providers);
        isolatedProviders.add(instanceMeta);
        log.debug(" ==> isolatedProviders = {}", isolatedProviders);
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
