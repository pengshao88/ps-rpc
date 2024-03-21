package cn.pengshao.rpc.core.provider;

import cn.pengshao.rpc.core.annotaion.PsProvider;
import cn.pengshao.rpc.core.api.RegistryCenter;
import cn.pengshao.rpc.core.meta.InstanceMeta;
import cn.pengshao.rpc.core.meta.ProviderMeta;
import cn.pengshao.rpc.core.util.MethodUtils;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
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
    private RegistryCenter registryCenter;
    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    @Value("${server.port}")
    private String port;
    private InstanceMeta instance;

    @PostConstruct
    public void init() {
        registryCenter = applicationContext.getBean(RegistryCenter.class);
        Map<String, Object> serviceBeans = applicationContext.getBeansWithAnnotation(PsProvider.class);
        serviceBeans.keySet().forEach(System.out::println);
        serviceBeans.values().forEach(this::genInterfaces);
    }

    private void genInterfaces(Object impl) {
        Arrays.stream(impl.getClass().getInterfaces()).forEach(service ->
                Arrays.stream(service.getMethods())
                        .filter(method -> !MethodUtils.checkLocalMethod(method.getName()))
                        .forEach(method -> createProvider(service, impl, method))
        );
    }

    private void createProvider(Class<?> service, Object impl, Method method) {
        ProviderMeta providerMeta = ProviderMeta.builder().method(method)
                .serviceImpl(impl).methodSign(MethodUtils.getMethodSign(method)).build();
        System.out.println(" create a provider: " + providerMeta);
        skeleton.add(service.getCanonicalName(), providerMeta);
    }

    public void start() {
        try {
            registryCenter.start();
            String ip = InetAddress.getLocalHost().getHostAddress();            ;
            instance = InstanceMeta.http(ip, Integer.parseInt(port));
            skeleton.keySet().forEach(this::registerService);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerService(String service) {
        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);
        rc.register(service, instance);
    }

    public void stop() {
        System.out.println(" ===> provider unregisterService.");
        skeleton.keySet().forEach(this::unregisterService);
        registryCenter.stop();
    }

    private void unregisterService(String service) {
        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);
        rc.unregister(service, instance);
    }

}
