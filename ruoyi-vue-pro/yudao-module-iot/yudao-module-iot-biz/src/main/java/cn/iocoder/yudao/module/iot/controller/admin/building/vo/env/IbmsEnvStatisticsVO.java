package cn.iocoder.yudao.module.iot.controller.admin.building.vo.env;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 环境监测统计 Response VO")
@Data
public class IbmsEnvStatisticsVO {

    @Schema(description = "传感器总数")
    private Long totalCount;

    @Schema(description = "在线数量")
    private Long onlineCount;

    @Schema(description = "离线数量")
    private Long offlineCount;

    @Schema(description = "故障数量")
    private Long faultCount;

    @Schema(description = "温湿度传感器数量")
    private Long tempHumidityCount;

    @Schema(description = "PM2.5传感器数量")
    private Long pm25Count;

    @Schema(description = "CO2传感器数量")
    private Long co2Count;

    @Schema(description = "噪音传感器数量")
    private Long noiseCount;

    @Schema(description = "光照传感器数量")
    private Long illuminationCount;

    @Schema(description = "气压传感器数量")
    private Long pressureCount;

    @Schema(description = "今日告警数")
    private Long todayAlarmCount;

    @Schema(description = "未处理告警数")
    private Long unhandledAlarmCount;

    @Schema(description = "室外温度")
    private BigDecimal outdoorTemperature;

    @Schema(description = "室外湿度")
    private BigDecimal outdoorHumidity;

    @Schema(description = "室外PM2.5")
    private BigDecimal outdoorPm25;

    @Schema(description = "风向")
    private String windDirection;

    @Schema(description = "风速")
    private BigDecimal windSpeed;

}
