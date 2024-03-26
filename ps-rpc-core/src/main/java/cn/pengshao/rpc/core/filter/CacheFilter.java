package cn.pengshao.rpc.core.filter;

import cn.pengshao.rpc.core.api.Filter;
import cn.pengshao.rpc.core.api.RpcRequest;
import cn.pengshao.rpc.core.api.RpcResponse;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/26 22:54
 */
public class CacheFilter implements Filter {

    static Cache<String, Object> cache = CacheBuilder.newBuilder()
            .maximumWeight(256 * 1024 *1024)
            .weigher((k, v) -> k.toString().getBytes().length + v.toString().getBytes().length)
            .expireAfterAccess(60, TimeUnit.SECONDS)
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .concurrencyLevel(4)
            .build();

    @Override
    public Object preFilter(RpcRequest request) {
        return cache.getIfPresent(request.toString());
    }

    @Override
    public Object postFilter(RpcRequest request, RpcResponse response, Object result) {
        cache.put(request.toString(), result);
        return result;
    }
}
