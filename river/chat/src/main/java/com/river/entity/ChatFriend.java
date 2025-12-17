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
 * 好友关系实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("chat_friend")
@Schema(description = "好友关系实体")
public class ChatFriend implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户类型: 1-普通用户 2-管理员")
    private Integer userType;

    @Schema(description = "好友ID")
    private Long friendId;

    @Schema(description = "好友类型: 1-普通用户 2-管理员")
    private Integer friendType;

    @Schema(description = "好友备注")
    private String remark;

    @Schema(description = "私聊聊天室ID")
    private Long roomId;

    @Schema(description = "状态: 0-已删除 1-正常 2-拉黑")
    private Integer status;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
