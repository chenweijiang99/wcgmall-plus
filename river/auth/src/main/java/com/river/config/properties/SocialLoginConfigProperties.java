package com.river.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 第三方登录配置
 * 用于切换聚合登录和OAuth2登录方式
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "social-login")
public class SocialLoginConfigProperties {

    /**
     * 第三方登录类型
     * juhe: 聚合登录
     * oauth2: OAuth2登录（JustAuth）
     */
    private String type = "juhe";

    /**
     * 是否启用第三方登录
     */
    private Boolean enabled = true;

    /**
     * 判断是否使用聚合登录
     */
    public boolean isJuhe() {
        return "juhe".equalsIgnoreCase(type);
    }

    /**
     * 判断是否使用OAuth2登录
     */
    public boolean isOauth2() {
        return "oauth2".equalsIgnoreCase(type);
    }
}
