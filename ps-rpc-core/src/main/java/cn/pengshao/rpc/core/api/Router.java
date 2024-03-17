package cn.pengshao.rpc.core.api;

import java.util.List;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/17 8:14
 */
public interface Router<T> {

    List<T> route(List<T> providers);

    Router DEFAULT = providers -> providers;

}
