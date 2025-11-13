package com.river.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 前端页面地址配置属性
 *
 * @author river
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "front")
public class FrontConfigProperties {
    private String url;
}
