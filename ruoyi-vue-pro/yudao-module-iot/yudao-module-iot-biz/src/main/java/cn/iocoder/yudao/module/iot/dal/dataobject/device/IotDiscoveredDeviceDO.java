package cn.iocoder.yudao.module.iot.dal.dataobject.device;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IoT 发现设备 DO
 *
 * @author 长辉信息科技有限公司
 */
@TableName("iot_discovered_device")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDiscoveredDeviceDO extends BaseDO {
    
    /**
     * ID
     */
    @TableId
    private Long id;
    
   
    private String ipAddress;
    
    /**
     * MAC地址
     */
    private String mac;
    
    /**
     * 厂商
     */
    private String vendor;
    
    /**
     * 型号
     */
    private String model;
    
    /**
     * 序列号
     */
    private String serialNumber;
    
    /**
     * 设备类型
     */
    private String deviceType;
    
    /**
     * 固件版本
     */
    private String firmwareVersion;
    
    /**
     * 发现方式
     */
    private String discoveryMethod;
    
    /**
     * 发现时间
     */
    private LocalDateTime discoveryTime;
    
    /**
     * 是否已添加
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean added;
    
    /**
     * 是否已激活
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean activated;
    
    /**
     * 激活后的设备ID（关联 iot_device.id）
     */
    private Long activatedDeviceId;
    
    /**
     * 激活时间
     */
    private LocalDateTime activatedTime;
    
    /**
     * 激活操作人ID
     */
    private Long activatedBy;
    
    /**
     * 平台设备ID
     */
    private Long deviceId;
    
    /**
     * 状态：1=已发现 2=已通知 3=已忽略 4=待处理 5=已激活
     */
    private Integer status;
    
    /**
     * 通知次数
     */
    private Integer notifiedCount;
    
    /**
     * 最后通知时间
     */
    private LocalDateTime lastNotifiedTime;
    
    /**
     * 忽略操作人ID
     */
    private Long ignoredBy;
    
    /**
     * 忽略时间
     */
    private LocalDateTime ignoredTime;
    
    /**
     * 忽略原因
     */
    private String ignoreReason;
    
    /**
     * 忽略截止时间（NULL表示永久忽略）
     */
    private LocalDateTime ignoreUntil;
}





