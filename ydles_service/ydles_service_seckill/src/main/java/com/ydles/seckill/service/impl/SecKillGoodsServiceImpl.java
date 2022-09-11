package com.ydles.seckill.service.impl;

import com.ydles.seckill.pojo.SeckillGoods;
import com.ydles.seckill.service.SecKillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SecKillGoodsServiceImpl implements SecKillGoodsService {
    @Autowired
    RedisTemplate redisTemplate;
    private static final String SECKILL_KEY = "seckill_goods_";
    //秒杀商品库存头
    public static final String SECKILL_GOODS_STOCK_COUNT_KEY = "seckill_goods_stock_count_";

    //查询秒杀商品列表
    @Override
    public List<SeckillGoods> list(String time) {
        List<SeckillGoods> list = redisTemplate.boundHashOps(SECKILL_KEY + time).values();
        //更新库存数据的来源
        for (SeckillGoods seckillGoods : list) {
            String value = (String) redisTemplate.opsForValue().get(SECKILL_GOODS_STOCK_COUNT_KEY + seckillGoods.getId());
            seckillGoods.setStockCount(Integer.parseInt(value));
        }
        return list;
    }


}

