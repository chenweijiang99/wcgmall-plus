package com.river.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "登录用户信息")
public class LoginUserInfo implements Serializable {

    @Schema(description = "用户ID")
    private Integer id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "性别")
    private Integer sex;

    @Schema(description = "个性签名")
    private String signature;

    @Schema(description = "token")
    private String token;

    @Schema(description = "权限列表")
    private List<String> permissions;

    @Schema(description = "角色信息")
    private List<String> roles;
}
