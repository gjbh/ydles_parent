package com.ydles.order.controller;

import com.ydles.entity.Result;
import com.ydles.entity.StatusCode;
import com.ydles.goods.pojo.Sku;
import com.ydles.order.config.TokenDecode;
import com.ydles.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;
    @Autowired
    TokenDecode tokenDecode;
    @GetMapping("/addCart")
    public Result addCart(@RequestParam("skuId") String skuId, @RequestParam("num") Integer num){
        //动态获取当前人信息,暂时静态
        //String username = "itlils";
        //动态
        String username = tokenDecode.getUserInfo().get("username");
        cartService.addCart(skuId,num,username);
        return new Result(true, StatusCode.OK,"加入购物车成功");
    }
    //查询购物车
    @GetMapping(value = "/list")
    public Map list(){
        //暂时静态，后续修改
        String username = "itcast";
        return cartService.list(username);
    }

}
