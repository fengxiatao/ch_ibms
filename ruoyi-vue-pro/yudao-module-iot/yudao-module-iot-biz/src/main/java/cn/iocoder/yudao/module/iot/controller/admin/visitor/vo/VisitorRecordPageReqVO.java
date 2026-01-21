package cn.iocoder.yudao.module.iot.controller.admin.visitor.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 访客记录分页查询 Request VO
 * 需求补充：支持按照条件筛选访客记录条目
 */
@Schema(description = "管理后台 - 访客记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class VisitorRecordPageReqVO extends PageParam {

    @Schema(description = "访客姓名", example = "张三")
    private String visitorName;

    @Schema(description = "来访事由", example = "商务洽谈")
    private String visitReason;

    @Schema(description = "被访人姓名", example = "李四")
    private String visiteeName;

    @Schema(description = "来访状态：1-在访，2-离访，3-已取消")
    private Integer visitorStatus;

    @Schema(description = "来访时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] arrivalTime;

    @Schema(description = "时间快捷选择类型：today-今日，week-近一周，month-近一月")
    private String timeRangeType;

}


























