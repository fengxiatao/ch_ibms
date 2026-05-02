package cn.iocoder.yudao.module.iot.controller.admin.video;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.PlayUrlRespVO;
import cn.iocoder.yudao.module.iot.service.video.ZlmStreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * ZLMediaKit 流媒体控制器
 * 
 * <p>提供低延迟视频播放接口，替代大华 RPC2 直连方式</p>
 *
 * @author IBMS
 */
@Tag(name = "管理后台 - ZLMediaKit 流媒体")
@RestController
@RequestMapping("/iot/video/zlm")
@Validated
@Slf4j
public class ZlmStreamController {

    @Resource
    private ZlmStreamService zlmStreamService;

    @GetMapping("/live/{channelId}")
    @Operation(summary = "获取通道实时播放地址", 
            description = "返回多协议播放地址，推荐使用 wsFlvUrl（低延迟）或 webrtcUrl（极低延迟）")
    @Parameter(name = "channelId", description = "通道ID", required = true)
    @Parameter(name = "subtype", description = "码流类型: 0=主码流/高清(默认), 1=子码流/标清", required = false)
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<PlayUrlRespVO> getLivePlayUrl(
            @PathVariable("channelId") Long channelId,
            @RequestParam(value = "subtype", defaultValue = "0") Integer subtype) {
        log.info("[ZLM] 请求实时播放地址: channelId={}, subtype={}", channelId, subtype);
        PlayUrlRespVO playUrl = zlmStreamService.getLivePlayUrl(channelId, subtype);
        return success(playUrl);
    }

    @PostMapping("/stop/{channelId}")
    @Operation(summary = "停止通道流")
    @Parameter(name = "channelId", description = "通道ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:camera:update')")
    public CommonResult<Boolean> stopStream(@PathVariable("channelId") Long channelId) {
        log.info("[ZLM] 停止流: channelId={}", channelId);
        boolean success = zlmStreamService.stopStream(channelId);
        return success(success);
    }

    @GetMapping("/status/{channelId}")
    @Operation(summary = "检查通道流状态")
    @Parameter(name = "channelId", description = "通道ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<Boolean> isStreamOnline(@PathVariable("channelId") Long channelId) {
        boolean online = zlmStreamService.isStreamOnline(channelId);
        return success(online);
    }

    @GetMapping("/playback/{channelId}")
    @Operation(summary = "获取录像回放播放地址",
            description = "从 NVR 拉取指定时间段的录像流，返回多协议播放地址")
    @Parameter(name = "channelId", description = "通道ID", required = true)
    @Parameter(name = "startTime", description = "开始时间（ISO格式或时间戳）", required = true)
    @Parameter(name = "endTime", description = "结束时间（ISO格式或时间戳）", required = true)
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<PlayUrlRespVO> getPlaybackUrl(
            @PathVariable("channelId") Long channelId,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime) {
        log.info("[ZLM] 请求录像回放地址: channelId={}, startTime={}, endTime={}", channelId, startTime, endTime);
        PlayUrlRespVO playUrl = zlmStreamService.getPlaybackUrl(channelId, startTime, endTime);
        return success(playUrl);
    }

    @PostMapping("/clear-all")
    @Operation(summary = "清除所有流代理", 
            description = "清空 ZLMediaKit 上缓存的所有流代理，用于修复因 RTSP URL 错误导致的缓存问题")
    @PreAuthorize("@ss.hasPermission('iot:camera:update')")
    public CommonResult<Integer> clearAllStreams() {
        log.info("[ZLM] 请求清除所有流代理");
        int count = zlmStreamService.clearAllStreams();
        return success(count);
    }

    @GetMapping(value = "/snap/{channelId}", produces = MediaType.IMAGE_JPEG_VALUE)
    @Operation(summary = "获取通道封面快照（JPEG）",
            description = "用于列表页 / 播放器 idle/loading 状态的占位封面，降低首屏体感延迟。" +
                    "ZLM 内部按 60s 缓存 jpeg，前端可放心配 CDN/浏览器缓存。")
    @Parameter(name = "channelId", description = "通道ID", required = true)
    @Parameter(name = "subtype", description = "码流类型: 0=主码流, 1=子码流（默认，封面用子码流体积小）", required = false)
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public ResponseEntity<byte[]> getChannelSnapshot(
            @PathVariable("channelId") Long channelId,
            @RequestParam(value = "subtype", defaultValue = "1") Integer subtype) {
        byte[] body = zlmStreamService.getChannelSnapshot(channelId, subtype);
        if (body == null || body.length == 0) {
            // 抓帧失败时，返回 503 让前端可降级到默认占位图
            return ResponseEntity.status(503).build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                // 浏览器侧缓存 60 秒，与 ZLM 端 expire_sec 对齐
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=60")
                .body(body);
    }
}
