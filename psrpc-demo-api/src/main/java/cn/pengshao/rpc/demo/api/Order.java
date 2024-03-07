package cn.pengshao.rpc.demo.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/7 23:05
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    Integer id;
    Float amount;
}
