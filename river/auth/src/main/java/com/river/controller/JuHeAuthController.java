package com.mojian.controller;

import com.mojian.dto.JuHeLoginResponse;
import com.mojian.dto.user.LoginUserInfo;
import com.mojian.service.JuHeService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Api(tags = "聚合登录管理")
public class JuHeAuthController {

    private final JuHeService juHeService;

    @GetMapping("/api/juhe/getJuHeAuth/{type}")
    public JuHeLoginResponse getJuHeAuth(@PathVariable Integer type) {
        return juHeService.getJuHeAuth(type);
    }
    @GetMapping("/api/juhe/checkLogin/{userUid}")
    public void checkLogin(@PathVariable String userUid, HttpServletResponse httpServletResponse) throws IOException {
       juHeService.checkJuHeLogin(userUid,httpServletResponse);
    }
}
