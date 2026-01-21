package cn.iocoder.yudao.module.iot.controller.admin.access.vo.permissiongroup;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 门禁权限组更新 Request VO
 */
@Schema(description = "管理后台 - 门禁权限组更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class IotAccessPermissionGroupUpdateReqVO extends IotAccessPermissionGroupCreateReqVO {

    @Schema(description = "权限组ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "权限组ID不能为空")
    private Long id;

}
