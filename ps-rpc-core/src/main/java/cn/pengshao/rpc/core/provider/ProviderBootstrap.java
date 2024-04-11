package cn.pengshao.rpc.core.provider;

import cn.pengshao.rpc.core.annotaion.PsProvider;
import cn.pengshao.rpc.core.api.RegistryCenter;
import cn.pengshao.rpc.core.api.RpcException;
import cn.pengshao.rpc.core.config.AppConfigProperties;
import cn.pengshao.rpc.core.config.ProviderConfigProperties;
import cn.pengshao.rpc.core.meta.InstanceMeta;
import cn.pengshao.rpc.core.meta.ProviderMeta;
import cn.pengshao.rpc.core.meta.ServiceMeta;
import cn.pengshao.rpc.core.util.MethodUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ProviderBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;
    private RegistryCenter registryCenter;
    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    private String port;
    private AppConfigProperties appConfigProperties;
    private ProviderConfigProperties providerConfigProperties;
    private InstanceMeta instance;

    public ProviderBootstrap(String port, AppConfigProperties appConfigProperties,
                             ProviderConfigProperties providerConfigProperties) {
        this.port = port;
        this.appConfigProperties = appConfigProperties;
        this.providerConfigProperties = providerConfigProperties;
    }

    @PostConstruct
    public void init() {
        registryCenter = applicationContext.getBean(RegistryCenter.class);
        Map<String, Object> serviceBeans = applicationContext.getBeansWithAnnotation(PsProvider.class);
        serviceBeans.keySet().forEach(log::info);
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
        log.info(" create a provider: " + providerMeta);
        skeleton.add(service.getCanonicalName(), providerMeta);
    }

    public void start() {
        try {
            registryCenter.start();
            String ip = InetAddress.getLocalHost().getHostAddress();            ;
            instance = InstanceMeta.http(ip, Integer.parseInt(port));
            instance.addParams(providerConfigProperties.getMetas());
            skeleton.keySet().forEach(this::registerService);
        } catch (UnknownHostException e) {
            throw new RpcException(e);
        }
    }

    private void registerService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(appConfigProperties.getId())
                .namespace(appConfigProperties.getNamespace())
                .env(appConfigProperties.getEnv())
                .name(service)
                .version(appConfigProperties.getVersion()).build();
        registryCenter.register(serviceMeta, instance);
    }

    @PreDestroy
    public void stop() {
        log.info(" ===> provider unregisterService.");
        skeleton.keySet().forEach(this::unregisterService);
        registryCenter.stop();
    }

    private void unregisterService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(appConfigProperties.getId())
                .namespace(appConfigProperties.getNamespace())
                .env(appConfigProperties.getEnv())
                .name(service)
                .version(appConfigProperties.getVersion()).build();
        registryCenter.unregister(serviceMeta, instance);
    }

}
