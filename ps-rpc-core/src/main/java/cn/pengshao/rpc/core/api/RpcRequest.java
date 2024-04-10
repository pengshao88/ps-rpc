package cn.pengshao.rpc.core.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

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
    String methodSign;
    Object[] args;

    // 跨调用方需要传递的参数
    private Map<String, String> params = new HashMap<>();

}
