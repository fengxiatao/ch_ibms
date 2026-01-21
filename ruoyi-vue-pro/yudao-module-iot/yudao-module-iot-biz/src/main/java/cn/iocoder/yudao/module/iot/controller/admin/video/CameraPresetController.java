package cn.iocoder.yudao.module.iot.controller.admin.video;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.CameraPresetRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.CameraPresetSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.video.CameraPresetDO;
import cn.iocoder.yudao.module.iot.service.video.CameraPresetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 摄像头预设点 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - 摄像头预设点")
@RestController
@RequestMapping("/iot/camera/preset")
@Validated
public class CameraPresetController {

    @Resource
    private CameraPresetService presetService;

    @PostMapping("/create")
    @Operation(summary = "创建预设点")
    @PreAuthorize("@ss.hasPermission('iot:camera:preset:create')")
    public CommonResult<Long> createPreset(@Valid @RequestBody CameraPresetSaveReqVO createReqVO) {
        return success(presetService.createPreset(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新预设点")
    @PreAuthorize("@ss.hasPermission('iot:camera:preset:update')")
    public CommonResult<Boolean> updatePreset(@Valid @RequestBody CameraPresetSaveReqVO updateReqVO) {
        presetService.updatePreset(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除预设点")
    @Parameter(name = "id", description = "预设点ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:camera:preset:delete')")
    public CommonResult<Boolean> deletePreset(@RequestParam("id") Long id) {
        presetService.deletePreset(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得预设点")
    @Parameter(name = "id", description = "预设点ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:camera:preset:query')")
    public CommonResult<CameraPresetRespVO> getPreset(@RequestParam("id") Long id) {
        CameraPresetDO preset = presetService.getPreset(id);
        return success(BeanUtils.toBean(preset, CameraPresetRespVO.class));
    }

    @GetMapping("/list-by-channel")
    @Operation(summary = "获取通道的所有预设点")
    @Parameter(name = "channelId", description = "通道ID", required = true, example = "123")
    @PreAuthorize("@ss.hasPermission('iot:camera:preset:query')")
    public CommonResult<List<CameraPresetRespVO>> getPresetListByChannelId(@RequestParam("channelId") Long channelId) {
        List<CameraPresetDO> list = presetService.getPresetListByChannelId(channelId);
        return success(BeanUtils.toBean(list, CameraPresetRespVO.class));
    }

}
