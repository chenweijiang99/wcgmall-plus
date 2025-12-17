package com.river.config.websocket;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket握手拦截器
 */
@Slf4j
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            if (request instanceof ServletServerHttpRequest servletRequest) {
                // 从请求参数获取token
                String token = servletRequest.getServletRequest().getParameter("token");
                if (token != null && !token.isEmpty()) {
                    // 使用Sa-Token验证token
                    Object loginId = StpUtil.getLoginIdByToken(token);
                    if (loginId != null) {
                        // 解析用户信息 (格式: userType:userId)
                        String[] parts = loginId.toString().split(":");
                        if (parts.length == 2) {
                            attributes.put("userType", Integer.parseInt(parts[0]));
                            attributes.put("userId", Long.parseLong(parts[1]));
                            log.info("WebSocket握手成功: userType={}, userId={}", parts[0], parts[1]);
                            return true;
                        }
                        // 兼容只有userId的情况
                        attributes.put("userType", 1);
                        attributes.put("userId", Long.parseLong(loginId.toString()));
                        log.info("WebSocket握手成功: userId={}", loginId);
                        return true;
                    }
                }
            }
            log.warn("WebSocket握手失败: token无效");
            return false;
        } catch (Exception e) {
            log.error("WebSocket握手异常", e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手完成后的处理
    }
}
