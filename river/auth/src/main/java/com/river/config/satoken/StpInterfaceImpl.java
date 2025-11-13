package com.river.config.satoken;

import cn.dev33.satoken.stp.StpInterface;
import com.river.common.Constants;
import com.river.enums.MenuTypeEnum;
import com.river.mapper.SysMenuMapper;
import com.river.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysMenuMapper menuMapper;

    private final SysRoleMapper roleMapper;


    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<String> roles = roleMapper.selectRolesCodeByUserId(loginId);

        if (roles.contains(Constants.ADMIN)) {
            return menuMapper.getPermissionList(MenuTypeEnum.BUTTON.getCode());
        }
        return menuMapper.getPermissionListByUserId(loginId,MenuTypeEnum.BUTTON.getCode());
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return roleMapper.selectRolesCodeByUserId(loginId);
    }
}
