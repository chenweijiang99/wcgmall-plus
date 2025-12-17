package com.river.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天消息VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "聊天消息VO")
public class ChatMessageVO {

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "聊天室ID")
    private Long roomId;

    @Schema(description = "发送者ID")
    private Long senderId;

    @Schema(description = "发送者类型")
    private Integer senderType;

    @Schema(description = "发送者昵称")
    private String senderNickname;

    @Schema(description = "发送者头像")
    private String senderAvatar;

    @Schema(description = "消息类型")
    private Integer msgType;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "媒体文件URL")
    private String mediaUrl;

    @Schema(description = "媒体时长")
    private Integer mediaDuration;

    @Schema(description = "媒体文件大小")
    private Long mediaSize;

    @Schema(description = "缩略图URL")
    private String thumbnailUrl;

    @Schema(description = "回复的消息")
    private ChatMessageVO replyMsg;

    @Schema(description = "@的用户列表")
    private List<AtUserVO> atUsers;

    @Schema(description = "是否撤回")
    private Integer isRevoked;

    @Schema(description = "是否是自己发送的")
    private Boolean isSelf;

    @Schema(description = "发送时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "@用户信息")
    public static class AtUserVO {
        private Long userId;
        private Integer userType;
        private String nickname;
    }
}
