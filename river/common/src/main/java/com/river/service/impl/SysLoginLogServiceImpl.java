package com.river.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.river.entity.SysLoginLog;
import com.river.mapper.SysLoginLogMapper;
import com.river.service.SysLoginLogService;
import com.river.utils.IpUtil;
import com.river.utils.PageUtil;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 登录日志服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {

    /**
     * 查询分页列表
     */
    @Override
    public IPage<SysLoginLog> listSysLoginLog(SysLoginLog sysLoginLog) {
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(sysLoginLog.getUsername() != null, SysLoginLog::getUsername, sysLoginLog.getUsername());
        wrapper.eq(sysLoginLog.getStatus() != null, SysLoginLog::getStatus, sysLoginLog.getStatus());
        wrapper.orderByDesc(SysLoginLog::getLoginTime);
        return page(PageUtil.getPage(), wrapper);
    }

    /**
     * 记录登录日志
     */
    @Async
    @Override
    public void recordLoginLog(Long userId, String username, Integer status, String loginType, String message) {
        // 获取请求信息
        HttpServletRequest request = IpUtil.getRequest();
        String ip = IpUtil.getIp();
        String ipSource = IpUtil.getIp2region(ip);

        String browser = "";
        String os = "";
        if (request != null) {
            UserAgent userAgent = IpUtil.getUserAgent(request);
            browser = userAgent.getBrowser().getName();
            os = userAgent.getOperatingSystem().getName();
        }

        SysLoginLog loginLog = SysLoginLog.builder()
                .userId(userId)
                .username(username)
                .ip(ip)
                .ipSource(ipSource)
                .browser(browser)
                .os(os)
                .status(status)
                .loginType(loginType)
                .message(message)
                .loginTime(LocalDateTime.now())
                .build();

        save(loginLog);
    }
}
