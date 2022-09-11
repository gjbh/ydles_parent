package com.ydles.system.filter;

import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
@Component
public class IpFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取客户端的访问ip
        //获取客户端的访问ip
        System.out.println("经过了第一个过滤器");
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        String hoststring = remoteAddress.getHostString();
        System.out.println("客户端的ip为:"+hoststring);
        String hostName = remoteAddress.getHostName();
        System.out.println("客户端的名字为:"+hostName);
        //System.out.println("ip:"+remoteAddress.getHostName());
        if (hoststring.equals("0.0.0.0")) { //0.0.0.0 根据自己的需求进行修改IP
            //禁止访问
            response.setStatusCode(HttpStatus.FORBIDDEN);//403 Forbidden 不允许访问
            return response.setComplete();
        }
        //放行
        //chain.filter(exchange);//过滤器链继续执行
        return chain.filter(exchange);
    }

    //过滤器的执行优先级,返回值越小,执行优先级越高
    @Override
    public int getOrder() {
        return 1;
    }
}
