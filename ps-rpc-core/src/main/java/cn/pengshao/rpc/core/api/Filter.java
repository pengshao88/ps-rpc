package cn.pengshao.rpc.core.api;

/**
 * Description: 过滤器
 *
 * @Author: yezp
 * @date 2024/3/17 8:14
 */
public interface Filter {

    Object preFilter(RpcRequest request);

    Object postFilter(RpcRequest request, RpcResponse response, Object result);

    // Filter next(); todo 责任链模式

    Filter Default = new Filter() {
        @Override
        public RpcResponse preFilter(RpcRequest request) {
            return null;
        }

        @Override
        public Object postFilter(RpcRequest request, RpcResponse response, Object result) {
            return null;
        }
    };
}
