package cn.iocoder.yudao.module.iot.core.biz.dto;

import lombok.Data;

/**
 * IoT 设备信息 Response DTO
 *
 * @author 长辉信息科技有限公司
 */
@Data
public class IotDeviceRespDTO {

    /**
     * 设备编号
     */
    private Long id;
    /**
     * 产品标识
     */
    private String productKey;
    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 租户编号
     */
    private Long tenantId;

    // ========== 产品相关字段 ==========

    /**
     * 产品编号
     */
    private Long productId;
    /**
     * 编解码器类型
     */
    private String codecType;
    
    // ========== 设备相关字段 ==========
    
    /**
     * 设备标识（DeviceKey）
     */
    private String deviceKey;
    
    /**
     * 设备地址（IP地址或域名）
     */
    private String address;
    
    /**
     * 设备配置（JSON 字符串格式，存储 username、password 等）
     * 
     * 示例：{"username":"admin","password":"admin123","onvifPort":8999}
     */
    private String config;

}