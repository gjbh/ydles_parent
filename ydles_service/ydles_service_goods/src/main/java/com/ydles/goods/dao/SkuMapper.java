package com.ydles.goods.dao;

import com.ydles.goods.pojo.Sku;
import com.ydles.order.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;
public interface SkuMapper extends Mapper<Sku> {
    //减库存
    @Update("UPDATE tb_sku SET num=num-#{num},sale_num=sale_num+#{num} WHERE id=#{skuId} AND num>=#{num}")
    public int decrCount(OrderItem orderItem);

    //回滚库存
    @Update("UPDATE tb_sku SET num=num+#{num},sale_num=sale_num-#{num} WHERE id=#{skuId}")
    public void resumeStock(@Param("skuId") String skuId,@Param("num") Integer num);
}
