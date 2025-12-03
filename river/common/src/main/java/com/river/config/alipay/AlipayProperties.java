package com.river.config.alipay;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝配置类
 * @author 枳枳
 */
@Component
@ConfigurationProperties(prefix = "alipay")
@Data
public class AlipayProperties {
    private String appId;
    private String privateKey;
    private String alipayPublicKey;
    private String notifyUrl;
    private String returnUrl;
    private String charset;
    private String signType;
    private String gatewayUrl;
    private String format;
}
