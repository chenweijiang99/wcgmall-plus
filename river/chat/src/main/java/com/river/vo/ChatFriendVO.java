package com.river.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 好友VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "好友VO")
public class ChatFriendVO {

    @Schema(description = "好友关系ID")
    private Long id;

    @Schema(description = "好友ID")
    private Long friendId;

    @Schema(description = "好友类型")
    private Integer friendType;

    @Schema(description = "好友昵称")
    private String nickname;

    @Schema(description = "好友头像")
    private String avatar;

    @Schema(description = "好友备注")
    private String remark;

    @Schema(description = "私聊聊天室ID")
    private Long roomId;

    @Schema(description = "是否在线")
    private Boolean online;

    @Schema(description = "添加时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
