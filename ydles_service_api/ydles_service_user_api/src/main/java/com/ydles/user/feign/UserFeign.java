package com.ydles.user.feign;

import com.sun.java.browser.plugin2.liveconnect.v1.Result;
import com.ydles.user.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@FeignClient("user")
public interface UserFeign {
    @GetMapping("/user/load/{username}")
    public User findUserInfo(@PathVariable("username") String username);
    @GetMapping("/user/points/add")
    public Result addPoints(Integer points);
}
