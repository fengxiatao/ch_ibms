package cn.iocoder.yudao.module.iot.controller.admin.video;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.CameraRecordingPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.CameraRecordingRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.DahuaRecordingFileRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.QueryDahuaRecordingReqVO;
import cn.iocoder.yudao.module.iot.service.video.CameraRecordingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 摄像头录像记录 Controller
 *
 * @author 长辉信息
 */
@Tag(name = "管理后台 - 摄像头录像记录")
@RestController
@RequestMapping("/iot/camera/recording")
@Validated
public class CameraRecordingController {

    @Resource
    private CameraRecordingService cameraRecordingService;

    @GetMapping("/page")
    @Operation(summary = "获得录像记录分页")
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<PageResult<CameraRecordingRespVO>> getRecordingPage(@Valid CameraRecordingPageReqVO pageReqVO) {
        PageResult<CameraRecordingRespVO> pageResult = cameraRecordingService.getRecordingPage(pageReqVO);
        return success(pageResult);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除录像记录")
    @Parameter(name = "id", description = "录像记录ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:camera:delete')")
    public CommonResult<Boolean> deleteRecording(@RequestParam("id") Long id) {
        cameraRecordingService.deleteRecording(id);
        return success(true);
    }

    @PostMapping("/start")
    @Operation(summary = "开始录像")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @Parameter(name = "duration", description = "录像时长(秒)，0表示持续录像", required = false)
    @Parameter(name = "policy", description = "录像源策略：nvr/server/both，默认nvr优先", required = false)
    @PreAuthorize("@ss.hasPermission('iot:camera:recording')")
    public CommonResult<Long> startRecording(
            @RequestParam("deviceId") Long deviceId,
            @RequestParam(value = "duration", required = false, defaultValue = "0") Integer duration,
            @RequestParam(value = "policy", required = false) String policy) {
        Long recordingId = cameraRecordingService.startRecording(deviceId, duration, policy);
        return success(recordingId);
    }

    @PostMapping("/stop")
    @Operation(summary = "停止录像")
    @Parameter(name = "recordingId", description = "录像记录ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:camera:recording')")
    public CommonResult<Boolean> stopRecording(@RequestParam("recordingId") Long recordingId) {
        cameraRecordingService.stopRecording(recordingId);
        return success(true);
    }

    @PostMapping("/upload")
    @Operation(summary = "上传录像文件并创建记录")
    @PreAuthorize("@ss.hasPermission('iot:camera:recording')")
    public CommonResult<Long> uploadRecording(
            @RequestParam("deviceId") Long deviceId,
            @RequestParam(value = "recordingType", required = false) Integer recordingType,
            @RequestParam("file") MultipartFile file) throws Exception {
        Long id = cameraRecordingService.uploadRecording(deviceId, recordingType, file);
        return success(id);
    }

    @PostMapping("/query-dahua-files")
    @Operation(summary = "使用大华SDK查询NVR录像文件列表")
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<List<DahuaRecordingFileRespVO>> queryDahuaRecordingFiles(
            @Valid @RequestBody QueryDahuaRecordingReqVO reqVO) {
        List<DahuaRecordingFileRespVO> result = cameraRecordingService.queryDahuaRecordingFiles(reqVO);
        return success(result);
    }

}


