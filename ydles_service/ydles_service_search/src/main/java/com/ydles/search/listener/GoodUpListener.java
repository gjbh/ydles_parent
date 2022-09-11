package com.ydles.search.listener;

import com.ydles.search.config.RabbitMQConfig;
import com.ydles.search.service.EsManagerService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class GoodUpListener {
    @Autowired
    EsManagerService esManagerService;
    @RabbitListener(queues = RabbitMQConfig.SEARCH_ADD_QUEUE)
    public void recieveMsg(String spuId) {
        System.out.println("监听的货品上架了：" + spuId);
        esManagerService.importDataBySpuId(spuId);
    }
}
