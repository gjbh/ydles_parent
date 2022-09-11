package com.ydles.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
  //feign拦截器    只要feign远程调用，作用：把上一次请求头jwt带到这一次请求中
@Component
public class FeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        //1拿到上一层请求头中的jwt
        //这一次整体请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if(requestAttributes!=null){
            //拿到我们常用的这个请求
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            if(request!=null){
                //请所有的求头
                Enumeration<String> headerNames = request.getHeaderNames();
                //遍历
                while (headerNames.hasMoreElements()){
                    String headName = headerNames.nextElement();
                    if(headName.equalsIgnoreCase("Authorization")){
                        String jwt = request.getHeader(headName);//Bearer jwt
                        //2jwt 带到这一次 feign调用中
                        requestTemplate.header(headName,jwt);
                    }
                }
            }
        }
    }
}