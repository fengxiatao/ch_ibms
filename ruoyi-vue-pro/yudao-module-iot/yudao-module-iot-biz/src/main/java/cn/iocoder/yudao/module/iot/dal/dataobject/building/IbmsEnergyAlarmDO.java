package cn.iocoder.yudao.module.iot.dal.dataobject.building;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 能耗告警 DO
 *
 * @author 智慧楼宇系统
 */
@TableName("ibms_energy_alarm")
@KeySequence("ibms_energy_alarm_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IbmsEnergyAlarmDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 表具ID
     */
    private Long meterId;

    /**
     * 表具编号
     */
    private String meterCode;

    /**
     * 表具名称
     */
    private String meterName;

    /**
     * 告警类型：1-用量超标 2-异常波动 3-设备离线 4-通讯异常
     */
    private Integer alarmType;

    /**
     * 告警级别：1-提示 2-警告 3-严重
     */
    private Integer alarmLevel;

    /**
     * 告警内容
     */
    private String alarmContent;

    /**
     * 告警值
     */
    private String alarmValue;

    /**
     * 阈值
     */
    private String thresholdValue;

    /**
     * 告警时间
     */
    private LocalDateTime alarmTime;

    /**
     * 状态：0-未处理 1-处理中 2-已处理
     */
    private Integer status;

    /**
     * 处理人
     */
    private String handler;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理备注
     */
    private String handleRemark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 租户编号
     */
    private Long tenantId;

}
