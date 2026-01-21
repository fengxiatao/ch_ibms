package cn.iocoder.yudao.module.iot.controller.admin.gis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * GIS 热力图 Request VO
 *
 * @author IBMS Team
 */
@Schema(description = "管理后台 - GIS 热力图 Request VO")
@Data
public class IotGisHeatmapReqVO {

    @Schema(description = "最小经度", example = "113.0")
    private BigDecimal minX;

    @Schema(description = "最小纬度", example = "23.0")
    private BigDecimal minY;

    @Schema(description = "最大经度", example = "114.0")
    private BigDecimal maxX;

    @Schema(description = "最大纬度", example = "24.0")
    private BigDecimal maxY;

    @Schema(description = "设备类型", example = "sensor")
    private String deviceType;

    @Schema(description = "热力值类型", example = "count")
    private String valueType = "count"; // count, temperature, humidity, etc.
}












