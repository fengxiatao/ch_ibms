package cn.iocoder.yudao.module.iot.controller.admin.building.vo.env;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 环境告警记录 Response VO")
@Data
public class IbmsEnvAlarmRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "传感器ID")
    private Long sensorId;

    @Schema(description = "传感器编码")
    private String sensorCode;

    @Schema(description = "传感器名称")
    private String sensorName;

    @Schema(description = "告警类型 1-温度 2-湿度 3-PM2.5 4-CO2 5-噪音 6-光照 7-气压 8-离线")
    private Integer alarmType;

    @Schema(description = "告警级别 1-一般 2-重要 3-紧急")
    private Integer alarmLevel;

    @Schema(description = "告警内容")
    private String alarmContent;

    @Schema(description = "告警值")
    private BigDecimal alarmValue;

    @Schema(description = "阈值下限")
    private BigDecimal thresholdMin;

    @Schema(description = "阈值上限")
    private BigDecimal thresholdMax;

    @Schema(description = "告警时间")
    private LocalDateTime alarmTime;

    @Schema(description = "状态 0-未处理 1-处理中 2-已处理 3-已忽略")
    private Integer status;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "处理人")
    private String handler;

    @Schema(description = "处理备注")
    private String handleRemark;

    @Schema(description = "区域名称")
    private String areaName;

}
