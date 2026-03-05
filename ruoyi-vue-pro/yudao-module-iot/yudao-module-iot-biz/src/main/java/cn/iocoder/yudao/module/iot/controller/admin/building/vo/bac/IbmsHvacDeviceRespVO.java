package cn.iocoder.yudao.module.iot.controller.admin.building.vo.bac;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 暖通空调设备 Response VO")
@Data
public class IbmsHvacDeviceRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "设备编码")
    private String deviceCode;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备类型 1-空调主机 2-新风机组 3-送风机 4-排风机 5-冷却塔 6-冷冻泵 7-冷却泵")
    private Integer deviceType;

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

    @Schema(description = "额定功率")
    private BigDecimal ratedPower;

    @Schema(description = "当前功率")
    private BigDecimal currentPower;

    @Schema(description = "状态 0-离线 1-在线 2-故障")
    private Integer status;

    @Schema(description = "运行状态 0-停止 1-运行 2-待机")
    private Integer runningStatus;

    @Schema(description = "运行模式 1-制冷 2-制热 3-通风 4-自动")
    private Integer runMode;

    @Schema(description = "设定温度")
    private BigDecimal setTemperature;

    @Schema(description = "回风温度")
    private BigDecimal returnTemperature;

    @Schema(description = "送风温度")
    private BigDecimal supplyTemperature;

    @Schema(description = "风速档位")
    private Integer fanSpeed;

    @Schema(description = "阀门开度")
    private Integer valveOpening;

    @Schema(description = "累计运行时长(小时)")
    private BigDecimal runningHours;

    @Schema(description = "最后通讯时间")
    private LocalDateTime lastCommunicateTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
