package cn.iocoder.yudao.module.iot.controller.admin.building.vo.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 照明告警 Response VO")
@Data
public class IbmsLightingAlarmRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "设备类型 1-网关 2-控制器 3-回路")
    private Integer deviceType;

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "告警类型")
    private String alarmType;

    @Schema(description = "告警级别 1-一般 2-重要 3-紧急")
    private Integer alarmLevel;

    @Schema(description = "告警内容")
    private String alarmContent;

    @Schema(description = "告警时间")
    private LocalDateTime alarmTime;

    @Schema(description = "状态 0-未处理 1-处理中 2-已处理 3-已忽略")
    private Integer status;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "处理人")
    private String handler;

    @Schema(description = "处理备注")
    private String handleRemark;

}
