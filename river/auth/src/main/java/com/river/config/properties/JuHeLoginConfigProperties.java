package com.mojian.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "juhe")
public class JuHeLoginConfigProperties {
    private String id;
    private String key;
    private String loginUrl;
    private String checkLoginUrl;
    private String returnUrl;
}
