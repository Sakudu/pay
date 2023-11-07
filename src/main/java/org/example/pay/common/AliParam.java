package org.example.pay.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author gwd
 * @date 2023/11/6 9:56
 */
@Data
@Component
@ConfigurationProperties(prefix = "ali")
public class AliParam {

    private String appId;

    private String uId;

    private String opAppId;

    private String privateKey;

    private String publicKey;

    private String notifyUrl;

    private String pcReturnUrl;

    private String h5ReturnUrl;

    private String signType;

    private String charSet;

    public String gateWayUrl;

    public String appCertPath;

    public String aliPayCertPath;

    public String aliPayRootCertPath;

}
