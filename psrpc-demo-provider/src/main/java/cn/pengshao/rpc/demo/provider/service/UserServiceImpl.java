package cn.pengshao.rpc.demo.provider.service;

import cn.pengshao.rpc.core.annotaion.PsProvider;
import cn.pengshao.rpc.demo.api.User;
import cn.pengshao.rpc.demo.api.UserService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
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
    public User findById(int id, String name) {
        return new User(id, "findById_" + name);
    }

    @Override
    public String getName(String name) {
        return name + "_" + new Random().nextInt(100);
    }

    @Override
    public long getId(long id) {
        return id;
    }

    @Override
    public long getId(User user) {
        return user.getId().longValue();
    }

    @Override
    public long getId(float id) {
        return 1L;
    }

    @Override
    public String getName() {
        return "method_getName";
    }

    @Override
    public String getName(int id) {
        return "getName_" + id;
    }

    @Override
    public int[] getIds() {
        return new int[] {10, 20, 30};
    }

    @Override
    public long[] getLongIds() {
        return new long[] {1, 2, 3};
    }

    @Override
    public int[] getIds(int[] ids) {
        return ids;
    }

    @Override
    public List<User> getList(List<User> userList) {
        return userList;
    }

    @Override
    public Map<String, User> getMap(Map<String, User> userMap) {
        return userMap;
    }

}
