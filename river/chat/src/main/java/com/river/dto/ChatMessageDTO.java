package com.river.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 发送消息DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "发送消息DTO")
public class ChatMessageDTO {

    @NotNull(message = "聊天室ID不能为空")
    @Schema(description = "聊天室ID")
    private Long roomId;

    @NotNull(message = "消息类型不能为空")
    @Schema(description = "消息类型: 1-文本 2-图片 3-视频 4-语音 5-表情")
    private Integer msgType;

    @Schema(description = "消息内容(文本/表情代码)")
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
    private List<Long> atUserIds;
}
