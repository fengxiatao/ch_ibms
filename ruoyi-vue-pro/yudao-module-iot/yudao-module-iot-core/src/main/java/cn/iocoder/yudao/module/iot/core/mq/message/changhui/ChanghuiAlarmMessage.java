package cn.iocoder.yudao.module.iot.core.mq.message.changhui;

import cn.iocoder.yudao.module.iot.core.mq.message.GatewayEventDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 长辉设备报警消息
 * 
 * <p>用于 Gateway → Biz 的长辉设备报警上报</p>
 * 
 * <p><b>字段类型约束（Requirements 4.1, 4.2, 4.3）：</b></p>
 * <ul>
 *   <li>eventType 使用 Integer 常量（参见 {@link EventType}）</li>
 *   <li>alarmType 使用 Integer 常量（参见 {@link AlarmType}）</li>
 * </ul>
 * 
 * <p>实现 {@link GatewayEventDTO} 接口以支持通过 GatewayMessagePublisher 发布事件</p>
 *
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChanghuiAlarmMessage implements GatewayEventDTO {

    private static final long serialVersionUID = 1L;

    /**
     * 设备ID
     */
    private Long deviceId;
    
    /**
     * 设备类型（如 "CHANGHUI"）
     */
    private String deviceType;

    /**
     * 测站编码（10字节十六进制字符串）
     */
    private String stationCode;

    /**
     * 报警类型
     * @see AlarmType
     */
    private Integer alarmType;
    
    /**
     * 报警类型名称（用于显示）
     */
    private String alarmTypeName;

    /**
     * 报警值（具体含义取决于报警类型）
     */
    private String alarmValue;

    /**
     * 事件类型
     * @see EventType
     */
    private Integer eventType;
    
    /**
     * 事件类型名称（用于显示）
     */
    private String eventTypeName;

    /**
     * 事件时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * 租户ID
     */
    private Long tenantId;
    
    /**
     * 原始数据（十六进制字符串）
     */
    private String rawData;
    
    /**
     * 报警描述
     */
    private String description;
    
    /**
     * 事件类型常量
     * <p>Requirements: 4.3 - 使用 Integer 常量而非 String</p>
     */
    public interface EventType {
        /** 报警事件 */
        int ALARM = 1;
        /** 报警恢复 */
        int ALARM_RESTORE = 2;
    }
    
    /**
     * 报警类型常量
     * <p>Requirements: 4.3, 13.1 - 使用 Integer 常量而非 String</p>
     * <p>支持的报警类型：过扭矩、过电流、过电压、欠电压、水位、闸位</p>
     */
    public interface AlarmType {
        /** 过扭矩报警 */
        int OVER_TORQUE = 1;
        /** 过电流报警 */
        int OVER_CURRENT = 2;
        /** 过电压报警 */
        int OVER_VOLTAGE = 3;
        /** 欠电压报警 */
        int LOW_VOLTAGE = 4;
        /** 水位报警 */
        int WATER_LEVEL = 5;
        /** 闸位报警 */
        int GATE_POSITION = 6;
        /** 通信故障 */
        int COMMUNICATION_FAULT = 7;
        /** 设备故障 */
        int DEVICE_FAULT = 8;
        /** 其他报警 */
        int OTHER = 99;
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
            case EventType.ALARM_RESTORE: return "ALARM_RESTORE";
            default: return "UNKNOWN";
        }
    }
    
    /**
     * 获取报警类型名称
     * 
     * @return 报警类型名称
     */
    public String getAlarmTypeName() {
        if (alarmTypeName != null) {
            return alarmTypeName;
        }
        if (alarmType == null) {
            return null;
        }
        switch (alarmType) {
            case AlarmType.OVER_TORQUE: return "OVER_TORQUE";
            case AlarmType.OVER_CURRENT: return "OVER_CURRENT";
            case AlarmType.OVER_VOLTAGE: return "OVER_VOLTAGE";
            case AlarmType.LOW_VOLTAGE: return "LOW_VOLTAGE";
            case AlarmType.WATER_LEVEL: return "WATER_LEVEL";
            case AlarmType.GATE_POSITION: return "GATE_POSITION";
            case AlarmType.COMMUNICATION_FAULT: return "COMMUNICATION_FAULT";
            case AlarmType.DEVICE_FAULT: return "DEVICE_FAULT";
            case AlarmType.OTHER: return "OTHER";
            default: return "UNKNOWN";
        }
    }
    
    /**
     * 创建报警消息
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param stationCode 测站编码
     * @param alarmType 报警类型
     * @param alarmValue 报警值
     * @return 报警消息
     */
    public static ChanghuiAlarmMessage of(Long deviceId, String deviceType, 
            String stationCode, Integer alarmType, String alarmValue) {
        return ChanghuiAlarmMessage.builder()
                .deviceId(deviceId)
                .deviceType(deviceType)
                .stationCode(stationCode)
                .alarmType(alarmType)
                .alarmValue(alarmValue)
                .eventType(EventType.ALARM)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建报警恢复消息
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param stationCode 测站编码
     * @param alarmType 报警类型
     * @return 报警恢复消息
     */
    public static ChanghuiAlarmMessage ofRestore(Long deviceId, String deviceType, 
            String stationCode, Integer alarmType) {
        return ChanghuiAlarmMessage.builder()
                .deviceId(deviceId)
                .deviceType(deviceType)
                .stationCode(stationCode)
                .alarmType(alarmType)
                .eventType(EventType.ALARM_RESTORE)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
