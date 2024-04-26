package cn.pengshao.rpc.core.consumer;

import cn.pengshao.rpc.core.annotaion.PsConsumer;
import cn.pengshao.rpc.core.registry.RegistryCenter;
import cn.pengshao.rpc.core.api.RpcContext;
import cn.pengshao.rpc.core.meta.InstanceMeta;
import cn.pengshao.rpc.core.meta.ServiceMeta;
import cn.pengshao.rpc.core.util.MethodUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
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
@Slf4j
public class ConsumerBootstrap implements ApplicationContextAware, EnvironmentAware {

    private Map<String, Object> stub = new HashMap<>();
    ApplicationContext applicationContext;
    Environment environment;

    public void start() {
        RpcContext context = applicationContext.getBean(RpcContext.class);
        RegistryCenter registryCenter = applicationContext.getBean(RegistryCenter.class);

        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            // 查找带有PsConsumer 注解的字段，反射注入
            List<Field> fields = MethodUtils.findAnnotatedField(bean.getClass(), PsConsumer.class);
            for (Field field : fields) {
                Class<?> service = field.getType();
                String serviceName = service.getCanonicalName();
                try {
                    Object consumer = stub.get(serviceName);
                    if (consumer == null) {
                        consumer = createRegistry(service, context, registryCenter);
                        stub.put(serviceName, consumer);
                    }

                    field.setAccessible(true);
                    field.set(bean, consumer);
                } catch (Exception e) {
                    log.warn(" ==> Field[{}.{}] create consumer failed.", serviceName, field.getName());
                    log.error("Ignore and print it as: ", e);
                }
            }
        }
    }

    private Object createRegistry(Class<?> service, RpcContext context, RegistryCenter registryCenter) {
        String serviceName = service.getCanonicalName();
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(context.getParam("app.id"))
                .namespace(context.getParam("app.namespace"))
                .env(context.getParam("app.env"))
                .name(serviceName)
                .version(context.getParam("app.version")).build();
        List<InstanceMeta> providers = registryCenter.fetchAll(serviceMeta);
        log.info("fetchAll providers: " + providers);

        registryCenter.subscribe(serviceMeta, event -> {
            providers.clear();
            providers.addAll(event.getData());
        });
        return createConsumer(service, context, providers);
    }

    private Object createConsumer(Class<?> service, RpcContext context, List<InstanceMeta> providers) {
        return Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service},
                new PsInvocationHandler(service, context, providers));
    }

}
