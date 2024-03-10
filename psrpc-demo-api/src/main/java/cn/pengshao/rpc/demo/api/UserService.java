package cn.pengshao.rpc.demo.api;

/**
 * Description: 用户接口
 *
 * @Author: yezp
 * @date 2024/3/6 23:26
 */
public interface UserService {

    User findById(int id);

    User findById(Integer id);
}
