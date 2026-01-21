package cn.iocoder.yudao.framework.quartz.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务业务类型枚举
 * 
 * 用于分类和管理不同业务模块的定时任务
 * 
 * @author IBMS Team
 */
@Getter
@AllArgsConstructor
public enum JobBusinessType {
    
    // ========== IoT 设备类 ==========
    /**
     * 设备离线检查
     * 定期检测设备在线状态，标记离线设备
     */
    IOT_DEVICE_OFFLINE_CHECK("iot_device", "设备离线检查", "iot", 3),
    
    /**
     * 设备健康检查
     * 检查设备运行状态、硬件状态等
     */
    IOT_DEVICE_HEALTH_CHECK("iot_device", "设备健康检查", "iot", 5),
    
    /**
     * 设备数据采集
     * 定时采集设备的传感器数据、状态数据等
     */
    IOT_DEVICE_DATA_COLLECT("iot_device", "设备数据采集", "iot", 5),
    
    /**
     * 设备状态同步
     * 同步设备状态到第三方平台
     */
    IOT_DEVICE_STATUS_SYNC("iot_device", "设备状态同步", "iot", 5),
    
    /**
     * OTA升级
     * 固件空中升级任务调度
     */
    IOT_OTA_UPGRADE("iot_device", "OTA升级", "iot", 5),
    
    /**
     * 长辉设备升级监控
     * 监控长辉TCP设备的固件升级任务状态，处理4G网络不稳定导致的超时等问题
     */
    IOT_CHANGHUI_UPGRADE_MONITOR("iot_device", "长辉升级监控", "iot", 5),
    
    /**
     * 设备维护提醒
     * 提醒设备定期维护
     */
    IOT_DEVICE_MAINTENANCE_REMIND("iot_device", "设备维护提醒", "iot", 7),
    
    // ========== IoT 产品类 ==========
    /**
     * 产品数据统计
     * 统计产品的在线率、故障率等指标
     */
    IOT_PRODUCT_STATISTICS("iot_product", "产品数据统计", "iot", 7),
    
    /**
     * 产品数据汇总
     * 汇总产品相关的设备数据
     */
    IOT_PRODUCT_DATA_AGGREGATE("iot_product", "产品数据汇总", "iot", 7),
    
    // ========== 智能照明类 ==========
    /**
     * 照明定时任务
     * 执行照明策略，如定时开关灯、场景切换等
     */
    LIGHTING_SCHEDULE("lighting", "照明定时任务", "lighting", 3),
    
    /**
     * 照明能耗统计
     * 统计照明系统的能耗数据
     */
    LIGHTING_ENERGY_STAT("lighting", "照明能耗统计", "lighting", 7),
    
    /**
     * 照明场景预设
     * 预设照明场景的自动执行
     */
    LIGHTING_SCENE_PRESET("lighting", "照明场景预设", "lighting", 5),
    
    // ========== 空间管理类 ==========
    /**
     * 空间设施巡检
     * 定期检查空间设施的状态
     */
    SPATIAL_INSPECTION("spatial", "空间设施巡检", "spatial", 5),
    
    /**
     * 环境数据采集
     * 采集温湿度、空气质量等环境参数
     */
    SPATIAL_ENV_COLLECT("spatial", "环境数据采集", "spatial", 5),
    
    /**
     * 空间占用率分析
     * 分析空间的使用情况和占用率
     */
    SPATIAL_OCCUPANCY_ANALYSIS("spatial", "空间占用率分析", "spatial", 7),
    
    /**
     * 空间清洁计划
     * 管理和调度空间清洁任务
     */
    SPATIAL_CLEANING_SCHEDULE("spatial", "空间清洁计划", "spatial", 7),
    
    /**
     * 园区设备统计
     * 统计园区内各类设备的运行状态
     */
    CAMPUS_DEVICE_STATISTICS("spatial", "园区设备统计", "spatial", 7),
    
    /**
     * 建筑能耗统计
     * 统计单栋建筑的能耗数据
     */
    BUILDING_ENERGY_STATISTICS("spatial", "建筑能耗统计", "spatial", 7),
    
    /**
     * 楼层环境监测
     * 监测楼层的温湿度、空气质量等
     */
    FLOOR_ENV_MONITOR("spatial", "楼层环境监测", "spatial", 5),
    
    /**
     * 区域设备巡检
     * 巡检特定区域内的所有设备
     */
    AREA_DEVICE_INSPECTION("spatial", "区域设备巡检", "spatial", 5),
    
