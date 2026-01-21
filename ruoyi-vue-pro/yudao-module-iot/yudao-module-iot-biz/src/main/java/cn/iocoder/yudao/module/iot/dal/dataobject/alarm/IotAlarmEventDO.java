package cn.iocoder.yudao.module.iot.dal.dataobject.alarm;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 报警主机事件记录 DO
 *
 * @author 芋道源码
 */
@TableName("iot_alarm_event")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAlarmEventDO extends TenantBaseDO {

    /**
     * 事件ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 报警主机ID
     */
    private Long hostId;

    /**
     * 事件码（如1130、3401等）
     */
    private String eventCode;

    /**
     * 事件类型：ALARM-报警, ARM-布防, DISARM-撤防, FAULT-故障, BYPASS-旁路
     */
    private String eventType;

    /**
     * 事件级别：INFO-信息, WARNING-警告, ERROR-错误, CRITICAL-严重
     */
    private String eventLevel;

    /**
     * 分区号
     */
    private Integer areaNo;

    /**
     * 防区号
     */
    private Integer zoneNo;

    /**
     * 用户号
     */
    private Integer userNo;

    /**
     * 序列号
     */
    private String sequence;

    /**
     * 事件描述
     */
    private String eventDesc;

    /**
     * 原始数据
     */
    private String rawData;

    /**
     * 是否新事件：1-新事件（千位1），0-恢复事件（千位3）
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean isNewEvent;

    /**
     * 是否已处理：0-未处理，1-已处理
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean isHandled;

    /**
     * 处理人
     */
    private String handledBy;

    /**
     * 处理时间
     */
    private LocalDateTime handledTime;

    /**
     * 处理备注
     */
    private String handleRemark;
}
