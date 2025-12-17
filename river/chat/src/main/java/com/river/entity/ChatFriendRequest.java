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
 * 好友申请实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("chat_friend_request")
@Schema(description = "好友申请实体")
public class ChatFriendRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "申请人ID")
    private Long fromUserId;

    @Schema(description = "申请人类型: 1-普通用户 2-管理员")
    private Integer fromUserType;

    @Schema(description = "被申请人ID")
    private Long toUserId;

    @Schema(description = "被申请人类型: 1-普通用户 2-管理员")
    private Integer toUserType;

    @Schema(description = "申请消息")
    private String message;

    @Schema(description = "状态: 0-待处理 1-已同意 2-已拒绝 3-已过期")
    private Integer status;

    @Schema(description = "处理时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime handleTime;

    @Schema(description = "申请时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
