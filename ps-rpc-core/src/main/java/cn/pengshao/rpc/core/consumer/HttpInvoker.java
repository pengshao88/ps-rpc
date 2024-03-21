package cn.pengshao.rpc.core.consumer;

import cn.pengshao.rpc.core.api.RpcRequest;
import cn.pengshao.rpc.core.api.RpcResponse;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/21 21:57
 */
public interface HttpInvoker {

    RpcResponse<?> post(RpcRequest rpcRequest, String url);
}
