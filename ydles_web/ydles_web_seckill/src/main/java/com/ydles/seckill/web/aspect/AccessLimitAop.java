package com.ydles.seckill.web.aspect;

import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.RateLimiter;
import com.ydles.entity.Result;
import com.ydles.entity.StatusCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Aspect//切面类
@Scope
@Component
public class AccessLimitAop {
    @Autowired
    private HttpServletResponse response;
    //设置令牌的生成速率
    private RateLimiter rateLimiter = RateLimiter.create(2.0); //每秒生成两个令牌存入桶中

    @Pointcut("@annotation(com.ydles.seckill.web.aspect.AccessLimit)")
    public void limit(){
    }

    @Around("limit()") //环绕增强
    public Object around(ProceedingJoinPoint proceedingJoinPoint){
        //限流逻辑
        //拿令牌
        boolean result = rateLimiter.tryAcquire();
        Object object = null;
        if(result){
            //拿到令牌了
            try {
                //切面往后走
                object = proceedingJoinPoint.proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }else {
            //拿不到令牌
            Result<Object> objectResult = new Result<>(false, StatusCode.ACCESSLIMIT, "限流了，请售后再试");
            String msg = JSON.toJSONString(objectResult);
            //将信息写回到用户的浏览器
            writeMsg(response,msg);
        }
        return object;
    }

    //给用户写回数据
    public void  writeMsg(HttpServletResponse response, String msg){
        ServletOutputStream outputStream=null;
        try {
            response.setContentType("application/json;charset=utf-8");
            outputStream = response.getOutputStream();
            outputStream.write(msg.getBytes("utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

}