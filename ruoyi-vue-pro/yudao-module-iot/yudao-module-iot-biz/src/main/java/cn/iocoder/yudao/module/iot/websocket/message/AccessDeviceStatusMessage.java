package cn.iocoder.yudao.module.iot.websocket.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 门禁设备状态更新消息
 *
 * @author 智能化系统
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessDeviceStatusMessage {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备IP
     */
    private String deviceIp;

    /**
     * 在线状态：0-离线，1-在线
     */
    private Integer onlineStatus;

    /**
     * 状态变更类型：online-上线，offline-离线，activated-激活成功，activation_failed-激活失败，reconnecting-重连中
     */
    private String statusType;

    /**
     * 时间戳
     */
    private Long timestamp;

    // ==================== 激活状态相关字段 (Requirements: 5.3) ====================

    /**
     * 激活状态：ACTIVATED-已激活，FAILED-激活失败，RECONNECTING-重连中，ACTIVATING-激活中，NOT_ACTIVATED-未激活
     */
    private String activationStatus;

    /**
     * 激活耗时（毫秒）
     */
    private Long activationTimeMs;

    /**
     * 重连次数
     */
    private Integer reconnectCount;

    /**
     * 错误信息
     */
    private String errorMessage;

}
