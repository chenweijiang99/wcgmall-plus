package com.river.dto.user;

import com.river.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "新增用户参数")
public class SysUserAddAndUpdateDto {

    @Schema(description = "用户信息")
    private SysUser user;

    @Schema(description = "角色ID列表")
    private List<Integer> roleIds;
}
