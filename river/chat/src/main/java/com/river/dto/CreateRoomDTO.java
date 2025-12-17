package com.river.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建聊天室DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "创建聊天室DTO")
public class CreateRoomDTO {

    @NotBlank(message = "群名称不能为空")
    @Schema(description = "群名称")
    private String name;

    @Schema(description = "群头像")
    private String avatar;

    @NotEmpty(message = "成员列表不能为空")
    @Schema(description = "成员ID列表")
    private List<Long> memberIds;

    @Schema(description = "成员类型列表(与memberIds对应)")
    private List<Integer> memberTypes;
}
