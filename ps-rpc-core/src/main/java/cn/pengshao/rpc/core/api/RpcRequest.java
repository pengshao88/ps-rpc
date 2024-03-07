package cn.pengshao.rpc.core.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/7 22:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest {

    String service;
    String method;
    Object[] args;

}
