package com.river.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.river.annotation.OperationLogger;
import com.river.common.Result;
import com.river.entity.SysSocialConfig;
import com.river.service.SysSocialConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 第三方登录配置 控制器
 */
@RestController
@RequestMapping("/sys/socialConfig")
@RequiredArgsConstructor
@Tag(name = "第三方登录配置管理")
public class SysSocialConfigController {

    private final SysSocialConfigService sysSocialConfigService;

    @GetMapping("/list")
    @Operation(summary = "获取第三方登录配置列表(分页)")
    public Result<IPage<SysSocialConfig>> list(SysSocialConfig config) {
        return Result.success(sysSocialConfigService.selectPage(config));
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有第三方登录配置")
    public Result<List<SysSocialConfig>> all() {
        return Result.success(sysSocialConfigService.selectAll());
    }

    @SaIgnore
    @GetMapping("/enabledList")
    @Operation(summary = "获取当前模式下启用的第三方登录配置")
    public Result<List<SysSocialConfig>> enabledList() {
        return Result.success(sysSocialConfigService.selectEnabledByMode());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取第三方登录配置详情")
    public Result<SysSocialConfig> getInfo(@PathVariable("id") Long id) {
        return Result.success(sysSocialConfigService.getById(id));
    }

    @GetMapping("/type/{socialType}")
    @Operation(summary = "根据类型获取第三方登录配置")
    public Result<SysSocialConfig> getByType(@PathVariable("socialType") String socialType) {
        return Result.success(sysSocialConfigService.selectByType(socialType));
    }

    @PostMapping("/add")
    @SaCheckPermission("sys:socialConfig:add")
    @OperationLogger(value = "添加第三方登录配置")
    @Operation(summary = "添加第三方登录配置")
    public Result<Object> add(@RequestBody SysSocialConfig config) {
        return Result.success(sysSocialConfigService.insert(config));
    }

    @PutMapping("/update")
    @SaCheckPermission("sys:socialConfig:update")
    @Operation(summary = "修改第三方登录配置")
    @OperationLogger(value = "修改第三方登录配置")
    public Result<Object> edit(@RequestBody SysSocialConfig config) {
        return Result.success(sysSocialConfigService.update(config));
    }

    @PutMapping("/updateStatus")
    @SaCheckPermission("sys:socialConfig:update")
    @Operation(summary = "修改第三方登录配置状态")
    @OperationLogger(value = "修改第三方登录配置状态")
    public Result<Object> updateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        return Result.success(sysSocialConfigService.updateStatus(id, status));
    }

    @DeleteMapping("/delete/{ids}")
    @SaCheckPermission("sys:socialConfig:delete")
    @Operation(summary = "删除第三方登录配置")
    @OperationLogger(value = "删除第三方登录配置")
    public Result<Object> remove(@PathVariable List<Long> ids) {
        return Result.success(sysSocialConfigService.deleteByIds(ids));
    }

    @SaIgnore
    @GetMapping("/settings")
    @Operation(summary = "获取第三方登录全局设置")
    public Result<Map<String, Object>> getSettings() {
        return Result.success(sysSocialConfigService.getGlobalSettings());
    }

    @PutMapping("/settings")
    @SaCheckPermission("sys:socialConfig:update")
    @Operation(summary = "更新第三方登录全局设置")
    @OperationLogger(value = "更新第三方登录全局设置")
    public Result<Object> updateSettings(@RequestBody Map<String, Object> settings) {
        return Result.success(sysSocialConfigService.updateGlobalSettings(settings));
    }

    @SaIgnore
    @GetMapping("/loginMode")
    @Operation(summary = "获取全局登录模式")
    public Result<String> getLoginMode() {
        return Result.success(sysSocialConfigService.getGlobalLoginMode());
    }

    @PutMapping("/loginMode/{mode}")
    @SaCheckPermission("sys:socialConfig:update")
    @Operation(summary = "设置全局登录模式")
    @OperationLogger(value = "设置全局登录模式")
    public Result<Object> setLoginMode(@PathVariable("mode") String mode) {
        return Result.success(sysSocialConfigService.setGlobalLoginMode(mode));
    }
}
