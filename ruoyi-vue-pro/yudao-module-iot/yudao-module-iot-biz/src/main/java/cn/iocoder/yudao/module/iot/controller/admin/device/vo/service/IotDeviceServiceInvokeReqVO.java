package cn.iocoder.yudao.module.iot.controller.admin.device.vo.service;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * IoT 设备服务调用 - 请求 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - IoT 设备服务调用请求 Request VO")
@Data
public class IotDeviceServiceInvokeReqVO {

    @Schema(description = "设备ID", required = true, example = "1")
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @Schema(description = "服务标识符", required = true, example = "Snapshot")
    @NotBlank(message = "服务标识符不能为空")
    private String serviceIdentifier;

    @Schema(description = "服务名称", example = "抓拍")
    private String serviceName;

    @Schema(description = "服务参数（JSON格式）", example = "{\"quality\":\"high\"}")
    private Map<String, Object> serviceParams;
}












