package com.ydles.seckill.service;

import com.ydles.seckill.pojo.SeckillGoods;

import java.util.List;

public interface SecKillGoodsService {
    //查询秒杀商品列表
    List<SeckillGoods> list(String time);
}
