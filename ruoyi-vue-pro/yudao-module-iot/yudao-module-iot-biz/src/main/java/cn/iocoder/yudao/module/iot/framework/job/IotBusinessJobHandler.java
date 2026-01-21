package cn.iocoder.yudao.module.iot.framework.job;

import cn.iocoder.yudao.framework.quartz.core.handler.BusinessJobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 业务任务基类
 * 
 * <p>所有 IoT 相关的定时任务都应该继承此类，而不是直接实现 JobHandler
 * 
 * <p>提供的功能：
 * <ul>
 *   <li>统一的执行流程（日志记录、异常处理）</li>
 *   <li>多租户支持（@TenantJob）</li>
 *   <li>执行时间统计</li>
 *   <li>自动设置任务分组为 "iot"</li>
 * </ul>
 * 
 * <p>使用示例：
 * <pre>
 * &#64;Component
 * public class MyDeviceCheckJob extends IotBusinessJobHandler {
 *     
 *     &#64;Override
 *     public JobBusinessType getBusinessType() {
 *         return JobBusinessType.IOT_DEVICE_HEALTH_CHECK;
 *     }
 *     
 *     &#64;Override
 *     public int getPriority() {
 *         return JobPriority.HIGH;
 *     }
 *     
 *     &#64;Override
 *     protected String doExecute(String param) throws Exception {
 *         // 实现具体的业务逻辑
 *         return "执行成功";
 *     }
 * }
 * </pre>
 * 
 * @author IBMS Team
 * @see BusinessJobHandler
 */
@Slf4j
public abstract class IotBusinessJobHandler implements BusinessJobHandler {
    
    /**
     * 任务分组固定为 "iot"
     * 
     * @return "iot"
     */
    @Override
    public final String getJobGroup() {
        return "iot";
    }
    
    /**
     * 默认优先级：普通优先级 (5)
     * 
     * <p>子类可以覆盖此方法以设置不同的优先级
     * 
     * @return 优先级，默认为 5（普通优先级）
     */
    @Override
    public int getPriority() {
        return 5; // JobPriority.NORMAL
    }
    
    /**
     * 默认不允许并发执行
     * 
     * <p>子类可以覆盖此方法以允许并发
     * 
     * @return false（默认不允许并发）
     */
    @Override
    public boolean isConcurrent() {
        return false;
    }
    
    /**
     * 默认冲突策略：跳过
     * 
     * <p>子类可以覆盖此方法以设置不同的冲突策略
     * 
     * @return "SKIP"（默认跳过）
     */
    @Override
    public String getConflictStrategy() {
        return "SKIP";
    }
    
    /**
     * 默认最大并发数：1
     * 
     * <p>仅当 isConcurrent() 返回 true 时有效
     * <p>子类可以覆盖此方法以设置不同的最大并发数
     * 
     * @return 1（默认最大并发数为1）
     */
    @Override
    public Integer getMaxConcurrentCount() {
        return 1;
    }
    
    /**
     * 默认无依赖任务
     * 
     * <p>子类可以覆盖此方法以设置依赖任务
     * 
     * @return null（默认无依赖）
     */
    @Override
    public String getDependJobs() {
        return null;
    }
    
    /**
     * 默认无资源限制
     * 
     * <p>子类可以覆盖此方法以设置资源限制
     * 
     * @return null（默认无资源限制）
     */
    @Override
    public String getResourceLimit() {
        return null;
    }
    
    /**
     * 执行任务（模板方法）
     * 
     * <p>此方法实现了任务执行的标准流程：
     * <ol>
     *   <li>记录开始日志</li>
     *   <li>调用子类实现的 doExecute() 方法</li>
     *   <li>记录成功日志和执行时间</li>
     *   <li>捕获异常并记录错误日志</li>
     * </ol>
     * 
     * <p>子类应该实现 {@link #doExecute(String)} 方法，而不是覆盖此方法
     * 
     * @param param 任务参数（通常是 JSON 格式）
     * @return 执行结果（建议返回 JSON 格式）
     * @throws Exception 执行过程中的异常
     */
    @Override
    @TenantJob  // 支持多租户
    public final String execute(String param) throws Exception {
        long startTime = System.currentTimeMillis();
        
        try {
            // 记录开始日志
            log.info("[IoT任务] 开始执行: {}, 业务类型: {}, 参数: {}", 
                     this.getClass().getSimpleName(),
                     getBusinessType().getDescription(), 
                     param);
            
            // 执行业务逻辑（由子类实现）
            String result = doExecute(param);
            
            // 计算执行时间
            long duration = System.currentTimeMillis() - startTime;
            
            // 记录成功日志
            log.info("[IoT任务] 执行成功: {}, 业务类型: {}, 耗时: {}ms, 结果: {}", 
                     this.getClass().getSimpleName(),
                     getBusinessType().getDescription(), 
                     duration, 
                     truncateResult(result));
            
            return result;
            
        } catch (Exception e) {
            // 计算执行时间
            long duration = System.currentTimeMillis() - startTime;
            
            // 记录错误日志
            log.error("[IoT任务] 执行失败: {}, 业务类型: {}, 耗时: {}ms, 错误: {}", 
                      this.getClass().getSimpleName(),
                      getBusinessType().getDescription(), 
                      duration, 
                      e.getMessage(), 
                      e);
            
            // 重新抛出异常，让框架进行重试处理
            throw e;
        }
    }
    
    /**
     * 执行具体的业务逻辑（由子类实现）
     * 
     * <p>子类必须实现此方法，完成具体的业务功能
     * 
     * <p>实现建议：
     * <ul>
     *   <li>幂等性：任务可以重复执行而不产生副作用</li>
     *   <li>超时控制：避免长时间阻塞</li>
     *   <li>批量处理：大数据量要分批处理</li>
     *   <li>异常处理：捕获异常，避免单个失败影响整体</li>
     *   <li>返回结果：建议返回 JSON 格式，便于日志查看</li>
     * </ul>
     * 
     * @param param 任务参数（通常是 JSON 格式的字符串）
     * @return 执行结果（建议返回 JSON 格式的字符串）
     * @throws Exception 执行过程中的异常
     */
    protected abstract String doExecute(String param) throws Exception;
    
    /**
     * 截断返回结果（用于日志输出）
     * 
     * <p>如果结果太长，只记录前200个字符，避免日志过大
     * 
     * @param result 原始结果
     * @return 截断后的结果
     */
    private String truncateResult(String result) {
        if (result == null) {
            return "null";
        }
        
        if (result.length() <= 200) {
            return result;
        }
        
        return result.substring(0, 200) + "... (总长度: " + result.length() + ")";
    }
}

