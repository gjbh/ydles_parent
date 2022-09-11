package com.ydles.seckill.controller;

import com.ydles.entity.Result;
import com.ydles.entity.StatusCode;
import com.ydles.seckill.pojo.SeckillGoods;
import com.ydles.seckill.service.SecKillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/seckillgoods")
public class SecKillController {

    @Autowired
    private SecKillGoodsService secKillGoodsService;

    /**
     * 查询秒杀商品列表
     * @param time
     * @return
     */
    @RequestMapping("/list")
    public Result<List<SeckillGoods>> list(@RequestParam("time") String time){
        List<SeckillGoods> seckillGoodsList  = secKillGoodsService.list(time);
        return new Result<List<SeckillGoods>>(true, StatusCode.OK,"查询秒杀商品成功",seckillGoodsList);
    }
}