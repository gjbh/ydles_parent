package com.ydles.goods.feign;

import com.ydles.entity.Result;
import com.ydles.goods.pojo.Category;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@FeignClient(name="goods")
public interface CategoryFeign {
    //根据id查询
    @GetMapping("/category/{id}")
    public Result<Category> findById(@PathVariable Integer id);
}
