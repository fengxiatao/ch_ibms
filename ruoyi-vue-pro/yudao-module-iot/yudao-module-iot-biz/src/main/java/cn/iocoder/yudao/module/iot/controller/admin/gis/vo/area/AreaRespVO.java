package cn.iocoder.yudao.module.iot.controller.admin.gis.vo.area;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 区域 Response VO")
@Data
public class AreaRespVO {

    @Schema(description = "区域ID", required = true, example = "1")
    private Long id;

    @Schema(description = "所属楼层ID", required = true, example = "1")
    private Long floorId;

    @Schema(description = "区域名称", required = true, example = "办公区A")
    private String name;

    @Schema(description = "区域编码", required = true, example = "AREA001")
    private String code;

    @Schema(description = "区域类型", example = "OFFICE")
    private String areaType;

    @Schema(description = "区域面积（平方米）", example = "150.00")
    private BigDecimal areaSize;

    @Schema(description = "可用面积（平方米）", example = "135.00")
    private BigDecimal usableArea;

    @Schema(description = "最大容纳人数", example = "30")
    private Integer maxOccupancy;

    @Schema(description = "当前人数", example = "25")
    private Integer currentOccupancy;

    @Schema(description = "主要用途", example = "办公")
    private String primaryPurpose;

    @Schema(description = "责任人", example = "张三")
    private String responsiblePerson;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "是否启用", example = "true")
    private Boolean isActive;

    @Schema(description = "是否有监控", example = "true")
    private Boolean hasCctv;

    @Schema(description = "是否有门禁", example = "true")
    private Boolean hasAccessControl;

    @Schema(description = "是否有消防设施", example = "true")
    private Boolean hasFireEquipment;

    @Schema(description = "温度设定（℃）", example = "24.0")
    private BigDecimal temperatureSetting;

    @Schema(description = "湿度设定（%）", example = "50.0")
    private BigDecimal humiditySetting;

    @Schema(description = "照明功率（W）", example = "1200.0")
    private BigDecimal lightingPower;

    @Schema(description = "插座功率（W）", example = "3000.0")
    private BigDecimal socketPower;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "边界坐标（GeoJSON格式）")
    private String boundary;

    @Schema(description = "扩展属性（JSON格式）")
    private String properties;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", required = true)
    private LocalDateTime updateTime;

}

