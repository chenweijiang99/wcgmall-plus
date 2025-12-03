package com.river.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.river.entity.SysUser;
import com.river.vo.user.SysUserVo;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    SysUser selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    SysUser selectByEmail(@Param("email") String email);

    IPage<SysUserVo> selectUserPage(@Param("page") Page<Object> page, @Param("sysUser") SysUser sysUser);

    /**
     * 查询最近7天用户增长趋势
     * @return 用户增长数据
     */
    @MapKey("date")
    List<Map<String, Object>> selectUserGrowth();

    /**
     * 查询今日新增用户数量
     * @return 今日新增用户数量
     */
    Long selectTodayNewUsers();
}
