package com.river.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.river.entity.SysRole;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {
    /**
     * 分页查询角色
     */
    IPage<SysRole> listRoles(String name);

    /**
     * 新增角色
     */
    void addRole(SysRole role);

    /**
     * 更新角色
     */
    void updateRole(SysRole role);

    /**
     * 删除角色
     */
    void delete(List<Integer> ids);


    /**
     * 获取角色所拥有的菜单权限
     * @param id
     * @return
     */
    List<Integer> getRoleMenus(Integer id);

    /**
     * 分配角色权限
     * @param id
     * @param menuIds
     * @return
     */
    Boolean updateRoleMenus(Integer id, List<Integer> menuIds);

}
