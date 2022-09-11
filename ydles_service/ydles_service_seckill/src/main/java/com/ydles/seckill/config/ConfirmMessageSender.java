package com.ydles.seckill.config;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ConfirmMessageSender implements RabbitTemplate.ConfirmCallback{
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    RedisTemplate redisTemplate;

    //redis 放的时候 开头的key
    public static final String MESSAGE_CONFIRM_KEY="message_confirm_";

    //规定 有个构造器
    public ConfirmMessageSender(RabbitTemplate rabbitTemplate){
        //本类和容器中的rabbitTemplate 建立联系
        this.rabbitTemplate=rabbitTemplate;
        //使用rabbitTemplate发的消息，必须有回调，回调给本类
        rabbitTemplate.setConfirmCallback(this);
    }

    //回调方法 exchange异步告知这条消息发送成功
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if(ack){
            //发送成功  删除掉存储空间的数据
            redisTemplate.delete(correlationData.getId());
            redisTemplate.delete(MESSAGE_CONFIRM_KEY+correlationData.getId());
        }else {
            //发送失败  重发
            Map<String, String> map = redisTemplate.boundHashOps(MESSAGE_CONFIRM_KEY + correlationData.getId()).entries();
            String exchange = map.get("exchange");
            String routingKey = map.get("routingKey");
            String message = map.get("message");

            //重发
            rabbitTemplate.convertAndSend(exchange,routingKey,message);
            //预警 调用第三方接口：发短信，发邮件给到运维技术组长。log.warn
        }
    }

    //自定义发送的方法
    public void send(String exchange,String routingKey,String message){
        //设置消息的唯一id
        CorrelationData correlationData=new CorrelationData(UUID.randomUUID().toString());
        //运维快速的看哪个msg有问题了
        redisTemplate.boundValueOps(correlationData.getId()).set(message);

        //保存发送的信息到 redis
        Map<String, String> map=new HashMap<>();
        map.put("exchange",exchange);
        map.put("routingKey",routingKey);
        map.put("message",message);
        redisTemplate.boundHashOps(MESSAGE_CONFIRM_KEY+correlationData.getId()).putAll(map);

        //真正发送
        rabbitTemplate.convertAndSend(exchange,routingKey,message,correlationData);
    }



}
