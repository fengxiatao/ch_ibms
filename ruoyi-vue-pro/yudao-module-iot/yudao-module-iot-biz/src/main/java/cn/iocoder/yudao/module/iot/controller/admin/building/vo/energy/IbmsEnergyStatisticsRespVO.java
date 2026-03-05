package cn.iocoder.yudao.module.iot.controller.admin.building.vo.energy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "管理后台 - 能耗日统计 Response VO")
@Data
public class IbmsEnergyStatisticsRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "统计日期")
    private LocalDate statisticsDate;

    @Schema(description = "仪表ID")
    private Long meterId;

    @Schema(description = "仪表编码")
    private String meterCode;

    @Schema(description = "仪表名称")
    private String meterName;

    @Schema(description = "仪表类型")
    private Integer meterType;

    @Schema(description = "区域ID")
    private Long areaId;

    @Schema(description = "区域名称")
    private String areaName;

    @Schema(description = "起始读数")
    private BigDecimal startReading;

    @Schema(description = "结束读数")
    private BigDecimal endReading;

    @Schema(description = "日用量")
    private BigDecimal dailyUsage;

    @Schema(description = "峰时用量")
    private BigDecimal peakUsage;

    @Schema(description = "谷时用量")
    private BigDecimal valleyUsage;

    @Schema(description = "平时用量")
    private BigDecimal flatUsage;

    @Schema(description = "同比增长率")
    private BigDecimal yoyGrowthRate;

    @Schema(description = "环比增长率")
    private BigDecimal momGrowthRate;

}
