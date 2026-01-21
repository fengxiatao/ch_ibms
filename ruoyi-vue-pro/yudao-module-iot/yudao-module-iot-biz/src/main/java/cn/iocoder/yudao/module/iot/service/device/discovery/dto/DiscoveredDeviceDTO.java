package cn.iocoder.yudao.module.iot.service.device.discovery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 发现的设备信息DTO
 * 
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveredDeviceDTO {

    /**
     * 请求ID（链路追踪/幂等关联）
     */
    private String requestId;

    /**
     * 租户ID（多租户上下文）
     */
    private Long tenantId;
    
    /**
     * 设备IP地址
     */
    private String ipAddress;

    
    /**
     * 设备MAC地址
     */
    private String mac;
    
    /**
     * 设备厂商
     */
    private String vendor;
    
    /**
     * 设备型号
     */
    private String model;
    
    /**
     * 设备序列号
     */
    private String serialNumber;
    
    /**
     * 设备名称
     */
    private String deviceName;
    
    /**
     * 设备类型（camera、nvr、dvr等）
     */
    private String deviceType;
    
    /**
     * 固件版本
     */
    private String firmwareVersion;
    
    /**
     * 硬件版本
     */
    private String hardwareVersion;
    
    /**
     * HTTP端口
     */
    private Integer httpPort;
    
    /**
     * RTSP端口
     */
    private Integer rtspPort;
    
    /**
     * ONVIF端口
     */
    private Integer onvifPort;
    
    /**
     * 是否支持ONVIF
     */
    private Boolean onvifSupported;
    
    /**
     * 发现方式（onvif、scan、ssdp）
     */
    private String discoveryMethod;
    
    /**
     * 发现时间
     */
    private LocalDateTime discoveryTime;
    
    /**
     * 是否在线
     */
    private Boolean online;
    
    /**
     * 是否已添加到平台
     */
    private Boolean added;
    
    /**
     * 平台设备ID（如果已添加）
     */
    private Long platformDeviceId;
    
    /**
     * 备注
     */
    private String remark;
}







