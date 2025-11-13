package com.river.quartz;

import com.river.entity.SysJob;
import com.river.utils.JobInvokeUtils;
import org.quartz.JobExecutionContext;

/**
 * @author blue
 * @date 2021/12/8
 * @apiNote 定时任务处理（允许并发执行）
 */
public class QuartzJobExecution extends AbstractQuartzJob {
    @Override
    protected void doExecute(JobExecutionContext context, SysJob job) throws Exception {
        JobInvokeUtils.invokeMethod(job);
    }
}
