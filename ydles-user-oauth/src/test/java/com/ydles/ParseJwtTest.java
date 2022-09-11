package com.ydles;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

public class ParseJwtTest {
    @Test
    public void parseJwt(){
        //基于公钥去解析jwt
        String jwt ="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhZGRyZXNzIjoidGFpeXVhbiIsImNvbnBhbnkiOiJpdGxpbHMifQ.L6nklrsZnooPAfBRu4AseGXVReq_E8nh4FJsYLJoS7vNxEpjBUXVGy5aXl8sS8rYhDEPGhwEgnFkNxe-ru9DiYTCnpGr0MgOKre7z0nswm6txy_6yzgA-Ydamv9rSSv1SrDOw0TzbWO50_QDrRrKqm57D2pRYZcp9yYdM-tpjKKpCJoskdPODUXqapliAZXSlASX4pdNd2dhzTZhpKpoLbPq3ZSp4T6pYCPYpLNkclaNhb5a6ou4Xc42rxUL2rk8F48RarFhtxTdNQr6I9HBK3AgL2zjLyMnRqWWwuK5LEOMsdDLFEXw8u780Tq6or8E3QmDTzC-UWiuXWFxj1ZYPg";

        String publicKey ="-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkKfnhqlP8P36ZPX9KHSZJsfVGm34Kv7SVHKziZ9VkpZ5zRot4dBYir5mAzzCPAJH6l57Sj1JqLdaM5FSUVS1JDhMXhL0IxgWCQH2skHyjXG+sqnSFLm1bS6Ucqa7PdhY214+2tsaKgUPNUvHOMW0N+Z+odSNWZq47dohxidufU0k6U4yYY69ACFyJTrvpLyYCKAr3TaKWt//Ru4OID2roN+DrQcoAfqx/t/54aSYsxicZOVlD9MIah8hfrVhLT4PZBwtPTIDMggkrvrlPFk5uAMhp9KAHk6PKWYE/fIhMUhRHvMObAroof1f7Fwft3F4e1pSoHU0is2i64hVRcZtfwIDAQAB-----END PUBLIC KEY-----";

        //解析令牌
        Jwt token = JwtHelper.decodeAndVerify(jwt, new RsaVerifier(publicKey));
        //获取负载
        String claims = token.getClaims();
        System.out.println(claims);


        String ydlershe = BCrypt.hashpw("123123", BCrypt.gensalt());
        System.out.println(ydlershe);
    }
}