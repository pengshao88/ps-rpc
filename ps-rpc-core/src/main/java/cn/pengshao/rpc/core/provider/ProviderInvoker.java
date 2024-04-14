package cn.pengshao.rpc.core.provider;

import cn.pengshao.rpc.core.api.RpcContext;
import cn.pengshao.rpc.core.api.RpcException;
import cn.pengshao.rpc.core.api.RpcRequest;
import cn.pengshao.rpc.core.api.RpcResponse;
import cn.pengshao.rpc.core.config.ProviderProperties;
import cn.pengshao.rpc.core.enums.ErrorCodeEnum;
import cn.pengshao.rpc.core.governance.SlidingTimeWindow;
import cn.pengshao.rpc.core.meta.ProviderMeta;
import cn.pengshao.rpc.core.util.TypeUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Description:在提供者端 执行服务方法
 *
 * @Author: yezp
 * @date 2024/3/21 22:29
 */
@Data
@Slf4j
public class ProviderInvoker {

    private MultiValueMap<String, ProviderMeta> skeleton;

//    private final int trafficControl;
    // todo 1201 : 改成map，针对不同的服务用不同的流控值
    // todo 1202 : 对多个节点是共享一个数值，，，把这个map放到redis

    private final Map<String, SlidingTimeWindow> windows = new HashMap<>();
    final ProviderProperties providerProperties;

    public ProviderInvoker(ProviderBootstrap providerBootstrap) {
        this.skeleton = providerBootstrap.getSkeleton();
        this.providerProperties = providerBootstrap.getProviderProperties();
        // this.trafficControl = Integer.parseInt(metas.getOrDefault("tc", "20"));
    }

    public RpcResponse<Object> invoke(RpcRequest request) {
        log.debug(" ===> providerInvoker invoke request:{}", request);
        if (!request.getParams().isEmpty()) {
            request.getParams().forEach(RpcContext::setContextParameter);
        }

        RpcResponse<Object> rpcResponse;
        String service = request.getService();
        synchronized (windows) {
            SlidingTimeWindow window = windows.computeIfAbsent(service, k -> new SlidingTimeWindow());
            int trafficControl = Integer.parseInt(providerProperties.getMetas().getOrDefault("tc", "20"));
            log.debug(" ===>> trafficControl:{} for {}", trafficControl, service);
            if (window.calcSum() > trafficControl) {
                // 流控
                String errorMsg = "service " + service + " invoked in 30s/[" + window.getSum()
                        + "] larger than tpsLimit = " + trafficControl;
                log.warn(errorMsg);
                return new RpcResponse<>(false, null, new RpcException(errorMsg, ErrorCodeEnum.EXCEED_LIMIT_EX.getErrorCode()));
            }
            window.record(System.currentTimeMillis());
            log.debug("service {} in window with {}", service, window.getSum());
        }

        List<ProviderMeta> providerMetas = skeleton.get(service);
        try {
            ProviderMeta providerMeta = findProviderMeta(providerMetas, request.getMethodSign());
            if (providerMeta == null) {
                return new RpcResponse<>(false, null, new RpcException(ErrorCodeEnum.NO_SUCH_METHOD.getErrorMsg()));
            }

            Method method = providerMeta.getMethod();
            Object[] args = processArgs(request.getArgs(), method.getParameterTypes(), method.getGenericParameterTypes());
            Object result = method.invoke(providerMeta.getServiceImpl(), args);
            rpcResponse = new RpcResponse<>(true, result, null);
        } catch (InvocationTargetException e) {
            rpcResponse =  new RpcResponse<>(false, null, new RpcException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException | IllegalArgumentException e) {
            rpcResponse =  new RpcResponse<>(false, null, new RpcException(e.getMessage()));
        } finally {
            // 防止内存泄露和上下文污染
            RpcContext.CONTEXT_PARAMETERS.get().clear();
        }
        log.debug(" ===> provider.invoke() = {}", rpcResponse);
        return rpcResponse;
    }

    private Object[] processArgs(Object[] args, Class<?>[] parameterTypes, Type[] genericParameterTypes) {
        if (args == null || args.length == 0) return args;
        Object[] actualArr = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            // 传入参数类型和泛型类型
            actualArr[i] = TypeUtils.castGeneric(args[i], parameterTypes[i], genericParameterTypes[i]);
        }
        return actualArr;
    }

    private ProviderMeta findProviderMeta(List<ProviderMeta> providerMetas, String methodSign) {
        Optional<ProviderMeta> optional = providerMetas.stream()
                .filter(x -> x.getMethodSign().equals(methodSign)).findFirst();
        return optional.orElse(null);
    }
}
