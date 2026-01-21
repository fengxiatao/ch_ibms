package cn.iocoder.yudao.module.iot.controller.admin.task.vo.log;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 任务执行日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TaskLogPageReqVO extends PageParam {

    @Schema(description = "任务配置ID", example = "1024")
    private Long taskConfigId;

    @Schema(description = "实体类型", example = "PRODUCT")
    private String entityType;

    @Schema(description = "实体ID", example = "1024")
    private Long entityId;

    @Schema(description = "实体名称", example = "大华摄像头")
    private String entityName;

    @Schema(description = "任务类型", example = "DEVICE_STATUS_SYNC")
    private String jobType;

    @Schema(description = "执行状态", example = "SUCCESS")
    private String executionStatus;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] startTime;

}
























































