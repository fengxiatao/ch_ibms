package cn.iocoder.yudao.module.iot.core.gateway.dto;

import lombok.Data;
import java.util.List;

/**
 * 设备验证响应（Gateway API）
 * 
 * <p>Gateway 返回给 Biz 的设备验证结果</p>
 */
@Data
public class DeviceVerifyResponse {

    /**
     * 请求ID（链路追踪/幂等关联）
     */
    private String requestId;

    /**
     * 租户ID（多租户上下文）
     */
    private Long tenantId;
    
    /**
     * 验证是否成功
     */
    private Boolean success;
    
    /**
     * 错误消息（验证失败时）
     */
    private String errorMessage;
    
    /**
     * 设备通道列表
     */
    private List<ChannelInfo> channels;
    
    /**
     * 设备信息
     */
    private DeviceInfo deviceInfo;
    
    /**
     * 通道信息
     */
    @Data
    public static class ChannelInfo {
        /**
         * 通道编号
         */
        private Integer channelNo;
        
        /**
         * 通道名称
         */
        private String channelName;
        
        /**
         * 是否支持云台
         */
        private Boolean ptzSupport;
        
        /**
         * 是否支持音频
         */
        private Boolean audioSupport;
        
        /**
         * 分辨率
         */
        private String resolution;
        
        /**
         * Profile Token（ONVIF）
         */
        private String profileToken;
        
        /**
         * 是否在线
         */
        private Boolean online;
        
        /**
         * 摄像机 IP 地址（可选）
         */
        private String ipAddress;
        
        /**
         * 设备类型
         */
        private String deviceType;
        
        /**
         * 序列号
         */
        private String serialNumber;
    }
    
    /**
     * 设备信息
     */
    @Data
    public static class DeviceInfo {
        /**
         * 厂商
         */
        private String vendor;
        
        /**
         * 型号
         */
        private String model;
        
        /**
         * 固件版本
         */
        private String firmwareVersion;
        
        /**
         * 序列号
         */
        private String serialNumber;
        
        /**
         * 设备类型（NVR/DVR/IPC等）
         */
        private String deviceType;
        
        /**
         * 通道数量
         */
        private Integer channelCount;
    }
    
    /**
     * 创建成功响应
     */
    public static DeviceVerifyResponse success(List<ChannelInfo> channels) {
        DeviceVerifyResponse response = new DeviceVerifyResponse();
        response.setSuccess(true);
        response.setChannels(channels);
        return response;
    }
    
    /**
     * 创建失败响应
     */
    public static DeviceVerifyResponse failed(String errorMessage) {
        DeviceVerifyResponse response = new DeviceVerifyResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }
}
