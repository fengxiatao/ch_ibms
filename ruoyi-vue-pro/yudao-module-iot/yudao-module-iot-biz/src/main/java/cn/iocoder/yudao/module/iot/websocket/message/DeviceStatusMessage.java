package cn.iocoder.yudao.module.iot.websocket.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备状态更新消息
 *
 * @author 芋道源码
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceStatusMessage {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备状态（online/offline）
     */
    private String status;

    /**
     * 时间戳
     */
    private Long timestamp;
}









