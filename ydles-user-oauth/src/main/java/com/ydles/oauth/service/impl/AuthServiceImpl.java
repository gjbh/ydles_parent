package com.ydles.oauth.service.impl;

import com.ydles.oauth.service.AuthService;
import com.ydles.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Autowired
    StringRedisTemplate stringredisTemplate;
    @Value("${auth.ttl}")
    private long ttl;

    @Override
    public AuthToken login(String username, String password, String clientId, String clientSecret) {
        // 1申请令牌
        //请求地址： http://localhost:9200/oauth/token
        ServiceInstance serviceInstance = loadBalancerClient.choose("user-auth");
        // http://localhost:9200
        URI uri = serviceInstance.getUri();
        String url = uri + "/oauth/token";

        //请求体 body
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);
        //请求头
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", this.getHttpBasic(clientId, clientSecret));
        //封装请求参数
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        //后端401 400，不认为是异常，直接返回给前端
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if(response.getRawStatusCode()!=400&&response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });

        /**
         * 1 url
         * 2请求方法
         * 3请求头和体
         * 4返回值类型
         */
        ResponseEntity<Map> responseMap = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        Map map = responseMap.getBody();
        if(map==null&&map.get("access_token")==null&&map.get("refresh_token")==null&&map.get("jti")==null){
            new RuntimeException("申请令牌失败！");
        }

        // 2封装数据结果
        AuthToken authToken=new AuthToken();
        authToken.setAccessToken((String) map.get("access_token"));
        authToken.setRefreshToken((String) map.get("refresh_token"));
        authToken.setJti((String) map.get("jti"));

        // 3将jti：jwt存储到redis
        stringredisTemplate.boundValueOps(authToken.getJti()).set(authToken.getAccessToken(),ttl , TimeUnit.SECONDS);

        return authToken;
    }

    //请求头中Authorization值的计算方法
    private String getHttpBasic(String clientId, String clientSecret) {

        String value = clientId + ":" + clientSecret;
        byte[] encode = Base64Utils.encode(value.getBytes());
        return "Basic "+new String(encode);
    }
}
