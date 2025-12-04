package com.river.service;


import com.river.dto.Captcha;
import com.river.dto.EmailRegisterDto;
import com.river.dto.EmailResetDto;
import com.river.dto.LoginDTO;
import com.river.dto.user.LoginUserInfo;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import me.zhyd.oauth.model.AuthCallback;

import java.io.IOException;

public interface AuthService {

    /**
     * 用户登录
     */
    LoginUserInfo login(LoginDTO loginDTO);

    /**
     * 获取当前登录用户信息
     */
    LoginUserInfo getLoginUserInfo(String source);

    /**
     * 发送注册邮箱验证码
     * @param email
     * @return
     */
    Boolean sendEmailCode(String email) throws MessagingException;

    /**
     * 邮箱账号注册
     * @param dto
     * @return
     */
    Boolean register(EmailRegisterDto dto);

    /**
     * 邮箱账号重置密码
     * @param dto
     * @return
     */
    Boolean forgot(EmailResetDto dto);
    /**
     * 获取第三方授权地址
     * @param source
     * @return
     */
    String renderAuth(String source);

    /**
     * 第三方登录回调
     * @param callback
     * @param source
     * @param httpServletResponse
     */
    void authLogin(AuthCallback callback, String source, HttpServletResponse httpServletResponse) throws IOException ;

    Boolean checkUsername(String username);

    Boolean checkEmail(String email);
}
