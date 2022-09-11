package com.ydles.order.listener;

import com.ydles.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component

public class OrderTackListener {

    @Autowired
    private OrderService orderService;

    @RabbitListener(queues = "order_tack")
    public void autoTack(String message){
        System.out.println("收到自动确认收货消息");
        orderService.autoTack(); //自动确认收货
    }
}