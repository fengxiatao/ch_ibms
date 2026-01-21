package cn.iocoder.yudao.module.iot.dal.dataobject.gis;

// PostgreSQL GIS 数据使用专用的 BaseDO
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * IoT 设备 GIS 数据 DO
 * 
 * 注意：此DO用于操作 PostgreSQL ibms_gis 数据库中的 iot_device 表
 * 与主库中的设备表分离，专门用于GIS空间数据
 *
 * @author 芋道源码
 */
@TableName(value = "iot_device", autoResultMap = false)  // 禁用自动ResultMap，避免误用JacksonTypeHandler
@KeySequence("iot_device_id_seq") // PostgreSQL 序列
@Data
@EqualsAndHashCode(callSuper = true)
public class IotDeviceGisDO extends PostgresBaseDO {

    /**
     * 设备ID
     */
    @TableId
    private Long id;

    /**
     * 房间ID
     */
    private Long roomId;

    /**
     * 楼层ID
     */
    private Long floorId;

    /**
     * 建筑ID
     */
    private Long buildingId;

    /**
     * 园区ID
     */
    private Long campusId;

    /**
     * 设备名称
     */
    private String name;

    /**
     * 设备编码
     */
    private String code;

    /**
     * 设备密钥
     */
    private String deviceKey;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 设备类别
     */
    private String category;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 子类型
     */
    private String subType;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 型号
     */
    private String model;

    /**
     * 制造商
     */
    private String manufacturer;

    /**
     * 序列号
     */
    private String serialNumber;

    /**
     * 固件版本
     */
    private String firmwareVersion;

    /**
     * 硬件版本
     */
    private String hardwareVersion;

    /**
     * 通讯协议
     */
    private String protocol;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * MAC地址
     */
    private String macAddress;

    /**
     * 网关
     */
    private String gateway;

    /**
     * 子网掩码
     */
    private String subnetMask;

    /**
     * 安装日期
     */
    private LocalDate installDate;

    /**
     * 安装位置描述
     */
    private String installLocation;

    /**
     * 安装高度（米）
     */
    private BigDecimal installHeight;

    /**
     * 安装方式
     */
    private String mountingType;

    /**
     * 保修开始日期
     */
    private LocalDate warrantyStart;

    /**
     * 保修结束日期
     */
    private LocalDate warrantyEnd;

    /**
     * 维护周期（天）
     */
    private Integer maintenanceCycle;

    /**
     * 上次维护日期
     */
    private LocalDate lastMaintenanceDate;

    /**
     * 下次维护日期
     */
    private LocalDate nextMaintenanceDate;

    /**
     * 维护供应商
     */
    private String maintenanceVendor;

    /**
     * 设备状态
     */
    private String status;

    /**
     * 健康状态
     */
    private String healthStatus;

    /**
     * 上线时间
     */
    private LocalDateTime onlineTime;

    /**
     * 离线时间
     */
    private LocalDateTime offlineTime;

    /**
     * 额定功率（W）
     */
    private BigDecimal ratedPower;

    /**
     * 额定电压（V）
     */
    private BigDecimal ratedVoltage;

    /**
     * 额定电流（A）
     */
    private BigDecimal ratedCurrent;

    /**
     * 功耗（W）
     */
    private BigDecimal powerConsumption;

    /**
     * 采样间隔（秒）
     */
    private Integer samplingInterval;

    /**
     * 数据保留天数
     */
    private Integer dataRetentionDays;

    /**
     * 是否启用告警
     */
    private Boolean alarmEnabled;

    /**
     * 几何数据（PostGIS geometry）
     * 虚拟坐标（米）
     */
    private String geom;

    /**
     * 海拔高度
     */
    private BigDecimal altitude;

    /**
     * 地址
     */
    private String address;

    /**
     * 扩展属性（JSON）
     */
    private String properties;

    /**
     * 配置信息（JSON）
     */
    private String config;

    /**
     * 备注
     */
    private String remark;

}


