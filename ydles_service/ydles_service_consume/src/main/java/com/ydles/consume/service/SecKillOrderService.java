package com.ydles.consume.service;

import com.ydles.seckill.pojo.SeckillOrder;

public interface SecKillOrderService {
    //下单到数据库
    int createOrder(SeckillOrder seckillOrder);
}
