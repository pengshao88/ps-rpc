package cn.pengshao.rpc.demo.provider.service;

import cn.pengshao.rpc.core.annotaion.PsProvider;
import cn.pengshao.rpc.demo.api.Order;
import cn.pengshao.rpc.demo.api.OrderService;
import org.springframework.stereotype.Component;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/7 23:07
 */
@Component
@PsProvider
public class OrderServiceImpl implements OrderService {
    @Override
    public Order findById(Integer id) {
        if (id == 404) {
            throw new RuntimeException("404 exception");
        }

        return new Order(id, 175.0f);
    }
}
