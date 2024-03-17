package cn.pengshao.rpc.core.api;

import java.util.List;

/**
 * Description:负载均衡  权重/权重-自适应
 * w=100, 8080 0-99， random <25 8080 else 8081
 * w=300, 8081
 *
 * 自适应：记录每个节点响应速度，响应速度越快，权重越大
 * avg * 0.3 + last * 0.7 = w
 *
 * @Author: yezp
 * @date 2024/3/17 8:15
 */
public interface LoadBalancer<T> {

    T choose(List<T> providers);

    LoadBalancer DEFAULT = providers -> (providers == null || providers.isEmpty()) ? null : providers.get(0);

}
