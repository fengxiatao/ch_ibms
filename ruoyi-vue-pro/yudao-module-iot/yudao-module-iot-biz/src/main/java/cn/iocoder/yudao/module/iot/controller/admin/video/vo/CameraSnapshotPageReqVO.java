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

@Schema(description = "管理后台 - 摄像头抓图记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CameraSnapshotPageReqVO extends PageParam {

    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "通道ID", example = "1")
    private Long channelId;

    @Schema(description = "通道ID列表（批量查询）", example = "[1,2,3]")
    private List<Long> channelIds;

    @Schema(description = "抓图类型(1:手动抓图 2:定时抓图 3:报警抓图 4:移动侦测抓图)", example = "1")
    private Integer snapshotType;

    @Schema(description = "事件类型（motion_detected:移动侦测, alarm:报警等）", example = "motion_detected")
    private String eventType;

    @Schema(description = "开始时间（查询）", example = "2025-10-30 00:00:00")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "结束时间（查询）", example = "2025-10-31 23:59:59")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

    @Schema(description = "是否已处理(0:未处理 1:已处理)", example = "false")
    private Boolean isProcessed;

}







