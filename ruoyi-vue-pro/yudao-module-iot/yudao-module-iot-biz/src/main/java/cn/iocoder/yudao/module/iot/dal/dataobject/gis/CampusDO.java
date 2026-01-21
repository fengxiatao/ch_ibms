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
 * 园区 DO
 * 对应数据库表: campus (PostgreSQL)
 * 
 * 注意：此类使用最小字段集，只包含确定存在的字段
 * 如需添加字段，请先确认 PostgreSQL 表结构
 *
 * @author IBMS Team
 */
@TableName(value = "campus", autoResultMap = false)
@KeySequence("campus_id_seq") // PostgreSQL 序列
@Data
@EqualsAndHashCode(callSuper = true)
public class CampusDO extends PostgresBaseDO {

    /**
     * 园区ID
     */
    @TableId
    private Long id;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 园区名称
     */
    private String name;

    /**
     * 园区编码
     */
    private String code;

    /**
     * 地址
     */
    private String address;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 别名
     */
    private String alias;

    /**
     * 园区类型
     */
    private String campusType;

    /**
     * 园区面积（平方米）
     */
    private BigDecimal area;

    /**
     * 建筑面积（平方米）
     */
    private BigDecimal buildingArea;

    /**
     * 绿化率
     */
    private BigDecimal greenRate;

    /**
     * 容积率
     */
    private BigDecimal floorAreaRatio;

    /**
     * 邮政编码
     */
    private String postalCode;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 物业公司
     */
    private String propertyCompany;

    /**
     * 管理模式
     */
    private String managementMode;

    /**
     * 运营状态
     */
    private String operationStatus;

    /**
     * 几何边界（PostGIS Geometry）
     */
    private String geom;

    /**
     * 中心点（PostGIS Point）
     */
    private String centerPoint;

    /**
     * 海拔高度（米）
     */
    private BigDecimal elevation;

    /**
     * 备注
     */
    private String remark;

    /**
     * 定时任务配置（JSON格式）
     * 示例：{"deviceStatistics":{"enabled":true,"interval":1,"unit":"HOUR","priority":7}}
     */
    @TableField("job_config")
    private String jobConfig;

}

