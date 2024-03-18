package cn.pengshao.rpc.core.registry;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/18 22:46
 */
public interface ChangedListener {
    void fire(Event event);
}
