package cn.iocoder.yudao.module.iot.core.mq.message.changhui;

import cn.iocoder.yudao.module.iot.core.mq.message.GatewayEventDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 长辉设备数据上报消息
 * 
 * <p>用于 Gateway → Biz 的长辉设备数据上报</p>
 * 
 * <p><b>字段类型约束（Requirements 4.1, 4.2, 4.3）：</b></p>
 * <ul>
 *   <li>eventType 使用 Integer 常量（参见 {@link EventType}）</li>
 *   <li>indicator 使用 String 常量（参见 ChanghuiIndicatorConstants）</li>
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
public class ChanghuiDataReportMessage implements GatewayEventDTO {

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
     * 指标类型（如 waterLevel, instantFlow, gatePosition 等）
     * @see cn.iocoder.yudao.module.iot.enums.changhui.ChanghuiIndicatorConstants
     */
    private String indicator;

    /**
     * 指标值
     */
    private BigDecimal value;

    /**
     * 指标单位（如 m, L/s, mm 等）
     */
    private String unit;

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
     * AFN码（用于协议层识别）
     */
    private Byte afnCode;
    
    /**
     * 原始数据（十六进制字符串）
     */
    private String rawData;
    
    /**
     * 事件类型常量
     * <p>Requirements: 4.3 - 使用 Integer 常量而非 String</p>
     */
    public interface EventType {
        /** 数据上报 */
        int DATA_REPORT = 1;
        /** 多指标查询响应 */
        int MULTI_QUERY_RESPONSE = 2;
        /** 历史数据上报 */
        int HISTORY_DATA_REPORT = 3;
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
            case EventType.DATA_REPORT: return "DATA_REPORT";
            case EventType.MULTI_QUERY_RESPONSE: return "MULTI_QUERY_RESPONSE";
            case EventType.HISTORY_DATA_REPORT: return "HISTORY_DATA_REPORT";
            default: return "UNKNOWN";
        }
    }
    
    /**
     * 创建数据上报消息
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param stationCode 测站编码
     * @param indicator 指标类型
     * @param value 指标值
     * @param unit 指标单位
     * @return 数据上报消息
     */
    public static ChanghuiDataReportMessage of(Long deviceId, String deviceType, 
            String stationCode, String indicator, BigDecimal value, String unit) {
        return ChanghuiDataReportMessage.builder()
                .deviceId(deviceId)
                .deviceType(deviceType)
                .stationCode(stationCode)
                .indicator(indicator)
                .value(value)
                .unit(unit)
                .eventType(EventType.DATA_REPORT)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建数据上报消息（带AFN码）
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param stationCode 测站编码
     * @param indicator 指标类型
     * @param value 指标值
     * @param unit 指标单位
     * @param afnCode AFN码
     * @return 数据上报消息
     */
    public static ChanghuiDataReportMessage of(Long deviceId, String deviceType, 
            String stationCode, String indicator, BigDecimal value, String unit, Byte afnCode) {
        return ChanghuiDataReportMessage.builder()
                .deviceId(deviceId)
                .deviceType(deviceType)
                .stationCode(stationCode)
                .indicator(indicator)
                .value(value)
                .unit(unit)
                .afnCode(afnCode)
                .eventType(EventType.DATA_REPORT)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
