package cn.iocoder.yudao.module.iot.controller.admin.building.vo.energy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 能耗仪表新增/修改 Request VO")
@Data
public class IbmsEnergyMeterSaveReqVO {

    @Schema(description = "主键（新增时不传）")
    private Long id;

    @Schema(description = "关联的 IBMS 设备台账 ID（ibms_device.id），推荐从智慧物联设备选择器中选择")
    private Long ibmsDeviceId;

    @Schema(description = "表具编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "表具编号不能为空")
    private String meterCode;

    @Schema(description = "表具名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "表具名称不能为空")
    private String meterName;

    @Schema(description = "表具类型：1-电表 2-水表 3-燃气表 4-冷量表 5-热量表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "表具类型不能为空")
    private Integer meterType;

    @Schema(description = "所属区域ID")
    private Long areaId;

    @Schema(description = "所属区域名称")
    private String areaName;

    @Schema(description = "楼层")
    private String floor;

    @Schema(description = "安装位置")
    private String location;

    @Schema(description = "状态：0-离线 1-在线 2-故障")
    private Integer status;

    @Schema(description = "当前读数")
    private BigDecimal currentReading;

    @Schema(description = "计量单位")
    private String unit;

    @Schema(description = "倍率")
    private BigDecimal multiplier;

    @Schema(description = "通讯方式：1-RS485 2-MBUS 3-LoRa 4-NB-IoT")
    private Integer communicationType;

    @Schema(description = "安装时间")
    private LocalDateTime installTime;

    @Schema(description = "备注")
    private String remark;

}
