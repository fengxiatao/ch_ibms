package cn.iocoder.yudao.module.iot.controller.admin.video;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.service.video.CameraRecordingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 录像回调 Controller（供 Gateway/ZLMediaKit 回调）
 * 无需登录鉴权，使用 Header Token 进行简单校验。
 */
@Tag(name = "开放回调 - 摄像头录像回调")
@RestController
@RequestMapping("/iot/camera/recording/callback")
@Slf4j
public class CameraRecordingCallbackController {

    @Resource
    private CameraRecordingService cameraRecordingService;

    @Resource
    private cn.iocoder.yudao.module.iot.websocket.DeviceMessagePushService deviceMessagePushService;

    @Value("${iot.recording.callback-token:}")
    private String callbackToken;

    private void checkToken(String token) {
        if (callbackToken == null || callbackToken.isEmpty()) {
            // 未配置 token 时放行（开发调试用）
            return;
        }
        if (!callbackToken.equals(token)) {
            throw new IllegalArgumentException("Invalid callback token");
        }
    }

    private static LocalDateTime parseTime(String s) {
        // 期望 ISO8601，允许简写到秒
        return LocalDateTime.parse(s, DateTimeFormatter.ISO_DATE_TIME);
    }

    @PostMapping("/create")
    @Operation(summary = "开始录像回调：创建录像记录(状态=录制中)")
    public CommonResult<Long> onCreate(@RequestHeader(value = "X-Callback-Token", required = false) String token,
                                       @RequestBody CreateReq req) {
        checkToken(token);
        Long id = cameraRecordingService.createRecording(req.getDeviceId(),
                req.getRecordingType(),
                parseTime(req.getStartTime()),
                req.getFilePath());
        log.info("[录像回调] 创建记录成功 id={} deviceId={}", id, req.getDeviceId());
        // 推送录像开始事件
        try {
            java.util.Map<String, Object> evt = new java.util.HashMap<>();
            evt.put("type", "started");
            evt.put("recordingId", id);
            evt.put("deviceId", req.getDeviceId());
            evt.put("filePath", req.getFilePath());
            evt.put("timestamp", System.currentTimeMillis());
            deviceMessagePushService.pushRecordingEvent(evt);
        } catch (Exception ignore) {}
        return success(id);
    }

    @PostMapping("/complete")
    @Operation(summary = "完成录像回调：更新时长/大小并标记已完成")
    public CommonResult<Boolean> onComplete(@RequestHeader(value = "X-Callback-Token", required = false) String token,
                                            @RequestBody CompleteReq req) {
        checkToken(token);
        cameraRecordingService.completeRecording(req.getRecordingId(),
                parseTime(req.getEndTime()),
                req.getFileSize(),
                req.getDuration());
        log.info("[录像回调] 完成记录成功 id={}", req.getRecordingId());
        // 推送录像完成事件
        try {
            java.util.Map<String, Object> evt = new java.util.HashMap<>();
            evt.put("type", "completed");
            evt.put("recordingId", req.getRecordingId());
            evt.put("fileSize", req.getFileSize());
            evt.put("duration", req.getDuration());
            evt.put("timestamp", System.currentTimeMillis());
            deviceMessagePushService.pushRecordingEvent(evt);
        } catch (Exception ignore) {}
        return success(true);
    }

    @Data
    public static class CreateReq {
        @NotNull
        private Long deviceId;
        // 1:手动 2:定时 3:报警触发 4:移动侦测
        private Integer recordingType = 1;
        // ISO8601，例如 2025-11-13T11:39:00
        @NotNull
        private String startTime;
        // 录像文件相对路径（由网关生成），例如 /recordings/123/20251113/recording_...mp4
        @NotNull
        private String filePath;
    }

    @Data
    public static class CompleteReq {
        @NotNull
        private Long recordingId;
        // ISO8601，例如 2025-11-13T11:45:30
        @NotNull
        private String endTime;
        // 字节
        @NotNull
        private Long fileSize;
        // 秒
        @NotNull
        private Integer duration;
    }
}
