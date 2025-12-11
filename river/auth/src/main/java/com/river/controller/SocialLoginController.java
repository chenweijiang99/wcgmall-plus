package com.river.controller;

import com.river.common.Result;
import com.river.service.SocialLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一第三方登录控制器
 * 根据配置自动切换聚合登录和OAuth2登录
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/social")
@Tag(name = "统一第三方登录")
public class SocialLoginController {

    private final SocialLoginService socialLoginService;

    @GetMapping("/login/{type}")
    @Operation(summary = "获取第三方登录链接", description = "根据配置返回聚合登录或OAuth2登录链接")
    public Result<Map<String, Object>> getLoginUrl(@PathVariable String type) {
        return Result.success(socialLoginService.getLoginUrl(type));
    }

    @GetMapping("/callback/{type}")
    @Operation(summary = "第三方登录回调", description = "处理聚合登录或OAuth2登录回调")
    public void handleCallback(
            @PathVariable String type,
            @RequestParam(required = false) String userUid,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String auth_code,
            HttpServletResponse response) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("userUid", userUid);
        params.put("code", code);
        params.put("state", state);
        params.put("auth_code", auth_code);
        socialLoginService.handleCallback(type, params, response);
    }

    @GetMapping("/types")
    @Operation(summary = "获取支持的第三方登录类型列表")
    public Result<List<Map<String, Object>>> getSupportedTypes() {
        return Result.success(socialLoginService.getSupportedLoginTypes());
    }

    @GetMapping("/mode")
    @Operation(summary = "获取当前登录模式", description = "返回 juhe 或 oauth2")
    public Result<String> getCurrentMode() {
        return Result.success(socialLoginService.getCurrentLoginMode());
    }
}
