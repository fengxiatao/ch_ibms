package cn.iocoder.yudao.module.iot.controller.admin.building.vo.bac;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 楼宇自控统计 Response VO")
@Data
public class IbmsBacStatisticsVO {

    // ===== 暖通空调设备统计 =====
    @Schema(description = "暖通设备总数")
    private Long hvacTotalCount;

    @Schema(description = "暖通设备在线数")
    private Long hvacOnlineCount;

    @Schema(description = "暖通设备运行数")
    private Long hvacRunningCount;

    @Schema(description = "暖通设备故障数")
    private Long hvacFaultCount;

    @Schema(description = "空调主机数量")
    private Long airConditionerCount;

    @Schema(description = "新风机组数量")
    private Long freshAirCount;

    @Schema(description = "送风机数量")
    private Long supplyFanCount;

    @Schema(description = "排风机数量")
    private Long exhaustFanCount;

    // ===== 给排水设备统计 =====
    @Schema(description = "给排水设备总数")
    private Long waterTotalCount;

    @Schema(description = "给排水设备在线数")
    private Long waterOnlineCount;

    @Schema(description = "给排水设备运行数")
    private Long waterRunningCount;

    @Schema(description = "给排水设备故障数")
    private Long waterFaultCount;

    @Schema(description = "生活水泵数量")
    private Long domesticPumpCount;

    @Schema(description = "消防水泵数量")
    private Long firePumpCount;

    @Schema(description = "污水泵数量")
    private Long sewagePumpCount;

    @Schema(description = "水箱数量")
    private Long waterTankCount;

    // ===== 告警统计 =====
    @Schema(description = "今日告警数")
    private Long todayAlarmCount;

    @Schema(description = "未处理告警数")
    private Long unhandledAlarmCount;

    @Schema(description = "紧急告警数")
    private Long urgentAlarmCount;

    // ===== 能耗统计 =====
    @Schema(description = "暖通当前功率(kW)")
    private BigDecimal hvacCurrentPower;

    @Schema(description = "给排水当前功率(kW)")
    private BigDecimal waterCurrentPower;

}
