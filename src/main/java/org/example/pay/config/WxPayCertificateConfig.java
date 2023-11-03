package org.example.pay.config;

import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import org.example.pay.common.WxParam;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.annotation.Resource;

/**
 * @author gwd
 * @date 2023/11/3 8:52
 */
@Configuration
public class WxPayCertificateConfig {

    @Resource
    private WxParam param;

    @Lazy
    @Bean("rsaConfig")
    public RSAAutoCertificateConfig initConfig() {
        RSAAutoCertificateConfig config = new RSAAutoCertificateConfig.Builder()
                .merchantId(param.getMchId())
                .privateKey(param.getPrivateKey())
                .merchantSerialNumber(param.getSerialNumber())
                .apiV3Key(param.getApiKey3())
                .build();
        return config;
    }
}
