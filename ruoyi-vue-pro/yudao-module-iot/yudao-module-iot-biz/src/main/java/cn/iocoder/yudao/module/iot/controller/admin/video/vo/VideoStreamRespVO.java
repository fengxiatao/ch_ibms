package cn.iocoder.yudao.module.iot.controller.admin.video.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台 - IoT 视频流 Response VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - IoT 视频流 Response VO")
@Data
public class VideoStreamRespVO {

    @Schema(description = "流ID", example = "abc123def456")
    private String streamId;

    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "流类型", example = "live")
    private String streamType; // live: 实时预览, playback: 录像回放

    @Schema(description = "RTSP源地址", example = "rtsp://admin:pass@192.168.1.202:554/Streaming/Channels/101")
    private String rtspUrl;

    @Schema(description = "HLS播放地址", example = "http://localhost:48080/hls/abc123def456/playlist.m3u8")
    private String hlsUrl;

    @Schema(description = "流状态", example = "running")
    private String status; // running: 运行中, stopped: 已停止, error: 错误

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "开始时间（回放）", example = "2025-10-26T10:00:00")
    private String startTime;

    @Schema(description = "结束时间（回放）", example = "2025-10-26T12:00:00")
    private String endTime;

    @Schema(description = "ZLMediaKit流Key", example = "live/abc123def456")
    private String zlmediaKey;
}

