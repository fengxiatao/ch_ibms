package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.zone;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 报警防区更新名称 Request VO")
@Data
public class IotAlarmZoneUpdateNameReqVO {

    @Schema(description = "防区ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "防区ID不能为空")
    private Long id;

    @Schema(description = "防区名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "前门防区")
    @NotBlank(message = "防区名称不能为空")
    private String zoneName;
}
