package org.example.pay.config;

import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import lombok.extern.slf4j.Slf4j;
import org.example.pay.common.AliParam;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;

/**
 * @author gwd
 * @date 2023/11/6 9:59
 */
@Slf4j
@Configuration
public class AliPayCertificateConfig {

    @Resource
    private AliParam aliParam;

    @Lazy
    @Bean
    public DefaultAlipayClient init() {
        DefaultAlipayClient alipayClient;
        try {
            String osName = System.getProperty("os.name");
            String serverPath;
            if (osName.startsWith("Windows")) {
                serverPath = ResourceUtils.getURL("classpath:").getPath();
            } else {
                serverPath = System.getProperty("user.dir");
            }
            CertAlipayRequest request = new CertAlipayRequest();
            request.setServerUrl(aliParam.getGateWayUrl());
            request.setAppId(aliParam.getAppId());
            request.setPrivateKey(aliParam.getPrivateKey());
            request.setFormat("json");
            request.setCharset(aliParam.getCharSet());
            request.setSignType(aliParam.getSignType());
            request.setCertPath(serverPath + aliParam.getAppCertPath());
            request.setAlipayPublicCertPath(serverPath + aliParam.getAliPayCertPath());
            request.setRootCertPath(serverPath + aliParam.getAliPayRootCertPath());
            alipayClient = new DefaultAlipayClient(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return alipayClient;
    }
}
