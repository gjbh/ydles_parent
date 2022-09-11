package com.ydles.canal.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import com.ydles.canal.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * @Created by IT李老师
 * 公主号 “IT李哥交朋友”
 * 个人微 itlils
 */
@CanalEventListener
public class SpuListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * spu 表更新
     * @param eventType
     * @param rowData
     */
    @ListenPoint(schema = "ydles_goods", table = {"tb_spu"})
    public void spuUp(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        System.err.println("tb_spu表数据发生变化:"+eventType);
        //修改前数据
        Map<String,String> oldMap = new HashMap<>();
        rowData.getBeforeColumnsList().forEach(column -> oldMap.put(column.getName(),column.getValue()));
        //修改后数据
        Map<String,String> newMap = new HashMap<>();
        rowData.getAfterColumnsList().forEach(column -> newMap.put(column.getName(),column.getValue()));


        //is_marketable  由0改为1表示上架
       if (oldMap.get("is_marketable").equals("0")&& newMap.get("is_marketable").equals("1")){
           String spuId = newMap.get("id");
           System.out.println("上品上架了"+spuId);
           //mq发spuId
           rabbitTemplate.convertAndSend(RabbitMQConfig.GOODS_UP_EXCHANGE,"",spuId);
       }

        //is_marketable  由1改为0表示下架
        if (oldMap.get("is_marketable").equals("1")&& newMap.get("is_marketable").equals("0")){
            String spuId = newMap.get("id");
            System.out.println("下品上架了"+spuId);
            //mq发spuId
            rabbitTemplate.convertAndSend(RabbitMQConfig.GOODS_DOWN_EXCHANGE,"",spuId);
        }
    }
}
