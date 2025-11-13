package com.river.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.List;

@Data
@TableName("comments")
@Schema(description = "评论对象")
public class Comments implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "父级评论ID")
    private Long pid;

    @Schema(description = "关联ID")
    private Long fid;

    @Schema(description = "模块标识")
    private String module;

    @Schema(description = "根节点ID")
    private Long rootId;

    @Schema(description = "评论人ID")
    private Long userId;

    @Schema(description = "用户名")
    @TableField(exist = false)
    private String username;

    @Schema(description = "评论时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "评论内容")
    private String comment;

}
