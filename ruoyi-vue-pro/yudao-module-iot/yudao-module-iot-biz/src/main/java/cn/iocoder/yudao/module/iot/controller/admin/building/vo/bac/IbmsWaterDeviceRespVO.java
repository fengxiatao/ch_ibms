package cn.iocoder.yudao.module.iot.controller.admin.building.vo.bac;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 给排水设备 Response VO")
@Data
public class IbmsWaterDeviceRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "设备编码")
    private String deviceCode;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备类型 1-生活水泵 2-消防水泵 3-污水泵 4-水箱 5-阀门")
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

    @Schema(description = "当前水位")
    private BigDecimal waterLevel;

    @Schema(description = "水位上限")
    private BigDecimal waterLevelMax;

    @Schema(description = "水位下限")
    private BigDecimal waterLevelMin;

    @Schema(description = "当前压力")
    private BigDecimal pressure;

    @Schema(description = "当前流量")
    private BigDecimal flowRate;

    @Schema(description = "累计运行时长(小时)")
    private BigDecimal runningHours;

    @Schema(description = "最后通讯时间")
    private LocalDateTime lastCommunicateTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
