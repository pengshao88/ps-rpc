package cn.pengshao.rpc.core.api;

import cn.pengshao.rpc.core.meta.InstanceMeta;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/17 10:27
 */
@Data
public class RpcContext {

    Router<InstanceMeta> router;

    LoadBalancer<InstanceMeta> loadBalancer;

    List<Filter> filters;

    private Map<String, String> parameters = new HashMap<>();

}
