package cn.pengshao.rpc.demo.consumer.controller;

import cn.pengshao.rpc.core.annotaion.PsConsumer;
import cn.pengshao.rpc.core.api.Router;
import cn.pengshao.rpc.core.cluster.GrayRouter;
import cn.pengshao.rpc.demo.api.User;
import cn.pengshao.rpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/17 9:21
 */
@RestController
@RequestMapping("/")
public class UserController {

    @PsConsumer
    UserService userService;
    @Autowired
    Router router;

    @GetMapping("findById")
    public User findById(@RequestParam("id") int id) {
        return userService.findById(id);
    }

    @GetMapping("find")
    public User find(@RequestParam("timeout") int timeout) {
        return userService.find(timeout);
    }

    @GetMapping("gray")
    public String gray(@RequestParam("gray") int grayRatio) {
        ((GrayRouter) router).setGrayRatio(grayRatio);
        return "success - grayRatio:" + grayRatio;
    }
}
