package cn.iocoder.yudao.module.iot.controller.admin.gis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备信息 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 设备信息 VO")
@Data
public class DeviceVO {

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10001")
    private Long id;

    @Schema(description = "房间ID", example = "1001")
    private Long roomId;

    @Schema(description = "楼层ID", example = "101")
    private Long floorId;

    @Schema(description = "建筑ID", example = "1")
    private Long buildingId;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "照明控制器_A栋1层01室_1")
    private String name;

    @Schema(description = "设备编码", example = "DEV-A-1-01-1")
    private String code;

    @Schema(description = "设备类别", example = "环境设备")
    private String category;

    @Schema(description = "设备类型", example = "照明")
    private String deviceType;

    @Schema(description = "子类型", example = "LED控制器")
    private String subType;

    @Schema(description = "品牌", example = "飞利浦")
    private String brand;

    @Schema(description = "型号", example = "HUE-001")
    private String model;

    @Schema(description = "IP地址", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "设备状态", example = "在线")
    private String status;

    @Schema(description = "健康状态", example = "正常")
    private String healthStatus;

    @Schema(description = "上线时间", example = "2024-01-01 08:00:00")
    private LocalDateTime onlineTime;

    @Schema(description = "离线时间", example = "2024-01-01 18:00:00")
    private LocalDateTime offlineTime;

    @Schema(description = "安装位置描述", example = "天花板中央")
    private String installLocation;

    @Schema(description = "安装高度（米）", example = "2.5")
    private BigDecimal installHeight;

    @Schema(description = "安装日期", example = "2023-12-01")
    private LocalDate installDate;

    @Schema(description = "几何数据（WKT格式）", example = "POINT(8 6)")
    private String geom;

    @Schema(description = "扩展属性（JSON）", example = "{\"brightness\": 80}")
    private String properties;

    @Schema(description = "配置信息（JSON）", example = "{\"mode\": \"auto\"}")
    private String config;

    @Schema(description = "备注", example = "主灯控制器")
    private String remark;

}












