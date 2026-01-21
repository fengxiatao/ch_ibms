package cn.iocoder.yudao.module.iot.controller.admin.gis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * GIS 设备 Response VO
 *
 * @author IBMS Team
 */
@Schema(description = "管理后台 - GIS 设备 Response VO")
@Data
public class IotGisDeviceRespVO {

    @Schema(description = "设备ID", example = "1")
    private Long id;

    @Schema(description = "设备名称", example = "温度传感器01")
    private String name;

    @Schema(description = "设备编码", example = "DEV001")
    private String code;

    @Schema(description = "设备类型", example = "sensor")
    private String type;

    @Schema(description = "设备状态", example = "online")
    private String status;

    @Schema(description = "经度", example = "113.264385")
    private BigDecimal longitude;

    @Schema(description = "纬度", example = "23.129112")
    private BigDecimal latitude;

    @Schema(description = "距离（米）", example = "150.5")
    private BigDecimal distance;

    @Schema(description = "所属建筑", example = "A栋")
    private String building;

    @Schema(description = "所属楼层", example = "3F")
    private String floor;

    @Schema(description = "所属房间", example = "301")
    private String room;

    @Schema(description = "产品名称", example = "温湿度传感器")
    private String productName;

    @Schema(description = "最后上线时间")
    private LocalDateTime lastOnlineTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}












