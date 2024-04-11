package cn.pengshao.rpc.core.registry.zk;

import cn.pengshao.rpc.core.api.RegistryCenter;
import cn.pengshao.rpc.core.api.RpcException;
import cn.pengshao.rpc.core.meta.InstanceMeta;
import cn.pengshao.rpc.core.meta.ServiceMeta;
import cn.pengshao.rpc.core.registry.ChangedListener;
import cn.pengshao.rpc.core.registry.Event;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/18 22:19
 */
@Slf4j
public class ZkRegistryCenter implements RegistryCenter {
    @Value("${psrpc.zk.server:localhost:2181}")
    String servers;

    @Value("${psrpc.zk.root:psrpc}")
    String root;

    private CuratorFramework client = null;
    private List<TreeCache> caches = new ArrayList<>();

    private boolean running = false;

    @Override
    public synchronized void start() {
        if (running) {
            log.info(" ===> zk client has started to server[" + servers + "/" + root + "], ignored.");
            return;
        }

        // 重试策略 1 2 4 8
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString(servers)
                .namespace(root)
                .retryPolicy(retryPolicy)
                .build();
        log.info(" ===> zk client starting.");
        client.start();
        running = true;
    }

    @Override
    public synchronized void stop() {
        if (!running) {
            log.info(" ===> zk client isn't running to server[" + servers + "/" + root + "], ignored.");
            return;
        }

        log.info(" ===> zk client stopping.");
        caches.forEach(TreeCache::close);
        client.close();
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.toPath();
        try {
            // 创建服务的持久化节点
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, service.toMetas().getBytes());
            }

            // 创建实例的临时性节点
            String instancePath = servicePath + "/" + instance.toPath();
            log.info(" ===> zk client register. path:" + instancePath);
            // 创建实例节点 并且把元数据信息存储到节点中
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, instance.toMetas().getBytes());
        } catch (Exception e) {
            throw new RpcException(e);
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
            throw new RpcException(e);
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
            return mapInstances(nodes, servicePath);
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    private List<InstanceMeta> mapInstances(List<String> nodes, String servicePath) {
        return nodes.stream().map(node -> {
            String[] strArr = node.split("_");
            InstanceMeta instanceMeta = InstanceMeta.http(strArr[0], Integer.parseInt(strArr[1]));
            String instancePath = servicePath + "/" + node;
            byte[] bytes;
            try {
                bytes = client.getData().forPath(instancePath);
            } catch (Exception e) {
                throw new RpcException(e);
            }
            // 拿到节点的元数据
            Map<String, Object> params = JSON.parseObject(new String(bytes));
            log.debug("instance:{} params:{}", instanceMeta.toUrl(), params);
            params.forEach((k, v) -> instanceMeta.getParameters().put(k, v == null ? null : v.toString()));
            return instanceMeta;
        }).collect(Collectors.toList());
    }

    @Override
    public void subscribe(ServiceMeta service, ChangedListener changedListener) {
        final TreeCache cache = TreeCache.newBuilder(client, "/"+service.toPath())
                .setCacheData(true).setMaxDepth(2).build();
        try {
            cache.getListenable().addListener(
                    (curator, event) -> {
                        synchronized (ZkRegistryCenter.class) {
                            if (running) {
                                // 有任何节点变动这里会执行
                                log.info("zk subscribe event: " + event);
                                List<InstanceMeta> nodes = fetchAll(service);
                                changedListener.fire(new Event(nodes));
                            }
                        }
                    }
            );
            cache.start();
            caches.add(cache);
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }
}
