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

    public final static ThreadLocal<Map<String, String>> CONTEXT_PARAMETERS = ThreadLocal.withInitial(HashMap::new);

    public static void setContextParameter(String key, String value) {
        CONTEXT_PARAMETERS.get().put(key, value);
    }

    public static String getContextParameter(String key) {
        return CONTEXT_PARAMETERS.get().get(key);
    }

    public static void removeContextParameter(String key) {
        CONTEXT_PARAMETERS.get().remove(key);
    }

}
