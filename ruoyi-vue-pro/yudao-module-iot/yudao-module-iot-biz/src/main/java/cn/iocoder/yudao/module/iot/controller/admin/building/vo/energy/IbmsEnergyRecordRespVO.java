package cn.iocoder.yudao.module.iot.controller.admin.building.vo.energy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 能耗采集记录 Response VO")
@Data
public class IbmsEnergyRecordRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "仪表ID")
    private Long meterId;

    @Schema(description = "仪表编码")
    private String meterCode;

    @Schema(description = "仪表名称")
    private String meterName;

    @Schema(description = "仪表读数")
    private BigDecimal reading;

    @Schema(description = "本次用量")
    private BigDecimal usage;

    @Schema(description = "采集时间")
    private LocalDateTime collectTime;

}
