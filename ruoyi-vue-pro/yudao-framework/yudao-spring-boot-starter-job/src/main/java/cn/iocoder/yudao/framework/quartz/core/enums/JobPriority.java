package cn.iocoder.yudao.framework.quartz.core.enums;

/**
 * 任务优先级常量
 * 
 * 定义任务的优先级级别，数字越小优先级越高
 * 
 * 使用示例：
 * <pre>
 * public int getPriority() {
 *     return JobPriority.HIGH;
 * }
 * </pre>
 * 
 * @author IBMS Team
 */
public interface JobPriority {
    
    /**
     * 关键任务 - 优先级 1
     * 
     * 适用场景：
     * - 安全告警任务
     * - 设备离线检测
     * - 紧急故障处理
     * 
     * 特点：最高优先级，会优先于所有其他任务执行
     */
    int CRITICAL = 1;
    
    /**
     * 高优先级任务 - 优先级 3
     * 
     * 适用场景：
     * - 实时数据采集
     * - 照明定时控制
     * - 重要业务任务
     * 
     * 特点：高优先级，仅次于关键任务
     */
    int HIGH = 3;
    
    /**
     * 普通优先级任务 - 优先级 5
     * 
     * 适用场景：
     * - 设备健康检查
     * - 环境数据采集
     * - 常规业务任务
     * 
     * 特点：默认优先级，大多数任务使用此级别
     */
    int NORMAL = 5;
    
    /**
     * 低优先级任务 - 优先级 7
     * 
     * 适用场景：
     * - 数据统计任务
     * - 报表生成
     * - 数据备份
     * 
     * 特点：低优先级，在系统负载较低时执行
     */
    int LOW = 7;
    
    /**
     * 后台任务 - 优先级 9
     * 
     * 适用场景：
     * - 数据清理
     * - 数据归档
     * - 日志清理
     * 
     * 特点：最低优先级，在系统空闲时执行
     */
    int BACKGROUND = 9;
    
    /**
     * 验证优先级是否有效
     * 
     * @param priority 优先级值
     * @return true 如果优先级在有效范围内 (1-9)
     */
    static boolean isValid(int priority) {
        return priority >= CRITICAL && priority <= BACKGROUND;
    }
    
    /**
     * 获取优先级描述
     * 
     * @param priority 优先级值
     * @return 优先级描述
     */
    static String getDescription(int priority) {
        switch (priority) {
            case CRITICAL:
                return "关键任务";
            case HIGH:
                return "高优先级";
            case NORMAL:
                return "普通优先级";
            case LOW:
                return "低优先级";
            case BACKGROUND:
                return "后台任务";
            default:
                return "未知优先级";
        }
    }
}






