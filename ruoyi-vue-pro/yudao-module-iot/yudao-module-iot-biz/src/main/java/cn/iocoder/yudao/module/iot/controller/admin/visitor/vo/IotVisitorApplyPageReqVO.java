package cn.iocoder.yudao.module.iot.controller.admin.visitor.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 访客申请分页查询 Request VO
 */
@Schema(description = "管理后台 - 访客申请分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class IotVisitorApplyPageReqVO extends PageParam {

    @Schema(description = "访客姓名", example = "张三")
    private String visitorName;

    @Schema(description = "被访人姓名", example = "李四")
    private String visiteeName;

    @Schema(description = "来访事由", example = "商务洽谈")
    private String visitReason;

    @Schema(description = "来访状态：0-待访，1-在访，2-离访，3-已取消", example = "0")
    private Integer visitStatus;

    @Schema(description = "审批状态：0-待审批，1-已通过，2-已拒绝", example = "1")
    private Integer approveStatus;

    @Schema(description = "计划来访时间-开始")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime visitTimeStart;

    @Schema(description = "计划来访时间-结束")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime visitTimeEnd;

}
