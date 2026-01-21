package cn.iocoder.yudao.module.iot.controller.admin.gis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * GIS 更新设备位置 Request VO
 *
 * @author IBMS Team
 */
@Schema(description = "管理后台 - GIS 更新设备位置 Request VO")
@Data
public class IotGisUpdateLocationReqVO {

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @Schema(description = "经度", requiredMode = Schema.RequiredMode.REQUIRED, example = "113.264385")
    @NotNull(message = "经度不能为空")
    private BigDecimal longitude;

    @Schema(description = "纬度", requiredMode = Schema.RequiredMode.REQUIRED, example = "23.129112")
    @NotNull(message = "纬度不能为空")
    private BigDecimal latitude;

    @Schema(description = "海拔（米）", example = "50.5")
    private BigDecimal altitude;

    @Schema(description = "详细地址", example = "广东省广州市天河区xxx")
    private String address;
}












