package cn.iocoder.yudao.module.iot.controller.admin.patrolplan.vo.record;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 轮巡记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PatrolRecordPageReqVO extends PageParam {

    @Schema(description = "计划ID", example = "1")
    private Long planId;

    @Schema(description = "任务ID", example = "1")
    private Long taskId;

    @Schema(description = "场景ID", example = "1")
    private Long sceneId;

    @Schema(description = "执行状态", example = "success")
    private String executeStatus;

    @Schema(description = "是否异常", example = "true")
    private Boolean hasAbnormal;

    @Schema(description = "是否已处理", example = "false")
    private Boolean handled;

    @Schema(description = "执行时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] executeTime;

}
