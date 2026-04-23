package cn.iocoder.yudao.module.iot.controller.admin.ibms.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Schema(description = "管理后台 - IBMS 设备批量更新分组")
@Data
public class IbmsDeviceUpdateGroupReqVO {

    @Schema(description = "设备主键（ibms_device.id）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "设备编号列表不能为空")
    private Set<Long> ids;

    @Schema(description = "分组编号列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "分组编号列表不能为空")
    private Set<Long> groupIds;
}
