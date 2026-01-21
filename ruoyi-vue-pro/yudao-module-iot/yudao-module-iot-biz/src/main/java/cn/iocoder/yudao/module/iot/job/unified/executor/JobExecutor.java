package cn.iocoder.yudao.module.iot.job.unified.executor;

import cn.iocoder.yudao.module.iot.job.unified.model.ScheduledTask;

/**
 * 任务执行器接口
 * 
 * <p>每种任务类型对应一个执行器实现
 * 
 * @author IBMS Team
 */
public interface JobExecutor {
    
    /**
     * 执行任务
     * 
     * @param task 任务信息
     * @return 执行结果
     * @throws Exception 执行异常
     */
    String execute(ScheduledTask task) throws Exception;
    
    /**
     * 获取支持的任务类型
     * 
     * @return 任务类型
     */
    String getJobType();
}






