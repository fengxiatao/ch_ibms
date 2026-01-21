package cn.iocoder.yudao.module.iot.controller.admin.gis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 楼层信息 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 楼层信息 VO")
@Data
public class FloorVO {

    @Schema(description = "楼层ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "101")
    private Long id;

    @Schema(description = "建筑ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long buildingId;

    @Schema(description = "楼层名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "A栋1层")
    private String name;

    @Schema(description = "楼层编码", example = "A-1F")
    private String code;

    @Schema(description = "楼层号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer floorNumber;

    @Schema(description = "楼层类型", example = "标准层")
    private String floorType;

    @Schema(description = "楼层高度（米）", example = "3.5")
    private BigDecimal floorHeight;

    @Schema(description = "楼层面积（平方米）", example = "4800")
    private BigDecimal floorArea;

    @Schema(description = "可用面积（平方米）", example = "4200")
    private BigDecimal usableArea;

    @Schema(description = "主要功能", example = "办公")
    private String primaryFunction;

    @Schema(description = "入住率（%）", example = "85.5")
    private BigDecimal occupancyRate;

    @Schema(description = "最大容纳人数", example = "200")
    private Integer maxOccupancy;

    @Schema(description = "几何数据（WKT格式）", example = "POLYGON((0 0, 80 0, 80 60, 0 60, 0 0))")
    private String geom;

    @Schema(description = "Z坐标（米）", example = "0")
    private BigDecimal absoluteElevation;

    @Schema(description = "房间数量", example = "10")
    private Integer roomCount;

    @Schema(description = "设备数量", example = "60")
    private Integer deviceCount;

    @Schema(description = "在线设备数量", example = "55")
    private Integer onlineDeviceCount;

    @Schema(description = "备注", example = "一楼大厅")
    private String remark;

}












