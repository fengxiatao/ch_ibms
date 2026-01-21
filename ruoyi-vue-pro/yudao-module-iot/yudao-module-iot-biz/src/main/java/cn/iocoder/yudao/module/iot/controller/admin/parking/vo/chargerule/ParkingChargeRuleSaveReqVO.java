package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.chargerule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Schema(description = "管理后台 - 收费规则新增/修改 Request VO")
@Data
public class ParkingChargeRuleSaveReqVO {

    @Schema(description = "规则ID", example = "1")
    private Long id;

    @Schema(description = "规则名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "工作日收费规则")
    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    @Schema(description = "收费模式：1-按次收费，2-按时收费", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "收费模式不能为空")
    private Integer chargeMode;

    @Schema(description = "按次收费金额", example = "10.00")
    private BigDecimal perTimeFee;

    @Schema(description = "免费时长(分钟)", example = "15")
    private Integer freeMinutes;

    @Schema(description = "首小时费用", example = "5.00")
    private BigDecimal firstHourFee;

    @Schema(description = "超出后每小时费用", example = "3.00")
    private BigDecimal extraHourFee;

    @Schema(description = "每日最高收费", example = "50.00")
    private BigDecimal maxDailyFee;

    @Schema(description = "循环收费方式：1-24小时循环，2-自然日循环", example = "1")
    private Integer cycleType;

    @Schema(description = "夜间折扣", example = "0.5")
    private BigDecimal nightDiscount;

    @Schema(description = "夜间开始时间", example = "22:00:00")
    private LocalTime nightStartTime;

    @Schema(description = "夜间结束时间", example = "06:00:00")
    private LocalTime nightEndTime;

    @Schema(description = "规则详细配置(JSON格式)")
    private String ruleConfig;

    @Schema(description = "状态：0-正常，1-停用", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "备注信息")
    private String remark;
}
