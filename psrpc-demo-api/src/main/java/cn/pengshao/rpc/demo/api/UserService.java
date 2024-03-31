package cn.pengshao.rpc.demo.api;

import java.util.List;
import java.util.Map;

/**
 * Description: 用户接口
 *
 * @Author: yezp
 * @date 2024/3/6 23:26
 */
public interface UserService {

    User findById(Integer id);

    int getId(int id);

    User findById(int id, String name);

    String getName(String name);

    long getId(long id);

    long getId(User user);

    long getId(float id);

    String getName();

    String getName(int id);

    int[] getIds();
    long[] getLongIds();
    int[] getIds(int[] ids);

    List<User> getList(List<User> userList);

    Map<String, User> getMap(Map<String, User> userMap);

    Boolean getFlag(boolean flag);

    User[] findUsers(User[] users);

    User find(int timeout);
}
