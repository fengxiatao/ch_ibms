package cn.iocoder.yudao.module.iot.dal.dataobject.gis;

// PostgreSQL GIS 数据使用专用的 BaseDO
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 房间 DO
 *
 * @author 芋道源码
 */
@TableName(value = "room", autoResultMap = false)  // 禁用自动ResultMap，避免误用JacksonTypeHandler
@KeySequence("room_id_seq") // PostgreSQL 序列
@Data
@EqualsAndHashCode(callSuper = true)
public class RoomDO extends PostgresBaseDO {

    /**
     * 房间ID
     */
    @TableId
    private Long id;

    /**
     * 楼层ID
     */
    private Long floorId;

    /**
     * 建筑ID
     */
    private Long buildingId;

    /**
     * 房间名称
     */
    private String name;

    /**
     * 房间编码
     */
    private String code;

    /**
     * 房间号
     */
    private String roomNumber;

    /**
     * 房间类型
     */
    private String roomType;

    /**
     * 房间面积（平方米）
     */
    private BigDecimal roomArea;

    /**
     * 层高（米）
     */
    private BigDecimal ceilingHeight;

    /**
     * 主要用途
     */
    private String primaryUse;

    /**
     * 容纳人数
     */
    private Integer capacity;

    /**
     * 是否公共区域
     */
    private Boolean isPublic;

    /**
     * 访问权限等级
     */
    private String accessLevel;

    /**
     * 装修等级
     */
    private String decorationLevel;

    /**
     * 地面材质
     */
    private String floorMaterial;

    /**
     * 墙面材质
     */
    private String wallMaterial;

    /**
     * 是否有窗户
     */
    private Boolean hasWindow;

    /**
     * 窗户数量
     */
    private Integer windowCount;

    /**
     * 是否有门
     */
    private Boolean hasDoor;

    /**
     * 门类型
     */
    private String doorType;

    /**
     * 占用状态
     */
    private String occupancyStatus;

    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * 租约开始日期
     */
    private LocalDate leaseStartDate;

    /**
     * 租约结束日期
     */
    private LocalDate leaseEndDate;

    /**
     * 设计温度（℃）
     */
    private BigDecimal designTemperature;

    /**
     * 设计湿度（%）
     */
    private BigDecimal designHumidity;

    /**
     * 通风换气率
     */
    private BigDecimal ventilationRate;

    /**
     * 几何数据（PostGIS geometry）
     * 虚拟坐标（米）
     */
    private String geom;

    /**
     * 门位置（PostGIS geometry）
     */
    private String doorLocation;

    /**
     * 备注
     */
    private String remark;

}


