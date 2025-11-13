package com.river.service;

import com.river.dto.Captcha;

public interface CaptchaService {
    /**
     * 获取验证码
     */
    Captcha getCaptcha();

    // 校验验证码方法在CaptchaUtil类中，方便AuthServiceImpl类调用
}
