package com.river.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.river.entity.SysSocialConfig;

import java.util.List;
import java.util.Map;

/**
 * 第三方登录配置表 服务接口
 */
public interface SysSocialConfigService extends IService<SysSocialConfig> {

    /**
     * 查询第三方登录配置分页列表
     */
    IPage<SysSocialConfig> selectPage(SysSocialConfig config);

    /**
     * 查询所有配置列表（不分页）
     */
    List<SysSocialConfig> selectAll();

    /**
     * 根据当前登录模式获取启用的登录方式列表
     */
    List<SysSocialConfig> selectEnabledByMode();

    /**
     * 根据登录类型查询配置
     */
    SysSocialConfig selectByType(String socialType);

    /**
     * 新增第三方登录配置
     */
    boolean insert(SysSocialConfig config);

    /**
     * 修改第三方登录配置
     */
    boolean update(SysSocialConfig config);

    /**
     * 批量删除第三方登录配置
     */
    boolean deleteByIds(List<Long> ids);

    /**
     * 更新状态
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 获取全局登录模式（juhe/oauth2）
     */
    String getGlobalLoginMode();

    /**
     * 设置全局登录模式
     */
    boolean setGlobalLoginMode(String mode);

    /**
     * 获取第三方登录全局设置
     */
    Map<String, Object> getGlobalSettings();

    /**
     * 更新第三方登录全局设置
     */
    boolean updateGlobalSettings(Map<String, Object> settings);
}
