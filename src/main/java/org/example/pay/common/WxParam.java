package org.example.pay.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author gaowende
 * @date 2023/11/2 16:49
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat")
public class WxParam {

    private String appId;

    private String mchId;

    private String notifyUrl;

    private String serialNumber;

    private String apiKey3;

    private String privateKey;
}
