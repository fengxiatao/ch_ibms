package cn.iocoder.yudao.module.iot.controller.admin.video.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 摄像头录像记录 Response VO")
@Data
public class CameraRecordingRespVO {

    @Schema(description = "录像ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "摄像头ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long cameraId;

    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "设备名称", example = "大堂摄像头")
    private String deviceName;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "时长(秒)", example = "3600")
    private Integer duration;

    @Schema(description = "文件路径", example = "/recordings/20251030/camera_1_001.mp4")
    private String filePath;

    @Schema(description = "文件大小(字节)", example = "1048576")
    private Long fileSize;

    @Schema(description = "文件访问URL", example = "http://192.168.1.246:8080/recordings/camera_1_001.mp4")
    private String fileUrl;

    @Schema(description = "录像类型(1:手动 2:定时 3:报警触发 4:移动侦测)", example = "1")
    private Integer recordingType;

    @Schema(description = "状态(0:录像中 1:已完成 2:已停止 3:异常)", example = "1")
    private Integer status;

    @Schema(description = "错误信息", example = "磁盘空间不足")
    private String errorMsg;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}







