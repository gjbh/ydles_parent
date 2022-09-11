package com.ydles.seckill.web.controller;

import com.ydles.entity.Result;
import com.ydles.entity.StatusCode;
import com.ydles.seckill.feign.SeckillOrderFeign;
import com.ydles.seckill.web.aspect.AccessLimit;
import com.ydles.seckill.web.util.CookieUtil;
import com.ydles.util.RandomUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

@RequestMapping("/wseckillorder")
@RestController
public class SecKillOrderController {
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    private SeckillOrderFeign secKillOrderFeign;
    @RequestMapping("/add")
    //限流
    @AccessLimit
    public Result add(@RequestParam("time") String time, @RequestParam("id")long id, @RequestParam("random") String random) {
        //校验随机数是否正确
        String cookieValue = this.readCookie();
        String redisRandomCode = (String) redisTemplate.opsForValue().get("randomcode_" + cookieValue);
        if (StringUtils.isEmpty(redisRandomCode)) {
            return new Result(false, StatusCode.ERROR, "下单失败");
        }
        if (!random.equals(redisRandomCode)) {
            return new Result(false, StatusCode.ERROR, "下单失败");
        }

        Result result = secKillOrderFeign.add(time, id);
        return result;
    }
    //生成随机数作为接口令牌, 有效期10秒
    @GetMapping("/getToken")
    @ResponseBody
    public String getToken() {
        //获取随机字符串
        String randomString = RandomUtil.getRandomString();
        //获取jti
        String cookieValue = this.readCookie();
        //短令牌作为key, 随机字符串作为value
        redisTemplate.opsForValue().set("randomcode_" + cookieValue, randomString, 10, TimeUnit.SECONDS);
        //返回随机字符串
        return randomString;
    }

    //读取cookie获取jti短令牌
    private String readCookie() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String jti = CookieUtil.readCookie(request, "uid").get("uid");
        return jti;
    }
}
