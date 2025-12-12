package com.river.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import com.river.annotation.OperationLogger;
import com.river.common.Result;
import com.river.entity.SysSiteConfig;
import com.river.service.SysSiteConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 网站配置 控制器
 */
@RestController
@RequestMapping("/sys/siteConfig")
@RequiredArgsConstructor
@Tag(name = "网站配置管理")
public class SysSiteConfigController {

    private final SysSiteConfigService sysSiteConfigService;

    @GetMapping("/list")
    @Operation(summary = "获取所有网站配置列表")
    public Result<List<SysSiteConfig>> list() {
        return Result.success(sysSiteConfigService.list());
    }

    @GetMapping("/map")
    @Operation(summary = "获取所有网站配置（Map形式）")
    public Result<Map<String, String>> getAllConfigMap() {
        return Result.success(sysSiteConfigService.getAllConfigMap());
    }

    @SaIgnore
    @GetMapping("/public")
    @Operation(summary = "获取公开的网站配置（无需登录）")
    public Result<Map<String, String>> getPublicConfig() {
        return Result.success(sysSiteConfigService.getAllConfigMap());
    }

    @GetMapping("/group/{group}")
    @Operation(summary = "根据分组获取配置")
    public Result<List<SysSiteConfig>> getByGroup(@PathVariable String group) {
        return Result.success(sysSiteConfigService.selectByGroup(group));
    }

    @SaIgnore
    @GetMapping("/key/{key}")
    @Operation(summary = "根据键获取配置值")
    public Result<String> getByKey(@PathVariable String key) {
        return Result.success(sysSiteConfigService.getValueByKey(key));
    }

    @PutMapping("/update")
    @SaCheckPermission("sys:siteConfig:update")
    @OperationLogger(value = "修改网站配置")
    @Operation(summary = "批量更新网站配置")
    public Result<Object> batchUpdate(@RequestBody Map<String, String> configs) {
        return Result.success(sysSiteConfigService.batchUpdate(configs));
    }

    @PutMapping("/update/{key}")
    @SaCheckPermission("sys:siteConfig:update")
    @OperationLogger(value = "修改网站配置")
    @Operation(summary = "更新单个网站配置")
    public Result<Object> updateByKey(@PathVariable String key, @RequestBody String value) {
        return Result.success(sysSiteConfigService.updateByKey(key, value));
    }
}
