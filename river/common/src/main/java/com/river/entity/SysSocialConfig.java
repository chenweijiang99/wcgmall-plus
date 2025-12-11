package com.river.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.river.utils.DateUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 第三方登录配置表
 */
@Data
@TableName("sys_social_config")
@Schema(description = "第三方登录配置表")
public class SysSocialConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "登录类型(qq/wechat/alipay/weibo/gitee/github等)")
    private String socialType;

    @Schema(description = "显示名称")
    private String socialName;

    @Schema(description = "登录模式(juhe聚合登录/oauth2)")
    private String loginMode;

    @Schema(description = "聚合登录类型编号")
    private Integer juheTypeCode;

    @Schema(description = "是否支持OAuth2")
    private Integer supportOauth2;

    @Schema(description = "是否支持聚合登录")
    private Integer supportJuhe;

    @Schema(description = "AppId/ClientId (OAuth2模式使用)")
    private String appId;

    @Schema(description = "AppSecret/ClientSecret (OAuth2模式使用)")
    private String appSecret;

    @Schema(description = "回调地址 (OAuth2模式使用)")
    private String redirectUrl;

    @Schema(description = "状态(0禁用 1启用)")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "备注")
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtil.YYYY_MM_DD_HH_MM_SS, timezone = "GMT+8")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    @JsonFormat(pattern = DateUtil.YYYY_MM_DD_HH_MM_SS, timezone = "GMT+8")
    private LocalDateTime updateTime;
}
