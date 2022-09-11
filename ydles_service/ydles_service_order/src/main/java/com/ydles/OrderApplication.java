package com.ydles;

import com.ydles.interceptor.FeignInterceptor;
import com.ydles.order.config.TokenDecode;
import com.ydles.util.IdWorker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Created by IT李老师
 * 公主号 “IT李哥交朋友”
 * 个人微 itlils
 */
@SpringBootApplication
@EnableEurekaClient
@MapperScan(basePackages = {"com.ydles.order.dao"})
@EnableFeignClients(basePackages = {"com.ydles.goods.feign","com.ydles.pay.feign"})
@EnableScheduling//开启定时任务
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class);
    }
    //雪花算法生成id
    @Value("${workerId}")
    private long workerId;

    @Value("${datacenterId}")
    private long datacenterId;
    @Bean
    public IdWorker idWorker(){
        return new IdWorker(workerId,datacenterId);
    }
    @Bean
    public TokenDecode tokenDecode(){
        return new TokenDecode();
    }

    @Bean
    public FeignInterceptor feignInterceptor(){
        return new FeignInterceptor();
    }
}
