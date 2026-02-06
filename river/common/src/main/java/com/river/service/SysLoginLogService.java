package com.river.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.river.entity.SysLoginLog;

/**
 * 登录日志服务接口
 */
public interface SysLoginLogService extends IService<SysLoginLog> {
    /**
     * 查询分页列表
     */
    IPage<SysLoginLog> listSysLoginLog(SysLoginLog sysLoginLog);

    /**
     * 记录登录日志
     *
     * @param userId    用户ID
     * @param username  用户名
     * @param status    登录状态（0成功 1失败）
     * @param loginType 登录方式
     * @param message   提示消息
     */
    void recordLoginLog(Long userId, String username, Integer status, String loginType, String message);
}
