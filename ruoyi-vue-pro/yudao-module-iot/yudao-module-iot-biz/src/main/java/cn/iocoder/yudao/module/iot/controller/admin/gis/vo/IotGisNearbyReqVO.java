package cn.iocoder.yudao.module.iot.controller.admin.gis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * GIS 附近查询 Request VO
 *
 * @author IBMS Team
 */
@Schema(description = "管理后台 - GIS 附近查询 Request VO")
@Data
public class IotGisNearbyReqVO {

    @Schema(description = "中心点经度", requiredMode = Schema.RequiredMode.REQUIRED, example = "113.264385")
    @NotNull(message = "经度不能为空")
    private BigDecimal longitude;

    @Schema(description = "中心点纬度", requiredMode = Schema.RequiredMode.REQUIRED, example = "23.129112")
    @NotNull(message = "纬度不能为空")
    private BigDecimal latitude;

    @Schema(description = "搜索半径（米）", example = "1000")
    private Integer radius = 1000;

    @Schema(description = "返回数量限制", example = "10")
    private Integer limit = 10;

    @Schema(description = "设备类型", example = "sensor")
    private String deviceType;

    @Schema(description = "设备状态", example = "online")
    private String status;
}












