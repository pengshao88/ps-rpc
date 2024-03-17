package cn.pengshao.rpc.core.cluster;

import cn.pengshao.rpc.core.api.LoadBalancer;

import java.security.SecureRandom;
import java.util.List;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/17 9:59
 */
public class RandomLoadBalancer<T> implements LoadBalancer<T> {

    static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public T choose(List<T> providers) {
        if (providers == null || providers.isEmpty()) {
            return null;
        }

        if (providers.size() == 1) {
            return providers.get(0);
        }

        int index = RANDOM.nextInt(providers.size());
        return providers.get(index);
    }

}
