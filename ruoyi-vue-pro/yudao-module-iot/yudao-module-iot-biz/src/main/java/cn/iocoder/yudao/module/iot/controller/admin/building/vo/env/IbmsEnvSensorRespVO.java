package cn.iocoder.yudao.module.iot.controller.admin.building.vo.env;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 环境传感器 Response VO")
@Data
public class IbmsEnvSensorRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "传感器编码")
    private String sensorCode;

    @Schema(description = "传感器名称")
    private String sensorName;

    @Schema(description = "传感器类型 1-温湿度 2-PM2.5 3-CO2 4-噪音 5-光照 6-气压")
    private Integer sensorType;

    @Schema(description = "型号")
    private String model;

    @Schema(description = "品牌")
    private String brand;

    @Schema(description = "区域ID")
    private Long areaId;

    @Schema(description = "区域名称")
    private String areaName;

    @Schema(description = "安装位置")
    private String installLocation;

    @Schema(description = "安装时间")
    private LocalDateTime installTime;

    @Schema(description = "采集周期（秒）")
    private Integer collectInterval;

    @Schema(description = "状态 0-离线 1-在线 2-故障")
    private Integer status;

    @Schema(description = "最新温度值")
    private BigDecimal temperature;

    @Schema(description = "最新湿度值")
    private BigDecimal humidity;

    @Schema(description = "最新PM2.5值")
    private BigDecimal pm25;

    @Schema(description = "最新CO2值")
    private BigDecimal co2;

    @Schema(description = "最新噪音值")
    private BigDecimal noise;

    @Schema(description = "最新光照值")
    private BigDecimal illumination;

    @Schema(description = "最新气压值")
    private BigDecimal pressure;

    @Schema(description = "最后通讯时间")
    private LocalDateTime lastCommunicateTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
