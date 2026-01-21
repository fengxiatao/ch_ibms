package cn.iocoder.yudao.module.iot.controller.admin.gis.vo.building;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 建筑新增/修改 Request VO")
@Data
public class BuildingSaveReqVO {

    @Schema(description = "建筑ID", example = "1")
    private Long id;

    @Schema(description = "所属园区ID", required = true, example = "1")
    @NotNull(message = "所属园区不能为空")
    private Long campusId;

    @Schema(description = "建筑名称", required = true, example = "A栋")
    @NotBlank(message = "建筑名称不能为空")
    private String name;

    @Schema(description = "建筑编码", required = true, example = "BUILD001")
    @NotBlank(message = "建筑编码不能为空")
    private String code;

    @Schema(description = "建筑类型", example = "OFFICE")
    private String buildingType;

    @Schema(description = "结构类型", example = "FRAME")
    private String structureType;

    @Schema(description = "总楼层数", example = "10")
    private Integer totalFloors;

    @Schema(description = "地上楼层数", example = "8")
    private Integer aboveGroundFloors;

    @Schema(description = "地下楼层数", example = "2")
    private Integer undergroundFloors;

    @Schema(description = "建筑高度（米）", example = "36.5")
    private BigDecimal buildingHeight;

    @Schema(description = "占地面积（平方米）", example = "2000.00")
    private BigDecimal footprintAreaSqm;

    @Schema(description = "总建筑面积（平方米）", example = "16000.00")
    private BigDecimal totalAreaSqm;

    @Schema(description = "可用面积（平方米）", example = "14000.00")
    private BigDecimal usableAreaSqm;

    @Schema(description = "建设年份", example = "2020")
    private Integer constructionYear;

    @Schema(description = "设计单位", example = "XX设计院")
    private String designUnit;

    @Schema(description = "施工单位", example = "XX建筑公司")
    private String constructionUnit;

    @Schema(description = "耐火等级", example = "GRADE_1")
    private String fireResistanceRating;

    @Schema(description = "抗震设防烈度", example = "7")
    private String seismicIntensity;

    @Schema(description = "电梯数量", example = "4")
    private Integer elevatorCount;

    @Schema(description = "是否有中央空调", example = "true")
    private Boolean hasCentralAc;

    @Schema(description = "是否有消防系统", example = "true")
    private Boolean hasFireSystem;

    @Schema(description = "是否有安防系统", example = "true")
    private Boolean hasSecuritySystem;

    @Schema(description = "是否有智能化系统", example = "true")
    private Boolean hasSmartSystem;

    @Schema(description = "供电容量（kVA）", example = "2000.0")
    private BigDecimal powerCapacityKva;

    @Schema(description = "供水容量（m³/日）", example = "100.0")
    private BigDecimal waterCapacityCubic;

    @Schema(description = "几何边界（GeoJSON格式）")
    private String geom;

    @Schema(description = "中心点（GeoJSON格式）")
    private String centroid;

    @Schema(description = "建筑朝向（度）", example = "180.0")
    private BigDecimal orientation;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "扩展属性（JSON格式）")
    private String properties;

}

