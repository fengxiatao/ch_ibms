package cn.iocoder.yudao.module.iot.core.video;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 摄像头录像事件（Gateway -> Biz）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CameraRecordingEvent {

    /** 请求ID（用于链路追踪/关联） */
    private String requestId;

    /** 租户ID（用于多租户支持） */
    private Long tenantId;

    /** 录像记录ID */
    private Long recordingId;
    /** 设备ID */
    private Long deviceId;

    /** 事件类型：started / completed / stopped / error */
    private String type;

    /** 录像文件相对路径（例如 /recordings/...） */
    private String filePath;

    /** 时长（秒） */
    private Integer duration;
    /** 文件大小（字节） */
    private Long fileSize;

    /** 错误信息（当 type=error 时） */
    private String errorMessage;

    /** 事件时间 */
    private LocalDateTime timestamp;
}
