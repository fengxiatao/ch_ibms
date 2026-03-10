package cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.appointment;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 访客预约分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class VisitorAppointmentPageReqVO extends PageParam {

    @Schema(description = "访客姓名", example = "张三")
    private String name;

    @Schema(description = "联系电话", example = "13800000000")
    private String phone;

    @Schema(description = "访客类型：business/vip/contractor/interview", example = "vip")
    private String type;

    @Schema(description = "审批状态：pending/approved/rejected/cancelled", example = "pending")
    private String status;

    @Schema(description = "预约来访时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] visitTime;
}

