package com.river.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_file_oss")
@Schema(description = "存储平台对象")
public class SysFileOss implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "访问域名")
    private String domain;

    @Schema(description = "access-key")
    private String accessKey;

    @Schema(description = "secret-key")
    private String secretKey;

    @Schema(description = "空间名")
    private String bucket;
    @Schema(description = "地域节点")
    private String endpoint;
    @Schema(description = "存储基础路径")
    private String basePath;

    @Schema(description = "存储类型")
    private String platform;

    @Schema(description = "是否启用存储")
    private Integer isEnable;

    @Schema(description = "本地存储路径")
    private String storagePath;

    @Schema(description = "本地启用访问")
    private Integer enableAccess;

    @Schema(description = "本地访问路径")
    private String pathPatterns;

    @Schema(description = "仓库所在地域")
    private String region;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
