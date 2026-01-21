package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.passrule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 放行规则 Response VO")
@Data
public class ParkingPassRuleRespVO {

    @Schema(description = "规则ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "规则名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "VIP放行规则")
    private String ruleName;

    @Schema(description = "适用车场ID列表(JSON数组)", example = "[1,2,3]")
    private String lotIds;

    @Schema(description = "特殊车类型(JSON数组)", example = "[\"武警车\",\"警车\"]")
    private String specialVehicleTypes;

    @Schema(description = "车辆类型(JSON数组)", example = "[\"免费车\",\"月租车\"]")
    private String vehicleCategories;

    @Schema(description = "收费车型(JSON数组)", example = "[\"小型车\",\"中型车\"]")
    private String chargeVehicleTypes;

    @Schema(description = "入场确认规则：1-自动放行，2-人工确认", example = "1")
    private Integer entryConfirmRule;

    @Schema(description = "出场确认规则：1-自动放行，2-人工确认", example = "1")
    private Integer exitConfirmRule;

    @Schema(description = "车道权限配置(JSON数组)", example = "[1,2,3]")
    private String laneIds;

    @Schema(description = "优先级", example = "10")
    private Integer priority;

    @Schema(description = "状态：0-正常，1-停用", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "备注信息")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;
}
