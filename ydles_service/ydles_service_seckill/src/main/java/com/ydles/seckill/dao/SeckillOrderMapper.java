package com.ydles.seckill.dao;

import com.ydles.seckill.pojo.SeckillGoods;
import com.ydles.seckill.pojo.SeckillOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface SeckillOrderMapper extends Mapper<SeckillGoods> {
    //查询秒杀订单信息
    @Select("select * from tb_seckill_order where user_id=#{username} and seckill_id=#{id}")
    SeckillOrder getSecKillOrderByUserNameAndGoodsId(@Param("username") String username,@Param("id") Long id);
}
