package com.ydles.web.gateway.utils;

public class UrlUtil {
    //哪些路径需要令牌
    public static String filterPath = "/api/worder/**,/api/wseckillorder,/api/seckill,/api/wxpay,/api/wxpay/**,/api/worder/**,/api/user/**,/api/address/**,/api/wcart/**,/api/cart/**,/api/categoryReport/**,/api/orderConfig/**,/api/order/**,/api/orderItem/**,/api/orderLog/**,/api/preferential/**,/api/returnCause/**,/api/returnOrder/**,/api/returnOrderItem/**";

    //传来一个路径 需不需要令牌
    public static boolean hasAuthorize(String url){
        String[] split = filterPath.replace("**", "").split(",");
        for (String s : split) {
            if(url.startsWith(s)){
                return true;
            }
        }
        return false;
    }
}


