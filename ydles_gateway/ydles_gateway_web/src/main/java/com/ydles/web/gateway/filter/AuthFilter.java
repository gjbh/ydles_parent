package com.ydles.web.gateway.filter;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter implements GlobalFilter, Ordered {
    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    public static final String LOGIN_URL = "http://localhost:8001/api/oauth/toLogin";
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //1判断当前请求是否为登陆，是就放行
        //1.1登陆时：放行
        String path = request.getURI().getPath(); // /oauth/login
        System.out.println("path:"+path);
        if(path.contains("/oauth/Login") || path.contains("/oauth/toLogin")){
            //放行
            return chain.filter(exchange);
        }
        //  2从cookie中获取jti，不存在，拒绝访问\
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        HttpCookie cookie = cookies.getFirst("uid");
        if(cookie== null){
            //拒绝访问
//            response.setStatusCode(HttpStatus.UNAUTHORIZED);
//            return response.setComplete();
            //跳转到登陆页面
           return  tologinPage(response,LOGIN_URL+"?FROM="+path);
        }
        //  3从redis获取jwt，不存在，则拒绝访问
        String jti=cookie.getValue();
        String jwt=stringRedisTemplate.opsForValue().get(jti);
        if(StringUtils.isEmpty(jwt)){
            //拒绝访问
//            response.setStatusCode(HttpStatus.UNAUTHORIZED);
//            return response.setComplete();
            //跳转到登陆页面
            return  tologinPage(response,LOGIN_URL+"?FROM="+path);
        }
        //  4对当前请求增强，让其携带令牌信息
        request.mutate().header("Authorization", "Bearer "+jwt);
        //放行
        return chain.filter(exchange);
    }
    //跳转到登陆页面
    private Mono<Void> tologinPage(ServerHttpResponse response, String loginUrl) {
        response.setStatusCode(HttpStatus.SEE_OTHER);
        response.getHeaders().set("Location",loginUrl);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}