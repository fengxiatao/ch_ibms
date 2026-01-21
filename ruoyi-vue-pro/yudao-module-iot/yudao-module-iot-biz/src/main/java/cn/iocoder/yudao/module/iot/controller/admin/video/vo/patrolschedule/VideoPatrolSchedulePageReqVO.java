package cn.iocoder.yudao.module.iot.controller.admin.video.vo.patrolschedule;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 视频定时轮巡计划分页查询 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 视频定时轮巡计划分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class VideoPatrolSchedulePageReqVO extends PageParam {

    @Schema(description = "计划名称", example = "工作日轮巡")
    private String name;

    @Schema(description = "轮巡计划ID", example = "1")
    private Long patrolPlanId;

    @Schema(description = "计划类型：1-日计划 2-周计划", example = "1")
    private Integer scheduleType;

    @Schema(description = "状态：0-禁用 1-启用", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
