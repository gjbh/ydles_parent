package com.ydles.seckill.feign;

import com.ydles.entity.Result;
import com.ydles.seckill.pojo.SeckillGoods;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name="seckill")
public interface SecKillFeign {
    @RequestMapping("/seckillgoods/list")
    public Result<List<SeckillGoods>> list(@RequestParam("time") String time);

}
