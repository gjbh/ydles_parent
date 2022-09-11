package com.ydles.pay.service;

import java.util.Map;

public interface WxPayService {
    //微信下单要二维码
    public Map nativePay(String orderId, Integer money);
    //查询订单
    Map<String,String> queryOrder(String orderId);
    //关闭订单
    Map closeOrder(String orderId);

}
