package cn.pengshao.rpc.core.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description: response for RPC call
 *
 * @Author: yezp
 * @date 2024/3/7 22:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse<T> {

    boolean status;
    T data;
    RpcException ex;

}
