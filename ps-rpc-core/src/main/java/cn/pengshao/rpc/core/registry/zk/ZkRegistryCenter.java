package cn.pengshao.rpc.core.registry.zk;

import cn.pengshao.rpc.core.api.RegistryCenter;
import cn.pengshao.rpc.core.meta.InstanceMeta;
import cn.pengshao.rpc.core.meta.ServiceMeta;
import cn.pengshao.rpc.core.registry.ChangedListener;
import cn.pengshao.rpc.core.registry.Event;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/18 22:19
 */
@Slf4j
public class ZkRegistryCenter implements RegistryCenter {
    @Value("${psrpc.zkServer}")
    String servers;

    @Value("${psrpc.zkRoot}")
    String root;

    private CuratorFramework client = null;
    private TreeCache treeCache;

    @Override
    public void start() {
        // 重试策略 1 2 4 8
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString(servers)
                .namespace(root)
                .retryPolicy(retryPolicy)
                .build();
        log.info(" ===> zk client starting.");
        client.start();
    }

    @Override
    public void stop() {
        log.info(" ===> zk client stopping.");
        treeCache.close();
        client.close();
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.toPath();
        try {
            // 创建服务的持久化节点
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath);
            }

            // 创建实例的临时性节点
            String instancePath = servicePath + "/" + instance.toPath();
            log.info(" ===> zk client register. path:" + instancePath);
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "provider".getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.toPath();
        try {
            // 判断服务节点是否存在
            if (client.checkExists().forPath(servicePath) == null) {
                return;
            }

            // 删除实例节点
            String instancePath = servicePath + "/" + instance.toPath();
            log.info(" ===> zk client unregister. path:" + instancePath);
            client.delete().forPath(instancePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        String servicePath = "/" + service.toPath();
        try {
            // 获取所有子节点
            List<String> nodes = client.getChildren().forPath(servicePath);
            log.info(" ===> zk client fetchAll.");
            nodes.forEach(log::info);
            return mapInstances(nodes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<InstanceMeta> mapInstances(List<String> nodes) {
        return nodes.stream().map(node -> {
            String[] strArr = node.split("_");
            return InstanceMeta.http(strArr[0], Integer.parseInt(strArr[1]));
        }).collect(Collectors.toList());
    }

    @Override
    public void subscribe(ServiceMeta service, ChangedListener changedListener) {
        treeCache = TreeCache.newBuilder(client, "/" + service)
                .setCacheData(true).setMaxDepth(2).build();
        try {
            treeCache.getListenable().addListener((curatorFramework, event) -> {
                // 有任何节点变动这里都会执行
                log.info(" ===> zk client subscribe. event:" + event);
                List<InstanceMeta> nodes = fetchAll(service);
                changedListener.fire(new Event(nodes));
            });
            treeCache.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
