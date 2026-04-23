package cn.iocoder.yudao.module.iot.dal.dataobject.ibms;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfigTypeHandler;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * IBMS 设备运行态 DO，与 {@code ibms_device} 1:1，主键为 {@code device_id}。
 */
@TableName(value = "ibms_device_runtime", autoResultMap = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IbmsDeviceRuntimeDO extends TenantBaseDO {

    /** 设备 ID，对应 ibms_device.id */
    @TableId("device_id")
    private Long deviceId;

    /** 设备状态，见 IotDeviceStateEnum */
    private Integer state;

    private LocalDateTime onlineTime;
    private LocalDateTime offlineTime;
    private LocalDateTime activeTime;

    private Long firmwareId;
    private Long gatewayId;

    private Integer locationType;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer areaId;
    private String address;

    private Long campusId;
    private Long buildingId;
    private Long floorId;
    private Long roomId;

    private BigDecimal localX;
    private BigDecimal localY;
    private BigDecimal localZ;

    private String installLocation;
    private String installHeightType;

    @TableField(typeHandler = DeviceConfigTypeHandler.class)
    private DeviceConfig config;

    private String jobConfig;
}
