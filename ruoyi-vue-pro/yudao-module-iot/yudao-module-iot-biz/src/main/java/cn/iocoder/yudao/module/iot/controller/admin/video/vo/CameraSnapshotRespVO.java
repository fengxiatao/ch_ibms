package cn.iocoder.yudao.module.iot.controller.admin.video.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 摄像头抓图记录 Response VO")
@Data
public class CameraSnapshotRespVO {

    @Schema(description = "快照ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "通道ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long channelId;

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long deviceId;

    @Schema(description = "通道名称", example = "大堂摄像头")
    private String channelName;

    @Schema(description = "快照URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "http://192.168.1.201/snapshot_001.jpg")
    private String snapshotUrl;

    @Schema(description = "快照文件路径", example = "/snapshots/20251030/camera_1_001.jpg")
    private String snapshotPath;

    @Schema(description = "文件大小(字节)", example = "65536")
    private Long fileSize;

    @Schema(description = "图片宽度", example = "1920")
    private Integer width;

    @Schema(description = "图片高度", example = "1080")
    private Integer height;

    @Schema(description = "抓拍时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime captureTime;

    @Schema(description = "抓图类型(1:手动抓图 2:定时抓图 3:报警抓图 4:移动侦测抓图)", example = "1")
    private Integer snapshotType;

    @Schema(description = "触发事件（用于报警抓图）", example = "人员入侵")
    private String triggerEvent;

    @Schema(description = "事件类型（motion_detected:移动侦测, alarm:报警等）", example = "motion_detected")
    private String eventType;

    @Schema(description = "描述", example = "移动侦测触发抓图")
    private String description;

    @Schema(description = "是否已处理(0:未处理 1:已处理)", example = "false")
    private Boolean isProcessed;

    @Schema(description = "处理人", example = "admin")
    private String processor;

    @Schema(description = "处理时间")
    private LocalDateTime processTime;

    @Schema(description = "处理备注", example = "误报，已忽略")
    private String processRemark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}







