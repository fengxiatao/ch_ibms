package cn.iocoder.yudao.module.iot.dal.dataobject.device;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 设备位置信息 DO
 * 用于存储设备在室内空间中的位置
 * 
 * @author IBMS Team
 */
@TableName("device_location")
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceLocationDO extends BaseDO {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 所属楼层ID
     */
    private Long floorId;

    /**
     * 所属建筑ID（冗余字段，便于查询）
     */
    private Long buildingId;

    /**
     * 所属区域ID（可选）
     */
    private Long areaId;

    /**
     * 本地X坐标（米）
     * 相对于建筑原点的X坐标
     */
    private BigDecimal localX;

    /**
     * 本地Y坐标（米）
     * 相对于建筑原点的Y坐标
     */
    private BigDecimal localY;

    /**
     * 本地Z坐标（米）
     * 相对于建筑原点的Z坐标（垂直高度）
     */
    private BigDecimal localZ;

    /**
     * 全局经度（可选，缓存字段）
     * 通过本地坐标转换得到
     */
    private BigDecimal globalLongitude;

    /**
     * 全局纬度（可选，缓存字段）
     * 通过本地坐标转换得到
     */
    private BigDecimal globalLatitude;

    /**
     * 全局海拔高度（可选，缓存字段）
     */
    private BigDecimal globalAltitude;

    /**
     * 安装日期
     */
    private java.time.LocalDate installDate;

    /**
     * 安装人员
     */
    private String installer;

    /**
     * 备注
     */
    private String remark;

}


















