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
@TableName("sys_operate_log")
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "系统操作日志表")
public class SysOperateLog implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "操作用户")
    private String username;

    @Schema(description = "请求接口")
    private String requestUrl;

    @Schema(description = "请求方式")
    private String type;

    @Schema(description = "操作名称")
    private String operationName;

    @Schema(description = "ip")
    private String ip;

    @Schema(description = "ip来源")
    private String source;

    @Schema(description = "请求接口耗时")
    private Long spendTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtil.YYYY_MM_DD_HH_MM_SS, timezone = "GMT+8")
    private LocalDateTime createTime;

    @Schema(description = "请求参数")
    private String paramsJson;

    @Schema(description = "类地址")
    private String classPath;

    @Schema(description = "方法名")
    private String methodName;

}
