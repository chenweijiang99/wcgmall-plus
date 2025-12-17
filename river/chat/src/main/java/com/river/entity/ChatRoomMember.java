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
 * 聊天室成员实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("chat_room_member")
@Schema(description = "聊天室成员实体")
public class ChatRoomMember implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "聊天室ID")
    private Long roomId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户类型: 1-普通用户 2-管理员")
    private Integer userType;

    @Schema(description = "群内昵称")
    private String nickname;

    @Schema(description = "角色: 0-普通成员 1-管理员 2-群主")
    private Integer role;

    @Schema(description = "是否禁言: 0-否 1-是")
    private Integer muted;

    @Schema(description = "禁言结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime mutedEndTime;

    @Schema(description = "最后已读消息ID")
    private Long lastReadMsgId;

    @Schema(description = "最后已读时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastReadTime;

    @Schema(description = "加入时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime joinTime;

    @Schema(description = "状态: 0-已退出 1-正常")
    private Integer status;
}
