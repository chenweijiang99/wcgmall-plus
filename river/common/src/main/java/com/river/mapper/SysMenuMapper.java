package com.river.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.river.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    /**
     * 获取用户菜单列表
     */
    List<SysMenu> selectMenusByUserId(@Param("userId") Integer userId);

    /**
     * 获取当前登录用户所拥有的权限
     * @param userId
     * @return
     */
    List<String> getPermissionListByUserId(@Param("userId") Object userId,@Param("type") String type);

    /**
     * 获取所有权限
     * @return
     */
    List<String> getPermissionList(String type);

    /**
     * 根据用户id获取菜单
     * @param userId
     * @return
     */
    List<SysMenu> getMenusByUserId(@Param("userId") int userId,@Param("type") String type);

    List<Integer> selectIds(LambdaQueryWrapper<SysMenu> eq);
}