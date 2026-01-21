package cn.iocoder.yudao.module.iot.controller.admin.spatial.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DXF文件信息 Response VO
 *
 * @author IBMS Team
 */
@Schema(description = "管理后台 - DXF文件信息 Response VO")
@Data
public class DxfInfoRespVO {

    @Schema(description = "建筑宽度（米）", example = "50.00")
    private BigDecimal buildingWidth;

    @Schema(description = "建筑长度（米）", example = "30.00")
    private BigDecimal buildingHeight;

    @Schema(description = "建筑高度（米，Z轴）", example = "15.00")
    private BigDecimal buildingDepth;

    @Schema(description = "最小X坐标", example = "0.00")
    private BigDecimal minX;

    @Schema(description = "最小Y坐标", example = "0.00")
    private BigDecimal minY;

    @Schema(description = "最小Z坐标", example = "0.00")
    private BigDecimal minZ;

    @Schema(description = "最大X坐标", example = "50.00")
    private BigDecimal maxX;

    @Schema(description = "最大Y坐标", example = "30.00")
    private BigDecimal maxY;

    @Schema(description = "最大Z坐标", example = "15.00")
    private BigDecimal maxZ;

    @Schema(description = "实体数量", example = "1234")
    private Integer entityCount;

    @Schema(description = "图层数量", example = "15")
    private Integer layerCount;

    @Schema(description = "单位", example = "mm")
    private String unit;

    @Schema(description = "缩放因子（转换为米）", example = "0.001")
    private BigDecimal scaleFactor;
}



















































