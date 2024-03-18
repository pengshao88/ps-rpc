package cn.pengshao.rpc.core.provider;

import cn.pengshao.rpc.core.annotaion.PsProvider;
import cn.pengshao.rpc.core.api.RegistryCenter;
import cn.pengshao.rpc.core.api.RpcRequest;
import cn.pengshao.rpc.core.api.RpcResponse;
import cn.pengshao.rpc.core.util.MethodUtils;
import cn.pengshao.rpc.core.util.TypeUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/7 22:14
 */
@Data
public class ProviderBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    @Value("${server.port}")
    private String port;

    private String instance;
    private Map<String, Object> serviceMap = new HashMap<>();
    private Map<String, Map<String, Method>> serviceMethodMap = new HashMap<>();

    @PostConstruct
    public void initProviders() {
        Map<String, Object> serviceBeans = applicationContext.getBeansWithAnnotation(PsProvider.class);
        serviceBeans.forEach((k, v) -> {
            System.out.println(k);
            genInterfaces(v);
        });
    }

    private void genInterfaces(Object v) {
        String serviceName = v.getClass().getInterfaces()[0].getCanonicalName();
        serviceMap.put(serviceName, v);

        Map<String, Method> methodMap = new HashMap<>();
        Class<?> anInterface = v.getClass().getInterfaces()[0];
        Method[] methods = anInterface.getMethods();
        // 获取所有方法的签名
        for (Method method : methods) {
            methodMap.put(MethodUtils.getMethodSign(method), method);
        }
        serviceMethodMap.put(serviceName, methodMap);
    }

    public void start() {
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            instance = ip + "_" + port;
            serviceMap.keySet().forEach(this::registerService);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerService(String service) {
        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);
        rc.register(service, instance);
    }

    @PreDestroy
    public void stop() {
        serviceMap.keySet().forEach(this::unregisterService);
    }

    private void unregisterService(String service) {
        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);
        rc.unregister(service, instance);
    }

    public RpcResponse invoke(RpcRequest request) {
        Object bean = serviceMap.get(request.getService());
        try {
            Method method = findMethod(request.getService(), request.getMethodSign());
            if (method == null) {
                return new RpcResponse(false, null, "method not found");
            }

            Object[] args = processArgs(request.getArgs(), method.getParameterTypes());
            Object result = method.invoke(bean, args);
            return new RpcResponse(true, result, "success");
        } catch (InvocationTargetException e) {
            return new RpcResponse(false, null, e.getTargetException().getMessage());
        } catch (IllegalAccessException e) {
            return new RpcResponse(false, null, e.getMessage());
        }
    }

    private Object[] processArgs(Object[] args, Class<?>[] parameterTypes) {
        if(args == null || args.length == 0) return args;
        Object[] actualArr = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            actualArr[i] = TypeUtils.cast(args[i], parameterTypes[i]);
        }
        return actualArr;
    }

    private Method findMethod(String serviceName, String methodSign) {
        if (serviceMethodMap.containsKey(serviceName)) {
            return serviceMethodMap.get(serviceName).get(methodSign);
        }
        return null;
    }

}
