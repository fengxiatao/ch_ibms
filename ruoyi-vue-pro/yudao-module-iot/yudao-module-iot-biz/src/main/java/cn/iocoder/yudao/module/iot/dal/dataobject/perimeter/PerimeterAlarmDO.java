package cn.iocoder.yudao.module.iot.dal.dataobject.perimeter;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@TableName(value = "iot_perimeter_alarm", autoResultMap = true)
@KeySequence("iot_perimeter_alarm_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerimeterAlarmDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String alarmCode;
    private Long zoneId;
    private String zoneName;
    private Long deviceId;
    private String deviceName;
    private Integer alarmType;
    private Integer alarmLevel;
    private LocalDateTime alarmTime;
    private String alarmDesc;
    private String videoUrl;
    private String photoUrl;
    private Integer handleStatus;
    private String handler;
    private LocalDateTime handleTime;
    private String handleRemark;
}


























