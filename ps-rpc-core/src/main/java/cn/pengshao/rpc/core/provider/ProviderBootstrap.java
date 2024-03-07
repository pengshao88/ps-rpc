package cn.pengshao.rpc.core.provider;

import cn.pengshao.rpc.core.annotaion.PsProvider;
import cn.pengshao.rpc.core.api.RpcRequest;
import cn.pengshao.rpc.core.api.RpcResponse;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    private Map<String, Object> serviceMap = new HashMap<>();

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
    }

    public RpcResponse invoke(RpcRequest request) {
        Object bean = serviceMap.get(request.getService());
        try {
            Method method = findMethod(bean.getClass(), request.getMethod());
            Object result = method.invoke(bean, request.getArgs());
            return new RpcResponse(true, result);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Method findMethod(Class<?> aClass, String methodName) {
        for (Method method : aClass.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

}
