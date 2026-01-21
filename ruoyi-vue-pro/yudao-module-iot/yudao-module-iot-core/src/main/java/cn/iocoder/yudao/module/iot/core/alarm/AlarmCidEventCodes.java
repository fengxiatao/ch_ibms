package cn.iocoder.yudao.module.iot.core.alarm;

import java.util.Map;
import java.util.Set;

/**
 * 报警主机 CID 事件码常量
 * 
 * <p>CID (Contact ID) 是报警行业标准的事件码格式。</p>
 * 
 * <p>CID 事件码规则：</p>
 * <ul>
 *   <li>千位为 1 表示新事件（如撤防、报警）</li>
 *   <li>千位为 3 表示事件恢复（如布防、报警恢复）</li>
 * </ul>
 * 
 * @author 长辉信息科技有限公司
 */
public final class AlarmCidEventCodes {
    
    private AlarmCidEventCodes() {
        // 私有构造函数，防止实例化
    }
    
    // ==================== 布防/撤防事件 ====================
    
    /** 布防（外出布防） */
    public static final String ARM_AWAY = "3401";
    /** 撤防 */
    public static final String DISARM = "1401";
    /** 居家布防（留守布防） */
    public static final String ARM_STAY = "3441";
    /** 单防区布防 */
    public static final String SINGLE_ZONE_ARM = "3973";
    /** 单防区撤防 */
    public static final String SINGLE_ZONE_DISARM = "1973";
    
    // ==================== 防区报警事件 ====================
    
    /** 防区报警 */
    public static final String ZONE_ALARM = "1130";
    /** 防区报警恢复 */
    public static final String ZONE_ALARM_RESTORE = "3130";
    /** 防区拆动 */
    public static final String ZONE_TAMPER = "1144";
    /** 防区拆动恢复 */
    public static final String ZONE_TAMPER_RESTORE = "3144";
    /** 防区活动监测超时 */
    public static final String ZONE_ACTIVITY_TIMEOUT = "1391";
    /** 防区活动监测恢复 */
    public static final String ZONE_ACTIVITY_RESTORE = "3391";
    
    // ==================== 防区旁路事件 ====================
    
    /** 防区旁路 */
    public static final String ZONE_BYPASS = "1570";
    /** 防区取消旁路 */
    public static final String ZONE_UNBYPASS = "3570";
    
    // ==================== 系统故障事件 ====================
    
    /** 模块通信故障 */
    public static final String MODULE_COMM_FAULT = "1333";
    /** 模块通信恢复 */
    public static final String MODULE_COMM_RESTORE = "3333";
    /** 模块拆动 */
    public static final String MODULE_TAMPER = "1341";
    /** 模块拆动恢复 */
    public static final String MODULE_TAMPER_RESTORE = "3341";
    /** 电话线路断开 */
    public static final String PHONE_LINE_FAULT = "1351";
    /** 电话线路恢复 */
    public static final String PHONE_LINE_RESTORE = "3351";
    /** 主机主电源断开 */
    public static final String AC_POWER_LOSS = "1301";
    /** 主机主电源恢复 */
    public static final String AC_POWER_RESTORE = "3301";
    /** 主机电池电压低 */
    public static final String BATTERY_LOW = "1302";
    /** 主机电池电压恢复 */
    public static final String BATTERY_RESTORE = "3302";
    /** 主机电池断开 */
    public static final String BATTERY_DISCONNECT = "1311";
    /** 主机电池恢复 */
    public static final String BATTERY_RECONNECT = "3311";
    /** 主机复位重启 */
    public static final String SYSTEM_RESET = "1305";
    /** 输出故障 */
    public static final String OUTPUT_FAULT = "1320";
    /** 输出故障恢复 */
    public static final String OUTPUT_FAULT_RESTORE = "3320";
    /** 中心报告通信故障 */
    public static final String CENTER_COMM_FAULT = "1350";
    /** 中心报告通信恢复 */
    public static final String CENTER_COMM_RESTORE = "3350";
    
    // ==================== 其他事件 ====================
    
    /** 挟持报警 */
    public static final String DURESS_ALARM = "1121";
    /** 密码重试次数过多 */
    public static final String PASSWORD_RETRY_EXCEEDED = "1461";
    /** 设置时间 */
    public static final String SET_TIME = "1625";
    /** 设置主机系统参数 */
    public static final String SET_SYSTEM_PARAM = "1306";
    
    // ==================== 电子围栏事件 ====================
    
    /** 电子围栏撤防 */
    public static final String FENCE_DISARM = "1762";
    /** 电子围栏（高压）布防 */
    public static final String FENCE_ARM_HIGH = "3766";
    /** 电子围栏低压布防 */
    public static final String FENCE_ARM_LOW = "3767";
    /** 电子围栏开路报警 */
    public static final String FENCE_OPEN_ALARM = "1759";
    /** 电子围栏开路报警恢复 */
    public static final String FENCE_OPEN_RESTORE = "3759";
    /** 电子围栏短路报警 */
    public static final String FENCE_SHORT_ALARM = "1763";
    /** 电子围栏短路报警恢复 */
    public static final String FENCE_SHORT_RESTORE = "3763";
    /** 电子围栏触网报警 */
    public static final String FENCE_TOUCH_ALARM = "1760";
    /** 电子围栏触网报警恢复 */
    public static final String FENCE_TOUCH_RESTORE = "3760";
    
    // ==================== 升降柱事件 ====================
    
    /** 升降柱下降 */
    public static final String BOLLARD_DOWN = "1710";
    /** 升降柱上升 */
    public static final String BOLLARD_UP = "3710";
    
    // ==================== 事件码到布防状态的映射 ====================
    
