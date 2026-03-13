package cn.iocoder.yudao.module.iot.controller.admin.security;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback.ChannelRecordingRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback.PlaybackClipReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback.PlaybackClipRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback.PlaybackQueryRecordingsBatchReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback.PlaybackQueryRecordingsReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback.PlaybackUrlReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.security.vo.playback.PlaybackUrlRespVO;
import cn.iocoder.yudao.module.iot.service.security.VideoPlaybackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 视频录像回放（兼容前端固定路径 /security/playback）
 *
 * <p>说明：当前项目业务统一放在 iot-biz，为兼容前端已发布的接口路径，Controller 放在 iot 模块中。</p>
 */
@Tag(name = "管理后台 - 视频录像回放")
@RestController
@RequestMapping("/security/playback")
@Validated
public class VideoPlaybackController {

    @Resource
    private VideoPlaybackService videoPlaybackService;

    @PostMapping("/query-recordings")
    @Operation(summary = "查询录像片段")
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<List<ChannelRecordingRespVO>> queryRecordings(@Valid @RequestBody PlaybackQueryRecordingsReqVO reqVO) {
        return success(videoPlaybackService.queryRecordings(reqVO));
    }

    @PostMapping("/query-recordings-batch")
    @Operation(summary = "批量查询录像片段")
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<List<ChannelRecordingRespVO>> queryRecordingsBatch(@Valid @RequestBody PlaybackQueryRecordingsBatchReqVO reqVO) {
        return success(videoPlaybackService.queryRecordingsBatch(reqVO));
    }

    @PostMapping("/get-url")
    @Operation(summary = "获取回放地址（ZLM 回放流）")
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<PlaybackUrlRespVO> getPlaybackUrl(@Valid @RequestBody PlaybackUrlReqVO reqVO) {
        return success(videoPlaybackService.getPlaybackUrl(reqVO));
    }

    @PostMapping("/close-stream")
    @Operation(summary = "关闭回放流")
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<Boolean> closeStream(@Parameter(description = "流ID", required = true) @RequestParam("streamId") String streamId) {
        videoPlaybackService.closePlaybackStream(streamId);
        return success(true);
    }

    @PostMapping("/clip")
    @Operation(summary = "按时间剪切录像并生成下载文件（占位实现）")
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<PlaybackClipRespVO> clipRecording(@Valid @RequestBody PlaybackClipReqVO reqVO) {
        return success(videoPlaybackService.clipRecording(reqVO));
    }
}

