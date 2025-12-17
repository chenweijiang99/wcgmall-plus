package com.river.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 处理好友申请DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "处理好友申请DTO")
public class HandleFriendRequestDTO {

    @NotNull(message = "申请ID不能为空")
    @Schema(description = "申请ID")
    private Long requestId;

    @NotNull(message = "处理结果不能为空")
    @Schema(description = "处理结果: 1-同意 2-拒绝")
    private Integer action;
}
