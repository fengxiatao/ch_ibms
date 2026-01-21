package cn.iocoder.yudao.module.iot.controller.admin.device.activation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备激活结果响应 VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 设备激活结果响应 VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceActivationResultRespVO {
    
    @Schema(description = "激活状态: activating(激活中), completed(成功), failed(失败)", example = "completed")
    private String status;
    
    @Schema(description = "设备ID（成功时返回）", example = "123")
    private Long deviceId;
    
    @Schema(description = "错误信息（失败时返回）", example = "认证失败：用户名或密码错误")
    private String errorMessage;
}













