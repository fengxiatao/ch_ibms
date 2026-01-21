package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.schedule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

/**
 * 定时布防任务 Base VO
 *
 * @author 长辉信息科技有限公司
 */
@Data
public class IotAlarmScheduleBaseVO {

    @NotNull(message = "报警主机ID不能为空")
    private Long hostId;

    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    @NotBlank(message = "布防类型不能为空")
    private String armType;

    @NotBlank(message = "执行星期不能为空")
    private String weekdays;

    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    private LocalTime endTime;

    private String remark;
}
