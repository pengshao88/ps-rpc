package cn.pengshao.rpc.core.meta;

import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * Description:提供者元数据
 *
 * @Author: yezp
 * @date 2024/3/21 22:33
 */
@Data
@Builder
public class ProviderMeta {

    Method method;
    String methodSign;
    Object serviceImpl;

}
