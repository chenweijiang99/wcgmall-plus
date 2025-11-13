package com.river.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.river.entity.SysJob;
import com.river.quartz.TaskException;
import org.quartz.SchedulerException;

import java.util.List;

/**
 * 定时任务调度表 服务类
 */
public interface SysJobService extends IService<SysJob> {

    /**
     * 分页
     *
     * @param jobName
     * @param jobGroup
     * @param status
     * @return
     */
    Page<SysJob> selectJobPage(String jobName, String jobGroup, String status);

    /**
     * 详情
     *
     * @param jobId
     * @return
     */
    SysJob selectJobById(Long jobId);

    /**
     * 添加
     * @param job
     * @return
     * @throws SchedulerException
     * @throws TaskException
     */
    SysJob addJob(SysJob job) throws SchedulerException, TaskException, TaskException;

    /**
     * 修改
     * @param job
     * @return
     * @throws SchedulerException
     * @throws TaskException
     */
    SysJob updateJob(SysJob job) throws SchedulerException, TaskException;

    /**
     * 删除
     * @param ids
     * @return
     */
    void deleteJob(List<Long> ids);


    /**
     * 立即执行
     * @param job
     * @return
     */
    void runJob(SysJob job);

    /**
     * 修改状态
     * @param job
     * @return
     * @throws SchedulerException
     */
    void changeStatus(SysJob job) throws SchedulerException;


    /**
     * 暂停任务
     * @param job
     * @return
     */
    void pauseJob(SysJob job) throws SchedulerException;

}
