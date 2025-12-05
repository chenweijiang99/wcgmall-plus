package com.river.controller.monitor;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.river.annotation.OperationLogger;
import com.river.common.Result;
import com.river.service.SysUserService;
import com.river.vo.user.OnlineUserVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/monitor/online")
@Tag(name = "在线用户")
public class OnlineUserController {

    private final SysUserService sysUserService;

    @GetMapping("/list")
    @Operation(summary = "获取在线用户列表")
    public Result<IPage<OnlineUserVo>> getOnlineUserList(String username) {
        return Result.success(sysUserService.getOnlineUserList(username));
    }

    @Operation(summary = "强制踢出")
    @GetMapping("/forceLogout/{token}")
    @OperationLogger("强制踢出")
    @SaCheckPermission("monitor:online:forceLogout")
    public Result<Void> forceLogout(@PathVariable String token) {
        StpUtil.logoutByTokenValue(token);
        return Result.success();
    }

}
