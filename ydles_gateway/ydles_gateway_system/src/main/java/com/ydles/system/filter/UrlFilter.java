package com.ydles.system.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
@Component
public class UrlFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求的url
        //日志打印
        System.out.println("经过了第二个过滤器");
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getURI().getPath();
        System.out.println("path:"+path);
        if (path.contains("/admin/delect")) {
            System.out.println("这个用户在这个时间段删除了管理员用户");
        }
        //放行
        //chain.filter(exchange);//过滤器链继续执行
        return chain.filter(exchange);
    }
    //过滤器的执行优先级,返回值越小,执行优先级越高
    @Override
    public int getOrder() {
        return 2;
    }

}
