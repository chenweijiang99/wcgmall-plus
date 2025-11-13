package com.river.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "captcha")
public class CaptchaConfigProperties {
    /**
     * 验证码类型：remote-网络图片，local-本地图片
     **/
    private String place;
    /**
     * 本地图片地址
     **/
    private String localPath;
    /**
     * 网络图片地址
     **/
    private String remotePath;
    /**
     * 拼图验证码允许偏差
     **/
    private Integer allowDeviation;

    /**
     * 验证码有效期，单位：秒
     **/
    private Integer expire;
    /**
     * 画布默认宽度
     **/
    private Integer canvasWidth;
    /**
     * 画布默认高度
     **/
    private Integer canvasHeight;
    /**
     * 阻塞块高度默认值
     **/
    private Integer blockHeight;
    /**
     * 阻塞块宽度默认值
     **/
    private Integer blockWidth;
     /**
      * 阻塞块凹凸半径默认值
      **/
    private Integer blockRadius;
}
