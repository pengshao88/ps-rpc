package cn.pengshao.rpc.demo.provider.test;

import cn.pengshao.rpc.core.config.ProviderProperties;
import cn.pengshao.rpc.core.test.TestZKServer;
import com.ctrip.framework.apollo.core.ApolloClientSystemConsts;
import com.ctrip.framework.apollo.mockserver.ApolloTestingServer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/26 23:26
 */
@Slf4j
@SpringBootTest
public class PsrpcDemoProviderApplicationTest {

    static TestZKServer zkServer = new TestZKServer();
    static ApolloTestingServer apollo = new ApolloTestingServer();

    @Autowired
    ProviderProperties providerProperties;

    @SneakyThrows
    @BeforeAll
    static void init() {
        log.info(" ====================================== ");
        log.info(" ====================================== ");
        log.info(" =============     ZK2182    ========== ");
        log.info(" ====================================== ");
        log.info(" ====================================== ");
        zkServer.start();
        log.info(" ====================================== ");
        log.info(" ====================================== ");
        log.info(" ===========     mock apollo    ======= ");
        log.info(" ====================================== ");
        log.info(" ====================================== ");
        apollo.start();
    }

    @Test
    void contextLoads() {
        log.info(" ===> PsrpcDemoProviderApplicationTest  .... ");
        log.info("....  ApolloClientSystemConsts.APOLLO_CONFIG_SERVICE  .....");
        log.info(System.getProperty(ApolloClientSystemConsts.APOLLO_CONFIG_SERVICE));
        log.info("....  ApolloClientSystemConsts.APOLLO_CONFIG_SERVICE  .....");
    }

    @Test
    void printProviderProperties() {
        log.info(" ===> PsrpcDemoProviderApplicationTests  .... ");
        log.info("....  providerProperties  .....");
        log.info("providerProperties:{}", providerProperties);
        log.info("....  providerProperties  .....");
    }

    @AfterAll
    static void destroy() {
        log.info(" ===========     stop zookeeper server    ======= ");
        zkServer.stop();
        log.info(" ===========     stop apollo mockserver   ======= ");
        apollo.close();
        log.info(" ===========     destroy in after all     ======= ");
    }

}
