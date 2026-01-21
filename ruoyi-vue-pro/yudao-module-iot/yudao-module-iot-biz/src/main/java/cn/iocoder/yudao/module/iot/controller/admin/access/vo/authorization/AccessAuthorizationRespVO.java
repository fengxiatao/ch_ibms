package cn.iocoder.yudao.module.iot.controller.admin.access.vo.authorization;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AccessAuthorizationRespVO {

    private Long id;
    private Integer authType;
    private Long authTargetId;
    private String authTargetName; // 授权目标名称
    private Long deviceId;
    private String deviceName; // 设备名称
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer authStatus;
    private String remark;
    private LocalDateTime createTime;

}


























