package cn.iocoder.yudao.module.iot.dal.dataobject.device;

import cn.iocoder.yudao.framework.mybatis.core.type.LongSetTypeHandler;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfigTypeHandler;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * IoT 设备 DO
 *
 * @author haohao
 */
@TableName(value = "iot_device", autoResultMap = true)
@KeySequence("iot_device_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceDO extends TenantBaseDO {

    /**
     * 设备编号 - 全部设备
     */
    public static final Long DEVICE_ID_ALL = 0L;

    /**
     * 设备 ID，主键，自增
     */
    @TableId
    private Long id;
    /**
     * 设备名称，在产品内唯一，用于标识设备
     */
    private String deviceName;
    /**
     * 设备备注名称
     */
    private String nickname;
    /**
     * 设备序列号
     */
    private String serialNumber;
    /**
     * 设备图片
     */
    private String picUrl;
    /**
     * 设备分组编号集合
     *
     * 关联 {@link IotDeviceGroupDO#getId()}
     */
    @TableField(typeHandler = LongSetTypeHandler.class)
    private Set<Long> groupIds;

    /**
     * 产品编号
     * <p>
     * 关联 {@link IotProductDO#getId()}
     */
    private Long productId;
    /**
     * 产品标识
     * <p>
     * 冗余 {@link IotProductDO#getProductKey()}
     */
    private String productKey;
    /**
     * 设备唯一标识
     * <p>
     * 格式：{productKey}_{serialNumber} 或 {productKey}_{UUID}
     * 用于设备认证和通信
     */
    private String deviceKey;
    /**
     * DXF实体唯一标识
     * <p>
     * 用于关联DXF图纸中的设备，格式：handle或自定义ID
     * 例如："1A3F"（DXF handle）或"INSERT_123"（自定义ID）
     * 用途：
     * 1. 识别设备是否已从DXF导入
     * 2. 用户修改设备名称后仍能正确关联
     * 3. 支持DXF与数据库的双向同步
     */
    private String dxfEntityId;
    /**
     * 设备类型
     * <p>
     * 冗余 {@link IotProductDO#getDeviceType()}
     */
    private Integer deviceType;
    
    /**
     * 所属子系统代码
     * <p>
     * 默认继承自产品的 subsystemCode
     * 例如：security.video（视频监控）
     * 关联 iot_subsystem 表的 code 字段
     */
    private String subsystemCode;
    
    /**
     * 是否手动覆盖子系统归属
     * <p>
     * true - 手动设置的子系统，不跟随产品变更
     * false - 继承自产品（默认）
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean subsystemOverride;
    
    /**
     * 关联的菜单ID列表（JSON数组）
     * <p>
     * 示例：[1001, 1002, 1003]
     * 表示该设备关联到菜单ID 1001、1002、1003
     * 为空或 menuOverride=false 时，继承自产品的 menuIds
     * 关联 system_menu 表的 id 字段
     */
    private String menuIds;
    
    /**
     * 主要菜单ID
     * <p>
     * 用于首页/默认显示，应该是 menuIds 中的一个
     * 为空或 menuOverride=false 时，继承自产品的 primaryMenuId
     * 关联 system_menu 表的 id 字段
     */
    private Long primaryMenuId;
    
    /**
     * 是否手动覆盖菜单配置
     * <p>
     * false - 继承自产品（默认）
     * true - 手动设置的菜单，不跟随产品变更
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean menuOverride;
    
    /**
     * 网关设备编号
     * <p>
     * 子设备需要关联的网关设备 ID
     * <p>
     * 关联 {@link IotDeviceDO#getId()}
     */
    private Long gatewayId;

    /**
     * 设备状态
     * <p>
     * 枚举 {@link IotDeviceStateEnum}
     */
    private Integer state;
    /**
     * 最后上线时间
     */
    private LocalDateTime onlineTime;
    /**
     * 最后离线时间
     */
    private LocalDateTime offlineTime;
    /**
     * 设备激活时间
     */
    private LocalDateTime activeTime;

    /**
     * 固件编号
     *
     * 关联 {@link IotOtaFirmwareDO#getId()}
     */
    private Long firmwareId;

    /**
     * 设备密钥，用于设备认证
     */
    private String deviceSecret;
    /**
     * 认证类型（如一机一密、动态注册）
     */
    // TODO @haohao：是不是要枚举哈
    private String authType;

    /**
     * 定位方式
     * <p>
     * 枚举 {@link cn.iocoder.yudao.module.iot.enums.product.IotLocationTypeEnum}
     */
    private Integer locationType;
    /**
     * 设备位置的纬度
     */
    private BigDecimal latitude;
    /**
     * 设备位置的经度
     */
    private BigDecimal longitude;
    /**
     * 地区编码
     * <p>
     * 关联 Area 的 id
     */
    private Integer areaId;
    /**
     * 设备详细地址
     */
    private String address;

    // ========== 室内空间定位字段 ==========

    /**
     * 所属园区ID
     * <p>
     * 关联 {@link cn.iocoder.yudao.module.iot.dal.dataobject.gis.CampusDO#getId()}
     */
    private Long campusId;
    /**
     * 所属建筑ID
     * <p>
     * 关联 {@link cn.iocoder.yudao.module.iot.dal.dataobject.gis.BuildingDO#getId()}
     */
    private Long buildingId;
    /**
     * 所属楼层ID
     * <p>
     * 关联 {@link cn.iocoder.yudao.module.iot.dal.dataobject.gis.FloorDO#getId()}
     */
    private Long floorId;
    /**
     * 所属区域ID（房间）
     * <p>
     * 关联 {@link cn.iocoder.yudao.module.iot.dal.dataobject.gis.AreaDO#getId()}
     */
    private Long roomId;

    /**
     * X坐标（东西方向，米）
     * <p>
     * 室内局部坐标，相对于区域（房间）左下角原点
     */
    private BigDecimal localX;
    /**
     * Y坐标（南北方向，米）
     * <p>
     * 室内局部坐标，相对于区域（房间）左下角原点
     */
    private BigDecimal localY;
    /**
     * Z坐标（安装高度，米）
     * <p>
     * 相对于楼层地面的高度
     */
    private BigDecimal localZ;

    /**
     * 安装位置描述
     * <p>
     * 例如：天花板、墙面西侧、门口上方等
     */
    private String installLocation;
    /**
     * 安装高度类型
     * <p>
     * 枚举值：floor-地面, desk-桌面(0.75m), wall-墙面(1.5m), ceiling-吊顶(2.8m), custom-自定义
     */
    private String installHeightType;

    /**
     * 设备类型特有配置（JSON格式）
     * <p>
     * 使用 DeviceConfigTypeHandler 自动序列化/反序列化。
     * 不同设备类型有不同的配置实现:
     * <ul>
     *   <li>门禁设备: {@link cn.iocoder.yudao.module.iot.dal.dataobject.device.config.AccessDeviceConfig}</li>
     *   <li>长辉设备: {@link cn.iocoder.yudao.module.iot.dal.dataobject.device.config.ChanghuiDeviceConfig}</li>
     * </ul>
     */
    @TableField(typeHandler = DeviceConfigTypeHandler.class)
    private DeviceConfig config;

    /**
     * 定时任务配置（JSON格式）
     * <p>
     * 示例：{"offlineCheck":{"enabled":true,"interval":5,"unit":"MINUTE","priority":1}}
     * <p>
     * 注意：设备级配置会覆盖产品级配置
     */
    private String jobConfig;

}