package cn.iocoder.yudao.module.iot.core.mq.message.changhui;

import cn.iocoder.yudao.module.iot.core.mq.message.GatewayEventDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 长辉设备升级状态消息
 * 
 * <p>用于 Gateway → Biz 的长辉设备升级状态上报</p>
 * 
 * <p><b>字段类型约束（Requirements 4.3, 5.3-5.5）：</b></p>
 * <ul>
 *   <li>status 使用 Integer 常量（参见 {@link Status}）</li>
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
public class ChanghuiUpgradeStatusMessage implements GatewayEventDTO {

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
     * 升级状态
     * @see Status
     */
    private Integer status;
    
    /**
     * 事件类型（用于 DEVICE_EVENT_REPORTED 路由）
     * @see EventType
     */
    private Integer eventType;
    
    /**
     * 事件类型名称（用于 DEVICE_EVENT_REPORTED 路由）
     *
     * <p>DeviceEventConsumer 优先读取 eventTypeName 来决定 eventType（method）。</p>
     */
    private String eventTypeName;
    
    /**
     * 升级状态名称（用于显示）
     */
    private String statusName;

    /**
     * 升级进度（0-100）
     */
    private Integer progress;

    /**
     * 事件时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 租户ID
     */
    private Long tenantId;
    
    /**
     * 升级任务ID
     */
    private Long taskId;
    
    /**
     * 固件版本
     */
    private String firmwareVersion;
    
    /**
     * 升级状态常量
     * <p>Requirements: 4.3 - 使用 Integer 常量而非 String</p>
     */
    public interface Status {
        /** 等待中 */
        int PENDING = 0;
        /** 进行中 */
        int IN_PROGRESS = 1;
        /** 成功 */
        int SUCCESS = 2;
        /** 失败 */
        int FAILED = 3;
        /** 已取消 */
        int CANCELLED = 4;
    }
    
    /**
     * 事件类型常量
     *
     * <p>注意：此处 eventType 仅用于事件路由，不等同于 status。</p>
     */
    public interface EventType {
        /** 升级状态（通用） */
        int UPGRADE_STATUS = 1;
        /** 升级进度 */
        int UPGRADE_PROGRESS = 2;
        /** 升级完成 */
        int UPGRADE_COMPLETE = 3;
        /** 升级失败 */
        int UPGRADE_FAILED = 4;
        /** 升级触发成功（设备确认可以升级） */
        int TRIGGER_SUCCESS = 5;
        /** 升级触发失败（设备拒绝升级） */
        int TRIGGER_FAILED = 6;
        /** URL接收成功（设备已接收升级URL） */
        int URL_RECEIVED = 7;
        /** URL接收失败 */
        int URL_RECEIVE_FAILED = 8;
        /** 固件下载中 */
        int DOWNLOADING = 9;
        /** 固件下载完成 */
        int DOWNLOAD_COMPLETE = 10;
    }
    
    /**
     * 获取状态名称
     * 
     * @return 状态名称
     */
    public String getStatusName() {
        if (statusName != null) {
            return statusName;
        }
        if (status == null) {
            return null;
        }
        switch (status) {
            case Status.PENDING: return "PENDING";
            case Status.IN_PROGRESS: return "IN_PROGRESS";
            case Status.SUCCESS: return "SUCCESS";
            case Status.FAILED: return "FAILED";
            case Status.CANCELLED: return "CANCELLED";
            default: return "UNKNOWN";
        }
    }
    
    /**
     * 获取事件类型名称（用于路由）
     */
    public String getEventTypeName() {
        if (eventTypeName != null) {
            return eventTypeName;
        }
        // 根据 eventType 推导事件类型名称
        if (eventType != null) {
            switch (eventType) {
                case EventType.TRIGGER_SUCCESS:
                    return "TRIGGER_SUCCESS";
                case EventType.TRIGGER_FAILED:
                    return "TRIGGER_FAILED";
                case EventType.URL_RECEIVED:
                    return "URL_RECEIVED";
                case EventType.URL_RECEIVE_FAILED:
                    return "URL_RECEIVE_FAILED";
                case EventType.DOWNLOADING:
                    return "DOWNLOADING";
                case EventType.DOWNLOAD_COMPLETE:
                    return "DOWNLOAD_COMPLETE";
                case EventType.UPGRADE_PROGRESS:
                    return "UPGRADE_PROGRESS";
                case EventType.UPGRADE_COMPLETE:
                    return "UPGRADE_COMPLETE";
                case EventType.UPGRADE_FAILED:
                    return "UPGRADE_FAILED";
                default:
                    break;
            }
        }
        // 根据 status 推导默认事件类型名称（兼容旧逻辑）
        if (status == null) {
            return "UPGRADE_STATUS";
        }
        switch (status) {
            case Status.IN_PROGRESS:
                return "UPGRADE_PROGRESS";
            case Status.SUCCESS:
                return "UPGRADE_COMPLETE";
            case Status.FAILED:
                return "UPGRADE_FAILED";
            default:
                return "UPGRADE_STATUS";
        }
    }
    
