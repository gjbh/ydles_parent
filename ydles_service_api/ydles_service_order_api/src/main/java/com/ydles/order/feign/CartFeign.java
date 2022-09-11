package com.ydles.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.xml.transform.Result;
import java.util.Map;

@FeignClient("order")
public interface CartFeign {
    @GetMapping("/cart/addCart")
    public Result add(@RequestParam("skuId") String skuId, @RequestParam("num") Integer num);

    /***
     * 查询用户购物车列表
     * @return
     */
    @GetMapping(value = "/cart/list")
    public Map list();
}

