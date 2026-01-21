package cn.iocoder.yudao.module.iot.controller.admin.access.vo.personpermission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 门禁人员权限批量分配请求 VO
 */
@Schema(description = "管理后台 - 门禁人员权限批量分配请求 VO")
@Data
public class IotAccessPersonPermissionBatchAssignReqVO {

    @Schema(description = "人员ID列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 2, 3]")
    @NotEmpty(message = "人员ID列表不能为空")
    private List<Long> personIds;

    @Schema(description = "权限组ID列表", example = "[1, 2, 3]")
    private List<Long> groupIds;

    @Schema(description = "设备ID列表", example = "[1, 2, 3]")
    private List<Long> deviceIds;

}
