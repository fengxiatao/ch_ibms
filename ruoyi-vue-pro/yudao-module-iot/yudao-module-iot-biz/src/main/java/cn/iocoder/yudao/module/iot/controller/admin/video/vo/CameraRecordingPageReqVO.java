package cn.iocoder.yudao.module.iot.controller.admin.video.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 摄像头录像记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CameraRecordingPageReqVO extends PageParam {

    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "摄像头ID（单个查询）", example = "1")
    private Long cameraId;

    @Schema(description = "摄像头ID列表（批量查询，优先级高于 cameraId）", example = "[1, 2, 3]")
    private List<Long> cameraIds;

    @Schema(description = "录像类型(1:手动 2:定时 3:报警触发 4:移动侦测)", example = "1")
    private Integer recordingType;

    @Schema(description = "状态(0:录像中 1:已完成 2:已停止 3:异常)", example = "1")
    private Integer status;

    @Schema(description = "开始时间（查询）", example = "2025-10-30 00:00:00")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "结束时间（查询）", example = "2025-10-31 23:59:59")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

}







