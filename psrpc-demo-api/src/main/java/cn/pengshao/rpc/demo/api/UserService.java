package cn.pengshao.rpc.demo.api;

/**
 * Description: 用户接口
 *
 * @Author: yezp
 * @date 2024/3/6 23:26
 */
public interface UserService {

    User findById(Integer id);

    int getId(int id);

    String getName(String name);
}
