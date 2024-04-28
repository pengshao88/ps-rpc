package cn.pengshao.rpc.core.registry.ps;

import cn.pengshao.rpc.core.config.PsRegistryProperties;
import cn.pengshao.rpc.core.consumer.HttpInvoker;
import cn.pengshao.rpc.core.meta.InstanceMeta;
import cn.pengshao.rpc.core.meta.ServiceMeta;
import cn.pengshao.rpc.core.registry.ChangedListener;
import cn.pengshao.rpc.core.registry.Event;
import cn.pengshao.rpc.core.registry.RegistryCenter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

/**
 * Description: implement for psRegistry center
 *
 * @Author: yezp
 * @date 2024/4/24 22:32
 */
@Slf4j
public class PsRegistryCenter implements RegistryCenter {

    public static final String REG_PATH = "/register";
    public static final String UNREG_PATH = "/unregister";
    public static final String FIND_ALL_PATH = "/findAll";
    public static final String VERSION_PATH = "/version";
    public static final String RENEWS_PATH = "/renews";
    public static final String CLUSTER_PATH = "/cluster";

    /**
     * 1、获取注册中心的集群
     * 2、选取 leader
     * 3、注册到 leader
     */
    @Autowired
    PsRegistryProperties registryProperties;

    private String registryServer;
    Map<String, Long> VERSIONS = new HashMap<>();
    MultiValueMap<InstanceMeta, ServiceMeta> RENEWS = new LinkedMultiValueMap<>();
    List<String> servers;
    PsHealthChecker psHealthChecker = new PsHealthChecker();

    @Override
    public void start() {
        List<String> servers = registryProperties.getServers();
        log.info(" ====>>>> [PsRegistryCenter] : start with servers : {}", servers);
        psHealthChecker.start();
        registryServerCheck();
        clusterCheck();
        providerCheck();
    }

    private void registryServerCheck() {
        // 无论 provider、consumer 都从leader 中读写
        doClusterCheck();
        if (StringUtils.isEmpty(registryServer)) {
            throw new RuntimeException(" ====>>>> [PsRegistryCenter] : get registry leader server fail.");
        }
    }

    @Override
    public void stop() {
        log.info(" ====>>>> [PsRegistryCenter] : stop with server : {}", registryServer);
        psHealthChecker.stop();
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        log.info(" ====>>>> [PsRegistryCenter] : register instance:{} for {}", instance, service);
        HttpInvoker.httpPost(JSON.toJSONString(instance), path(REG_PATH, service), InstanceMeta.class);
        log.info(" ====>>>> [PsRegistryCenter] : registered {}", instance);
        RENEWS.add(instance, service);
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        log.info(" ====>>>> [PsRegistryCenter] : unregister instance:{} for {}", instance, service);
        HttpInvoker.httpPost(JSON.toJSONString(instance), path(UNREG_PATH, service), InstanceMeta.class);
        log.info(" ====>>>> [PsRegistryCenter] : unregistered {}", instance);
        RENEWS.remove(instance, service);
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        log.info(" ====>>>> [PsRegistryCenter] : findAll instance for {}", service);
        List<InstanceMeta> instances = HttpInvoker.httpGet(path(FIND_ALL_PATH, service), new TypeReference<>() {
        });
        log.info(" ====>>>> [PsRegistryCenter] : findAll {}", instances);
        return instances;
    }

    private void providerCheck() {
        psHealthChecker.providerCheck(() -> RENEWS.keySet().forEach(instance -> {
            // 每个 service 探活
            Long timestamp = HttpInvoker.httpPost(JSON.toJSONString(instance),
                    path(RENEWS_PATH, RENEWS.get(instance)), Long.class);
            log.info(" ====>>>> [PsRegistryCenter] : renew instance:{} at {}", instance, timestamp);
        }));
    }

    @Override
    public void subscribe(ServiceMeta service, ChangedListener changedListener) {
        registryServerCheck();
        psHealthChecker.consumerCheck(() -> {
            Long version = VERSIONS.getOrDefault(service.toPath(), -1L);
            Long newVersion = HttpInvoker.httpGet(path(VERSION_PATH, service), Long.class);
            log.info(" ====>>>> [PsRegistryCenter] : version = {} newVersion = {}", newVersion, newVersion);
            // 版本号不同，更新
            if (newVersion > version) {
                List<InstanceMeta> instances = fetchAll(service);
                // 更新服务器列表
                changedListener.fire(new Event(instances));
                // 记录本次版本号
                VERSIONS.put(service.toPath(), newVersion);
            }
        });
    }

    public void clusterCheck() {
        psHealthChecker.clusterCheck(this::doClusterCheck);
    }

    private void doClusterCheck() {
        if (CollectionUtils.isEmpty(servers)) {
            servers = registryProperties.getServers();
        }

        // 获取集群信息
        List<Server> serverList = getClusterServers();
        if (serverList.isEmpty()) {
            return;
        }

        // 更新 集群信息
        List<String> newServers = new ArrayList<>();
        for (Server serverInfo : serverList) {
            if (serverInfo.isStatus() && serverInfo.isLeader()) {
                if (!serverInfo.getUrl().equals(registryServer)) {
                    // 换主
                    log.info(" ====>>>> [PsRegistryCenter] : find registry leader {} success.", serverInfo.getUrl());
                    registryServer = serverInfo.getUrl();
                }
            }
            newServers.add(serverInfo.getUrl());
        }
        servers = newServers;
        log.debug(" ====>>>> [PsRegistryCenter] : update registry servers : {}", servers);
    }

    private List<Server> getClusterServers() {
        for (String server : servers) {
            try {
                List<Server> serverList = HttpInvoker.httpGet(server + CLUSTER_PATH, new TypeReference<>() {
                });
                log.debug(" ====>>>> [PsRegistryCenter] : get servers from {} result : {}",
                        server, JSON.toJSONString(serverList));
                if (serverList != null && !serverList.isEmpty()) {
                    return serverList;
                }
            } catch (Exception e) {
                log.error(" ====>>>> [PsRegistryCenter] : get servers server {} fail.", server, e);
            }
        }
        return Collections.emptyList();
    }

    private String path(String context, ServiceMeta serviceMeta) {
        return registryServer + context + "?service=" + serviceMeta.toPath();
    }

    private String path(String context, List<ServiceMeta> serviceList) {
        StringBuilder sb = new StringBuilder();
        for (ServiceMeta service : serviceList) {
            sb.append(service.toPath()).append(",");
        }
        String services = sb.toString();
        if(services.endsWith(",")) services = services.substring(0, services.length() - 1);
        log.info(" ====>>>> [PsRegistryCenter] : renew instance for {}", services);
        return registryServer + context + "?services=" + services;
    }
}
