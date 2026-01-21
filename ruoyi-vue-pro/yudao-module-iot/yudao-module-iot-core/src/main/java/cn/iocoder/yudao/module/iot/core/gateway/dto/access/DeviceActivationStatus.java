package cn.iocoder.yudao.module.iot.core.gateway.dto.access;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 设备激活状态DTO
 * 用于跨模块传输设备激活状态信息
 * 
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceActivationStatus {
    
    /** 设备ID */
    private Long deviceId;
    
    /** 设备名称 */
    private String deviceName;
    
    /** 设备IP地址 */
    private String ipAddress;
    
    /** 设备端口 */
    private Integer port;
    
    /** 设备用户名（用于视频预览等认证场景） */
    private String username;
    
    /** 设备密码（用于视频预览等认证场景） */
    private String password;
    
    /** 激活状态 */
    private String status;
    
    /** 最后激活时间 */
    private LocalDateTime lastActivatedTime;
    
    /** 错误信息（激活失败时） */
    private String errorMessage;
    
    /** 重连次数 */
    private Integer reconnectCount;
    
    /**
     * 创建未激活状态
     */
    public static DeviceActivationStatus notActivated(Long deviceId, String deviceName, String ipAddress, Integer port) {
        return DeviceActivationStatus.builder()
                .deviceId(deviceId)
                .deviceName(deviceName)
                .ipAddress(ipAddress)
                .port(port)
                .status(DeviceActivationStatusEnum.NOT_ACTIVATED.name())
                .reconnectCount(0)
                .build();
    }
    
    /**
     * 创建激活中状态
     */
    public static DeviceActivationStatus activating(Long deviceId, String deviceName, String ipAddress, Integer port) {
        return DeviceActivationStatus.builder()
                .deviceId(deviceId)
                .deviceName(deviceName)
                .ipAddress(ipAddress)
                .port(port)
                .status(DeviceActivationStatusEnum.ACTIVATING.name())
                .reconnectCount(0)
                .build();
    }
    
    /**
     * 创建已激活状态
     */
    public static DeviceActivationStatus activated(Long deviceId, String deviceName, String ipAddress, Integer port) {
        return DeviceActivationStatus.builder()
                .deviceId(deviceId)
                .deviceName(deviceName)
                .ipAddress(ipAddress)
                .port(port)
                .status(DeviceActivationStatusEnum.ACTIVATED.name())
                .lastActivatedTime(LocalDateTime.now())
                .reconnectCount(0)
                .build();
    }
    
    /**
     * 创建激活失败状态
     */
    public static DeviceActivationStatus failed(Long deviceId, String deviceName, String ipAddress, Integer port, String errorMessage) {
        return DeviceActivationStatus.builder()
                .deviceId(deviceId)
                .deviceName(deviceName)
                .ipAddress(ipAddress)
                .port(port)
                .status(DeviceActivationStatusEnum.FAILED.name())
                .errorMessage(errorMessage)
                .reconnectCount(0)
                .build();
    }
    
    /**
     * 创建重连中状态
     */
    public static DeviceActivationStatus reconnecting(Long deviceId, String deviceName, String ipAddress, Integer port, int reconnectCount) {
        return DeviceActivationStatus.builder()
                .deviceId(deviceId)
                .deviceName(deviceName)
                .ipAddress(ipAddress)
                .port(port)
                .status(DeviceActivationStatusEnum.RECONNECTING.name())
                .reconnectCount(reconnectCount)
                .build();
    }
    
    /**
     * 判断是否已激活
     */
    public boolean isActivated() {
        return DeviceActivationStatusEnum.ACTIVATED.name().equals(status);
    }
    
    /**
     * 判断是否激活失败
     */
    public boolean isFailed() {
        return DeviceActivationStatusEnum.FAILED.name().equals(status);
    }
    
    /**
     * 判断是否正在重连
     */
    public boolean isReconnecting() {
        return DeviceActivationStatusEnum.RECONNECTING.name().equals(status);
    }
}
