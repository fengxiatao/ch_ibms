package cn.iocoder.yudao.module.iot.controller.admin.building.vo.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 智能照明统计 Response VO")
@Data
public class IbmsLightingStatisticsVO {

    @Schema(description = "回路总数")
    private Long circuitTotalCount;

    @Schema(description = "开启回路数")
    private Long circuitOnCount;

    @Schema(description = "关闭回路数")
    private Long circuitOffCount;

    @Schema(description = "故障回路数")
    private Long circuitFaultCount;

    @Schema(description = "场景总数")
    private Long sceneTotalCount;

    @Schema(description = "网关总数")
    private Long gatewayTotalCount;

    @Schema(description = "网关在线数")
    private Long gatewayOnlineCount;

    @Schema(description = "控制器总数")
    private Long controllerTotalCount;

    @Schema(description = "控制器在线数")
    private Long controllerOnlineCount;

    @Schema(description = "灯具总数")
    private Long lightTotalCount;

    @Schema(description = "总功率(kW)")
    private BigDecimal totalPower;

    @Schema(description = "当前功率(kW)")
    private BigDecimal currentPower;

    @Schema(description = "今日告警数")
    private Long todayAlarmCount;

    @Schema(description = "未处理告警数")
    private Long unhandledAlarmCount;

}
