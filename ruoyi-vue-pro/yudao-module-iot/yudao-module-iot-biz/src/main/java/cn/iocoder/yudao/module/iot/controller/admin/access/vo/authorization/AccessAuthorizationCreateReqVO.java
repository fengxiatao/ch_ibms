package cn.iocoder.yudao.module.iot.controller.admin.access.vo.authorization;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
public class AccessAuthorizationCreateReqVO {

    @NotNull(message = "授权类型不能为空")
    private Integer authType;

    @NotNull(message = "授权目标ID不能为空")
    private Long authTargetId;

    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    private Integer authStatus;

    private String remark;

}


























