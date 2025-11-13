package com.river.vo.user;

import com.river.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "用户分页视图对象")
public class SysUserVo extends SysUser {

    @Schema(description = "角色id集合")
    private List<Integer> roleIds;
}
