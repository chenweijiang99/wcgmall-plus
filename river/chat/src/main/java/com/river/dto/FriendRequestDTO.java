package com.river.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 好友申请DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "好友申请DTO")
public class FriendRequestDTO {

    @NotNull(message = "目标用户ID不能为空")
    @Schema(description = "目标用户ID")
    private Long toUserId;

    @Schema(description = "目标用户类型: 1-普通用户 2-管理员")
    private Integer toUserType;

    @Schema(description = "申请消息")
    private String message;
}
