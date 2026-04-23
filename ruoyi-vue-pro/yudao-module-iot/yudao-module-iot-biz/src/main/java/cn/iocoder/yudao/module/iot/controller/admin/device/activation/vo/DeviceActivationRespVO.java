package cn.iocoder.yudao.module.iot.controller.admin.device.activation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 设备激活结果（deviceId = ibms_device.id）
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 设备激活结果（IBMS）")
@Data
public class DeviceActivationRespVO {
    
    @Schema(description = "激活请求ID", example = "uuid")
    private String activationId;
    
    @Schema(description = "激活状态", example = "activating/completed/failed/not_found")
    private String status;
    
    @Schema(description = "IBMS 设备主键 ibms_device.id（成功时返回）", example = "1")
    private Long deviceId;
    
    @Schema(description = "错误信息（失败时返回）", example = "认证失败：用户名或密码错误")
    private String errorMessage;
}

















