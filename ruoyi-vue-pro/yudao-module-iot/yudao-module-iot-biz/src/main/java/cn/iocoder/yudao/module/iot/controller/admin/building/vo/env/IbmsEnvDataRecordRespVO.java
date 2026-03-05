package cn.iocoder.yudao.module.iot.controller.admin.building.vo.env;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 环境监测数据记录 Response VO")
@Data
public class IbmsEnvDataRecordRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "传感器ID")
    private Long sensorId;

    @Schema(description = "传感器编码")
    private String sensorCode;

    @Schema(description = "传感器名称")
    private String sensorName;

    @Schema(description = "温度值")
    private BigDecimal temperature;

    @Schema(description = "湿度值")
    private BigDecimal humidity;

    @Schema(description = "PM2.5值")
    private BigDecimal pm25;

    @Schema(description = "CO2值")
    private BigDecimal co2;

    @Schema(description = "噪音值")
    private BigDecimal noise;

    @Schema(description = "光照值")
    private BigDecimal illumination;

    @Schema(description = "气压值")
    private BigDecimal pressure;

    @Schema(description = "采集时间")
    private LocalDateTime collectTime;

}
