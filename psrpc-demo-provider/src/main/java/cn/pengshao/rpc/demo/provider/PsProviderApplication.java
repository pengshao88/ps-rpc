package cn.pengshao.rpc.demo.provider;

import cn.pengshao.psconfig.client.annotation.EnablePsConfig;
import cn.pengshao.rpc.core.config.ProviderConfig;
import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
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
//@EnableApolloConfig
@EnablePsConfig
public class PsProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(PsProviderApplication.class, args);
    }

}
