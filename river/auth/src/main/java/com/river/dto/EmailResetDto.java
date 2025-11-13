package com.river.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "根据邮箱重置密码DTO")
public class EmailResetDto {
    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "验证码")
    private String code;

    @Schema(description = "密码")
    private String password;
}
