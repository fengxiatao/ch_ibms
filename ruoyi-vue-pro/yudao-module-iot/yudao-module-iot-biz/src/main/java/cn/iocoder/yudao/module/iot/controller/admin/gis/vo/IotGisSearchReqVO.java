package cn.iocoder.yudao.module.iot.controller.admin.gis.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * GIS 空间搜索 Request VO
 *
 * @author IBMS Team
 */
@Schema(description = "管理后台 - GIS 空间搜索 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotGisSearchReqVO extends PageParam {

    @Schema(description = "搜索关键词", example = "温度传感器")
    private String keyword;

    @Schema(description = "图层名称", example = "device")
    private String layer;

    @Schema(description = "边界最小经度", example = "113.0")
    private BigDecimal minX;

    @Schema(description = "边界最小纬度", example = "23.0")
    private BigDecimal minY;

    @Schema(description = "边界最大经度", example = "114.0")
    private BigDecimal maxX;

    @Schema(description = "边界最大纬度", example = "24.0")
    private BigDecimal maxY;

    @Schema(description = "设备类型", example = "sensor")
    private String deviceType;

    @Schema(description = "设备状态", example = "online")
    private String status;
}












