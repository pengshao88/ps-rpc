package cn.pengshao.rpc.demo.provider;

import cn.pengshao.rpc.demo.api.User;
import cn.pengshao.rpc.demo.api.UserService;
import org.springframework.stereotype.Component;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/6 23:41
 */
@Component
public class UserServiceImpl implements UserService {

    @Override
    public User findById(int id) {
        return new User(100, "pengshao_" + System.currentTimeMillis());
    }

}
