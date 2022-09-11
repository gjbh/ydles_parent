package com.ydles.canal.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import com.ydles.canal.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Created by IT李老师
 * 公主号 “IT李哥交朋友”
 * 个人微 itlils
 */
@CanalEventListener
public class BusinessListener {

    @Autowired
    RabbitTemplate rabbitTemplate;
    /**
     *
     * @param eventType 当前操作数据库的类型
     * @param rowData 当前操作数据库的数据
     */
    @ListenPoint(schema = "ydles_business",table = "tb_ad")
    public void adUpdate(CanalEntry.EventType eventType,CanalEntry.RowData rowData){
        System.out.println("广告表数据发生改变");
        //        rowData.getBeforeColumnsList().forEach((c)-> System.out.println("改变前的数据："+c.getName()+"::"+c.getValue()));
//        System.out.println("========================================");
//        rowData.getAfterColumnsList().forEach((c)-> System.out.println("改变后的数据："+c.getName()+"::"+c.getValue()));
        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
            if(column.getName().equals("position")){
                System.out.println("发送数据到mq"+column.getValue());

                //发送消息
                rabbitTemplate.convertAndSend("", RabbitMQConfig.AD_UPDATE_QUEUE, column.getValue());
            }
        }
    }
}
