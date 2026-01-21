package cn.iocoder.yudao.module.iot.controller.admin.discovery.vo;

import cn.iocoder.yudao.module.iot.service.device.discovery.dto.DiscoveredDeviceDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * IoT 设备发现 - 扫描结果 Response VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - IoT 设备发现扫描结果 Response VO")
@Data
public class DeviceDiscoveryRespVO {
    
    @Schema(description = "扫描请求ID", example = "uuid")
    private String scanId;
    
    @Schema(description = "扫描状态", example = "scanning/completed/failed/not_found")
    private String status;
    
    @Schema(description = "发现的设备列表")
    private List<DiscoveredDeviceDTO> devices;
}

















