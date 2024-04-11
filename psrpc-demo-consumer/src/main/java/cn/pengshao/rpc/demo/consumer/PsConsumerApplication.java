package cn.pengshao.rpc.demo.consumer;

import cn.pengshao.rpc.core.config.ConsumerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/10 22:38
 */
@Import(ConsumerConfig.class)
@SpringBootApplication
public class PsConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PsConsumerApplication.class, args);
    }
}
