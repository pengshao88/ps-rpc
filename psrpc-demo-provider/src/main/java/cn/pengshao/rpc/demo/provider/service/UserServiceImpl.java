package cn.pengshao.rpc.demo.provider.service;

import cn.pengshao.rpc.core.annotaion.PsProvider;
import cn.pengshao.rpc.demo.api.User;
import cn.pengshao.rpc.demo.api.UserService;
import org.springframework.stereotype.Component;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/6 23:41
 */
@PsProvider
@Component
public class UserServiceImpl implements UserService {

    @Override
    public User findById(int id) {
        return new User(id, "pengshao_" + System.currentTimeMillis());
    }

}
