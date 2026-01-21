package cn.iocoder.yudao.module.iot.core.mq.message.changhui;

import cn.iocoder.yudao.module.iot.core.mq.message.GatewayEventDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 长辉设备控制结果消息
 * 
 * <p>用于 Gateway → Biz 的长辉设备控制结果上报</p>
 * 
 * <p><b>字段类型约束（Requirements 4.1, 4.2, 4.3）：</b></p>
 * <ul>
 *   <li>resultType 使用 Integer 常量（参见 {@link ResultType}）</li>
 *   <li>controlType 使用 Integer 常量（参见 {@link ControlType}）</li>
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
public class ChanghuiControlResultMessage implements GatewayEventDTO {

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
     * 控制类型
     * @see ControlType
     */
    private Integer controlType;
    
    /**
     * 控制类型名称（用于显示）
     */
    private String controlTypeName;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息（失败时填写）
     */
    private String errorMessage;

    /**
     * 结果类型
     * @see ResultType
     */
    private Integer resultType;
    
    /**
     * 结果类型名称（用于显示）
     */
    private String resultTypeName;

    /**
     * 事件时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * 租户ID
     */
    private Long tenantId;
    
    /**
     * 请求ID（用于关联请求和响应）
     */
    private String requestId;
    
    /**
     * 控制参数（原始请求参数）
     */
    private Map<String, Object> controlParams;
    
    /**
     * 响应数据
     */
    private Map<String, Object> responseData;
    
    /**
     * 结果类型常量
     * <p>Requirements: 4.3 - 使用 Integer 常量而非 String</p>
     */
    public interface ResultType {
        /** 控制成功 */
        int SUCCESS = 1;
        /** 控制失败 */
        int FAILED = 2;
        /** 超时 */
        int TIMEOUT = 3;
        /** 设备离线 */
        int DEVICE_OFFLINE = 4;
        /** 命令不支持 */
        int UNSUPPORTED = 5;
    }
    
    /**
     * 控制类型常量
     * <p>Requirements: 4.3, 14.1-14.3 - 使用 Integer 常量而非 String</p>
     */
    public interface ControlType {
        /** 模式切换 */
        int SWITCH_MODE = 1;
        /** 手动控制-上升 */
        int MANUAL_RISE = 2;
        /** 手动控制-下降 */
        int MANUAL_FALL = 3;
        /** 手动控制-停止 */
        int MANUAL_STOP = 4;
        /** 自动控制-流量控制 */
        int AUTO_FLOW = 5;
        /** 自动控制-开度控制 */
        int AUTO_OPENING = 6;
        /** 自动控制-水位控制 */
        int AUTO_LEVEL = 7;
        /** 自动控制-水量控制 */
        int AUTO_VOLUME = 8;
        /** 查询状态 */
        int QUERY_STATUS = 10;
        /** 其他控制 */
        int OTHER = 99;
    }
    
    /**
     * 获取结果类型名称
     * 
     * @return 结果类型名称
     */
    public String getResultTypeName() {
        if (resultTypeName != null) {
            return resultTypeName;
        }
        if (resultType == null) {
            return null;
        }
        switch (resultType) {
            case ResultType.SUCCESS: return "SUCCESS";
            case ResultType.FAILED: return "FAILED";
            case ResultType.TIMEOUT: return "TIMEOUT";
            case ResultType.DEVICE_OFFLINE: return "DEVICE_OFFLINE";
            case ResultType.UNSUPPORTED: return "UNSUPPORTED";
            default: return "UNKNOWN";
        }
    }
    
    /**
     * 获取控制类型名称
     * 
     * @return 控制类型名称
     */
    public String getControlTypeName() {
        if (controlTypeName != null) {
            return controlTypeName;
        }
        if (controlType == null) {
            return null;
        }
        switch (controlType) {
            case ControlType.SWITCH_MODE: return "SWITCH_MODE";
            case ControlType.MANUAL_RISE: return "MANUAL_RISE";
            case ControlType.MANUAL_FALL: return "MANUAL_FALL";
            case ControlType.MANUAL_STOP: return "MANUAL_STOP";
            case ControlType.AUTO_FLOW: return "AUTO_FLOW";
            case ControlType.AUTO_OPENING: return "AUTO_OPENING";
            case ControlType.AUTO_LEVEL: return "AUTO_LEVEL";
            case ControlType.AUTO_VOLUME: return "AUTO_VOLUME";
            case ControlType.QUERY_STATUS: return "QUERY_STATUS";
            case ControlType.OTHER: return "OTHER";
            default: return "UNKNOWN";
        }
    }
    
    /**
     * 创建成功结果消息
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param stationCode 测站编码
     * @param controlType 控制类型
     * @return 成功结果消息
     */
    public static ChanghuiControlResultMessage ofSuccess(Long deviceId, String deviceType, 
            String stationCode, Integer controlType) {
        return ChanghuiControlResultMessage.builder()
                .deviceId(deviceId)
                .deviceType(deviceType)
                .stationCode(stationCode)
                .controlType(controlType)
                .success(true)
                .resultType(ResultType.SUCCESS)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建失败结果消息
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param stationCode 测站编码
     * @param controlType 控制类型
     * @param errorMessage 错误信息
     * @return 失败结果消息
     */
    public static ChanghuiControlResultMessage ofFailed(Long deviceId, String deviceType, 
            String stationCode, Integer controlType, String errorMessage) {
        return ChanghuiControlResultMessage.builder()
                .deviceId(deviceId)
                .deviceType(deviceType)
                .stationCode(stationCode)
                .controlType(controlType)
                .success(false)
                .resultType(ResultType.FAILED)
                .errorMessage(errorMessage)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建超时结果消息
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param stationCode 测站编码
     * @param controlType 控制类型
     * @return 超时结果消息
     */
    public static ChanghuiControlResultMessage ofTimeout(Long deviceId, String deviceType, 
            String stationCode, Integer controlType) {
        return ChanghuiControlResultMessage.builder()
                .deviceId(deviceId)
                .deviceType(deviceType)
                .stationCode(stationCode)
                .controlType(controlType)
                .success(false)
                .resultType(ResultType.TIMEOUT)
                .errorMessage("Control command timeout")
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建设备离线结果消息
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param stationCode 测站编码
     * @param controlType 控制类型
     * @return 设备离线结果消息
     */
    public static ChanghuiControlResultMessage ofDeviceOffline(Long deviceId, String deviceType, 
            String stationCode, Integer controlType) {
        return ChanghuiControlResultMessage.builder()
                .deviceId(deviceId)
                .deviceType(deviceType)
                .stationCode(stationCode)
                .controlType(controlType)
                .success(false)
                .resultType(ResultType.DEVICE_OFFLINE)
                .errorMessage("Device is offline")
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
