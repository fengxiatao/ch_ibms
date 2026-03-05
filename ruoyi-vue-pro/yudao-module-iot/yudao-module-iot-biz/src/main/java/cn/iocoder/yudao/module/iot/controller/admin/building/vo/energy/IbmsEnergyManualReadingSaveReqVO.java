package cn.iocoder.yudao.module.iot.controller.admin.building.vo.energy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "管理后台 - 人工抄表新增 Request VO")
@Data
public class IbmsEnergyManualReadingSaveReqVO {

    @Schema(description = "仪表ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "仪表ID不能为空")
    private Long meterId;

    @Schema(description = "抄表日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "抄表日期不能为空")
    private LocalDate readingDate;

    @Schema(description = "本期读数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "本期读数不能为空")
    private BigDecimal currentReading;

    @Schema(description = "抄表人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "抄表人不能为空")
    private String reader;

    @Schema(description = "备注")
    private String remark;

}
