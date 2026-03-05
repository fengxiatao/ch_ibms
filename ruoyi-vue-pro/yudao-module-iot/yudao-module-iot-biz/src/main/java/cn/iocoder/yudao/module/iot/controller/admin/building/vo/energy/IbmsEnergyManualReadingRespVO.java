package cn.iocoder.yudao.module.iot.controller.admin.building.vo.energy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 人工抄表记录 Response VO")
@Data
public class IbmsEnergyManualReadingRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "仪表ID")
    private Long meterId;

    @Schema(description = "仪表编码")
    private String meterCode;

    @Schema(description = "仪表名称")
    private String meterName;

    @Schema(description = "抄表日期")
    private LocalDate readingDate;

    @Schema(description = "抄表时间")
    private LocalDateTime readingTime;

    @Schema(description = "上期读数")
    private BigDecimal lastReading;

    @Schema(description = "本期读数")
    private BigDecimal currentReading;

    @Schema(description = "本期用量")
    private BigDecimal consumption;

    @Schema(description = "抄表人")
    private String reader;

    @Schema(description = "状态：0-待复核 1-已确认 2-已作废")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "复核人")
    private String reviewer;

    @Schema(description = "复核时间")
    private LocalDateTime reviewTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
