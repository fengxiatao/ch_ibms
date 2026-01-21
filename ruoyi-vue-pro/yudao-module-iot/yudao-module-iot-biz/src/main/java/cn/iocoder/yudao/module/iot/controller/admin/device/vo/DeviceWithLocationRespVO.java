package cn.iocoder.yudao.module.iot.controller.admin.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "管理后台 - 设备（含位置）信息 Response VO")
@Data
public class DeviceWithLocationRespVO {

    // ========== 设备基本信息 ==========

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Long id;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "温度传感器-01")
    private String name;

    @Schema(description = "设备标识", example = "SENSOR_001")
    private String deviceKey;

    @Schema(description = "设备类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "SENSOR")
    private String deviceType;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long productId;

    @Schema(description = "产品名称", example = "温湿度传感器")
    private String productName;

    @Schema(description = "网关ID", example = "5")
    private Long gatewayId;

    @Schema(description = "网关名称", example = "网关-01")
    private String gatewayName;

    @Schema(description = "设备状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "ONLINE")
    private String status;

    @Schema(description = "设备序列号", example = "SN123456789")
    private String serialNumber;

    // ========== 位置信息 ==========

    @Schema(description = "位置ID", example = "1")
    private Long locationId;

    @Schema(description = "所属楼层ID", example = "10")
    private Long floorId;

    @Schema(description = "楼层名称", example = "3F")
    private String floorName;

    @Schema(description = "楼层编号", example = "3")
    private Integer floorNumber;

    @Schema(description = "所属建筑ID", example = "5")
    private Long buildingId;

    @Schema(description = "建筑名称", example = "1号楼")
    private String buildingName;

    @Schema(description = "所属区域ID", example = "100")
    private Long areaId;

    @Schema(description = "区域名称", example = "会议室A")
    private String areaName;

    @Schema(description = "区域类型", example = "ROOM")
    private String areaType;

    @Schema(description = "本地X坐标（米）", example = "15.5")
    private BigDecimal localX;

    @Schema(description = "本地Y坐标（米）", example = "20.3")
    private BigDecimal localY;

    @Schema(description = "本地Z坐标（米）", example = "1.5")
    private BigDecimal localZ;

    @Schema(description = "全局经度", example = "113.264385")
    private BigDecimal globalLongitude;

    @Schema(description = "全局纬度", example = "23.129112")
    private BigDecimal globalLatitude;

    @Schema(description = "全局海拔高度（米）", example = "15.5")
    private BigDecimal globalAltitude;

    // ========== 实时数据 ==========

    @Schema(description = "实时数据", example = "{\"temperature\": 25.5, \"humidity\": 60}")
    private Map<String, Object> realtimeData;

    @Schema(description = "最后通信时间")
    private LocalDateTime lastCommunicationTime;

    // ========== 其他信息 ==========

    @Schema(description = "安装日期", example = "2024-01-15")
    private LocalDateTime installDate;

    @Schema(description = "安装人员", example = "张三")
    private String installer;

    @Schema(description = "备注", example = "安装在天花板上")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}


















