package cn.iocoder.yudao.module.iot.framework.job;

import cn.iocoder.yudao.module.iot.job.unified.UnifiedJobScheduler;
import cn.iocoder.yudao.module.iot.job.unified.executor.JobExecutor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 任务执行器自动注册配置
 * 
 * <p>功能说明：
 * <ul>
 *   <li>Spring自动扫描所有 {@link JobExecutor} 实现类</li>
 *   <li>启动时自动注册到 {@link UnifiedJobScheduler}</li>
 *   <li>支持动态扩展新的执行器类型</li>
 * </ul>
 * 
 * @author IBMS Team
 */
@Configuration
@Slf4j
public class JobExecutorConfig {
    
    /**
     * Spring自动注入所有 JobExecutor 实现类
     */
    @Autowired(required = false)
    private List<JobExecutor> executors;
    
    /**
     * 注入统一调度器
     */
    @Resource
    private UnifiedJobScheduler scheduler;
    
    /**
     * 应用启动时自动注册所有执行器
     */
    @PostConstruct
    public void registerExecutors() {
        if (executors == null || executors.isEmpty()) {
            log.warn("【任务执行器】未找到任何 JobExecutor 实现类，请检查配置！");
            return;
        }
        
        log.info("【任务执行器】开始注册执行器...");
        
        int successCount = 0;
        for (JobExecutor executor : executors) {
            try {
                String jobType = executor.getJobType();
                scheduler.registerExecutor(jobType, executor);
                successCount++;
                log.info("【任务执行器】✅ 注册成功: {} -> {}", 
                        jobType, executor.getClass().getSimpleName());
            } catch (Exception e) {
                log.error("【任务执行器】❌ 注册失败: {}", executor.getClass().getSimpleName(), e);
            }
        }
        
        log.info("【任务执行器】注册完成！总数: {}, 成功: {}, 失败: {}", 
                executors.size(), successCount, executors.size() - successCount);
    }
}






