package cn.iocoder.yudao.framework.quartz.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务冲突策略枚举
 * 
 * 定义当任务上次执行未完成时，新的执行请求如何处理
 * 
 * @author IBMS Team
 */
@Getter
@AllArgsConstructor
public enum JobConflictStrategy {
    
    /**
     * 跳过本次执行
     * 
     * 适用场景：
     * - 数据采集类任务（错过一次不影响整体）
     * - 统计任务（下次执行会包含本次数据）
     * 
     * 示例：设备状态检查任务，如果上次还在执行，本次就跳过
     */
    SKIP("跳过", "如果上次任务还在执行，则跳过本次执行"),
    
    /**
     * 加入队列等待
     * 
     * 适用场景：
     * - 必须执行的任务（如：数据备份）
     * - 顺序敏感的任务（如：账单生成）
     * 
     * 示例：数据备份任务，必须保证每次都执行，因此排队等待
     */
    QUEUE("排队", "如果上次任务还在执行，则将本次任务加入队列等待"),
    
    /**
     * 中断旧任务，开始新任务
     * 
     * 适用场景：
     * - 实时性要求高的任务
     * - 新数据比旧数据更重要的场景
     * 
     * 示例：告警推送任务，新的告警优先级更高，中断旧任务
     * 
     * 注意：使用此策略需要确保任务支持中断，且中断不会导致数据不一致
     */
    INTERRUPT("中断", "中断上次执行，开始新的执行"),
    
    /**
     * 允许并发执行
     * 
     * 适用场景：
     * - 无状态任务
     * - 可分片的任务（不同实例处理不同数据）
     * 
     * 示例：场景规则执行任务，不同规则可以并发执行
     * 
     * 注意：需要配合 max_concurrent_count 字段限制最大并发数
     */
    CONCURRENT("并发", "允许多个实例同时执行");
    
    /**
     * 策略名称
     */
    private final String name;
    
    /**
     * 策略描述
     */
    private final String description;
    
    /**
     * 根据名称查找枚举
     * 
     * @param name 枚举名称
     * @return 对应的枚举，如果不存在则返回 null
     */
    public static JobConflictStrategy fromName(String name) {
        if (name == null) {
            return null;
        }
        for (JobConflictStrategy strategy : values()) {
            if (strategy.name().equals(name)) {
                return strategy;
            }
        }
        return null;
    }
}






