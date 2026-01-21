package cn.iocoder.yudao.module.iot.dal.dataobject.alarm;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 入侵报警规则配置 DO
 *
 * @author 长辉信息科技有限公司
 */
@TableName("iot_alarm_rule")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAlarmRuleDO extends TenantBaseDO {

    /**
     * 规则ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 通道名称
     */
    private String channelName;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 报警主机ID
     */
    private Long hostId;

    /**
     * 防区ID
     */
    private Long zoneId;

    /**
     * 报警类型：IVS-智能报警, NORMAL-普通报警
     */
    private String alarmType;

    /**
     * 监听的事件码列表，逗号分隔
     */
    private String eventCodes;

    /**
     * 事件名称列表，逗号分隔
     */
    private String eventNames;

    /**
     * 报警级别：1-低, 2-中, 3-高, 4-紧急
     */
    private Integer alarmLevel;

    /**
     * 是否启用声音告警：0-否, 1-是
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean enableSound;

    /**
     * 是否启用弹窗告警：0-否, 1-是
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean enablePopup;

    /**
     * 是否启用录像联动：0-否, 1-是
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean enableRecord;

    /**
     * 联动录像时长（秒）
     */
    private Integer recordDuration;

    /**
     * 布防模式：ALWAYS-全天, SCHEDULE-定时
     */
    private String armMode;

    /**
     * 布防时间表（JSON格式）
     */
    private String armSchedule;

    /**
     * 通知用户ID列表，逗号分隔
     */
    private String notifyUsers;

    /**
     * 通知方式：SMS-短信, EMAIL-邮件, WECHAT-微信
     */
    private String notifyMethods;

    /**
     * 规则状态：0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
