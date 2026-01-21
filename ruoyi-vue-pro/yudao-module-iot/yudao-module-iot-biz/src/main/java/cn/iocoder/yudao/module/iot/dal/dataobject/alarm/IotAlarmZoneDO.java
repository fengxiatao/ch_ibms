package cn.iocoder.yudao.module.iot.dal.dataobject.alarm;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 防区 DO
 *
 * @author 长辉信息科技有限公司
 */
@TableName("iot_alarm_zone")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAlarmZoneDO extends TenantBaseDO {

    /**
     * 防区ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属主机ID
     */
    private Long hostId;

    /**
     * 防区编号
     */
    private Integer zoneNo;

    /**
     * 防区名称
     */
    private String zoneName;

    /**
     * 防区类型：DOOR-门磁, PIR-红外, SMOKE-烟感, GAS-燃气, GLASS-玻璃破碎, EMERGENCY-紧急按钮
     */
    private String zoneType;

    /**
     * 区域位置
     */
    private String areaLocation;

    /**
     * 防区状态：ARM-布防, DISARM-撤防, BYPASS-旁路
     */
    private String zoneStatus;
    
    /**
     * 状态字符：a/b/A/B/C/D/E/F/G/H/I（协议原始状态）
     */
    private String status;
    
    /**
     * 状态名称
     */
    private String statusName;
    
    /**
     * 布防状态枚举：0-撤防，1-布防，2-旁路
     */
    private Integer armStatus;
    
    /**
     * 报警状态枚举：0-正常，1-报警中，11-17各类报警
     */
    private Integer alarmStatus;

    /**
     * 在线状态：0-离线, 1-在线
     */
    private Integer onlineStatus;
    
    /**
     * 所属分区ID
     */
    private Long partitionId;

    /**
     * 是否重要防区：0-否, 1-是
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean isImportant;

    /**
     * 是否24小时防区：0-否, 1-是
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean is24h;

    /**
     * 报警次数统计
     */
    private Integer alarmCount;

    /**
     * 最后报警时间
     */
    private LocalDateTime lastAlarmTime;

    /**
     * 备注
     */
    private String remark;
}
