package com.ydles.controller;

import com.ydles.entity.Result;
import com.ydles.entity.StatusCode;
import com.ydles.order.feign.CartFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/wcart")
public class CartController {
    @Autowired
    CartFeign cartFeign;
    //查询
    @GetMapping("/list")
    public String list(Model model){
        Map map = cartFeign.list();
        model.addAttribute("items",map);
        return "cart";
    }


    //添加
    @GetMapping("/add")
    @ResponseBody
    public Result<Map> add(String skuId, Integer num){
        cartFeign.add(skuId,num);
        //重新刷新
        Map map = cartFeign.list();
        return new Result<>(true, StatusCode.OK,"添加购物车成功",map);
    }

}