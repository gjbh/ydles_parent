package com.ydles.consume.listeney;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.ydles.consume.config.RabbitMQConfig;
import com.ydles.consume.service.SecKillOrderService;
import com.ydles.seckill.pojo.SeckillOrder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SecKillOrderListener {
    @Autowired
    SecKillOrderService secKillOrderService;
    @RabbitListener(queues = RabbitMQConfig.SECKILL_ORDER_QUEUE)
    public void receiveMsg(Message message, Channel channel){
        //预抓取总数
        try {
            channel.basicQos(300);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String msgStr = new String(message.getBody());
        System.out.println("接收到消息：" + message);
        //监听
        SeckillOrder seckillOrder = JSON.parseObject(message.getBody(), SeckillOrder.class);
        //接收到消息后，调用service方法
        int result = secKillOrderService.createOrder(seckillOrder);
        //如果成功，则返回1，否则返回0
        if (result > 0) {
            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
               e.printStackTrace();
               //log.error
            }
        }else {
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
            } catch (IOException e) {
                e.printStackTrace();
                //log.error
            }
        }
    }

}