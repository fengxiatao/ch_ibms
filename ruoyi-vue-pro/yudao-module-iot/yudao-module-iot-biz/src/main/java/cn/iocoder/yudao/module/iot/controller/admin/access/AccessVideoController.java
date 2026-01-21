package cn.iocoder.yudao.module.iot.controller.admin.access;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.video.AccessVideoPlayParamsRespVO;
import cn.iocoder.yudao.module.iot.service.access.AccessVideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 门禁设备视频预览接口
 * <p>
 * 提供门禁设备（如人脸一体机）的视频预览功能。
 * 前端通过此接口获取视频播放参数，然后使用 DHPlayer 播放实时视频。
 * <p>
 * Requirements:
 * - 2.1: 通过 Gateway 获取设备的 RTSP 流 URL
 * - 1.3: 检查设备是否支持视频预览
 *
 * @author 长辉信息科技有限公司
 */
@Tag(name = "门禁管理 - 视频预览")
@RestController
@RequestMapping("/iot/access/video")
@Validated
@Slf4j
public class AccessVideoController {

    @Resource
    private AccessVideoService accessVideoService;

    @GetMapping("/play-params")
    @Operation(summary = "获取门禁设备视频播放参数")
    @Parameter(name = "deviceId", description = "设备 ID", required = true)
    @Parameter(name = "channelNo", description = "通道号（默认 0 表示设备内置摄像头）")
    @PreAuthorize("@ss.hasPermission('iot:access-device:query')")
    public CommonResult<AccessVideoPlayParamsRespVO> getPlayParams(
            @RequestParam("deviceId") Long deviceId,
            @RequestParam(value = "channelNo", defaultValue = "0") Integer channelNo) {
        log.info("[门禁视频] 获取播放参数请求: deviceId={}, channelNo={}", deviceId, channelNo);
        AccessVideoPlayParamsRespVO params = accessVideoService.getPlayParams(deviceId, channelNo);
        return success(params);
    }

    @GetMapping("/support-check")
    @Operation(summary = "检查设备是否支持视频预览")
    @Parameter(name = "deviceId", description = "设备 ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-device:query')")
    public CommonResult<Boolean> checkVideoSupport(@RequestParam("deviceId") Long deviceId) {
        log.debug("[门禁视频] 检查视频支持请求: deviceId={}", deviceId);
        Boolean supported = accessVideoService.checkVideoSupport(deviceId);
        return success(supported);
    }
}
