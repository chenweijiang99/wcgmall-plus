package com.river.config.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "random-avatar")
public class RandomAvatarConfigProperties {

    /**
     * 随机头像地址
     */
    private String[] avatarList;
}
