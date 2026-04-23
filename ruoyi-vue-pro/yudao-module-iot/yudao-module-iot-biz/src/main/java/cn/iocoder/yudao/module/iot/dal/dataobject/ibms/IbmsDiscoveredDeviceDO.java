package cn.iocoder.yudao.module.iot.dal.dataobject.ibms;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IBMS 发现设备 DO（局域网扫描 / ONVIF 等发现记录）
 *
 * <p>对应表：ibms_discovered_device（由原 iot_discovered_device 重命名统一前缀）
 */
@TableName("ibms_discovered_device")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IbmsDiscoveredDeviceDO extends BaseDO {

    @TableId
    private Long id;

    private String ipAddress;

    private String mac;

    private String vendor;

    private String model;

    private String serialNumber;

    private String deviceType;

    private String firmwareVersion;

    private String discoveryMethod;

    private LocalDateTime discoveryTime;

    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean added;

    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean activated;

    /** 激活后的 IBMS 设备主键（ibms_device.id） */
    private Long activatedDeviceId;

    private LocalDateTime activatedTime;

    private Long activatedBy;

    private Long deviceId;

    /** 状态：1=已发现 2=已通知 3=已忽略 4=待处理 5=已激活 */
    private Integer status;

    private Integer notifiedCount;

    private LocalDateTime lastNotifiedTime;

    private Long ignoredBy;

    private LocalDateTime ignoredTime;

    private String ignoreReason;

    private LocalDateTime ignoreUntil;
}
