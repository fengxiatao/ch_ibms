package cn.iocoder.yudao.module.iot.controller.admin.gis.vo.floor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 楼层 Response VO")
@Data
public class FloorRespVO {

    @Schema(description = "楼层ID", required = true, example = "1")
    private Long id;

    @Schema(description = "所属建筑ID", required = true, example = "1")
    private Long buildingId;

    @Schema(description = "楼层名称", required = true, example = "1层")
    private String name;

    @Schema(description = "楼层编码", required = true, example = "FLOOR001")
    private String code;

    @Schema(description = "楼层号", required = true, example = "1")
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

    @Schema(description = "DXF文件路径", example = "/uploads/floor/xxx.dxf")
    private String dxfFilePath;

    @Schema(description = "DXF文件名称", example = "ST-04.dxf")
    private String dxfFileName;

    @Schema(description = "DXF文件大小（字节）", example = "1024000")
    private Long dxfFileSize;

    @Schema(description = "DXF文件上传时间", example = "2025-10-10 10:00:00")
    private LocalDateTime dxfUploadTime;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", required = true)
    private LocalDateTime updateTime;

}

