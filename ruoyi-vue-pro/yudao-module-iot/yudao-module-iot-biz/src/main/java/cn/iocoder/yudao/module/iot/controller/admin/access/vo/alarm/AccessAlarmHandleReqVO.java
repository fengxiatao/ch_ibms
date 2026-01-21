package cn.iocoder.yudao.module.iot.controller.admin.access.vo.alarm;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class AccessAlarmHandleReqVO {

    @NotNull(message = "告警ID不能为空")
    private Long id;

    @NotNull(message = "处理状态不能为空")
    private Integer handleStatus;

    private String handleRemark;

}


























