package cn.iocoder.yudao.module.iot.dal.dataobject.gis;

// PostgreSQL GIS 数据使用专用的 BaseDO
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 建筑 DO
 * 对应数据库表: building (PostgreSQL)
 *
 * @author IBMS Team
 */
@TableName(value = "building", autoResultMap = false)
@KeySequence("building_id_seq") // PostgreSQL 序列
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildingDO extends PostgresBaseDO {

    /**
     * 建筑ID
     */
    @TableId
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 所属园区ID
     */
    private Long campusId;

    /**
     * 建筑名称
     */
    private String name;

    /**
     * 建筑编码
     */
    private String code;

    /**
     * 别名
     */
    private String alias;

    /**
     * 建筑类型
     */
    private String buildingType;

    /**
     * 建筑结构类型
     */
    private String buildingStructure;

    /**
     * 消防等级
     */
    private String fireRating;

    /**
     * 总楼层数
     */
    private Integer totalFloors;

    /**
     * 地上楼层数
     */
    private Integer aboveGroundFloors;

    /**
     * 地下楼层数
     */
    private Integer undergroundFloors;

    /**
     * 建筑高度（米）
     */
    private BigDecimal buildingHeight;

    /**
     * 建筑面积（平方米）
     */
    private BigDecimal buildingArea;

    /**
     * 可用面积（平方米）
     */
    private BigDecimal usableArea;

    /**
     * 建设年份
     */
    private Integer constructionYear;

    /**
     * 竣工日期
     */
    private java.time.LocalDate completionDate;

    /**
     * 设计单位
     */
    private String designUnit;

    /**
     * 施工单位
     */
    private String constructionUnit;

    /**
     * 是否有电梯
     */
    private Boolean hasElevator;

    /**
     * 电梯数量
     */
    private Integer elevatorCount;

    /**
     * 是否有中央空调
     */
    private Boolean hasCentralAc;

    /**
     * 是否有消防系统
     */
    private Boolean hasFireSystem;

    /**
     * 是否有安防系统
     */
    private Boolean hasSecuritySystem;

    /**
     * 电力容量（kVA）
     */
    private BigDecimal powerCapacity;

    /**
     * 水容量（立方米）
     */
    private BigDecimal waterCapacity;

    /**
     * 管理员
     */
    private String manager;

    /**
     * 管理员电话
     */
    private String managerPhone;

    /**
     * 运营状态
     */
    private String operationStatus;

    /**
     * 几何边界（PostGIS Geometry）
     */
    private String geom;

    /**
     * 入口点（PostGIS Point）
     */
    private String entrancePoint;

    /**
     * 层高（米）
     */
    private BigDecimal floorHeight;

    /**
     * 备注
     */
    private String remark;

    /**
     * 定时任务配置（JSON格式）
     * 示例：{"energyStatistics":{"enabled":true,"interval":1,"unit":"DAY","priority":7}}
     */
    @TableField("job_config")
    private String jobConfig;

}
