package com.river.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.river.utils.DateUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@TableName("sys_login_log")
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "系统登录日志表")
public class SysLoginLog implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "登录IP")
    private String ip;

    @Schema(description = "IP来源")
    private String ipSource;

    @Schema(description = "浏览器")
    private String browser;

    @Schema(description = "操作系统")
    private String os;

    @Schema(description = "登录状态（0成功 1失败）")
    private Integer status;

    @Schema(description = "登录方式")
    private String loginType;

    @Schema(description = "提示消息")
    private String message;

    @Schema(description = "登录时间")
    @JsonFormat(pattern = DateUtil.YYYY_MM_DD_HH_MM_SS, timezone = "GMT+8")
    private LocalDateTime loginTime;

}
