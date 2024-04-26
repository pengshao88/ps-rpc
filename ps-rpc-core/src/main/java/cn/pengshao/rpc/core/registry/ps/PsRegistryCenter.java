package cn.pengshao.rpc.core.registry.ps;

import cn.pengshao.rpc.core.registry.RegistryCenter;
import cn.pengshao.rpc.core.consumer.HttpInvoker;
import cn.pengshao.rpc.core.meta.InstanceMeta;
import cn.pengshao.rpc.core.meta.ServiceMeta;
import cn.pengshao.rpc.core.registry.ChangedListener;
import cn.pengshao.rpc.core.registry.Event;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Description: implement for psRegistry center
 *
 * @Author: yezp
 * @date 2024/4/24 22:32
 */
@Slf4j
public class PsRegistryCenter implements RegistryCenter {

    // TODO 改成集群，获取 leader，往 leader 注册
    @Value("${psregistry.servers}")
    private String servers;

    Map<String, Long> VERSIONS = new HashMap<>();
    MultiValueMap<InstanceMeta, ServiceMeta> RENEWS = new LinkedMultiValueMap<>();
    ScheduledExecutorService consumerExecutor;
    ScheduledExecutorService producerExecutor;

    @Override
    public void start() {
        log.info(" ====>>>> [PsRegistryCenter] : start with server : {}", servers);
        consumerExecutor = Executors.newScheduledThreadPool(1);
        producerExecutor = Executors.newScheduledThreadPool(1);
        producerExecutor.scheduleWithFixedDelay(() -> RENEWS.keySet().forEach(instance -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (ServiceMeta serviceMeta : RENEWS.get(instance)) {
                stringBuilder.append(serviceMeta.toPath()).append(",");
            }

            String services = stringBuilder.toString();
            if (services.endsWith(",")) {
                services = services.substring(0, services.length() - 1);
            }
            // 每个 service 探活
            Long timestamp = HttpInvoker.httpPost(JSON.toJSONString(instance),
                    servers + "/renews?services=" + services, Long.class);
            log.info(" ====>>>> [PsRegistryCenter] : renew instance:{} for {} at {}", instance, services,
                    timestamp);
        }), 5000, 5000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        log.info(" ====>>>> [PsRegistryCenter] : stop with server : {}", servers);
        gracefulShutdown(consumerExecutor);
        gracefulShutdown(producerExecutor);
    }

    private void gracefulShutdown(ScheduledExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(3000, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            // ignore
        }
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        log.info(" ====>>>> [PsRegistryCenter] : register instance:{} for {}", instance, service);
        HttpInvoker.httpPost(JSON.toJSONString(instance), servers + "/register?service=" + service.toPath(), InstanceMeta.class);
        log.info(" ====>>>> [PsRegistryCenter] : registered {}", instance);
        RENEWS.add(instance, service);
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        log.info(" ====>>>> [PsRegistryCenter] : unregister instance:{} for {}", instance, service);
        HttpInvoker.httpPost(JSON.toJSONString(instance), servers + "/unregister?service=" + service.toPath(), InstanceMeta.class);
        log.info(" ====>>>> [PsRegistryCenter] : unregistered {}", instance);
        RENEWS.remove(instance, service);
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        log.info(" ====>>>> [PsRegistryCenter] : findAll instance for {}", service);
        List<InstanceMeta> instances = HttpInvoker.httpGet(servers + "/findAll?service=" + service.toPath(), new TypeReference<List<InstanceMeta>>() {
        });
        log.info(" ====>>>> [PsRegistryCenter] : findAll {}", instances);
        return instances;
    }

    @Override
    public void subscribe(ServiceMeta service, ChangedListener changedListener) {
        consumerExecutor.scheduleWithFixedDelay(() -> {
            Long version = VERSIONS.getOrDefault(service.toPath(), -1L);
            Long newVersion = HttpInvoker.httpGet(servers + "/version?service=" + service.toPath(), Long.class);
            log.info(" ====>>>> [PsRegistryCenter] : version = {} newVersion = {}", newVersion, newVersion);
            // 版本号不同，更新
            if (newVersion > version) {
                List<InstanceMeta> instances = fetchAll(service);
                // 更新服务器列表
                changedListener.fire(new Event(instances));
                // 记录本次版本号
                VERSIONS.put(service.toPath(), newVersion);
            }
        }, 1000, 5000, TimeUnit.MILLISECONDS);
    }
}
