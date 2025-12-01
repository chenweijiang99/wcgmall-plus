package com.river.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.io.Serializable;

@Data
@TableName("blog")
@Schema(description = "博客对象")
public class Blog implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "创建用户ID")
    private Long userId;

    @Schema(description = "作者")
    private String author;

    @Schema(description = "博客标题")
    private String title;

    @Schema(description = "博客内容")
    private String content;

    @Schema(description = "博客内容MD")
    private String contentMd;

    @Schema(description = "博客图片")
    private String image;

    @Schema(description = "0:下架 1：上架")
    private Integer status;

    @TableField(exist = false)
    private String userAvatar;

    @Schema(description = "用户签名")
    @TableField(exist = false)
    private String signature;
}
