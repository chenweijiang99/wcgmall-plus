package com.river.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.river.annotation.OperationLogger;
import com.river.common.Result;
import com.river.entity.SysRole;
import com.river.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/role")
@RequiredArgsConstructor
@Tag(name = "角色管理")
public class SysRoleController {

    private final SysRoleService sysRoleService;

    @GetMapping
    @Operation(summary = "获取角色列表")
    public Result<IPage<SysRole>> listRoles(@RequestParam(required = false) String name) {
        return Result.success(sysRoleService.listRoles(name));
    }

    @GetMapping("/{id}")
    public Result<SysRole> getRole(@PathVariable Integer id) {
        return Result.success(sysRoleService.getById(id));
    }

    @PostMapping
    @OperationLogger(value = "新增角色")
    @Operation(summary = "新增角色")
    @SaCheckPermission("sys:role:add")
    public Result<Void> addRole(@RequestBody SysRole role) {
        sysRoleService.addRole(role);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改角色")
    @OperationLogger(value = "修改角色")
    @SaCheckPermission("sys:role:update")
    public Result<Void> updateRole(@RequestBody SysRole role) {
        sysRoleService.updateRole(role);
        return Result.success();
    }


    @DeleteMapping("/delete/{ids}")
    @Operation(summary = "批量删除角色")
    @OperationLogger(value = "批量删除角色")
    @SaCheckPermission("sys:role:delete")
    public Result<Void> delete(@PathVariable List<Integer> ids) {
        sysRoleService.delete(ids);
        return Result.success();
    }

    @GetMapping("/menus/{id}")
    @Operation(summary = "获取角色所拥有的权限")
    public Result<List<Integer>> getRoleMenus(@PathVariable Integer id) {
        return Result.success(sysRoleService.getRoleMenus(id));
    }

    @PutMapping("/menus/{id}")
    @Operation(summary = "修改角色权限")
    @OperationLogger(value = "修改角色权限")
    @SaCheckPermission("sys:role:menus")
    public Result<Boolean> updateRoleMenus(@PathVariable Integer id,@RequestBody List<Integer> menuIds) {
        return Result.success(sysRoleService.updateRoleMenus(id,menuIds));
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有角色列表")
    public Result<List<SysRole>> all() {
        return Result.success(sysRoleService.list());
    }
}
