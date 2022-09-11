package com.ydles.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.ydles.seckill.config.ConfirmMessageSender;
import com.ydles.seckill.config.RabbitMQConfig;
import com.ydles.seckill.dao.SeckillOrderMapper;
import com.ydles.seckill.pojo.SeckillGoods;
import com.ydles.seckill.pojo.SeckillOrder;
import com.ydles.seckill.service.SecKillOrderService;
import com.ydles.util.IdWorker;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class SecKillOrderServiceImpl implements SecKillOrderService {
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    private ConfirmMessageSender confirmMessageSender;

    @Autowired
    SeckillOrderMapper seckillOrderMapper;
    @Autowired
    IdWorker idWorker;
    //redis 秒杀商品key开头
    public static final String SECKILL_GOODS_KEY = "seckill_goods_";
    //秒杀商品库存key头
    public static final String SECKILL_GOODS_STOCK_COUNT_KEY = "seckill_goods_stock_count_";

    //秒杀下单
    @Override
    public boolean add(Long id, String time, String username) {
        //防止重复提交
        String result = this.prevenRepeatCommit(username, id);
        if("fail".equals(result)){
            return false;
        }
        //防止重复购买
        SeckillOrder querySeckillOrder=seckillOrderMapper.getSecKillOrderByUserNameAndGoodsId(username,id);
        if(querySeckillOrder!=null){
            return false;
        }
        //1redis获取商品的数据以及库存量，如果没有，抛出异常
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(SECKILL_GOODS_KEY + time).get(id);
        if (seckillGoods == null) {
            return false;
        }
        //redisTemplate用的是string序列化
        String redisStock = (String) redisTemplate.boundValueOps(SECKILL_GOODS_STOCK_COUNT_KEY + id).get();
        if (StringUtils.isEmpty(redisStock)) {
            return false;
        }
        int stock = Integer.parseInt(redisStock);
        if (stock <= 0) {
            return false;
        }

        //2预扣减库存，如果扣成0，删除商品信息和库存信息
        //Integer integer = (Integer) redisTemplate.opsForValue().get(SECKILL_GOODS_STOCK_COUNT_KEY + id);//100
        //integer=integer-1;//99
        //redisTemplate.boundValueOps(SECKILL_GOODS_STOCK_COUNT_KEY + id).set(integer);//99
        Long decrement = redisTemplate.opsForValue().decrement(SECKILL_GOODS_STOCK_COUNT_KEY + id);
        if(decrement<=0){
            //如果扣成0，删除商品信息
            redisTemplate.boundHashOps(SECKILL_GOODS_KEY + time).delete(id);
            //库存信息
            redisTemplate.delete(SECKILL_GOODS_STOCK_COUNT_KEY + id);
        }

        //3生成秒杀订单
        SeckillOrder seckillOrder=new SeckillOrder();
        seckillOrder.setId(idWorker.nextId());
        seckillOrder.setSeckillId(id);
        seckillOrder.setMoney(seckillGoods.getCostPrice());
        seckillOrder.setUserId(username);
        seckillOrder.setSeckillId(Long.parseLong(seckillGoods.getSellerId()));
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setStatus("0");

        //4订单数据往mq发
        //发送消息
        //rabbitTemplate.convertAndSend("","","");
        confirmMessageSender.send("", RabbitMQConfig.SECKILL_ORDER_QUEUE, JSON.toJSONString(seckillOrder));
        return false;
    }
    //防止刷单
    public String prevenRepeatCommit(String username, Long id) {
        String key = "seckill_user_" + username + "_id_" + id;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            //第一次提交
            redisTemplate.expire(key, 2, TimeUnit.MINUTES);
            return "success";
        }else {
            return "fail";
        }
    }
}
