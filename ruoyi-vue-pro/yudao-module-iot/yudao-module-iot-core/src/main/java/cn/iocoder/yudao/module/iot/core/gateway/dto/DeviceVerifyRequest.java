package cn.iocoder.yudao.module.iot.core.gateway.dto;

import lombok.Data;

/**
 * 设备验证请求（Gateway API）
 * 
 * <p>Biz 调用 Gateway 验证设备时使用</p>
 */
@Data
public class DeviceVerifyRequest {

    /**
     * 请求ID（链路追踪/幂等关联）
     */
    private String requestId;

    /**
     * 租户ID（多租户上下文）
     */
    private Long tenantId;
    
    /**
     * 设备 IP 地址
     */
    private String ipAddress;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 厂商（可选，用于选择协议）
     */
    private String vendor;
    
    /**
     * 设备类型（可选）
     */
    private String deviceType;
    
    /**
     * 设备端口（可选，默认根据协议自动选择）
     * - ONVIF: 80
     * - 大华 SDK: 37777
     * - 海康 SDK: 8000
     */
    private Integer port;
}
