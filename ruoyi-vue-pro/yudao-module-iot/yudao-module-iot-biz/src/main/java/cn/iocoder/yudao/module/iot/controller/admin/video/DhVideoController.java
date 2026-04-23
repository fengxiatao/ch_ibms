package cn.iocoder.yudao.module.iot.controller.admin.video;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.DahuaPlaybackParamsRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceRuntimeDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import cn.iocoder.yudao.module.iot.service.ibms.device.IbmsDeviceRuntimeService;
import cn.iocoder.yudao.module.iot.service.video.CameraRecordingService;
import cn.iocoder.yudao.module.iot.service.video.IbmsDeviceVideoNetworkResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 大华视频参数
 */
@Tag(name = "管理后台 - 大华视频参数")
@RestController
@RequestMapping("/iot/video/dh")
@Validated
public class DhVideoController {

    @Resource
    private IbmsDeviceMapper ibmsDeviceMapper;

    @Resource
    private IbmsDeviceRuntimeService ibmsDeviceRuntimeService;

    @Resource
    private CameraRecordingService recordingService;
    
    @Resource
    private cn.iocoder.yudao.module.iot.service.camera.IotCameraService cameraService;
    

    @GetMapping("/play-params")
    @Operation(summary = "获取大华H5播放参数(wsURL/rtspURL/username/password/target)")
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<DhPlayParamsRespVO> getDhPlayParams(
            @Parameter(description = "设备ID", required = true)
            @RequestParam("deviceId") Long deviceId,
            @Parameter(description = "通道号(默认1)")
            @RequestParam(value = "channel", required = false, defaultValue = "1") Integer channel,
            @Parameter(description = "码流类型(0:子码流,1:主码流，默认0)")
            @RequestParam(value = "subType", required = false, defaultValue = "0") Integer subType
    ) {
        IbmsDeviceDO device = ibmsDeviceMapper.selectById(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在: deviceId=" + deviceId);
        }

        IbmsDeviceRuntimeDO runtime = ibmsDeviceRuntimeService.getByDeviceId(deviceId);
        IbmsDeviceVideoNetworkResolver.NetworkParams net = IbmsDeviceVideoNetworkResolver.resolve(device, runtime);

        if (StringUtils.isBlank(net.ip)) {
            throw new RuntimeException("设备IP未配置: deviceId=" + deviceId);
        }

        // 构造 Dahua H5 播放参数
        String wsURL = String.format("ws://%s:%d/rtspoverwebsocket", net.ip, net.httpPort);
        // Align with Dahua H5 demo: both wsURL and rtspURL use the device HTTP port
        String rtspURL = String.format(
                "rtsp://%s:%d/cam/realmonitor?channel=%d&subtype=%d&proto=Private3",
                net.ip, net.httpPort, channel, subType
        );
        String target = String.format("%s:%d", net.ip, net.httpPort);

        DhPlayParamsRespVO vo = new DhPlayParamsRespVO();
        vo.setWsURL(wsURL);
        vo.setRtspURL(rtspURL);
        vo.setUsername(net.username);
        vo.setPassword(net.password);
        vo.setTarget(target);
        return success(vo);
    }

    @GetMapping("/playback-params")
    @Operation(summary = "获取大华回放参数（根据通道ID和时间点查询录像文件并返回播放参数）")
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<DahuaPlaybackParamsRespVO> getPlaybackParams(
            @Parameter(description = "通道ID（摄像头ID）", required = true)
            @RequestParam("cameraId") Long cameraId,
            @Parameter(description = "开始时间", required = true, example = "2025-11-25 09:30:00")
            @RequestParam("startTime") String startTime,
            @Parameter(description = "结束时间", required = true, example = "2025-11-25 10:30:00")
            @RequestParam("endTime") String endTime
    ) {
        // 直接通过通道ID查询摄像头信息
        cn.iocoder.yudao.module.iot.dal.dataobject.camera.IotCameraDO camera = cameraService.getCamera(cameraId);
        if (camera == null) {
            throw new RuntimeException("摄像头不存在: cameraI d=" + cameraId);
        }

        IbmsDeviceDO device = ibmsDeviceMapper.selectById(camera.getDeviceId());
        if (device == null) {
            throw new RuntimeException("摄像头关联的设备不存在: deviceId=" + camera.getDeviceId());
        }
        IbmsDeviceRuntimeDO runtime = ibmsDeviceRuntimeService.getByDeviceId(camera.getDeviceId());
        IbmsDeviceVideoNetworkResolver.NetworkParams net = IbmsDeviceVideoNetworkResolver.resolve(device, runtime);
        if (StringUtils.isBlank(net.ip)) {
            throw new RuntimeException("设备IP未配置: deviceId=" + camera.getDeviceId());
        }

        // 运行态禁止伪造/模拟录像文件路径。
        // 该接口需要通过 newgateway + SDK 的真实录像查询结果（DEVICE_SERVICE_RESULT）来构造参数。
        throw new IllegalStateException("录像文件查询未实现（已移除模拟返回）。请通过 newgateway 真实录像查询结果对接实现");
    }
}
