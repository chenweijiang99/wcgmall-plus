package com.river.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 顺丰快递API配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "sf-express")
public class SfExpressConfig {

    /**
     * 顾客编码
     */
    private String partnerId;

    /**
     * 校验码
     */
    private String checkWord;

    /**
     * 接口地址
     */
    private String url;

    /**
     * 下单接口服务代码
     */
    private String createOrderServiceCode;

    /**
     * 路由查询接口服务代码
     */
    private String searchRoutesServiceCode;
}
