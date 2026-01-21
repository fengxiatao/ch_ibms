package cn.iocoder.yudao.module.iot.controller.admin.access.vo.department;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 门禁部门更新 Request VO
 */
@Schema(description = "管理后台 - 门禁部门更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class IotAccessDepartmentUpdateReqVO extends IotAccessDepartmentCreateReqVO {

    @Schema(description = "部门ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "部门ID不能为空")
    private Long id;

}
