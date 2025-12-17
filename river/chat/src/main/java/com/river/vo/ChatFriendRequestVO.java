package com.river.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 好友申请VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "好友申请VO")
public class ChatFriendRequestVO {

    @Schema(description = "申请ID")
    private Long id;

    @Schema(description = "申请人ID")
    private Long fromUserId;

    @Schema(description = "申请人类型")
    private Integer fromUserType;

    @Schema(description = "申请人昵称")
    private String fromUserNickname;

    @Schema(description = "申请人头像")
    private String fromUserAvatar;

    @Schema(description = "申请消息")
    private String message;

    @Schema(description = "状态: 0-待处理 1-已同意 2-已拒绝 3-已过期")
    private Integer status;

    @Schema(description = "申请时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
