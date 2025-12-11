package com.river.service.impl;

import com.river.entity.SysConfig;
import com.river.entity.SysSocialConfig;
import com.river.dto.JuHeLoginResponse;
import com.river.service.AuthService;
import com.river.service.JuHeService;
import com.river.service.SocialLoginService;
import com.river.service.SysConfigService;
import com.river.service.SysSocialConfigService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthCallback;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * 统一第三方登录服务实现
 * 根据数据库配置动态切换登录方式
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SocialLoginServiceImpl implements SocialLoginService {

    private final SysSocialConfigService sysSocialConfigService;
    private final SysConfigService sysConfigService;
    private final JuHeService juHeService;
    private final AuthService authService;

    private static final String SOCIAL_LOGIN_ENABLED_KEY = "social_login_enabled";

    @Override
    public Map<String, Object> getLoginUrl(String type) {
        Map<String, Object> result = new HashMap<>();
        String currentMode = getCurrentLoginMode();
        result.put("mode", currentMode);
        result.put("type", type);

        // 检查第三方登录是否开启
        if (!isSocialLoginEnabled()) {
            result.put("error", "第三方登录已禁用");
            return result;
        }

        // 获取该登录类型的配置
        SysSocialConfig config = sysSocialConfigService.selectByType(type.toLowerCase());
        if (config == null || config.getStatus() != 1) {
            result.put("error", "该登录方式未启用: " + type);
            return result;
        }

        if ("juhe".equalsIgnoreCase(currentMode)) {
            // 聚合登录模式
            if (config.getSupportJuhe() != 1) {
                result.put("error", "该登录方式不支持聚合登录: " + type);
                return result;
            }
            Integer juheType = config.getJuheTypeCode();
            if (juheType == null) {
                result.put("error", "聚合登录类型编号未配置: " + type);
                return result;
            }
            JuHeLoginResponse response = juHeService.getJuHeAuth(juheType);
            result.put("loginUrl", response.getLogurl());
            result.put("qrCode", response.getLogqrcode());
            result.put("cxid", response.getCxid());
        } else {
            // OAuth2登录模式
            if (config.getSupportOauth2() != 1) {
                result.put("error", "该登录方式不支持OAuth2: " + type);
                return result;
            }
            if (config.getAppId() == null || config.getAppId().isEmpty()) {
                result.put("error", "OAuth2配置不完整，请先配置AppId: " + type);
                return result;
            }
            String authUrl = authService.renderAuth(type.toLowerCase());
            result.put("loginUrl", authUrl);
        }

        return result;
    }

    @Override
    public void handleCallback(String type, Map<String, String> params, HttpServletResponse response) throws IOException {
        String currentMode = getCurrentLoginMode();

        // 检查第三方登录是否开启
        if (!isSocialLoginEnabled()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "第三方登录已禁用");
            return;
        }

        // 获取该登录类型的配置
        SysSocialConfig config = sysSocialConfigService.selectByType(type.toLowerCase());
        if (config == null || config.getStatus() != 1) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "该登录方式未启用");
            return;
        }

        if ("juhe".equalsIgnoreCase(currentMode)) {
            // 聚合登录回调
            String userUid = params.get("userUid");
            if (userUid != null) {
                juHeService.checkJuHeLogin(userUid, response);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "缺少userUid参数");
            }
        } else {
            // OAuth2回调
            AuthCallback callback = new AuthCallback();
            callback.setCode(params.get("code"));
            callback.setState(params.get("state"));
            callback.setAuth_code(params.get("auth_code"));
            authService.authLogin(callback, type.toLowerCase(), response);
        }
    }

    @Override
    public List<Map<String, Object>> getSupportedLoginTypes() {
        List<Map<String, Object>> types = new ArrayList<>();

        // 检查第三方登录是否开启
        if (!isSocialLoginEnabled()) {
            return types;
        }

        // 从数据库获取当前模式下启用的登录配置
        List<SysSocialConfig> enabledConfigs = sysSocialConfigService.selectEnabledByMode();

        for (SysSocialConfig config : enabledConfigs) {
            Map<String, Object> typeInfo = new HashMap<>();
            typeInfo.put("type", config.getSocialType());
            typeInfo.put("name", config.getSocialName());
            typeInfo.put("icon", config.getIcon());
            if (config.getJuheTypeCode() != null) {
                typeInfo.put("code", config.getJuheTypeCode());
            }
            types.add(typeInfo);
        }

        return types;
    }

    @Override
    public String getCurrentLoginMode() {
        return sysSocialConfigService.getGlobalLoginMode();
    }

    /**
     * 检查第三方登录是否开启
     */
    private boolean isSocialLoginEnabled() {
        SysConfig config = sysConfigService.selectConfigByKey(SOCIAL_LOGIN_ENABLED_KEY);
        return config == null || "Y".equals(config.getConfigValue());
    }
}
