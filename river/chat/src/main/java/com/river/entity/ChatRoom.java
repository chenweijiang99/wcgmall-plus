package com.river.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天室实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("chat_room")
@Schema(description = "聊天室实体")
public class ChatRoom implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "聊天室ID")
    private Long id;

    @Schema(description = "聊天室名称")
    private String name;

    @Schema(description = "聊天室头像")
    private String avatar;

    @Schema(description = "聊天室类型: 1-私聊 2-群聊")
    private Integer type;

    @Schema(description = "群主ID")
    private Long ownerId;

    @Schema(description = "群主类型: 1-普通用户 2-管理员")
    private Integer ownerType;

    @Schema(description = "群公告")
    private String announcement;

    @Schema(description = "最大成员数")
    private Integer maxMemberCount;

    @Schema(description = "状态: 0-禁用 1-正常")
    private Integer status;

    @Schema(description = "创建人")
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @Schema(description = "更新人")
    @TableField(fill = FieldFill.UPDATE)
    private String updateBy;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
