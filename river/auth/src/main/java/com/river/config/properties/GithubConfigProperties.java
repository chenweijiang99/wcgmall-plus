package com.river.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** github登录配置属性
 * @author river
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "github")
public class GithubConfigProperties {

    /**
     * appId
     */
    private String appId;

    /**
     * appSecret
     */
    private String appSecret;

    /**
     * 回调域名
     */
    private String redirectUrl;



}

