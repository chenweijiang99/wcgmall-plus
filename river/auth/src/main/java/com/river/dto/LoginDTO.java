package com.river.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "登录参数")
public class LoginDTO {

    @Schema(description = "用户名")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "登录来源 PC ADMIN")
    private String source;

    @NotBlank(message = "验证码nonceStr不能为空")
    private String nonceStr;

    @NotBlank(message = "验证码value不能为空")
    private String value;
}
