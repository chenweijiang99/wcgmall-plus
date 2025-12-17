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
 * 用户会话实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("chat_conversation")
@Schema(description = "用户会话实体")
public class ChatConversation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户类型: 1-普通用户 2-管理员")
    private Integer userType;

    @Schema(description = "聊天室ID")
    private Long roomId;

    @Schema(description = "最后一条消息ID")
    private Long lastMsgId;

    @Schema(description = "最后一条消息内容摘要")
    private String lastMsgContent;

    @Schema(description = "最后一条消息时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastMsgTime;

    @Schema(description = "未读消息数")
    private Integer unreadCount;

    @Schema(description = "是否置顶: 0-否 1-是")
    private Integer isTop;

    @Schema(description = "是否免打扰: 0-否 1-是")
    private Integer isMuted;

    @Schema(description = "是否删除会话: 0-否 1-是")
    private Integer isDeleted;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
