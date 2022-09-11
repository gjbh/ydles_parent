package com.ydles.oauth.controller;

import com.ydles.entity.Result;
import com.ydles.entity.StatusCode;
import com.ydles.oauth.service.AuthService;
import com.ydles.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
@Controller
@RequestMapping("/oauth")
    public class AuthController {
        @Autowired
        AuthService authService;
        @Value("${auth.clientId}")
        String clientId;
        @Value("${auth.clientSecret}")
        String clientSecret;
        @Value("${auth.cookieMaxAge}")
        int cookieMaxAge;
        @Value("${auth.cookieDomain}")
        String cookieDomain;

        @RequestMapping("/login")
        @ResponseBody
        public Result login(String username, String password, HttpServletResponse response){

            AuthToken authToken = authService.login(username, password, clientId, clientSecret);

            //三 responses cookie 放jti
            Cookie cookie = new Cookie("uid",authToken.getJti());
            cookie.setMaxAge(cookieMaxAge);
            cookie.setPath("/");
            cookie.setDomain(cookieDomain);
            cookie.setHttpOnly(false);
            response.addCookie(cookie);

            return new Result(true, StatusCode.OK,"登陆成功", authToken.getJti());
        }
    //页面跳转
    @GetMapping("/toLogin")
    public String toLogin(@RequestParam(value = "FROM",required = false) String from, Model model) throws Exception{
        model.addAttribute("from",from);
        return "login";
    }
    }
