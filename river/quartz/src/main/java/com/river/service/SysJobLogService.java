package com.river.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.river.dto.JobLogQuery;
import com.river.entity.SysJobLog;

public interface SysJobLogService extends IService<SysJobLog> {

    /**
     * 获取任务日志列表
     */
    Page<SysJobLog> selectJobLogPage(JobLogQuery query);

    /**
     * 新增任务日志
     */
    void addJobLog(SysJobLog jobLog);

    /**
     * 清空任务日志
     */
    void cleanJobLog();
} 