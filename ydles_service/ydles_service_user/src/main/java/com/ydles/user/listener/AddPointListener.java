package com.ydles.user.listener;

import com.alibaba.fastjson.JSON;
import com.ydles.order.config.RabbitMQConfig;
import com.ydles.order.pojo.Task;
import com.ydles.user.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class AddPointListener {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.CG_BUYING_ADDPOINT)
    public void receiveAddPointMessage(String message) {
        System.out.println("用户服务接收到了任务消息");

        //转换消息
        Task task = JSON.parseObject(message, Task.class);
        if (task == null || StringUtils.isEmpty(task.getRequestBody())) {
            return;
        }

        //判断redis中当前的任务是否存在
        Object value = redisTemplate.boundValueOps(task.getId()).get();
        if (value != null) {
            return;
        }

        int i = userService.addUserPoints(task);
        if(i==0){
            return;
        }
        //返回通知
        rabbitTemplate.convertAndSend(RabbitMQConfig.EX_BUYING_ADDPOINTUSER,RabbitMQConfig.CG_BUYING_ADDPOINT_KEY,JSON.toJSONString(task));
        System.out.println("添加积分成功");
    }
}
