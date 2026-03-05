package cn.iocoder.yudao.module.iot.controller.admin.building.vo.energy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 能耗总览 Response VO")
@Data
public class IbmsEnergyOverviewVO {

    // ===== 仪表统计 =====
    @Schema(description = "仪表总数")
    private Long meterTotalCount;

    @Schema(description = "仪表在线数")
    private Long meterOnlineCount;

    @Schema(description = "仪表离线数")
    private Long meterOfflineCount;

    @Schema(description = "仪表故障数")
    private Long meterFaultCount;

    @Schema(description = "电表数量")
    private Long electricMeterCount;

    @Schema(description = "水表数量")
    private Long waterMeterCount;

    @Schema(description = "燃气表数量")
    private Long gasMeterCount;

    @Schema(description = "冷量表数量")
    private Long coldMeterCount;

    @Schema(description = "热量表数量")
    private Long heatMeterCount;

    // ===== 今日能耗 =====
    @Schema(description = "今日用电量(kWh)")
    private BigDecimal todayElectricity;

    @Schema(description = "今日用水量(m³)")
    private BigDecimal todayWater;

    @Schema(description = "今日燃气量(m³)")
    private BigDecimal todayGas;

    @Schema(description = "今日冷量(kWh)")
    private BigDecimal todayCold;

    @Schema(description = "今日热量(kWh)")
    private BigDecimal todayHeat;

    // ===== 本月能耗 =====
    @Schema(description = "本月用电量(kWh)")
    private BigDecimal monthElectricity;

    @Schema(description = "本月用水量(m³)")
    private BigDecimal monthWater;

    @Schema(description = "本月燃气量(m³)")
    private BigDecimal monthGas;

    @Schema(description = "本月冷量(kWh)")
    private BigDecimal monthCold;

    @Schema(description = "本月热量(kWh)")
    private BigDecimal monthHeat;

    // ===== 能耗同环比 =====
    @Schema(description = "用电同比")
    private BigDecimal electricityYoy;

    @Schema(description = "用电环比")
    private BigDecimal electricityMom;

    @Schema(description = "用水同比")
    private BigDecimal waterYoy;

    @Schema(description = "用水环比")
    private BigDecimal waterMom;

    // ===== 告警统计 =====
    @Schema(description = "今日告警数")
    private Long todayAlarmCount;

    @Schema(description = "未处理告警数")
    private Long unhandledAlarmCount;

}
