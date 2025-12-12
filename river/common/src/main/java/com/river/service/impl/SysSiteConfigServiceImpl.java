package com.river.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.river.entity.SysSiteConfig;
import com.river.mapper.SysSiteConfigMapper;
import com.river.service.SysSiteConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网站配置表 服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysSiteConfigServiceImpl extends ServiceImpl<SysSiteConfigMapper, SysSiteConfig> implements SysSiteConfigService {

    private static final String CACHE_NAME = "siteConfig";

    @Override
    @Cacheable(value = CACHE_NAME, key = "#configKey")
    public SysSiteConfig selectByKey(String configKey) {
        return this.getOne(new LambdaQueryWrapper<SysSiteConfig>()
                .eq(SysSiteConfig::getConfigKey, configKey));
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'value:' + #configKey")
    public String getValueByKey(String configKey) {
        SysSiteConfig config = selectByKey(configKey);
        return config != null ? config.getConfigValue() : "";
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'allMap'")
    public Map<String, String> getAllConfigMap() {
        List<SysSiteConfig> list = this.list();
        Map<String, String> result = new HashMap<>();
        for (SysSiteConfig config : list) {
            result.put(config.getConfigKey(), config.getConfigValue() != null ? config.getConfigValue() : "");
        }
        return result;
    }

    @Override
    public List<SysSiteConfig> selectByGroup(String configGroup) {
        return this.list(new LambdaQueryWrapper<SysSiteConfig>()
                .eq(SysSiteConfig::getConfigGroup, configGroup)
                .orderByAsc(SysSiteConfig::getSort));
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public boolean batchUpdate(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            updateByKey(entry.getKey(), entry.getValue());
        }
        return true;
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public boolean updateByKey(String configKey, String configValue) {
        return this.update(new LambdaUpdateWrapper<SysSiteConfig>()
                .eq(SysSiteConfig::getConfigKey, configKey)
                .set(SysSiteConfig::getConfigValue, configValue));
    }
}