    /**
     * 创建进度更新消息
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param stationCode 测站编码
     * @param progress 进度（0-100）
     * @return 进度更新消息
     */
    public static ChanghuiUpgradeStatusMessage ofProgress(Long deviceId, String deviceType, 
            String stationCode, Integer progress) {
        return ChanghuiUpgradeStatusMessage.builder()
                .deviceId(deviceId)
                .deviceType(deviceType)
                .stationCode(stationCode)
                .status(Status.IN_PROGRESS)
                .progress(progress)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建成功消息
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param stationCode 测站编码
     * @return 成功消息
     */
    public static ChanghuiUpgradeStatusMessage ofSuccess(Long deviceId, String deviceType, 
            String stationCode) {
        return ChanghuiUpgradeStatusMessage.builder()
                .deviceId(deviceId)
                .deviceType(deviceType)
                .stationCode(stationCode)
                .status(Status.SUCCESS)
                .progress(100)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建失败消息
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param stationCode 测站编码
     * @param errorMessage 错误信息
     * @return 失败消息
     */
    public static ChanghuiUpgradeStatusMessage ofFailed(Long deviceId, String deviceType, 
            String stationCode, String errorMessage) {
        return ChanghuiUpgradeStatusMessage.builder()
                .deviceId(deviceId)
                .deviceType(deviceType)
                .stationCode(stationCode)
                .status(Status.FAILED)
                .errorMessage(errorMessage)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建升级触发成功消息
     * 
     * <p>设备确认可以升级，业务侧收到此消息后应发送 UPGRADE_URL</p>
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param stationCode 测站编码
     * @param protocolType 协议类型（如 0x07DA = 德通协议）
     * @return 触发成功消息
     */
    public static ChanghuiUpgradeStatusMessage ofTriggerSuccess(Long deviceId, String deviceType, 
            String stationCode, Integer protocolType) {
        return ChanghuiUpgradeStatusMessage.builder()
                .deviceId(deviceId)
                .deviceType(deviceType)
                .stationCode(stationCode)
                .eventType(EventType.TRIGGER_SUCCESS)
                .eventTypeName("TRIGGER_SUCCESS")
                .status(Status.PENDING)
                .statusName("TRIGGER_SUCCESS")
                .progress(0)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建升级触发失败消息
     * 
     * <p>设备拒绝升级触发</p>
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param stationCode 测站编码
     * @param errorMessage 错误信息
     * @return 触发失败消息
     */
    public static ChanghuiUpgradeStatusMessage ofTriggerFailed(Long deviceId, String deviceType, 
            String stationCode, String errorMessage) {
        return ChanghuiUpgradeStatusMessage.builder()
                .deviceId(deviceId)
                .deviceType(deviceType)
                .stationCode(stationCode)
                .eventType(EventType.TRIGGER_FAILED)
                .eventTypeName("TRIGGER_FAILED")
                .status(Status.FAILED)
                .statusName("TRIGGER_FAILED")
                .errorMessage(errorMessage)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建URL接收成功消息
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param stationCode 测站编码
     * @return URL接收成功消息
     */
    public static ChanghuiUpgradeStatusMessage ofUrlReceived(Long deviceId, String deviceType, 
            String stationCode) {
        return ChanghuiUpgradeStatusMessage.builder()
                .deviceId(deviceId)
                .deviceType(deviceType)
                .stationCode(stationCode)
                .eventType(EventType.URL_RECEIVED)
                .eventTypeName("URL_RECEIVED")
                .status(Status.IN_PROGRESS)
                .statusName("URL_RECEIVED")
                .progress(5)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建固件下载中消息
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param stationCode 测站编码
     * @return 下载中消息
     */
    public static ChanghuiUpgradeStatusMessage ofDownloading(Long deviceId, String deviceType, 
            String stationCode) {
        return ChanghuiUpgradeStatusMessage.builder()
                .deviceId(deviceId)
                .deviceType(deviceType)
                .stationCode(stationCode)
                .eventType(EventType.DOWNLOADING)
                .eventTypeName("DOWNLOADING")
                .status(Status.IN_PROGRESS)
                .statusName("DOWNLOADING")
                .progress(10)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建固件下载完成消息
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param stationCode 测站编码
     * @return 下载完成消息
     */
    public static ChanghuiUpgradeStatusMessage ofDownloadComplete(Long deviceId, String deviceType, 
            String stationCode) {
        return ChanghuiUpgradeStatusMessage.builder()
                .deviceId(deviceId)
                .deviceType(deviceType)
                .stationCode(stationCode)
                .eventType(EventType.DOWNLOAD_COMPLETE)
                .eventTypeName("DOWNLOAD_COMPLETE")
                .status(Status.IN_PROGRESS)
                .statusName("DOWNLOAD_COMPLETE")
                .progress(50)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
