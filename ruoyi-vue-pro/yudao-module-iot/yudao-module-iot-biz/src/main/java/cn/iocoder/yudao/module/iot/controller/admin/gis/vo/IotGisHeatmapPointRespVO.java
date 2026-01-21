package cn.iocoder.yudao.module.iot.controller.admin.gis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * GIS 热力图点 Response VO
 *
 * @author IBMS Team
 */
@Schema(description = "管理后台 - GIS 热力图点 Response VO")
@Data
public class IotGisHeatmapPointRespVO {

    @Schema(description = "经度", example = "113.264385")
    private BigDecimal longitude;

    @Schema(description = "纬度", example = "23.129112")
    private BigDecimal latitude;

    @Schema(description = "热力值", example = "25.5")
    private BigDecimal value;

    @Schema(description = "权重", example = "1.0")
    private BigDecimal weight;
}












