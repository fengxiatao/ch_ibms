package cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 访客预约审批 Request VO")
@Data
public class VisitorAppointmentApproveReqVO {

    @Schema(description = "预约编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8")
    @NotNull(message = "预约编号不能为空")
    private Long id;

    @Schema(description = "动作：approve/reject", requiredMode = Schema.RequiredMode.REQUIRED, example = "approve")
    @NotBlank(message = "动作不能为空")
    private String action;

    @Schema(description = "审批意见", requiredMode = Schema.RequiredMode.REQUIRED, example = "同意，请安排接待")
    @NotBlank(message = "审批意见不能为空")
    private String comment;
}

