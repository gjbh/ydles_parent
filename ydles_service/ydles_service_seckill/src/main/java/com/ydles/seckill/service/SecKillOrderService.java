package com.ydles.seckill.service;

public interface SecKillOrderService {
    // 创建秒杀订单
    boolean add(Long id, String time, String username);
}
