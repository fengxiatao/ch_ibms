package cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.abnormal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 访客异常事件 Response VO")
@Data
public class VisitorAbnormalEventRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "关联预约ID")
    private Long appointmentId;

    @Schema(description = "访客姓名")
    private String visitorName;

    @Schema(description = "访客电话")
    private String visitorPhone;

    @Schema(description = "异常类型：overtime/unauthorized/noshow")
    private String abnormalType;

    @Schema(description = "风险等级：high/medium/low")
    private String riskLevel;

    @Schema(description = "详情")
    private String details;

    @Schema(description = "发生时间")
    private LocalDateTime eventTime;

    @Schema(description = "当前状态/位置")
    private String currentStatus;

    @Schema(description = "是否已处理")
    private Boolean handled;
}

