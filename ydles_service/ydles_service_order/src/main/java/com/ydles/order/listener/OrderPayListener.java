package com.ydles.order.listener;

import com.alibaba.fastjson.JSON;
import com.ydles.order.config.RabbitMQConfig;
import com.ydles.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderPayListener {
    @Autowired
    private OrderService orderService;
    @RabbitListener(queues = RabbitMQConfig.ORDER_PAY)
    public void receiveMsg(String msg) {
        //监听
        System.out.println("收到消息：" + msg);
        Map map = JSON.parseObject(msg, Map.class);
        //调用业务层,完成订单数据库的修改
        orderService.updatePayStatus((String) map.get("orderId"), (String) map.get("transactionId"));
    }
}

