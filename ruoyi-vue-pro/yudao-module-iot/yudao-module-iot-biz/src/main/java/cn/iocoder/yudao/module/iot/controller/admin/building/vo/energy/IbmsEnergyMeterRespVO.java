package cn.iocoder.yudao.module.iot.controller.admin.building.vo.energy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 能耗计量表 Response VO")
@Data
public class IbmsEnergyMeterRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "关联的 IBMS 设备台账 ID（ibms_device.id）")
    private Long ibmsDeviceId;

    @Schema(description = "关联的 IBMS 设备名称（联表 ibms_device.name，仅展示）")
    private String ibmsDeviceName;

    @Schema(description = "关联的 IBMS 设备编码（联表 ibms_device.device_code，仅展示）")
    private String ibmsDeviceCode;

    @Schema(description = "关联的 IBMS 设备 IP（联表 ibms_device.ip，仅展示）")
    private String ibmsDeviceIp;

    @Schema(description = "关联的 IBMS 设备空间（联表 ibms_device.space，仅展示）")
    private String ibmsDeviceSpace;

    @Schema(description = "仪表编码")
    private String meterCode;

    @Schema(description = "仪表名称")
    private String meterName;

    @Schema(description = "仪表类型 1-电表 2-水表 3-燃气表 4-冷量表 5-热量表")
    private Integer meterType;

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

    @Schema(description = "倍率")
    private BigDecimal ratio;

    @Schema(description = "当前读数")
    private BigDecimal currentReading;

    @Schema(description = "上次读数")
    private BigDecimal lastReading;

    @Schema(description = "今日用量")
    private BigDecimal todayUsage;

    @Schema(description = "本月用量")
    private BigDecimal monthUsage;

    @Schema(description = "状态 0-离线 1-在线 2-故障")
    private Integer status;

    @Schema(description = "最后通讯时间")
    private LocalDateTime lastCommunicateTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
