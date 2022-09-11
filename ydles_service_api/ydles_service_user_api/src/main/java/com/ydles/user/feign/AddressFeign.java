package com.ydles.user.feign;

import com.ydles.user.pojo.Address;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("user")
public interface AddressFeign {
    @GetMapping("/address/list")
    //根据username查询list<Address>
    public List<Address> list();

}
