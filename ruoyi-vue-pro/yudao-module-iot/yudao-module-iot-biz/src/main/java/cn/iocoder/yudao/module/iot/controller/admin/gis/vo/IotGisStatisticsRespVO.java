package cn.iocoder.yudao.module.iot.controller.admin.gis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * GIS 统计信息 Response VO
 *
 * @author IBMS Team
 */
@Schema(description = "管理后台 - GIS 统计信息 Response VO")
@Data
public class IotGisStatisticsRespVO {

    @Schema(description = "园区数量", example = "3")
    private Integer campusCount;

    @Schema(description = "建筑数量", example = "15")
    private Integer buildingCount;

    @Schema(description = "楼层数量", example = "120")
    private Integer floorCount;

    @Schema(description = "房间数量", example = "800")
    private Integer roomCount;

    @Schema(description = "设备总数", example = "1580")
    private Integer deviceCount;

    @Schema(description = "在线设备数", example = "1420")
    private Integer onlineDeviceCount;

    @Schema(description = "离线设备数", example = "160")
    private Integer offlineDeviceCount;

    @Schema(description = "告警设备数", example = "15")
    private Integer alarmDeviceCount;
}












