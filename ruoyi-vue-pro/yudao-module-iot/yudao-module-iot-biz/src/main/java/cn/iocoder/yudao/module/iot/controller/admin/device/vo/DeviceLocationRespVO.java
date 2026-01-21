package cn.iocoder.yudao.module.iot.controller.admin.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 设备位置信息 Response VO")
@Data
public class DeviceLocationRespVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Long deviceId;

    @Schema(description = "设备名称", example = "温度传感器-01")
    private String deviceName;

    @Schema(description = "设备类型", example = "SENSOR")
    private String deviceType;

    @Schema(description = "所属楼层ID", example = "10")
    private Long floorId;

    @Schema(description = "楼层名称", example = "3F")
    private String floorName;

    @Schema(description = "所属建筑ID", example = "5")
    private Long buildingId;

    @Schema(description = "建筑名称", example = "1号楼")
    private String buildingName;

    @Schema(description = "所属区域ID", example = "100")
    private Long areaId;

    @Schema(description = "区域名称", example = "会议室A")
    private String areaName;

    @Schema(description = "本地X坐标（米）", requiredMode = Schema.RequiredMode.REQUIRED, example = "15.5")
    private BigDecimal localX;

    @Schema(description = "本地Y坐标（米）", requiredMode = Schema.RequiredMode.REQUIRED, example = "20.3")
    private BigDecimal localY;

    @Schema(description = "本地Z坐标（米）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.5")
    private BigDecimal localZ;

    @Schema(description = "全局经度", example = "113.264385")
    private BigDecimal globalLongitude;

    @Schema(description = "全局纬度", example = "23.129112")
    private BigDecimal globalLatitude;

    @Schema(description = "全局海拔高度（米）", example = "15.5")
    private BigDecimal globalAltitude;

    @Schema(description = "安装日期", example = "2024-01-15")
    private LocalDate installDate;

    @Schema(description = "安装人员", example = "张三")
    private String installer;

    @Schema(description = "备注", example = "安装在天花板上")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}


















