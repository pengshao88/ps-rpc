package cn.pengshao.rpc.demo.test.consumer;

import cn.pengshao.rpc.core.test.TestZKServer;
import cn.pengshao.rpc.demo.consumer.PsConsumerApplication;
import cn.pengshao.rpc.demo.provider.PsProviderApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/26 23:32
 */
@Slf4j
@SpringBootTest(classes = {PsConsumerApplication.class})
public class PsrpcDemoConsumerApplicationTests {

    static ApplicationContext context;

    static TestZKServer zkServer = new TestZKServer();

    @BeforeAll
    static void init() {
        log.info(" ====================================== ");
        log.info(" ====================================== ");
        log.info(" ====================================== ");

        zkServer.start();
        context = SpringApplication.run(PsProviderApplication.class,
                "--server.port=8094", "--psrpc.zkServer=localhost:2182",
                "--logging.level.cn.pengshao.rpc=info");
    }

    @Test
    void contextLoads() {
        log.info(" ===> PsrpcDemoConsumerApplicationTests test  .... ");
    }

    @AfterAll
    static void destroy() {
        SpringApplication.exit(context, () -> 1);
        zkServer.stop();
    }

}
