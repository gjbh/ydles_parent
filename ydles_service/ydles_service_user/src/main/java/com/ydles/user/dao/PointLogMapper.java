package com.ydles.user.dao;

import com.ydles.user.pojo.PointLog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface PointLogMapper extends Mapper<PointLog> {
    @Update("select * from tb_point_log where order_id =#{orderId}")
    PointLog findPointLogByOrderId(@Param("orderId") String orderId);
}
