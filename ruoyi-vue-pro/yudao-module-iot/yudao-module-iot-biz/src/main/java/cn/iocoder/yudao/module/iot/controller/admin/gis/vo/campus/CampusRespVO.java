package cn.iocoder.yudao.module.iot.controller.admin.gis.vo.campus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 园区 Response VO")
@Data
public class CampusRespVO {

    @Schema(description = "园区ID", required = true, example = "1")
    private Long id;

    @Schema(description = "园区名称", required = true, example = "科技园")
    private String name;

    @Schema(description = "园区编码", required = true, example = "CAMPUS001")
    private String code;

    @Schema(description = "园区类型", example = "TECHNOLOGY")
    private String campusType;

    @Schema(description = "地址", example = "广东省深圳市南山区科技园路")
    private String address;

    @Schema(description = "省份", example = "广东省")
    private String province;

    @Schema(description = "城市", example = "深圳市")
    private String city;

    @Schema(description = "区县", example = "南山区")
    private String district;

    @Schema(description = "邮政编码", example = "518000")
    private String postalCode;

    @Schema(description = "总用地面积（平方米）", example = "50000.00")
    private BigDecimal totalAreaSqm;

    @Schema(description = "建筑占地面积（平方米）", example = "20000.00")
    private BigDecimal buildingAreaSqm;

    @Schema(description = "绿化面积（平方米）", example = "15000.00")
    private BigDecimal greenAreaSqm;

    @Schema(description = "绿化率（%）", example = "30.00")
    private BigDecimal greenRate;

    @Schema(description = "容积率", example = "2.5")
    private BigDecimal plotRatio;

    @Schema(description = "建筑数量", example = "5")
    private Integer buildingCount;

    @Schema(description = "最大容纳人数", example = "5000")
    private Integer maxCapacity;

    @Schema(description = "联系人", example = "张三")
    private String contactPerson;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "联系邮箱", example = "zhangsan@example.com")
    private String contactEmail;

    @Schema(description = "物业公司", example = "XX物业管理有限公司")
    private String propertyCompany;

    @Schema(description = "管理模式", example = "SELF_OPERATED")
    private String managementMode;

    @Schema(description = "运营状态", example = "OPERATING")
    private String operationStatus;

    @Schema(description = "几何边界（GeoJSON格式）")
    private String geom;

    @Schema(description = "中心点（GeoJSON格式）")
    private String centroid;

    @Schema(description = "海拔高度（米）", example = "50.5")
    private BigDecimal altitude;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "扩展属性（JSON格式）")
    private String properties;

    @Schema(description = "租户ID", required = true, example = "1")
    private Long tenantId;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", required = true)
    private LocalDateTime updateTime;

}

