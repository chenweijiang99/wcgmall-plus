package com.river.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.river.common.Result;
import com.river.entity.SysOperateLog;
import com.river.service.SysOperateLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/sys/operateLog")
@RequiredArgsConstructor
@Tag(name = "操作日志管理")
public class SysOperateLogController {

    private final SysOperateLogService sysOperateLogService;

    @GetMapping
    @Operation(summary = "获取操作日志列表")
    public Result<IPage<SysOperateLog>> list(SysOperateLog sysOperateLog) {
        return Result.success(sysOperateLogService.listSysOperateLog(sysOperateLog));
    }

    @DeleteMapping("delete/{ids}")
    @Operation(summary = "批量删除操作日志")
    @SaCheckPermission("sys:operateLog:delete")
    public Result<Void> delete(@PathVariable List<Long> ids) {
        sysOperateLogService.removeBatchByIds(ids);
        return Result.success();
    }
}
