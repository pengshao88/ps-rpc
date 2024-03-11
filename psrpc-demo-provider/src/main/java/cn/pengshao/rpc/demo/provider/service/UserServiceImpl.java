package cn.pengshao.rpc.demo.provider.service;

import cn.pengshao.rpc.core.annotaion.PsProvider;
import cn.pengshao.rpc.demo.api.User;
import cn.pengshao.rpc.demo.api.UserService;
import org.springframework.stereotype.Component;

import java.util.Random;

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
    public User findById(Integer id) {
        return new User(id, "pengshao_" + System.currentTimeMillis());
    }

    @Override
    public int getId(int id) {
        return id;
    }

    @Override
    public String getName(String name) {
        return name + "_" + new Random().nextInt(100);
    }

}
