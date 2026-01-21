package cn.iocoder.yudao.module.iot.controller.admin.access.vo.alarm;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AccessAlarmRespVO {

    private Long id;
    private String alarmCode;
    private Long deviceId;
    private String deviceName;
    private Long campusId;
    private Long buildingId;
    private Long floorId;
    private Long areaId;
    private String locationPath;
    private String alarmEvent;
    private Integer alarmLevel;
    private String alarmType;
    private LocalDateTime alarmTime;
    private String alarmDesc;
    private String photoUrl;
    private Integer handleStatus;
    private String handler;
    private LocalDateTime handleTime;
    private String handleRemark;

}


























