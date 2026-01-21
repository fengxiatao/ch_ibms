package cn.iocoder.yudao.framework.quartz.core.handler;

import cn.iocoder.yudao.framework.quartz.core.enums.JobBusinessType;
import cn.iocoder.yudao.framework.quartz.core.enums.JobPriority;

/**
 * 业务任务处理器接口
 * 
 * 所有业务定时任务都应该实现此接口，而不是直接实现 {@link JobHandler}
 * 
 * 此接口扩展了基础的 JobHandler，增加了业务相关的属性：
 * - 业务类型：用于分类和管理
 * - 优先级：用于任务调度
 * - 并发控制：是否允许并发执行
 * - 分组：用于资源隔离
 * 
 * <p>使用示例：
 * <pre>
 * &#64;Component
 * public class MyDeviceCheckJob implements BusinessJobHandler {
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
 *     public String execute(String param) throws Exception {
 *         // 实现业务逻辑
 *         return "执行成功";
 *     }
 * }
 * </pre>
 * 
 * @author IBMS Team
 * @see JobHandler
 * @see JobBusinessType
 * @see JobPriority
 */
public interface BusinessJobHandler extends JobHandler {
    
    /**
     * 获取任务业务类型
     * 
     * <p>业务类型用于：
     * <ul>
     *   <li>任务分类和管理</li>
     *   <li>监控统计</li>
     *   <li>权限控制</li>
     * </ul>
     * 
     * @return 业务类型枚举
     */
    JobBusinessType getBusinessType();
    
    /**
     * 获取任务优先级
     * 
     * <p>优先级范围：1-9，数字越小优先级越高
     * <ul>
     *   <li>1 - 关键任务（如：安全告警）</li>
     *   <li>3 - 高优先级（如：实时数据采集）</li>
     *   <li>5 - 普通优先级（默认值）</li>
     *   <li>7 - 低优先级（如：统计任务）</li>
     *   <li>9 - 后台任务（如：数据清理）</li>
     * </ul>
     * 
     * @return 优先级值，默认为 {@link JobPriority#NORMAL}
     */
    default int getPriority() {
        return JobPriority.NORMAL;
    }
    
    /**
     * 是否支持并发执行
     * 
     * <p>如果返回 false，则同一时间只能有一个实例在执行
     * <p>如果返回 true，则可以同时执行多个实例（受 maxConcurrentCount 限制）
     * 
     * <p>适合并发执行的场景：
     * <ul>
     *   <li>无状态任务</li>
     *   <li>可分片的任务（不同实例处理不同数据）</li>
     *   <li>不涉及资源竞争的任务</li>
     * </ul>
     * 
     * <p>不适合并发执行的场景：
     * <ul>
     *   <li>涉及全局状态修改的任务</li>
     *   <li>有资源竞争的任务</li>
     *   <li>顺序敏感的任务</li>
     * </ul>
     * 
     * @return true 如果支持并发执行，false 否则（默认）
     */
    default boolean isConcurrent() {
        return false;
    }
    
    /**
     * 获取任务分组
     * 
     * <p>任务分组用于资源隔离，同一分组的任务共享同一个资源池（线程池、数据库连接池等）
     * <p>默认使用业务类型的分组
     * 
     * <p>系统预定义的分组：
     * <ul>
     *   <li>iot - IoT 物联网模块</li>
     *   <li>lighting - 智能照明模块</li>
     *   <li>spatial - 空间管理模块</li>
     *   <li>energy - 能源管理模块</li>
     *   <li>security - 安防系统模块</li>
     *   <li>system - 系统管理模块</li>
     * </ul>
     * 
     * @return 任务分组名称
     */
    default String getJobGroup() {
        return getBusinessType().getGroup();
    }
    
    /**
     * 获取最大并发数
     * 
     * <p>仅当 {@link #isConcurrent()} 返回 true 时有效
     * <p>限制同一时间最多可以执行多少个实例
     * 
     * @return 最大并发数，默认为 1
     */
    default Integer getMaxConcurrentCount() {
        return 1;
    }
    
    /**
     * 获取冲突策略
     * 
     * <p>定义当任务上次执行未完成时，新的执行请求如何处理
     * 
     * <p>可选策略：
     * <ul>
     *   <li>SKIP - 跳过本次执行（默认）</li>
     *   <li>QUEUE - 加入队列等待</li>
     *   <li>INTERRUPT - 中断旧任务，开始新任务</li>
     *   <li>CONCURRENT - 允许并发执行</li>
     * </ul>
     * 
     * @return 冲突策略名称
     */
    default String getConflictStrategy() {
        return "SKIP";
    }
    
    /**
     * 获取依赖的任务
     * 
     * <p>任务依赖关系，只有依赖的任务都执行完成后，本任务才会执行
     * <p>格式：逗号分隔的任务ID列表，如 "101,102,103"
     * 
     * @return 依赖的任务ID列表，null 表示无依赖
     */
    default String getDependJobs() {
        return null;
    }
    
    /**
     * 获取资源限制
     * 
     * <p>JSON 格式的资源限制配置
     * <p>示例：{"maxMemory": 512, "maxCpu": 50, "maxDuration": 300000}
     * 
     * <ul>
     *   <li>maxMemory - 最大内存限制（MB）</li>
     *   <li>maxCpu - 最大CPU使用率（%）</li>
     *   <li>maxDuration - 最大执行时长（毫秒）</li>
     * </ul>
     * 
     * @return JSON 格式的资源限制配置，null 表示无限制
     */
    default String getResourceLimit() {
        return null;
    }
}

