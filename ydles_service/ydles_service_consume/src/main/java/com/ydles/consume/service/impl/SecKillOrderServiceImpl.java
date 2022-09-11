package com.ydles.consume.service.impl;

import com.ydles.consume.dao.SeckillGoodsMapper;
import com.ydles.consume.dao.SeckillOrderMapper;
import com.ydles.consume.service.SecKillOrderService;
import com.ydles.seckill.pojo.SeckillOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecKillOrderServiceImpl implements SecKillOrderService {
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private SeckillOrderMapper seckillOrderMapper;
    @Override
    @Transactional
    public int createOrder(SeckillOrder seckillOrder) {
        //更改库存
        int result = seckillGoodsMapper.updateStockCount(seckillOrder.getSeckillId());
        if(result<=0){
            return result;
        }
        //添加订单
        int insertSelective = seckillOrderMapper.insertSelective(seckillOrder);
        if (insertSelective<=0){
            return result;
        }
        return 1;
    }
}
