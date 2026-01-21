package cn.iocoder.yudao.module.iot.controller.admin.gis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * GIS 距离测量 Response VO
 *
 * @author IBMS Team
 */
@Schema(description = "管理后台 - GIS 距离测量 Response VO")
@Data
public class IotGisDistanceRespVO {

    @Schema(description = "距离（米）", example = "1523.45")
    private BigDecimal distanceInMeters;

    @Schema(description = "距离（千米）", example = "1.52")
    private BigDecimal distanceInKilometers;

    @Schema(description = "起点经度", example = "113.264385")
    private BigDecimal startLongitude;

    @Schema(description = "起点纬度", example = "23.129112")
    private BigDecimal startLatitude;

    @Schema(description = "终点经度", example = "113.280000")
    private BigDecimal endLongitude;

    @Schema(description = "终点纬度", example = "23.140000")
    private BigDecimal endLatitude;
}












