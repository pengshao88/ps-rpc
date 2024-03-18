package cn.pengshao.rpc.core.registry;

import cn.pengshao.rpc.core.api.RegistryCenter;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/18 22:19
 */
public class ZkRegistryCenter implements RegistryCenter {
    private CuratorFramework client = null;

    @Override
    public void start() {
        // 重试策略 1 2 4 8
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString("localhost:2181")
                .namespace("psrpc")
                .retryPolicy(retryPolicy)
                .build();
        System.out.println(" ===> zk client starting.");
        client.start();
    }

    @Override
    public void stop() {
        System.out.println(" ===> zk client stopping.");
        client.close();
    }

    @Override
    public void register(String serviceName, String instance) {
        String servicePath = "/" + serviceName;
        try {
            // 创建服务的持久化节点
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath);
            }

            // 创建实例的临时性节点
            String instancePath = servicePath + "/" + instance;
            System.out.println(" ===> zk client register. path:" + instancePath);
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "provider".getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unregister(String serviceName, String instance) {
        String servicePath = "/" + serviceName;
        try {
            // 判断服务节点是否存在
            if (client.checkExists().forPath(servicePath) == null) {
                return;
            }

            // 删除实例节点
            String instancePath = servicePath + "/" + instance;
            System.out.println(" ===> zk client unregister. path:" + instancePath);
            client.delete().forPath(instancePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> fetchAll(String service) {
        String servicePath = "/" + service;
        try {
            // 获取所有子节点
            List<String> nodes = client.getChildren().forPath(servicePath);
            System.out.println(" ===> zk client fetchAll.");
            nodes.forEach(System.out::println);
            return nodes;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void subscribe(String service, ChangedListener changedListener) {
        final TreeCache treeCache = TreeCache.newBuilder(client, "/" + service)
                .setCacheData(true).setMaxDepth(2).build();

        try {
            treeCache.getListenable().addListener((curatorFramework, event) -> {
                // 有任何节点变动这里都会执行
                System.out.println(" ===> zk client subscribe. event:" + event);
                List<String> nodes = fetchAll(service);
                changedListener.fire(new Event(nodes));
            });
            treeCache.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
