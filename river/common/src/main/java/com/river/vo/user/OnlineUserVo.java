package com.river.vo.user;

import com.river.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class OnlineUserVo extends SysUser {

    @Schema(description = "token")
    private String tokenValue;

}
