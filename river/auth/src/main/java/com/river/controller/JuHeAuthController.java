package com.river.controller;


import com.river.common.Result;
import com.river.dto.JuHeLoginResponse;
import com.river.entity.SysDictData;
import com.river.service.JuHeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "聚合登录管理")
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

    @GetMapping("/api/juhe/getLoginType")
    @Operation(summary = "获取登录类型")
    public Result<List<String>> getLoginType() {
        return Result.success(juHeService.getLoginType());
    }
}
