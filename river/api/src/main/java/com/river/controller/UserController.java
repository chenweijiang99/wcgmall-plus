package com.river.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.river.common.Result;
import com.river.dto.user.UpdatePwdDTO;
import com.river.entity.SysUser;
import com.river.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController("userInfoController")
@RequestMapping("/user/user")
@Slf4j
@Tag(name = "用户相关接口")
@RequiredArgsConstructor
public class UserController {
    private final SysUserService sysUserService;

    @GetMapping("/userInfo")
    @Operation(summary = "获取用户信息")
    public Result<SysUser> userInfo() {
        return Result.success(sysUserService.userGetInfo());
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户信息")
    public Result<Object> update(@RequestBody SysUser user) {
        return Result.success(sysUserService.userUpdate(user));
    }
    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public Result<Object> logout(@RequestHeader("Authorization") String token){
        StpUtil.logoutByTokenValue(token);
        return Result.success();
    }
    @PostMapping("/updatePwd")
    @Operation(summary = "更新用户密码")
    public Result<Void> updatePwd(@RequestBody UpdatePwdDTO updatePwdDTO) {
        sysUserService.updatePwd(updatePwdDTO);
        return Result.success();
    }
}
