package cn.pengshao.rpc.demo.provider;

import cn.pengshao.rpc.core.provider.ProviderConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/7 22:41
 */
@SpringBootApplication
@Import({ProviderConfig.class})
public class PsProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(PsProviderApplication.class, args);
    }

}
