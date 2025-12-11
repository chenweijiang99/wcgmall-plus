package com.river.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.river.entity.SysConfig;
import com.river.entity.SysSocialConfig;
import com.river.exception.ServiceException;
import com.river.mapper.SysSocialConfigMapper;
import com.river.service.SysConfigService;
import com.river.service.SysSocialConfigService;
import com.river.utils.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 第三方登录配置表 服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysSocialConfigServiceImpl extends ServiceImpl<SysSocialConfigMapper, SysSocialConfig> implements SysSocialConfigService {

    private final SysConfigService sysConfigService;

    /**
     * 全局登录模式配置key
     */
    private static final String SOCIAL_LOGIN_MODE_KEY = "social_login_mode";

    /**
     * 第三方登录开关配置key
     */
    private static final String SOCIAL_LOGIN_ENABLED_KEY = "social_login_enabled";

    @Override
    public IPage<SysSocialConfig> selectPage(SysSocialConfig config) {
        LambdaQueryWrapper<SysSocialConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(config.getSocialName()), SysSocialConfig::getSocialName, config.getSocialName());
        wrapper.eq(StringUtils.hasText(config.getSocialType()), SysSocialConfig::getSocialType, config.getSocialType());
        wrapper.eq(StringUtils.hasText(config.getLoginMode()), SysSocialConfig::getLoginMode, config.getLoginMode());
        wrapper.eq(config.getStatus() != null, SysSocialConfig::getStatus, config.getStatus());
        wrapper.orderByAsc(SysSocialConfig::getSort);
        return page(PageUtil.getPage(), wrapper);
    }

    @Override
    @Cacheable(cacheNames = "social_config", key = "'all_list'")
    public List<SysSocialConfig> selectAll() {
        LambdaQueryWrapper<SysSocialConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysSocialConfig::getSort);
        return list(wrapper);
    }

    @Override
    public List<SysSocialConfig> selectEnabledByMode() {
        String mode = getGlobalLoginMode();
        LambdaQueryWrapper<SysSocialConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysSocialConfig::getStatus, 1);
        // 根据模式筛选支持该模式的登录方式
        if ("juhe".equalsIgnoreCase(mode)) {
            wrapper.eq(SysSocialConfig::getSupportJuhe, 1);
        } else {
            wrapper.eq(SysSocialConfig::getSupportOauth2, 1);
            // OAuth2模式需要有配置appId
            wrapper.isNotNull(SysSocialConfig::getAppId);
            wrapper.ne(SysSocialConfig::getAppId, "");
        }
        wrapper.orderByAsc(SysSocialConfig::getSort);
        return list(wrapper);
    }

    @Override
    @Cacheable(cacheNames = "social_config", key = "#socialType")
    public SysSocialConfig selectByType(String socialType) {
        return getOne(new LambdaQueryWrapper<SysSocialConfig>()
                .eq(SysSocialConfig::getSocialType, socialType));
    }

    @Override
    @CacheEvict(cacheNames = "social_config", allEntries = true)
    public boolean insert(SysSocialConfig config) {
        // 检查类型是否已存在
        SysSocialConfig exist = selectByType(config.getSocialType());
        if (exist != null) {
            throw new ServiceException("该登录类型配置已存在");
        }
        return save(config);
    }

    @Override
    @CacheEvict(cacheNames = "social_config", allEntries = true)
    public boolean update(SysSocialConfig config) {
        // 检查类型是否已存在（排除自己）
        LambdaQueryWrapper<SysSocialConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysSocialConfig::getSocialType, config.getSocialType());
        wrapper.ne(SysSocialConfig::getId, config.getId());
        SysSocialConfig exist = getOne(wrapper);
        if (exist != null) {
            throw new ServiceException("该登录类型配置已存在");
        }
        return updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "social_config", allEntries = true)
    public boolean deleteByIds(List<Long> ids) {
        return removeByIds(ids);
    }

    @Override
    @CacheEvict(cacheNames = "social_config", allEntries = true)
    public boolean updateStatus(Long id, Integer status) {
        LambdaUpdateWrapper<SysSocialConfig> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysSocialConfig::getId, id);
        wrapper.set(SysSocialConfig::getStatus, status);
        return update(wrapper);
    }

    @Override
    public String getGlobalLoginMode() {
        SysConfig config = sysConfigService.selectConfigByKey(SOCIAL_LOGIN_MODE_KEY);
        return config != null ? config.getConfigValue() : "juhe";
    }

    @Override
    @CacheEvict(cacheNames = "social_config", allEntries = true)
    public boolean setGlobalLoginMode(String mode) {
        SysConfig config = sysConfigService.selectConfigByKey(SOCIAL_LOGIN_MODE_KEY);
        if (config != null) {
            config.setConfigValue(mode);
            sysConfigService.update(config);
        } else {
            config = new SysConfig();
            config.setConfigName("第三方登录模式");
            config.setConfigKey(SOCIAL_LOGIN_MODE_KEY);
            config.setConfigValue(mode);
            config.setConfigType("Y");
            config.setRemark("juhe: 聚合登录; oauth2: OAuth2登录");
            sysConfigService.insert(config);
        }
        return true;
    }

    @Override
    public Map<String, Object> getGlobalSettings() {
        Map<String, Object> settings = new HashMap<>();

        // 获取登录模式
        SysConfig modeConfig = sysConfigService.selectConfigByKey(SOCIAL_LOGIN_MODE_KEY);
        settings.put("loginMode", modeConfig != null ? modeConfig.getConfigValue() : "juhe");

        // 获取是否开启第三方登录
        SysConfig enabledConfig = sysConfigService.selectConfigByKey(SOCIAL_LOGIN_ENABLED_KEY);
        settings.put("enabled", enabledConfig != null ? "Y".equals(enabledConfig.getConfigValue()) : true);

        return settings;
    }

    @Override
    @CacheEvict(cacheNames = "social_config", allEntries = true)
    public boolean updateGlobalSettings(Map<String, Object> settings) {
        // 更新登录模式
        if (settings.containsKey("loginMode")) {
            setGlobalLoginMode(settings.get("loginMode").toString());
        }

        // 更新是否开启第三方登录
        if (settings.containsKey("enabled")) {
            boolean enabled = Boolean.parseBoolean(settings.get("enabled").toString());
            SysConfig config = sysConfigService.selectConfigByKey(SOCIAL_LOGIN_ENABLED_KEY);
            if (config != null) {
                config.setConfigValue(enabled ? "Y" : "N");
                sysConfigService.update(config);
            } else {
                config = new SysConfig();
                config.setConfigName("第三方登录开关");
                config.setConfigKey(SOCIAL_LOGIN_ENABLED_KEY);
                config.setConfigValue(enabled ? "Y" : "N");
                config.setConfigType("Y");
                config.setRemark("Y: 开启; N: 关闭");
                sysConfigService.insert(config);
            }
        }

        return true;
    }
}
