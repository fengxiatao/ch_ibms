package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 报警主机更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotAlarmHostUpdateReqVO extends IotAlarmHostBaseVO {

    @Schema(description = "主机ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "主机ID不能为空")
    private Long id;
}
