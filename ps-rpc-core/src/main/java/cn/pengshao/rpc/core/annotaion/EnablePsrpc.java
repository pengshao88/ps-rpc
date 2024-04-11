package cn.pengshao.rpc.core.annotaion;

import cn.pengshao.rpc.core.config.ConsumerConfig;
import cn.pengshao.rpc.core.config.ProviderConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Description: 整合一个入口
 *
 * @Author: yezp
 * @date 2024/4/11 22:21
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Import({ConsumerConfig.class, ProviderConfig.class})
public @interface EnablePsrpc {
}