    /**
     * 布防/撤防事件码到布防状态的映射
     */
    public static final Map<String, String> EVENT_TO_ARM_STATUS = Map.of(
        ARM_AWAY, "ARM_ALL",           // 布防（外出布防）
        DISARM, "DISARM",              // 撤防
        ARM_STAY, "ARM_EMERGENCY"      // 居家布防（留守布防）
    );
    
    /**
     * 防区报警事件码集合
     */
    public static final Set<String> ZONE_ALARM_EVENTS = Set.of(
        ZONE_ALARM,           // 防区报警
        ZONE_TAMPER,          // 防区拆动
        ZONE_ACTIVITY_TIMEOUT // 防区活动监测超时
    );
    
    /**
     * 防区报警恢复事件码集合
     */
    public static final Set<String> ZONE_ALARM_RESTORE_EVENTS = Set.of(
        ZONE_ALARM_RESTORE,    // 防区报警恢复
        ZONE_TAMPER_RESTORE,   // 防区拆动恢复
        ZONE_ACTIVITY_RESTORE  // 防区活动监测恢复
    );
    
    // ==================== 辅助方法 ====================
    
    /**
     * 判断是否为报警事件（千位为1）
     *
     * @param eventCode 事件码
     * @return 是否为报警事件
     */
    public static boolean isAlarmEvent(String eventCode) {
        return eventCode != null && eventCode.length() == 4 && eventCode.charAt(0) == '1';
    }
    
    /**
     * 判断是否为恢复事件（千位为3）
     *
     * @param eventCode 事件码
     * @return 是否为恢复事件
     */
    public static boolean isRestoreEvent(String eventCode) {
        return eventCode != null && eventCode.length() == 4 && eventCode.charAt(0) == '3';
    }
    
    /**
     * 判断是否为布防/撤防事件
     *
     * @param eventCode 事件码
     * @return 是否为布防/撤防事件
     */
    public static boolean isArmDisarmEvent(String eventCode) {
        return EVENT_TO_ARM_STATUS.containsKey(eventCode);
    }
    
    /**
     * 判断是否为防区报警事件
     *
     * @param eventCode 事件码
     * @return 是否为防区报警事件
     */
    public static boolean isZoneAlarmEvent(String eventCode) {
        return ZONE_ALARM_EVENTS.contains(eventCode);
    }
    
    /**
     * 判断是否为防区报警恢复事件
     *
     * @param eventCode 事件码
     * @return 是否为防区报警恢复事件
     */
    public static boolean isZoneAlarmRestoreEvent(String eventCode) {
        return ZONE_ALARM_RESTORE_EVENTS.contains(eventCode);
    }
    
    /**
     * 判断是否为防区旁路事件
     *
     * @param eventCode 事件码
     * @return 是否为防区旁路事件
     */
    public static boolean isZoneBypassEvent(String eventCode) {
        return ZONE_BYPASS.equals(eventCode);
    }
    
    /**
     * 判断是否为防区取消旁路事件
     *
     * @param eventCode 事件码
     * @return 是否为防区取消旁路事件
     */
    public static boolean isZoneUnbypassEvent(String eventCode) {
        return ZONE_UNBYPASS.equals(eventCode);
    }
    
    /**
     * 判断是否为单防区布防事件
     *
     * @param eventCode 事件码
     * @return 是否为单防区布防事件
     */
    public static boolean isSingleZoneArmEvent(String eventCode) {
        return SINGLE_ZONE_ARM.equals(eventCode);
    }
    
    /**
     * 判断是否为单防区撤防事件
     *
     * @param eventCode 事件码
     * @return 是否为单防区撤防事件
     */
    public static boolean isSingleZoneDisarmEvent(String eventCode) {
        return SINGLE_ZONE_DISARM.equals(eventCode);
    }
    
    /**
     * 获取事件码对应的布防状态
     *
     * @param eventCode 事件码
     * @return 布防状态，如果不是布防/撤防事件则返回 null
     */
    public static String getArmStatus(String eventCode) {
        return EVENT_TO_ARM_STATUS.get(eventCode);
    }
    
    /**
     * 获取事件码的描述
     *
     * @param eventCode 事件码
     * @return 事件描述
     */
    public static String getEventDescription(String eventCode) {
        if (eventCode == null) {
            return "未知事件";
        }
        
        return switch (eventCode) {
            case ARM_AWAY -> "布防（外出布防）";
            case DISARM -> "撤防";
            case ARM_STAY -> "居家布防";
            case SINGLE_ZONE_ARM -> "单防区布防";
            case SINGLE_ZONE_DISARM -> "单防区撤防";
            case ZONE_ALARM -> "防区报警";
            case ZONE_ALARM_RESTORE -> "防区报警恢复";
            case ZONE_TAMPER -> "防区拆动";
            case ZONE_TAMPER_RESTORE -> "防区拆动恢复";
            case ZONE_ACTIVITY_TIMEOUT -> "防区活动监测超时";
            case ZONE_ACTIVITY_RESTORE -> "防区活动监测恢复";
            case ZONE_BYPASS -> "防区旁路";
            case ZONE_UNBYPASS -> "防区取消旁路";
            case MODULE_COMM_FAULT -> "模块通信故障";
            case MODULE_COMM_RESTORE -> "模块通信恢复";
            case AC_POWER_LOSS -> "主机主电源断开";
            case AC_POWER_RESTORE -> "主机主电源恢复";
            case BATTERY_LOW -> "主机电池电压低";
            case BATTERY_RESTORE -> "主机电池电压恢复";
            case SYSTEM_RESET -> "主机复位重启";
            case DURESS_ALARM -> "挟持报警";
            case PASSWORD_RETRY_EXCEEDED -> "密码重试次数过多";
            default -> "事件码: " + eventCode;
        };
    }
}
