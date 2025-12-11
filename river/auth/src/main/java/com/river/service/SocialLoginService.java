package com.river.service;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 统一第三方登录服务接口
 * 用于统一处理聚合登录和OAuth2登录
 */
public interface SocialLoginService {

    /**
     * 获取第三方登录链接
     * @param type 登录类型（如：qq, weibo, gitee, github 等）
     * @return 包含登录链接的响应
     */
    Map<String, Object> getLoginUrl(String type);

    /**
     * 处理登录回调
     * @param type 登录类型
     * @param params 回调参数
     * @param response HTTP响应
     */
    void handleCallback(String type, Map<String, String> params, HttpServletResponse response) throws IOException;

    /**
     * 获取支持的登录类型列表
     * @return 登录类型列表
     */
    List<Map<String, Object>> getSupportedLoginTypes();

    /**
     * 获取当前使用的登录方式
     * @return juhe 或 oauth2
     */
    String getCurrentLoginMode();
}
