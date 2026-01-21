package cn.iocoder.yudao.module.iot.controller.admin.gis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * GIS 建筑 Response VO
 *
 * @author IBMS Team
 */
@Schema(description = "管理后台 - GIS 建筑 Response VO")
@Data
public class IotGisBuildingRespVO {

    @Schema(description = "建筑ID", example = "1")
    private Long id;

    @Schema(description = "建筑名称", example = "A栋智能制造中心")
    private String name;

    @Schema(description = "建筑编码", example = "BUILD_A01")
    private String code;

    @Schema(description = "建筑别名", example = "A栋")
    private String alias;

    @Schema(description = "所属园区ID", example = "1")
    private Long campusId;

    @Schema(description = "建筑类型", example = "production")
    private String buildingType;

    @Schema(description = "建筑结构", example = "steel")
    private String buildingStructure;

    @Schema(description = "消防等级", example = "first")
    private String fireRating;

    @Schema(description = "总楼层数", example = "6")
    private Integer totalFloors;

    @Schema(description = "地上楼层数", example = "6")
    private Integer aboveGroundFloors;

    @Schema(description = "地下楼层数", example = "0")
    private Integer undergroundFloors;

    @Schema(description = "建筑高度（米）", example = "24.5")
    private Double height;

    @Schema(description = "建筑面积（平方米）", example = "3600.0")
    private Double builtArea;

    @Schema(description = "备注", example = "主生产车间")
    private String remark;
}










