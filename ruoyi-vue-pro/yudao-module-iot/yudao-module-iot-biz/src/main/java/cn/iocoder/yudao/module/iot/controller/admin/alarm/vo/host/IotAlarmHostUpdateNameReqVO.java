package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 报警主机更新名称 Request VO")
@Data
public class IotAlarmHostUpdateNameReqVO {

    @Schema(description = "主机ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "主机ID不能为空")
    private Long id;

    @Schema(description = "主机名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号楼主机")
    @NotBlank(message = "主机名称不能为空")
    private String hostName;
}