    // ========== 能源管理类 ==========
    /**
     * 能耗统计
     * 统计各类设备的能耗数据，生成报表
     */
    ENERGY_STATISTICS("energy", "能耗统计", "energy", 7),
    
    /**
     * 能源优化调度
     * 基于峰谷电价等进行能源优化调度
     */
    ENERGY_OPTIMIZATION("energy", "能源优化调度", "energy", 5),
    
    /**
     * 能耗数据采集
     * 从智能电表等设备采集能耗数据
     */
    ENERGY_DATA_COLLECT("energy", "能耗数据采集", "energy", 5),
    
    /**
     * 能耗分析报告
     * 生成能耗分析报告
     */
    ENERGY_ANALYSIS_REPORT("energy", "能耗分析报告", "energy", 7),
    
    /**
     * 峰谷电价切换
     * 根据峰谷电价时段自动切换用电策略
     */
    ENERGY_PEAK_VALLEY_SWITCH("energy", "峰谷电价切换", "energy", 3),
    
    /**
     * 能耗费用计算
     * 计算各区域、设备的能耗费用
     */
    ENERGY_COST_CALCULATE("energy", "能耗费用计算", "energy", 7),
    
    // ========== 安防系统类 ==========
    /**
     * 录像清理
     * 清理过期的监控录像
     */
    SECURITY_VIDEO_CLEANUP("security", "录像清理", "security", 9),
    
    /**
     * 安防巡检
     * 执行安防系统的巡检任务
     */
    SECURITY_PATROL("security", "安防巡检", "security", 5),
    
    /**
     * 安防告警检查
     * 检查和处理安防告警事件
     */
    SECURITY_ALARM_CHECK("security", "安防告警检查", "security", 3),
    
    /**
     * 监控视频备份
     * 备份重要的监控视频
     */
    SECURITY_VIDEO_BACKUP("security", "监控视频备份", "security", 7),
    
    /**
     * 门禁日志归档
     * 归档门禁访问日志
     */
    SECURITY_ACCESS_LOG_ARCHIVE("security", "门禁日志归档", "security", 9),
    
    // ========== HVAC（暖通空调）类 ==========
    /**
     * 空调定时控制
     * 根据时间表控制空调开关
     */
    HVAC_SCHEDULE("hvac", "空调定时控制", "hvac", 3),
    
    /**
     * 空调优化调度
     * 根据环境参数优化空调运行
     */
    HVAC_OPTIMIZATION("hvac", "空调优化调度", "hvac", 5),
    
    /**
     * 空调能耗统计
     * 统计空调系统的能耗
     */
    HVAC_ENERGY_STATISTICS("hvac", "空调能耗统计", "hvac", 7),
    
    // ========== 通用系统类 ==========
    /**
     * 数据清理
     * 清理系统的临时数据、过期日志等
     */
    SYSTEM_DATA_CLEANUP("system", "数据清理", "system", 9),
    
    /**
     * 数据备份
     * 定时备份关键数据
     */
    SYSTEM_DATA_BACKUP("system", "数据备份", "system", 7),
    
    /**
     * 数据归档
     * 归档历史数据
     */
    SYSTEM_DATA_ARCHIVE("system", "数据归档", "system", 9),
    
    /**
     * 报表生成
     * 定时生成各类统计报表
     */
    SYSTEM_REPORT_GENERATE("system", "报表生成", "system", 7),
    
    /**
     * 统一任务调度器
     * 统一管理和调度所有定时任务
     */
    IOT_UNIFIED_SCHEDULER("system", "统一任务调度器", "iot", 1);
    
    /**
     * 任务类别
     * 如：iot_device, iot_product, lighting 等
     */
    private final String category;
    
    /**
     * 任务描述
     * 用于显示在管理界面
     */
    private final String description;
    
    /**
     * 任务分组
     * 用于资源隔离，同一分组的任务共享资源池
     */
    private final String group;
    
    /**
     * 默认优先级
     * 1-9，数字越小优先级越高
     */
    private final int defaultPriority;
    
    /**
     * 根据名称查找枚举
     * 
     * @param name 枚举名称
     * @return 对应的枚举，如果不存在则返回 null
     */
    public static JobBusinessType fromName(String name) {
        if (name == null) {
            return null;
        }
        for (JobBusinessType type : values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }
}

