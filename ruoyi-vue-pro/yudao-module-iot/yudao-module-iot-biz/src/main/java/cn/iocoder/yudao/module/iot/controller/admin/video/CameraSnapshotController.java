package cn.iocoder.yudao.module.iot.controller.admin.video;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.CameraSnapshotPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.CameraSnapshotRespVO;
import cn.iocoder.yudao.module.iot.service.video.CameraSnapshotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 摄像头抓图记录 Controller
 *
 * @author 长辉信息
 */
@Tag(name = "管理后台 - 摄像头抓图记录")
@RestController
@RequestMapping("/iot/camera/snapshot")
@Validated
public class CameraSnapshotController {

    @Resource
    private CameraSnapshotService cameraSnapshotService;

    @GetMapping("/page")
    @Operation(summary = "获得抓图记录分页")
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<PageResult<CameraSnapshotRespVO>> getSnapshotPage(@Valid CameraSnapshotPageReqVO pageReqVO) {
        PageResult<CameraSnapshotRespVO> pageResult = cameraSnapshotService.getSnapshotPage(pageReqVO);
        return success(pageResult);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除抓图记录")
    @Parameter(name = "id", description = "抓图记录ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:camera:delete')")
    public CommonResult<Boolean> deleteSnapshot(@RequestParam("id") Long id) {
        cameraSnapshotService.deleteSnapshot(id);
        return success(true);
    }

    @PutMapping("/mark-processed")
    @Operation(summary = "标记抓图记录为已处理")
    @PreAuthorize("@ss.hasPermission('iot:camera:update')")
    public CommonResult<Boolean> markAsProcessed(
            @RequestParam("id") Long id,
            @RequestParam("processor") String processor,
            @RequestParam(value = "remark", required = false) String remark) {
        cameraSnapshotService.markAsProcessed(id, processor, remark);
        return success(true);
    }

    @PostMapping("/capture")
    @Operation(summary = "手动抓拍")
    @Parameter(name = "channelId", description = "通道ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:camera:snapshot')")
    public CommonResult<Long> captureSnapshot(@RequestParam("channelId") Long channelId) {
        Long snapshotId = cameraSnapshotService.captureSnapshot(channelId);
        return success(snapshotId);
    }

    @GetMapping("/latest")
    @Operation(summary = "获取通道最近一条抓图记录")
    @Parameter(name = "channelId", description = "通道ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:camera:query')")
    public CommonResult<CameraSnapshotRespVO> getLatestSnapshot(@RequestParam("channelId") Long channelId) {
        return success(cameraSnapshotService.getLatestSnapshot(channelId));
    }

    @PostMapping("/upload")
    @Operation(summary = "上传抓图文件并创建记录")
    @PreAuthorize("@ss.hasPermission('iot:camera:snapshot')")
    public CommonResult<Long> uploadSnapshot(
            @RequestParam("channelId") Long channelId,
            @RequestParam(value = "snapshotType", required = false) Integer snapshotType,
            @RequestParam("file") MultipartFile file) throws Exception {
        Long id = cameraSnapshotService.uploadSnapshot(channelId, snapshotType, file);
        return success(id);
    }

}


