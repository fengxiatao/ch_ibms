package cn.iocoder.yudao.module.iot.core.mq.message.alarm;

import cn.iocoder.yudao.module.iot.core.mq.message.GatewayEventDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 报警主机事件消息
 * 
 * <p>用于 Gateway → Biz 的报警主机事件上报</p>
 * 
 * <p><b>字段类型约束（Requirements 4.3）：</b></p>
 * <ul>
 *   <li>eventType 使用 Integer 常量（参见 {@link EventType}）</li>
 *   <li>eventLevel 使用 Integer 常量（参见 {@link EventLevel}）</li>
 * </ul>
 *
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmHostEventMessage implements GatewayEventDTO {

    private static final long serialVersionUID = 1L;

    /**
     * 报警主机ID
     */
    private Long hostId;
    
    /**
     * 设备ID（与 hostId 相同，用于统一接口）
     */
    private Long deviceId;
    
    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 事件码（如1130、3401等）
     */
    private String eventCode;

    /**
     * 事件类型
     * @see EventType
     */
    private Integer eventType;
    
    /**
     * 事件类型名称（用于显示，如 ALARM、ARM、DISARM 等）
     * @deprecated 请使用 eventType 字段（Integer 类型）
     */
    @Deprecated
    private String eventTypeName;

    /**
     * 事件级别
     * @see EventLevel
     */
    private Integer eventLevel;
    
    /**
     * 事件级别名称（用于显示，如 INFO、WARNING、ERROR 等）
     * @deprecated 请使用 eventLevel 字段（Integer 类型）
     */
    @Deprecated
    private String eventLevelName;

    /**
     * 分区号
     */
    private Integer areaNo;

    /**
     * 防区号
     */
    private Integer zoneNo;

    /**
     * 用户号
     */
    private Integer userNo;

    /**
     * 序列号
     */
    private String sequence;

    /**
     * 事件描述
     */
    private String eventDesc;

    /**
     * 原始数据
     */
    private String rawData;

    /**
     * 是否新事件：1-新事件（千位1），0-恢复事件（千位3）
     */
    private Boolean isNewEvent;

    /**
     * 事件时间
     */
    private LocalDateTime eventTime;
    
    /**
     * 事件时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * 租户ID
     */
    private Long tenantId;
    
    /**
     * 事件类型常量
     * <p>Requirements: 4.3 - 使用 Integer 常量而非 String</p>
     */
    public interface EventType {
        /** 报警事件 */
        int ALARM = 1;
        /** 布防事件 */
        int ARM = 2;
        /** 撤防事件 */
        int DISARM = 3;
        /** 故障事件 */
        int FAULT = 4;
        /** 旁路事件 */
        int BYPASS = 5;
        /** 恢复事件 */
        int RESTORE = 6;
        /** 状态更新事件 */
        int STATUS_UPDATE = 10;
        /** 心跳事件 */
        int HEARTBEAT = 99;
    }
    
    /**
     * 事件级别常量
     * <p>Requirements: 4.3 - 使用 Integer 常量而非 String</p>
     */
    public interface EventLevel {
        /** 信息级别 */
        int INFO = 1;
        /** 警告级别 */
        int WARNING = 2;
        /** 错误级别 */
        int ERROR = 3;
        /** 严重级别 */
        int CRITICAL = 4;
    }
    
    /**
     * 获取事件类型名称
     * 
     * @return 事件类型名称
     */
    public String getEventTypeName() {
        if (eventTypeName != null) {
            return eventTypeName;
        }
        if (eventType == null) {
            return null;
        }
        switch (eventType) {
            case EventType.ALARM: return "ALARM";
            case EventType.ARM: return "ARM";
            case EventType.DISARM: return "DISARM";
            case EventType.FAULT: return "FAULT";
            case EventType.BYPASS: return "BYPASS";
            case EventType.RESTORE: return "RESTORE";
            case EventType.STATUS_UPDATE: return "STATUS_UPDATE";
            case EventType.HEARTBEAT: return "HEARTBEAT";
            default: return "UNKNOWN";
        }
    }
    
    /**
     * 获取事件级别名称
     * 
     * @return 事件级别名称
     */
    public String getEventLevelName() {
        if (eventLevelName != null) {
            return eventLevelName;
        }
        if (eventLevel == null) {
            return null;
        }
        switch (eventLevel) {
            case EventLevel.INFO: return "INFO";
            case EventLevel.WARNING: return "WARNING";
            case EventLevel.ERROR: return "ERROR";
            case EventLevel.CRITICAL: return "CRITICAL";
            default: return "UNKNOWN";
        }
    }
}
