package cn.pengshao.rpc.demo.consumer.controller;

import cn.pengshao.rpc.core.annotaion.PsConsumer;
import cn.pengshao.rpc.demo.api.User;
import cn.pengshao.rpc.demo.api.UserService;
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

    @GetMapping("findById")
    public User findById(@RequestParam("id") int id) {
        return userService.findById(id);
    }

    @GetMapping("find")
    public User find(@RequestParam("timeout") int timeout) {
        return userService.find(timeout);
    }

}
