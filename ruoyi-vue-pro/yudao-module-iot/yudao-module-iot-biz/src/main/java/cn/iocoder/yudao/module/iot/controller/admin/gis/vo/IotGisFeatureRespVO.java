package cn.iocoder.yudao.module.iot.controller.admin.gis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * GIS 要素 Response VO
 *
 * @author IBMS Team
 */
@Schema(description = "管理后台 - GIS 要素 Response VO")
@Data
public class IotGisFeatureRespVO {

    @Schema(description = "要素ID", example = "1")
    private Long id;

    @Schema(description = "要素名称", example = "温度传感器01")
    private String name;

    @Schema(description = "要素类型", example = "device")
    private String type;

    @Schema(description = "经度", example = "113.264385")
    private BigDecimal longitude;

    @Schema(description = "纬度", example = "23.129112")
    private BigDecimal latitude;

    @Schema(description = "几何类型", example = "Point")
    private String geometryType;

    @Schema(description = "属性信息")
    private Map<String, Object> properties;
}












