package com.river.vo.user;

import com.river.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "个人信息")
public class SysUserProfileVo {

   @Schema(description = "用户信息")
    private SysUser sysUser;

   @Schema(description = "角色")
    private List<String> roles;
}
