package cn.iocoder.yudao.module.iot.controller.admin.security.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 安防热力地图数据响应 VO
 *
 * @author 长辉信息
 */
@Schema(description = "管理后台 - 安防热力地图数据响应 VO")
@Data
public class SecurityHeatmapDataRespVO {

    @Schema(description = "区域ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long areaId;

    @Schema(description = "区域名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "大厅")
    private String areaName;

    @Schema(description = "抓拍数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "500")
    private Integer captureCount;

    @Schema(description = "经度", requiredMode = Schema.RequiredMode.REQUIRED, example = "116.404")
    private BigDecimal longitude;

    @Schema(description = "纬度", requiredMode = Schema.RequiredMode.REQUIRED, example = "39.915")
    private BigDecimal latitude;

}



















