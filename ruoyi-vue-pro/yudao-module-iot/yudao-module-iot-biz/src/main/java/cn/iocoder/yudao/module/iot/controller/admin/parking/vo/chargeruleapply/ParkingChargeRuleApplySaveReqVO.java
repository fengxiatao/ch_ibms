package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.chargeruleapply;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 收费规则应用新增/修改 Request VO")
@Data
public class ParkingChargeRuleApplySaveReqVO {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "应用名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "临时车收费")
    @NotBlank(message = "应用名称不能为空")
    private String applyName;

    @Schema(description = "适用车场ID列表(JSON数组)", example = "[1,2,3]")
    private String lotIds;

    @Schema(description = "车辆类型：temporary-临时车", example = "temporary")
    private String vehicleCategory;

    @Schema(description = "收费车型(JSON数组)", example = "[\"小型车\",\"中型车\"]")
    private String chargeVehicleTypes;

    @Schema(description = "关联的收费规则ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "收费规则不能为空")
    private Long ruleId;

    @Schema(description = "优先级", example = "10")
    private Integer priority;

    @Schema(description = "是否启用：0-关闭，1-启用", example = "1")
    private Integer enabled;

    @Schema(description = "状态：0-正常，1-停用", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "备注信息")
    private String remark;
}
