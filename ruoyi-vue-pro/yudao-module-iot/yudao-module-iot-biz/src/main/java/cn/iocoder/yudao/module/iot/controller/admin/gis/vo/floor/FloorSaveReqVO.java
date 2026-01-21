package cn.iocoder.yudao.module.iot.controller.admin.gis.vo.floor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 楼层新增/修改 Request VO")
@Data
public class FloorSaveReqVO {

    @Schema(description = "楼层ID", example = "1")
    private Long id;

    @Schema(description = "所属建筑ID", required = true, example = "1")
    @NotNull(message = "所属建筑不能为空")
    private Long buildingId;

    @Schema(description = "楼层名称", required = true, example = "1层")
    @NotBlank(message = "楼层名称不能为空")
    private String name;

    @Schema(description = "楼层编码", required = true, example = "FLOOR001")
    @NotBlank(message = "楼层编码不能为空")
    private String code;

    @Schema(description = "楼层号", required = true, example = "1")
    @NotNull(message = "楼层号不能为空")
    private Integer floorNumber;

    @Schema(description = "楼层类型", example = "OFFICE")
    private String floorType;

    @Schema(description = "楼层高度（米）", example = "3.5")
    private BigDecimal floorHeight;

    @Schema(description = "楼层面积（平方米）", example = "2000.00")
    private BigDecimal floorArea;

    @Schema(description = "可用面积（平方米）", example = "1800.00")
    private BigDecimal usableArea;

    @Schema(description = "主要功能", example = "办公")
    private String primaryFunction;

    @Schema(description = "入住率（%）", example = "85.5")
    private BigDecimal occupancyRate;

    @Schema(description = "最大容纳人数", example = "200")
    private Integer maxOccupancy;

    @Schema(description = "是否有喷淋系统", example = "true")
    private Boolean hasSprinkler;

    @Schema(description = "是否有烟雾探测器", example = "true")
    private Boolean hasSmokeDetector;

    @Schema(description = "是否有紧急出口", example = "true")
    private Boolean hasEmergencyExit;

    @Schema(description = "紧急出口数量", example = "2")
    private Integer emergencyExitCount;

    @Schema(description = "空调类型", example = "CENTRAL")
    private String acType;

    @Schema(description = "夏季设计温度（℃）", example = "26.0")
    private BigDecimal designTempSummer;

    @Schema(description = "冬季设计温度（℃）", example = "20.0")
    private BigDecimal designTempWinter;

    @Schema(description = "描述")
    private String description;

}

