package cn.pengshao.rpc.core.registry;

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
    List<String> data;
}
