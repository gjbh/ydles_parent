package com.github.wxpay.sdk;

import java.io.InputStream;

public class MyConfig extends WXPayConfig {
    @Override
    public String getAppID() {
        return "wxababcd122d1618eb";
    }

    @Override
    public String getMchID() {
        return "1611671554";
    }

    @Override
    public String getKey() {
        return "ydlclass66666688888YDLCLASS66688";
    }

    @Override
    public InputStream getCertStream() {
        return null;
    }

    @Override
    public IWXPayDomain getWXPayDomain() {
        return new IWXPayDomain() {
            @Override
            public void report(String s, long l, Exception e) {

            }

            @Override
            public DomainInfo getDomain(WXPayConfig wxPayConfig) {
                return new DomainInfo("api.mch.weixin.qq.com",true);
            }
        };
    }
}
