package cn.pengshao.rpc.core.test;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingCluster;
import org.apache.curator.utils.CloseableUtils;

/**
 * Description: mock zk server
 *
 * @Author: yezp
 * @date 2024/3/26 23:23
 */
@Slf4j
public class TestZKServer {

    TestingCluster cluster;
    @SneakyThrows
    public void start() {
        InstanceSpec instanceSpec = new InstanceSpec(null, 2182,
                -1, -1, true,
                -1, -1, -1);
        cluster = new TestingCluster(instanceSpec);
        log.info("TestingZooKeeperServer starting ...");
        cluster.start();
        cluster.getServers().forEach(s -> log.info(" ===> " + s.getInstanceSpec()));
        log.info("TestingZooKeeperServer started.");
    }

    @SneakyThrows
    public void stop() {
        log.info("TestingZooKeeperServer stopping ...");
        cluster.stop();
        CloseableUtils.closeQuietly(cluster);
        log.info("TestingZooKeeperServer stopped.");
    }

}
