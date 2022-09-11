package com.ydles.goods.feign;

import com.ydles.entity.Result;
import com.ydles.goods.pojo.Sku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@FeignClient(name = "goods")
public interface SkuFeign {
    //根据spuId查询skuList
    @GetMapping("/sku/spu/{spuId}")
    public List<Sku> findSkuListBySpuId(@PathVariable("spuId")String spuId);

    @GetMapping("/sku/{id}")
    public Result<Sku> findById(@PathVariable String id);
    //减库存
    @PostMapping("/sku/decr/count")
    public Result decrCount(@RequestParam("username") String username);
    //回滚库存
    @PutMapping(value = "/sku/resumeStock")
    public Result resumeStock(@RequestParam("skuId") String skuId, @RequestParam("num")Integer num);
}
