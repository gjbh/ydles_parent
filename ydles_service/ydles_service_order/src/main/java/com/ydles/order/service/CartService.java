package com.ydles.order.service;

import java.util.Map;

public interface CartService {
    //添加购物车
    public void addCart(String skuId,Integer num,String username);
    //查询购物车
    public Map list(String name);
}
