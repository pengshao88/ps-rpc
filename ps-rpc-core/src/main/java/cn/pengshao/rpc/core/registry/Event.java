package cn.pengshao.rpc.core.registry;

import cn.pengshao.rpc.core.meta.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/18 22:57
 */
@Data
@AllArgsConstructor
public class Event {
    List<InstanceMeta> data;
}
