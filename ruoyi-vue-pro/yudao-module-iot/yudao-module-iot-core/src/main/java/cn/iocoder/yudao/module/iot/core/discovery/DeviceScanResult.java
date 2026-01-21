package cn.iocoder.yudao.module.iot.core.discovery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 设备扫描结果
 * 
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceScanResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 扫描ID（对应请求的scanId）
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
     * 扫描状态（scanning, completed, failed）
     */
    private String status;
    
    /**
     * 发现的设备列表
     */
    private List<DiscoveredDevice> devices;
    
    /**
     * 错误信息（如果失败）
     */
    private String errorMessage;
}
