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
 * 聊天消息实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("chat_message")
@Schema(description = "聊天消息实体")
public class ChatMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "聊天室ID")
    private Long roomId;

    @Schema(description = "发送者ID")
    private Long senderId;

    @Schema(description = "发送者类型: 1-普通用户 2-管理员")
    private Integer senderType;

    @Schema(description = "消息类型: 1-文本 2-图片 3-视频 4-语音 5-表情 6-系统消息")
    private Integer msgType;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "媒体文件URL")
    private String mediaUrl;

    @Schema(description = "媒体时长(秒)")
    private Integer mediaDuration;

    @Schema(description = "媒体文件大小(字节)")
    private Long mediaSize;

    @Schema(description = "缩略图URL")
    private String thumbnailUrl;

    @Schema(description = "回复的消息ID")
    private Long replyMsgId;

    @Schema(description = "@的用户ID列表")
    private String atUserIds;

    @Schema(description = "是否撤回: 0-否 1-是")
    private Integer isRevoked;

    @Schema(description = "撤回时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime revokeTime;

    @Schema(description = "状态: 0-删除 1-正常")
    private Integer status;

    @Schema(description = "发送时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
