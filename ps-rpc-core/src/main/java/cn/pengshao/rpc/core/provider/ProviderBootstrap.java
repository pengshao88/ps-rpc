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
            StringBuilder signatureBuilder = new StringBuilder();
            signatureBuilder.append(method.getName());
            // 获取并添加参数类型的简单名称
            for (Class<?> paramType : method.getParameterTypes()) {
                signatureBuilder.append('_').append(paramType.getSimpleName());
            }
            // 添加返回类型
            signatureBuilder.append("_").append(method.getReturnType().getSimpleName());
            // 输出方法签名
            System.out.println("Method Signature: " + signatureBuilder);
            methodMap.put(signatureBuilder.toString(), method);
        }
        serviceMethodMap.put(serviceName, methodMap);
    }

    public RpcResponse invoke(RpcRequest request) {
        Object bean = serviceMap.get(request.getService());
        try {
            Method method = findMethod(request.getService(), request.getMethodSign());
            Object result = method.invoke(bean, request.getArgs());
            return new RpcResponse(true, result);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Method findMethod(String serviceName, String methodSign) {
        if (serviceMethodMap.containsKey(serviceName)) {
            return serviceMethodMap.get(serviceName).get(methodSign);
        }
        return null;
    }

}
