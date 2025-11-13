package com.river.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.river.entity.SysJob;
import com.river.exception.ServiceException;
import com.river.mapper.SysJobMapper;
import com.river.quartz.ScheduleConstants;
import com.river.quartz.TaskException;
import com.river.service.SysJobService;
import com.river.utils.CronUtils;
import com.river.utils.PageUtil;
import com.river.utils.ScheduleUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

/**
 * 定时任务调度表 服务实现类
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SysJobServiceImpl extends ServiceImpl<SysJobMapper, SysJob> implements SysJobService {

    private final Scheduler scheduler;


    /**
     * 项目启动时，初始化定时器 主要是防止手动修改数据库导致未同步到定时任务处理
     * （注：不能手动修改数据库ID和任务组名，否则会导致脏数据）
     */
    @PostConstruct
    public void init() throws SchedulerException, TaskException {
        scheduler.clear();
        List<SysJob> jobList = baseMapper.selectList(null);
        for (SysJob job : jobList) {
            ScheduleUtils.createScheduleJob(scheduler, job);
        }
    }

    /**
     * 任务列表
     *
     * @return
     */
    @Override
    public Page<SysJob> selectJobPage(String jobName, String jobGroup, String status) {
        LambdaQueryWrapper<SysJob> queryWrapper = new LambdaQueryWrapper<SysJob>()
                .like(StringUtils.isNotBlank(jobName), SysJob::getJobName,jobName)
                .eq(StringUtils.isNotBlank(jobGroup), SysJob::getJobGroup,jobGroup)
                .eq(StringUtils.isNotBlank(status), SysJob::getStatus,status);

        return page(PageUtil.getPage(),queryWrapper);
    }

    /**
     * 任务详情
     *
     * @param jobId
     * @return
     */
    @Override
    public SysJob selectJobById(Long jobId) {
        SysJob job = baseMapper.selectById(jobId);
        Date nextExecution = CronUtils.getNextExecution(job.getCronExpression());
        job.setNextValidTime(nextExecution);
        return job;
    }

    /**
     * 新增任务
     *
     * @param job 调度信息
     * @return
     * @throws SchedulerException
     */
    @Transactional(rollbackFor = Exception.class)
    public SysJob addJob(SysJob job) throws SchedulerException, TaskException {
        checkCronIsValid(job);

        baseMapper.insert(job);
        ScheduleUtils.createScheduleJob(scheduler, job);
        return job;
    }


    @Transactional(rollbackFor = Exception.class)
    public SysJob updateJob(SysJob job) throws SchedulerException, TaskException {
        checkCronIsValid(job);

        SysJob properties = baseMapper.selectById(job.getJobId());
        baseMapper.updateById(job);
        updateSchedulerJob(job, properties.getJobGroup());

        return job;
    }


    /**
     * 批量删除任务
     *
     * @param ids
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteJob(List<Long> ids) {
        for (Long jobId : ids) {
            SysJob job = baseMapper.selectById(jobId);
            try {
                scheduler.deleteJob(ScheduleUtils.getJobKey(job.getJobId(), job.getJobGroup()));
            } catch (SchedulerException e) {
                throw new ServiceException("任务删除失败");
            }
        }
        baseMapper.deleteBatchIds(ids);

    }

    /**
     * 暂停任务
     *
     * @param job 调度信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pauseJob(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        job.setStatus(ScheduleConstants.Status.PAUSE.getValue());
        baseMapper.updateById(job);
        scheduler.pauseJob(ScheduleUtils.getJobKey(jobId, jobGroup));
    }

    /**
     * 立即运行任务
     *
     * @param job 调度信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void runJob(SysJob job) {
        try {
            Long jobId = job.getJobId();
            String jobGroup = job.getJobGroup();
            SysJob properties = selectJobById(job.getJobId());
            // 参数
            JobDataMap dataMap = new JobDataMap();
            dataMap.put(ScheduleConstants.TASK_PROPERTIES, properties);
            JobKey jobKey = ScheduleUtils.getJobKey(jobId, jobGroup);
            if (scheduler.checkExists(jobKey))
            {
                scheduler.triggerJob(jobKey, dataMap);
            }
        } catch (Exception e) {
            throw new ServiceException("定时任务运行失败！失败原因:" + e.getMessage());
        }
    }

    /**
     * 任务调度状态修改
     *
     * @param job 调度信息
     */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(SysJob job) throws SchedulerException {
        String status = job.getStatus();
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        int row = baseMapper.updateById(job);
        if (row > 0){
            if (ScheduleConstants.Status.NORMAL.getValue().equals(status)) {
                scheduler.resumeJob(ScheduleUtils.getJobKey(jobId, jobGroup));
            } else if (ScheduleConstants.Status.PAUSE.getValue().equals(status)) {
                scheduler.pauseJob(ScheduleUtils.getJobKey(jobId, jobGroup));
            }
        }
    }


    /**
     * 验证cron
     * @param job
     */
    private void checkCronIsValid(SysJob job) {
        boolean valid = CronUtils.isValid(job.getCronExpression());
        if (!valid){
            throw new ServiceException("Cron表达式验证失败！");
        }
    }

    /**
     * 更新任务
     * @param job      任务对象
     * @param jobGroup 任务组名
     */
    public void updateSchedulerJob(SysJob job, String jobGroup) throws SchedulerException, TaskException {
        Long jobId = job.getJobId();
        // 判断是否存在
        JobKey jobKey = ScheduleUtils.getJobKey(jobId, jobGroup);
        if (scheduler.checkExists(jobKey)) {
            // 防止创建时存在数据问题 先移除，然后在执行创建操作
            scheduler.deleteJob(jobKey);
        }
        ScheduleUtils.createScheduleJob(scheduler, job);
    }
}
