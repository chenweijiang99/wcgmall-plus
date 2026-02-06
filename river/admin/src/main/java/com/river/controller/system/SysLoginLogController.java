package com.river.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.river.annotation.OperationLogger;
import com.river.common.Result;
import com.river.entity.SysLoginLog;
import com.river.service.SysLoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/sys/loginLog")
@RequiredArgsConstructor
@Tag(name = "登录日志管理")
public class SysLoginLogController {

    private final SysLoginLogService sysLoginLogService;

    @GetMapping
    @Operation(summary = "获取登录日志列表")
    public Result<IPage<SysLoginLog>> list(SysLoginLog sysLoginLog) {
        return Result.success(sysLoginLogService.listSysLoginLog(sysLoginLog));
    }

    @DeleteMapping("delete/{ids}")
    @Operation(summary = "批量删除登录日志")
    @SaCheckPermission("sys:loginLog:delete")
    @OperationLogger(value = "批量删除登录日志")
    public Result<Void> delete(@PathVariable List<Long> ids) {
        sysLoginLogService.removeBatchByIds(ids);
        return Result.success();
    }

    @DeleteMapping("clean")
    @Operation(summary = "清空登录日志")
    @SaCheckPermission("sys:loginLog:delete")
    @OperationLogger(value = "清空登录日志")
    public Result<Void> clean() {
        sysLoginLogService.remove(null);
        return Result.success();
    }
}
