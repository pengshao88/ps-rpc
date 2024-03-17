package cn.pengshao.rpc.core.consumer;

import cn.pengshao.rpc.core.annotaion.PsConsumer;
import cn.pengshao.rpc.core.api.LoadBalancer;
import cn.pengshao.rpc.core.api.RegistryCenter;
import cn.pengshao.rpc.core.api.Router;
import cn.pengshao.rpc.core.api.RpcContext;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/10 22:39
 */
@Data
public class ConsumerBootstrap implements ApplicationContextAware, EnvironmentAware {

    private Map<String, Object> stub = new HashMap<>();

    ApplicationContext applicationContext;
    Environment environment;

    public void start() {
        RpcContext context = new RpcContext();
        context.setRouter(applicationContext.getBean(Router.class));
        context.setLoadBalancer(applicationContext.getBean(LoadBalancer.class));
        RegistryCenter registryCenter = applicationContext.getBean(RegistryCenter.class);

        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            // 查找带有PsConsumer 注解的字段，反射注入
            List<Field> fields = findAnnotatedField(bean.getClass());
            for (Field field : fields) {
                try {
                    Class<?> service = field.getType();
                    String serviceName = service.getCanonicalName();
                    if (stub.containsKey(serviceName)) {
                        continue;
                    }

                    Object consumer = createConsumer(service, context, registryCenter);
                    field.setAccessible(true);
                    field.set(bean, consumer);
                    stub.put(serviceName, consumer);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }

    }

    private Object createConsumer(Class<?> service, RpcContext context, RegistryCenter registryCenter) {
        return createConsumer(service, context, registryCenter.fetchAll(service.getCanonicalName()));
    }

    private Object createConsumer(Class<?> service, RpcContext context, List<String> providers) {
        return Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service},
                new PsInvocationHandler(service, context, providers));
    }

    private List<Field> findAnnotatedField(Class<?> clazz) {
        List<Field> result = new ArrayList<>();
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(PsConsumer.class)) {
                    result.add(field);
                }
            }
            // 被代理的类 是原始类的子类
            clazz = clazz.getSuperclass();
        }
        return result;
    }

}
