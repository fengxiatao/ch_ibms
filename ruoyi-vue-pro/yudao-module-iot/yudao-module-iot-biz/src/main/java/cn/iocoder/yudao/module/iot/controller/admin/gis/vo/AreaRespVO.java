package cn.iocoder.yudao.module.iot.controller.admin.gis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 区域响应 VO
 *
 * @author ruoyi
 */
@Schema(description = "管理后台 - 区域响应 VO")
@Data
public class AreaRespVO {

    @Schema(description = "区域ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "楼层ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "101")
    private Long floorId;

    @Schema(description = "建筑ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long buildingId;

    @Schema(description = "园区ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long campusId;

    @Schema(description = "区域名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "101室")
    private String name;

    @Schema(description = "区域编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "B1-F1-R01")
    private String code;

    @Schema(description = "区域类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "ROOM")
    private String areaType;

    @Schema(description = "子类型", example = "office")
    private String subType;

    @Schema(description = "区域面积（平方米）", example = "20.5")
    private BigDecimal areaSqm;

    @Schema(description = "最大容纳人数", example = "4")
    private Integer capacity;

    @Schema(description = "几何数据（GeoJSON）", example = "{\"type\":\"Polygon\",\"coordinates\":[...]}")
    private String geom;

    @Schema(description = "中心点（GeoJSON）", example = "{\"type\":\"Point\",\"coordinates\":[...]}")
    private String centerPoint;

    @Schema(description = "最小高度（米）", example = "0.0")
    private BigDecimal zMin;

    @Schema(description = "最大高度（米）", example = "3.0")
    private BigDecimal zMax;

    @Schema(description = "连接的区域ID数组", example = "[2, 3, 15]")
    private String connectedAreaIds;

    @Schema(description = "填充颜色", example = "#E3F2FD")
    private String fillColor;

    @Schema(description = "边框颜色", example = "#2196F3")
    private String strokeColor;

    @Schema(description = "透明度", example = "0.7")
    private BigDecimal opacity;

    @Schema(description = "显示顺序", example = "1")
    private Integer displayOrder;

    @Schema(description = "是否可见", example = "true")
    private Boolean isVisible;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}











