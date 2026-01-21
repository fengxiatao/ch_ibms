package cn.iocoder.yudao.module.iot.core.mq.message.nvr;

import cn.iocoder.yudao.module.iot.core.mq.message.GatewayEventDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * NVR 事件消息
 * 
 * <p>用于 Gateway → Biz 的 NVR 设备事件上报</p>
 * 
 * <p><b>字段类型约束（Requirements 4.3）：</b></p>
 * <ul>
 *   <li>eventType 使用 Integer 常量（参见 {@link EventType}）</li>
 * </ul>
 *
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NvrEventMessage implements GatewayEventDTO {

    private static final long serialVersionUID = 1L;

    /**
     * 设备ID
     */
    private Long deviceId;
    
    /**
     * 设备类型
     */
    private String deviceType;
    
    /**
     * 插件ID
     */
    private String pluginId;

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
     * 通道号
     */
    private Integer channelNo;

    /**
     * 事件时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * 租户ID
     */
    private Long tenantId;
    
    /**
     * 原始数据
     */
    private String rawData;
    
    /**
     * 扩展数据
     */
    private Map<String, Object> extData;
    
    /**
     * 事件类型常量
     * <p>Requirements: 4.3 - 使用 Integer 常量而非 String</p>
     */
    public interface EventType {
        /** 移动侦测 */
        int MOTION_DETECT = 1;
        /** 视频丢失 */
        int VIDEO_LOSS = 2;
        /** 视频遮挡 */
        int VIDEO_BLIND = 3;
        /** 硬盘错误 */
        int DISK_ERROR = 4;
        /** 硬盘满 */
        int DISK_FULL = 5;
        /** 网络断开 */
        int NETWORK_DISCONNECT = 6;
        /** 非法访问 */
        int ILLEGAL_ACCESS = 7;
        /** 录像开始 */
        int RECORDING_START = 10;
        /** 录像结束 */
        int RECORDING_END = 11;
        /** 抓图完成 */
        int CAPTURE_COMPLETE = 12;
        /** PTZ 控制 */
        int PTZ_CONTROL = 20;
        /** 通道状态变化 */
        int CHANNEL_STATUS_CHANGE = 30;
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
            case EventType.MOTION_DETECT: return "MOTION_DETECT";
            case EventType.VIDEO_LOSS: return "VIDEO_LOSS";
            case EventType.VIDEO_BLIND: return "VIDEO_BLIND";
            case EventType.DISK_ERROR: return "DISK_ERROR";
            case EventType.DISK_FULL: return "DISK_FULL";
            case EventType.NETWORK_DISCONNECT: return "NETWORK_DISCONNECT";
            case EventType.ILLEGAL_ACCESS: return "ILLEGAL_ACCESS";
            case EventType.RECORDING_START: return "RECORDING_START";
            case EventType.RECORDING_END: return "RECORDING_END";
            case EventType.CAPTURE_COMPLETE: return "CAPTURE_COMPLETE";
            case EventType.PTZ_CONTROL: return "PTZ_CONTROL";
            case EventType.CHANNEL_STATUS_CHANGE: return "CHANNEL_STATUS_CHANGE";
            default: return "UNKNOWN";
        }
    }
}
