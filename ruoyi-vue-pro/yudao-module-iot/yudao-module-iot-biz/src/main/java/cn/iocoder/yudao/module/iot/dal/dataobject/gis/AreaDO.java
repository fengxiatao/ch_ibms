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
 * 区域 DO
 * 对应数据库表: area (PostgreSQL)
 * 
 * @author IBMS Team
 */
@TableName(value = "area", autoResultMap = false)
@KeySequence("area_id_seq") // PostgreSQL 序列
@Data
@EqualsAndHashCode(callSuper = true)
public class AreaDO extends PostgresBaseDO {

    /**
     * 区域ID
     */
    @TableId
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 所属楼层ID
     */
    private Long floorId;

    /**
     * 所属建筑ID (可选，冗余字段，用于快速查询)
     */
    private Long buildingId;

    /**
     * 所属园区ID (可选，冗余字段，用于快速查询)
     */
    private Long campusId;

    /**
     * 区域名称
     */
    private String name;

    /**
     * 区域编码
     */
    private String code;

    /**
     * 区域类型：ROOM, CORRIDOR, ELEVATOR, STAIRCASE, RESTROOM, EQUIPMENT_ROOM, PUBLIC
     */
    private String areaType;

    /**
     * 子类型（如：office, meeting_room, workspace等）
     */
    private String subType;

    /**
     * 区域面积（平方米）
     */
    private BigDecimal areaSqm;

    /**
     * 最大容纳人数
     */
    private Integer capacity;

    /**
     * 几何数据（真实地理坐标或虚拟坐标）
     */
    private String geom;

    /**
     * 区域边界（虚拟坐标，单位：米）- 如果存在此字段
     */
    @TableField("local_geom")
    private String localGeom;

    /**
     * 中心点
     */
    private String centerPoint;

    /**
     * 最小高度（米）
     */
    private BigDecimal zMin;

    /**
     * 最大高度（米）
     */
    private BigDecimal zMax;

    /**
     * 3D几何数据
     */
    @TableField("geom_3d")
    private String geom3d;

    /**
     * 3D中心点
     */
    @TableField("centroid_3d")
    private String centroid3d;

    /**
     * 连接的区域ID数组（用于快速查询邻接区域）
     */
    private String connectedAreaIds;

    /**
     * 填充颜色
     */
    private String fillColor;

    /**
     * 边框颜色
     */
    private String strokeColor;

    /**
     * 透明度（0-1）
     */
    private BigDecimal opacity;

    /**
     * 显示顺序
     */
    private Integer displayOrder;

    /**
     * 是否可见
     */
    private Boolean isVisible;

    /**
     * IndoorGML ID（符合国际标准）
     */
    private String indoorgmlId;

    /**
     * IndoorGML类型（CellSpace, TransitionSpace等）
     */
    private String indoorgmlType;

    /**
     * 导航网络（JSON格式，存储拓扑网络信息）
     */
    private String navigationNetwork;

    /**
     * 描述
     */
    private String description;

    /**
     * 扩展属性（JSON格式）
     */
    private String properties;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 定时任务配置（JSON格式）
     * 示例：{"deviceInspection":{"enabled":true,"interval":1,"unit":"HOUR","priority":5}}
     */
    @TableField("job_config")
    private String jobConfig;

}
