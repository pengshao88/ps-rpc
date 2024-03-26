package cn.pengshao.rpc.demo.provider.test;

import cn.pengshao.rpc.core.test.TestZKServer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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

    @BeforeAll
    static void init() {
        zkServer.start();
    }

    @Test
    void contextLoads() {
        log.info(" ===> PsrpcDemoProviderApplicationTest  .... ");
    }

    @AfterAll
    static void destroy() {
        zkServer.stop();
    }

}
