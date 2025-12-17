package com.river.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话列表VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "会话列表VO")
public class ChatConversationVO {

    @Schema(description = "会话ID")
    private Long id;

    @Schema(description = "聊天室ID")
    private Long roomId;

    @Schema(description = "聊天室类型: 1-私聊 2-群聊")
    private Integer roomType;

    @Schema(description = "会话名称(私聊为对方昵称,群聊为群名)")
    private String name;

    @Schema(description = "会话头像")
    private String avatar;

    @Schema(description = "最后一条消息内容")
    private String lastMsgContent;

    @Schema(description = "最后一条消息类型")
    private Integer lastMsgType;

    @Schema(description = "最后一条消息时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastMsgTime;

    @Schema(description = "未读消息数")
    private Integer unreadCount;

    @Schema(description = "是否置顶")
    private Integer isTop;

    @Schema(description = "是否免打扰")
    private Integer isMuted;

    @Schema(description = "对方在线状态(私聊)")
    private Boolean online;
}
