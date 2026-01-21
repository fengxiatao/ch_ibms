package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.zone;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 防区更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotAlarmZoneUpdateReqVO extends IotAlarmZoneBaseVO {

    @Schema(description = "防区ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "防区ID不能为空")
    private Long id;
}
