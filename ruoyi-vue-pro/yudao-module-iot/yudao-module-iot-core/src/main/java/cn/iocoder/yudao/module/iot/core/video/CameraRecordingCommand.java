package cn.iocoder.yudao.module.iot.core.video;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 摄像头录像控制命令（Biz -> Gateway）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CameraRecordingCommand {

    /** 请求ID（用于链路追踪/关联） */
    private String requestId;

    /** 租户ID（用于多租户支持） */
    private Long tenantId;

    /** 设备ID */
    private Long deviceId;
    /** 录像记录ID */
    private Long recordingId;
    /** 命令：start / stop */
    private String command;

    /** 时长（秒），0 表示持续 */
    private Integer duration;
    /** 期望文件保存路径（相对前缀）例如 /recordings/xxx.mp4 */
    private String filePath;

    /** 策略：nvr / server / both */
    private String prefer;
    /** 是否双录（等价 prefer==both） */
    private Boolean dual;

    /** 生成时间 */
    private LocalDateTime timestamp;
}
