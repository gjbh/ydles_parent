package com.ydles.search.listener;

import com.ydles.search.config.RabbitMQConfig;
import com.ydles.search.service.EsManagerService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoodsDelListener {
    @Autowired
    private EsManagerService esManagerService;

    @RabbitListener(queues = RabbitMQConfig.SEARCH_DELETE_QUEUE)
    public void receiveMessage(String spuId){
        System.out.println("商品下架了:  "+spuId);
        //调用业务层完成索引库数据删除
        esManagerService.importDataBySpuId(spuId);
    }
}
