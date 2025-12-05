package com.river.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.river.annotation.OperationLogger;
import com.river.common.Result;
import com.river.entity.SysConfig;
import com.river.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 参数配置表 控制器
 */
@RestController
@RequestMapping("/sys/config")
@RequiredArgsConstructor
@Tag(name = "参数配置表管理")
public class SysConfigController {

    private final SysConfigService sysConfigService;

    @GetMapping("/list")
    @Operation(summary = "获取参数配置表列表")
    public Result<IPage<SysConfig>> list(SysConfig sysConfig) {
        return Result.success(sysConfigService.selectPage(sysConfig));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取参数配置表详情")
    public Result<SysConfig> getInfo(@PathVariable("id") Long id) {
        return Result.success(sysConfigService.getById(id));
    }

    @PostMapping("/add")
    @SaCheckPermission("sys:config:add")
    @OperationLogger(value = "添加参数配置表")
    @Operation(summary = "添加参数配置表")
    public Result<Object> add(@RequestBody SysConfig sysConfig) {
        return Result.success(sysConfigService.insert(sysConfig));
    }

    @PutMapping("/update")
    @SaCheckPermission("sys:config:update")
    @Operation(summary = "修改参数配置表")
    @OperationLogger(value = "修改参数配置表")
    public Result<Object> edit(@RequestBody SysConfig sysConfig) {
        return Result.success(sysConfigService.update(sysConfig));
    }

    @DeleteMapping("/delete/{ids}")
    @SaCheckPermission("sys:config:delete")
    @Operation(summary = "删除参数配置表")
    @OperationLogger(value = "删除参数配置表")
    public Result<Object> remove(@PathVariable List<Long> ids) {
        return Result.success(sysConfigService.deleteByIds(ids));
    }

    @SaIgnore
    @GetMapping("/getConfigByKey/{key}")
    @Operation(summary = "根据key获取参数配置详情")
    public Result<SysConfig> selectConfigByKey(@PathVariable("key") String key) {
        return Result.success(sysConfigService.selectConfigByKey(key));
    }
}
