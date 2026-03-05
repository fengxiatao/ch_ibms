package cn.iocoder.yudao.module.iot.controller.admin.building.vo.energy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "管理后台 - 能耗费率新增/修改 Request VO")
@Data
public class IbmsEnergyRateSaveReqVO {

    @Schema(description = "主键（新增时不传）")
    private Long id;

    @Schema(description = "费率名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "费率名称不能为空")
    private String rateName;

    @Schema(description = "能源类型：1-电力 2-水 3-燃气 4-冷 5-热", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "能源类型不能为空")
    private Integer energyType;

    @Schema(description = "费率类型：1-单一费率 2-阶梯费率 3-峰谷电价", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "费率类型不能为空")
    private Integer rateType;

    @Schema(description = "阶梯等级")
    private Integer tierLevel;

    @Schema(description = "阶梯起始用量")
    private BigDecimal tierStart;

    @Schema(description = "阶梯结束用量")
    private BigDecimal tierEnd;

    @Schema(description = "时段类型：peak-峰 valley-谷 flat-平")
    private String timePeriod;

    @Schema(description = "开始时间")
    private LocalTime startTime;

    @Schema(description = "结束时间")
    private LocalTime endTime;

    @Schema(description = "单价（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "单价不能为空")
    private BigDecimal unitPrice;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @Schema(description = "生效日期")
    private LocalDate effectiveDate;

    @Schema(description = "失效日期")
    private LocalDate expireDate;

    @Schema(description = "备注")
    private String remark;

}
