package cn.iocoder.yudao.module.iot.controller.admin.gis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * GIS 边界范围查询 Request VO
 *
 * @author IBMS Team
 */
@Schema(description = "管理后台 - GIS 边界范围查询 Request VO")
@Data
public class IotGisDevicesInBoundsReqVO {

    @Schema(description = "最小经度", requiredMode = Schema.RequiredMode.REQUIRED, example = "113.0")
    @NotNull(message = "最小经度不能为空")
    private BigDecimal minX;

    @Schema(description = "最小纬度", requiredMode = Schema.RequiredMode.REQUIRED, example = "23.0")
    @NotNull(message = "最小纬度不能为空")
    private BigDecimal minY;

    @Schema(description = "最大经度", requiredMode = Schema.RequiredMode.REQUIRED, example = "114.0")
    @NotNull(message = "最大经度不能为空")
    private BigDecimal maxX;

    @Schema(description = "最大纬度", requiredMode = Schema.RequiredMode.REQUIRED, example = "24.0")
    @NotNull(message = "最大纬度不能为空")
    private BigDecimal maxY;

    @Schema(description = "设备类型", example = "sensor")
    private String deviceType;

    @Schema(description = "设备状态", example = "online")
    private String status;
}












