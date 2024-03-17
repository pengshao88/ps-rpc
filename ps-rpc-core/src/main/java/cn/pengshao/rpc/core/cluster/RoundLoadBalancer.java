package cn.pengshao.rpc.core.cluster;

import cn.pengshao.rpc.core.api.LoadBalancer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/17 9:59
 */
public class RoundLoadBalancer<T> implements LoadBalancer<T> {

    AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public T choose(List<T> providers) {
        if (providers == null || providers.isEmpty()) {
            return null;
        }

        if (providers.size() == 1) {
            return providers.get(0);
        }
        // 除了符号位外，其他位数都是正数 确保结果为非负数
        return providers.get((atomicInteger.getAndIncrement() & 0x7fffffff) % providers.size());
    }

}
