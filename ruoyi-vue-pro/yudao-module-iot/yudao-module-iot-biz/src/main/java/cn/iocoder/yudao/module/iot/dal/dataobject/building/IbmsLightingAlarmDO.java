package cn.iocoder.yudao.module.iot.dal.dataobject.building;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 照明告警 DO
 *
 * @author 智慧楼宇系统
 */
@TableName("ibms_lighting_alarm")
@KeySequence("ibms_lighting_alarm_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IbmsLightingAlarmDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备类型：1-回路 2-网关 3-控制器
     */
    private Integer deviceType;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 告警级别：1-提示 2-警告 3-严重
     */
    private Integer alarmLevel;

    /**
     * 告警内容
     */
    private String alarmContent;

    /**
     * 告警时间
     */
    private LocalDateTime alarmTime;

    /**
     * 持续时间
     */
    private String duration;

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
