package cn.iocoder.yudao.module.iot.dal.dataobject.access;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 门禁告警记录 DO
 *
 * @author 智能化系统
 */
@TableName(value = "iot_access_alarm", autoResultMap = true)
@KeySequence("iot_access_alarm_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessAlarmDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 告警编号
     */
    private String alarmCode;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 园区ID
     */
    private Long campusId;

    /**
     * 建筑ID
     */
    private Long buildingId;

    /**
     * 楼层ID
     */
    private Long floorId;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 位置路径
     */
    private String locationPath;

    /**
     * 告警事件
     */
    private String alarmEvent;

    /**
     * 告警级别
     * 
     * 枚举 {@link cn.iocoder.yudao.module.iot.enums.access.AccessAlarmLevelEnum}
     * 1-一般，2-重要，3-紧急
     */
    private Integer alarmLevel;

    /**
     * 告警类型
     */
    private String alarmType;

    /**
     * 告警时间
     */
    private LocalDateTime alarmTime;

    /**
     * 告警描述
     */
    private String alarmDesc;

    /**
     * 照片URL
     */
    private String photoUrl;

    /**
     * 处理状态
     * 0-未处理，1-处理中，2-已处理，3-已忽略
     */
    private Integer handleStatus;

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

}


























