package com.ydles.seckill.controller;

import com.ydles.entity.Result;
import com.ydles.entity.StatusCode;
import com.ydles.seckill.config.TokenDecode;
import com.ydles.seckill.feign.SeckillOrderFeign;
import com.ydles.seckill.service.SecKillOrderService;
import com.ydles.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/seckillorder")
public class SecKillOrderController {

    @Autowired
    private TokenDecode tokenDecode;

    @Autowired
    private SecKillOrderService secKillOrderService;

    @Autowired
    SeckillOrderFeign seckillOrderFeign;
    /**
     * 秒杀下单
     * @param time 当前时间段
     * @param id 秒杀商品id
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestParam("time") String time, @RequestParam("id") Long id){
        //获取当前登陆人
        String username = tokenDecode.getUserInfo().get("username");
        String formatStr = DateUtil.formatStr(time);
        boolean result = secKillOrderService.add(id,formatStr,username);
        if (result){
            return new Result(true, StatusCode.OK,"下单成功");
        }else{
            return new Result(false,StatusCode.ERROR,"下单失败");
        }
    }
}