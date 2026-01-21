package cn.iocoder.yudao.module.iot.controller.admin.gis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * GIS 空间关系 Response VO
 *
 * @author IBMS Team
 */
@Schema(description = "管理后台 - GIS 空间关系 Response VO")
@Data
public class IotGisSpatialRelationRespVO {

    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "设备名称", example = "温度传感器01")
    private String deviceName;

    @Schema(description = "经度", example = "113.264385")
    private BigDecimal longitude;

    @Schema(description = "纬度", example = "23.129112")
    private BigDecimal latitude;

    @Schema(description = "所属园区ID", example = "1")
    private Long campusId;

    @Schema(description = "所属园区名称", example = "科技园")
    private String campusName;

    @Schema(description = "所属建筑ID", example = "1")
    private Long buildingId;

    @Schema(description = "所属建筑名称", example = "A栋")
    private String buildingName;

    @Schema(description = "所属楼层ID", example = "1")
    private Long floorId;

    @Schema(description = "所属楼层名称", example = "3F")
    private String floorName;

    @Schema(description = "所属房间ID", example = "1")
    private Long roomId;

    @Schema(description = "所属房间名称", example = "301")
    private String roomName;

    @Schema(description = "相邻设备数量", example = "5")
    private Integer nearbyDeviceCount;

    @Schema(description = "同房间设备数量", example = "3")
    private Integer sameRoomDeviceCount;

    @Schema(description = "同楼层设备数量", example = "20")
    private Integer sameFloorDeviceCount;

    @Schema(description = "同建筑设备数量", example = "80")
    private Integer sameBuildingDeviceCount;
}












