package cn.pengshao.rpc.core.api;

import lombok.Data;

import java.util.List;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/17 10:27
 */
@Data
public class RpcContext {

    Router router;

    LoadBalancer loadBalancer;

    List<Filter> filters;

}
