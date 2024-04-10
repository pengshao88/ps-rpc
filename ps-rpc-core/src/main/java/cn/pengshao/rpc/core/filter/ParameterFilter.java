package cn.pengshao.rpc.core.filter;

import cn.pengshao.rpc.core.api.Filter;
import cn.pengshao.rpc.core.api.RpcContext;
import cn.pengshao.rpc.core.api.RpcRequest;
import cn.pengshao.rpc.core.api.RpcResponse;

import java.util.Map;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/4/2 23:20
 */
public class ParameterFilter implements Filter {
    @Override
    public Object preFilter(RpcRequest request) {
        Map<String, String> params = RpcContext.CONTEXT_PARAMETERS.get();
        if(!params.isEmpty()) {
            request.getParams().putAll(params);
        }
        return null;
    }

    @Override
    public Object postFilter(RpcRequest request, RpcResponse response, Object result) {
        RpcContext.CONTEXT_PARAMETERS.get().clear();
        return null;
    }
}
