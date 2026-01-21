package cn.iocoder.yudao.module.iot.core.discovery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 设备扫描请求
 * 
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceScanRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 扫描ID（唯一标识）
     */
    private String scanId;

    /**
     * 请求ID（链路追踪/幂等关联）
     */
    private String requestId;

    /**
     * 租户ID（用于多租户支持）
     */
    private Long tenantId;
    
    /**
     * 扫描类型（onvif, upnp, ssdp等）
     */
    private String scanType;
    
    /**
     * 超时时间（秒）
     */
    private Integer timeout;
    
    /**
     * IP范围（可选，如：192.168.1.0/24）
     */
    private String ipRange;
}
