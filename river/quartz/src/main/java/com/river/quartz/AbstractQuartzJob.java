package com.river.quartz;

import com.river.common.Constants;
import com.river.entity.SysJob;
import com.river.entity.SysJobLog;
import com.river.mapper.SysJobLogMapper;
import com.river.utils.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.LocalDateTime;


/**
 * @author blue
 * @date 2021/12/8
 * @apiNote 抽象quartz调用
 */
public abstract class AbstractQuartzJob implements org.quartz.Job {
    private static final Logger log = LoggerFactory.getLogger(AbstractQuartzJob.class);

    /**
     * 线程本地变量
     */
    private static final ThreadLocal<LocalDateTime> THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public void execute(JobExecutionContext context) {
        SysJob job = new SysJob();
        BeanUtils.copyProperties(context.getMergedJobDataMap().get(ScheduleConstants.TASK_PROPERTIES), job);
        try {
            before(context, job);
            doExecute(context, job);
            after(context, job, null);
        } catch (Exception e) {
            log.error("任务执行异常  - ：", e);
            after(context, job, e);
        }
    }

    /**
     * 执行前
     *
     * @param context 工作执行上下文对象
     * @param job  系统计划任务
     */
    protected void before(JobExecutionContext context, SysJob job) {
        THREAD_LOCAL.set(LocalDateTime.now());
    }

    /**
     * 执行后
     *
     * @param context 工作执行上下文对象
     * @param job  系统计划任务
     */
    protected void after(JobExecutionContext context, SysJob job, Exception e) {
        LocalDateTime startTime = THREAD_LOCAL.get();
        THREAD_LOCAL.remove();
        //redis心跳任务不添加日志
        if (!job.getInvokeTarget().contains("redisTimer")) {
            final SysJobLog jobLog = new SysJobLog();
            jobLog.setJobId(job.getJobId());
            jobLog.setJobName(job.getJobName());
            jobLog.setJobGroup(job.getJobGroup());
            jobLog.setInvokeTarget(job.getInvokeTarget());
            jobLog.setStartTime(startTime);
            jobLog.setStopTime(LocalDateTime.now());
            long runMs = Duration.between(jobLog.getStopTime(), jobLog.getStartTime()).toMillis();
            jobLog.setJobMessage(jobLog.getJobName() + " 总共耗时：" + runMs + "毫秒");
            if (e != null) {
                jobLog.setStatus(Constants.YES);
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw, true));
                String str = sw.toString();
                String errorMsg = StringUtils.substring(str, 0, 2000);
                jobLog.setExceptionInfo(errorMsg);
            } else {
                jobLog.setStatus(Constants.NO);
            }

            // 写入数据库当中
            SpringUtil.getBean(SysJobLogMapper.class).insert(jobLog);
        }

    }

    /**
     * 执行方法，由子类重载
     *
     * @param context 工作执行上下文对象
     * @param job  系统计划任务
     * @throws Exception 执行过程中的异常
     */
    protected abstract void doExecute(JobExecutionContext context, SysJob job) throws Exception;
}
