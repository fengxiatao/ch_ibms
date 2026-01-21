package cn.iocoder.yudao.module.iot.controller.admin.access.vo.device;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 门禁设备激活 Request VO
 */
@Schema(description = "管理后台 - 门禁设备激活 Request VO")
@Data
public class IotAccessDeviceActivateReqVO {

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "110")
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

}
