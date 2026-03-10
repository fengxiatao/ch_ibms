package cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.abnormal;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 访客异常事件分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class VisitorAbnormalEventPageReqVO extends PageParam {

    @Schema(description = "异常类型：overtime/unauthorized/noshow")
    private String abnormalType;

    @Schema(description = "风险等级：high/medium/low")
    private String riskLevel;

    @Schema(description = "是否已处理")
    private Boolean handled;

    @Schema(description = "发生时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] eventTime;
}

