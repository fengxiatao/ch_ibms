package cn.iocoder.yudao.module.iot.dal.dataobject.building;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 环境告警记录 DO
 *
 * @author 智慧楼宇系统
 */
@TableName("ibms_env_alarm")
@KeySequence("ibms_env_alarm_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IbmsEnvAlarmDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 传感器ID
     */
    private Long sensorId;

    /**
     * 传感器编号
     */
    private String sensorCode;

    /**
     * 传感器名称
     */
    private String sensorName;

    /**
     * 告警类型：1-温度异常 2-湿度异常 3-PM2.5超标 4-PM10超标 5-CO2超标 6-设备离线
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
     * 恢复时间
     */
    private LocalDateTime recoverTime;

    /**
     * 状态：0-未处理 1-处理中 2-已处理 3-已恢复
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
