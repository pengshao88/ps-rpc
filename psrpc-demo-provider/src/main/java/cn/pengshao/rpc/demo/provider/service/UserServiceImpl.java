package cn.pengshao.rpc.demo.provider.service;

import cn.pengshao.rpc.core.annotaion.PsProvider;
import cn.pengshao.rpc.core.api.RpcContext;
import cn.pengshao.rpc.core.api.RpcException;
import cn.pengshao.rpc.core.enums.ErrorCodeEnum;
import cn.pengshao.rpc.demo.api.User;
import cn.pengshao.rpc.demo.api.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/6 23:41
 */
@Slf4j
@PsProvider
@Component
public class UserServiceImpl implements UserService {

    final static SecureRandom RANDOM = new SecureRandom();

    @Autowired
    Environment environment;

    @Override
    public User findById(Integer id) {
        return new User(id, "pengshao-V1-"
                + environment.getProperty("server.port") + "_"
                + System.currentTimeMillis());
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
        return name + "_" + RANDOM.nextInt(100);
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
        userList.add(new User(RANDOM.nextInt(3000), "getList"));
        return userList;
    }

    @Override
    public Map<String, User> getMap(Map<String, User> userMap) {
        userMap.put("user", new User(RANDOM.nextInt(3000) + 3000, "getMap"));
        return userMap;
    }

    @Override
    public Boolean getFlag(boolean flag) {
        return flag;
    }

    @Override
    public User[] findUsers(User[] users) {
        return users;
    }

    @Override
    public User ex(boolean flag) {
        if (flag) {
            throw new RuntimeException("throw an exception.");
        }
        return new User(RANDOM.nextInt(3000), "ex");
    }

    @Override
    public User find(int timeout) {
        long start = System.currentTimeMillis();
        String serverPort = environment.getProperty("server.port");
        try {
            if (Arrays.asList(timeoutPorts.split(",")).contains(serverPort)) {
                Thread.sleep(timeout);
            }
        } catch (Exception e) {
            throw new RpcException(ErrorCodeEnum.INTERRUPTED_EXCEPTION.getErrorMsg());
        }
        log.debug("find user cost:{}", System.currentTimeMillis() - start);
        return new User(timeout, serverPort);
    }

    String timeoutPorts = "8081,8090";

    @Override
    public void setTimeoutPorts(String timeoutPorts) {
        this.timeoutPorts = timeoutPorts;
    }

    @Override
    public String echoParameter(String key) {
        log.debug(" RpcContext.ContextParameters:");
        for (Map.Entry<String, String> entry : RpcContext.CONTEXT_PARAMETERS.get().entrySet()) {
            log.debug("key:{},value:{}", entry.getKey(), entry.getValue());
        }
        return RpcContext.getContextParameter(key);
    }

}
