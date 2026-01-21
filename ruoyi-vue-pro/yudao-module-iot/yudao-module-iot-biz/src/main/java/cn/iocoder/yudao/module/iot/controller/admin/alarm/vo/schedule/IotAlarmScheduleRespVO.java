package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 定时布防任务 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotAlarmScheduleRespVO extends IotAlarmScheduleBaseVO {

    @Schema(description = "任务ID", example = "1")
    private Long id;

    @Schema(description = "状态：0-禁用, 1-启用", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
