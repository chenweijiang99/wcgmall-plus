package com.river.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.river.entity.SysSiteConfig;

import java.util.List;
import java.util.Map;

/**
 * 网站配置表 服务接口
 */
public interface SysSiteConfigService extends IService<SysSiteConfig> {

    /**
     * 根据配置键获取配置
     */
    SysSiteConfig selectByKey(String configKey);

    /**
     * 根据配置键获取配置值
     */
    String getValueByKey(String configKey);

    /**
     * 获取所有配置（Map形式）
     */
    Map<String, String> getAllConfigMap();

    /**
     * 根据分组获取配置
     */
    List<SysSiteConfig> selectByGroup(String configGroup);

    /**
     * 批量更新配置
     */
    boolean batchUpdate(Map<String, String> configs);

    /**
     * 更新单个配置
     */
    boolean updateByKey(String configKey, String configValue);
}
