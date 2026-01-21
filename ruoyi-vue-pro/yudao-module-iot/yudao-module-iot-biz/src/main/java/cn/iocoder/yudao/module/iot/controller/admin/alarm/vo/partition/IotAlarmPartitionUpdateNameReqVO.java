package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.partition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 报警分区更新名称 Request VO")
@Data
public class IotAlarmPartitionUpdateNameReqVO {

    @Schema(description = "分区ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "分区ID不能为空")
    private Long id;

    @Schema(description = "分区名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一楼分区")
    @NotBlank(message = "分区名称不能为空")
    private String partitionName;
}
