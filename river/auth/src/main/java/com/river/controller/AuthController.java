package com.river.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.river.common.Result;
import com.river.dto.Captcha;
import com.river.dto.EmailRegisterDto;
import com.river.dto.EmailResetDto;
import com.river.dto.LoginDTO;
import com.river.dto.user.*;
import com.river.service.AuthService;
import com.river.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.model.AuthCallback;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Tag(name = "认证管理")
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;


    @Operation(summary = "用户登录")
    @PostMapping("/auth/login")
    public Result<LoginUserInfo> login(@Validated @RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    @GetMapping("/auth/checkUsername")
    @Operation(summary = "检查用户名是否存在 true 表示存在")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        return Result.success(authService.checkUsername(username));
    }

    @GetMapping("/auth/checkEmail")
    @Operation(summary = "检查邮箱是否存在 true 表示存在")
    public Result<Boolean> checkEmail(@RequestParam String email) {
        return Result.success(authService.checkEmail(email));
    }

    @SaIgnore
    @Operation(summary = "获取滑块验证码")
    @GetMapping("/auth/getCaptcha")
    public Result<Captcha> getCaptcha() {
        return Result.success(captchaService.getCaptcha());
    }

    @Operation(summary = "用户登出")
    @PostMapping("/auth/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success(null);
    }

    @Operation(summary = "发送注册邮箱验证码")
    @GetMapping("/auth/getEmailCode")
    public Result<Boolean> getEmailCode(String email) throws MessagingException {
        return Result.success(authService.sendEmailCode(email));
    }

    @Operation(summary = "邮箱账号注册")
    @PostMapping("/auth/register")
    public Result<Boolean> register(@RequestBody EmailRegisterDto dto){
        return Result.success(authService.register(dto));
    }

    @Operation(summary = "根据邮箱修改密码")
    @PostMapping("/auth/forgot")
    public Result<Boolean> forgot(@RequestBody EmailResetDto dto){
        return Result.success(authService.forgot(dto));
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/auth/info")
    public Result<LoginUserInfo> getUserInfo(@RequestParam(defaultValue = "admin") String source) {
        return Result.success(authService.getLoginUserInfo(source));
    }

    @RequestMapping("/auth/render/{source}")
    @Operation(summary= "获取第三方授权地址")
    public Result<String> renderAuth(HttpServletResponse response, @PathVariable String source) {
        return Result.success(authService.renderAuth(source));
    }

    @RequestMapping("/auth/callback/{source}")
    @Operation(summary= "第三方登录回调")
    public void login(AuthCallback callback, @PathVariable String source, HttpServletResponse httpServletResponse) throws IOException {
        authService.authLogin(callback,source,httpServletResponse);
    }
}
