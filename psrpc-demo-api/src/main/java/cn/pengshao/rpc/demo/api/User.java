package cn.pengshao.rpc.demo.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description: 用户
 *
 * @Author: yezp
 * @date 2024/3/6 23:28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    Integer id;
    String name;

}
