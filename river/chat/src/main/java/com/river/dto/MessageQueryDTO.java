package com.river.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息查询DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "消息查询DTO")
public class MessageQueryDTO {

    @NotNull(message = "聊天室ID不能为空")
    @Schema(description = "聊天室ID")
    private Long roomId;

    @Schema(description = "最后一条消息ID(用于分页)")
    private Long lastMsgId;

    @Schema(description = "每页数量")
    private Integer pageSize;
}
