package cn.iocoder.yudao.module.iot.controller.admin.access.vo.device;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 门禁门控制 Request VO
 */
@Schema(description = "管理后台 - 门禁门控制 Request VO")
@Data
public class IotAccessDoorControlReqVO {

    @Schema(description = "通道ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "通道ID不能为空")
    private Long channelId;

    @Schema(description = "控制类型（open/close/always_open/always_closed/cancel_always）", example = "open")
    private String controlType;

}
