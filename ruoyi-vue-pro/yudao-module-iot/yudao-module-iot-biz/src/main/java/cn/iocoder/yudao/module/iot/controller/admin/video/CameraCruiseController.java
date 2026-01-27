package cn.iocoder.yudao.module.iot.controller.admin.video;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.video.CameraCruiseDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.video.CameraCruisePointDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.video.CameraPresetDO;
import cn.iocoder.yudao.module.iot.service.video.CameraCruiseService;
import cn.iocoder.yudao.module.iot.service.video.CameraPresetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 摄像头巡航
 *
 * @author 芋道源码
 */
@Slf4j
@Tag(name = "管理后台 - 摄像头巡航")
@RestController
@RequestMapping("/iot/camera/cruise")
public class CameraCruiseController {

    @Resource
    private CameraCruiseService cruiseService;

    @Resource
    private CameraPresetService presetService;

    @PostMapping("/create")
    @Operation(summary = "创建巡航路线")
    @PreAuthorize("@ss.hasPermission('iot:camera:cruise:create')")
    public CommonResult<Long> createCruise(@Valid @RequestBody CameraCruiseSaveReqVO createReqVO) {
        return success(cruiseService.createCruise(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新巡航路线")
    @PreAuthorize("@ss.hasPermission('iot:camera:cruise:update')")
    public CommonResult<Boolean> updateCruise(@Valid @RequestBody CameraCruiseSaveReqVO updateReqVO) {
        cruiseService.updateCruise(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除巡航路线")
    @Parameter(name = "id", description = "巡航路线ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:camera:cruise:delete')")
    public CommonResult<Boolean> deleteCruise(@RequestParam("id") Long id) {
        cruiseService.deleteCruise(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得巡航路线")
    @Parameter(name = "id", description = "巡航路线ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:camera:cruise:query')")
    public CommonResult<CameraCruiseRespVO> getCruise(@RequestParam("id") Long id) {
        log.info("[getCruise] 开始查询巡航路线详情, id={}", id);
        CameraCruiseDO cruise = cruiseService.getCruise(id);
        if (cruise == null) {
            log.warn("[getCruise] 巡航路线不存在, id={}", id);
            return success(null);
        }
        CameraCruiseRespVO respVO = BeanUtils.toBean(cruise, CameraCruiseRespVO.class);

        // 查询巡航点列表（通过 Service 层）
        List<CameraCruisePointDO> points = cruiseService.getCruisePointListByCruiseId(id);
        log.info("[getCruise] 查询到 {} 个巡航点, cruiseId={}", points.size(), id);

        // 查询预设点信息（通过 Service 层）
        Map<Long, CameraPresetDO> presetMap = new HashMap<>();
        if (!points.isEmpty()) {
            List<Long> presetIds = points.stream().map(CameraCruisePointDO::getPresetId).collect(Collectors.toList());
            log.info("[getCruise] 预设点ID列表: {}", presetIds);
            presetMap = presetService.getPresetListByIds(presetIds).stream()
                    .collect(Collectors.toMap(CameraPresetDO::getId, p -> p));
            log.info("[getCruise] 查询到 {} 个预设点信息", presetMap.size());
        }

        // 组装巡航点信息
        Map<Long, CameraPresetDO> finalPresetMap = presetMap;
        List<CameraCruisePointRespVO> pointVOs = points.stream().map(point -> {
            CameraCruisePointRespVO vo = BeanUtils.toBean(point, CameraCruisePointRespVO.class);
            CameraPresetDO preset = finalPresetMap.get(point.getPresetId());
            if (preset != null) {
                vo.setPresetNo(preset.getPresetNo());
                vo.setPresetName(preset.getPresetName());
            }
            return vo;
        }).collect(Collectors.toList());

        respVO.setPoints(pointVOs);
        log.info("[getCruise] 返回巡航路线详情, cruiseName={}, pointCount={}", respVO.getCruiseName(), pointVOs.size());
        return success(respVO);
    }

    @GetMapping("/list-by-channel")
    @Operation(summary = "获取通道的所有巡航路线")
    @Parameter(name = "channelId", description = "通道ID", required = true, example = "123")
    @PreAuthorize("@ss.hasPermission('iot:camera:cruise:query')")
    public CommonResult<List<CameraCruiseRespVO>> getCruiseListByChannelId(@RequestParam("channelId") Long channelId) {
        List<CameraCruiseDO> list = cruiseService.getCruiseListByChannelId(channelId);
        List<CameraCruiseRespVO> result = BeanUtils.toBean(list, CameraCruiseRespVO.class);
        
        // 为每个巡航路线查询巡航点数量（通过 Service 层）
        for (CameraCruiseRespVO cruise : result) {
            List<CameraCruisePointDO> points = cruiseService.getCruisePointListByCruiseId(cruise.getId());
            // 只设置简单的巡航点信息（不包含预设点详情），用于显示数量
            List<CameraCruisePointRespVO> pointVOs = points.stream()
                    .map(point -> {
                        CameraCruisePointRespVO vo = new CameraCruisePointRespVO();
                        vo.setId(point.getId());
                        vo.setPresetId(point.getPresetId());
                        vo.setSortOrder(point.getSortOrder());
                        vo.setDwellTime(point.getDwellTime());
                        return vo;
                    })
                    .collect(Collectors.toList());
            cruise.setPoints(pointVOs);
        }
        
        return success(result);
    }

    @PostMapping("/start")
    @Operation(summary = "启动巡航")
    @Parameter(name = "id", description = "巡航路线ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:camera:cruise:control')")
    public CommonResult<Boolean> startCruise(@RequestParam("id") Long id) {
        cruiseService.startCruise(id);
        return success(true);
    }

    @PostMapping("/stop")
    @Operation(summary = "停止巡航")
    @Parameter(name = "id", description = "巡航路线ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:camera:cruise:control')")
    public CommonResult<Boolean> stopCruise(@RequestParam("id") Long id) {
        cruiseService.stopCruise(id);
        return success(true);
    }

    // ========== 巡航点管理 ==========

    @GetMapping("/point/list")
    @Operation(summary = "获取巡航线路的所有点")
    @Parameter(name = "cruiseId", description = "巡航路线ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:camera:cruise:query')")
    public CommonResult<List<CameraCruisePointRespVO>> getCruisePointList(@RequestParam("cruiseId") Long cruiseId) {
        // 通过 Service 层查询巡航点
        List<CameraCruisePointDO> points = cruiseService.getCruisePointListByCruiseId(cruiseId);
        
        // 查询预设点信息（通过 Service 层）
        Map<Long, CameraPresetDO> presetMap = new HashMap<>();
        if (!points.isEmpty()) {
            List<Long> presetIds = points.stream().map(CameraCruisePointDO::getPresetId).collect(Collectors.toList());
            presetMap = presetService.getPresetListByIds(presetIds).stream()
                    .collect(Collectors.toMap(CameraPresetDO::getId, p -> p));
        }
        
        // 组装返回数据
        Map<Long, CameraPresetDO> finalPresetMap = presetMap;
        List<CameraCruisePointRespVO> result = points.stream().map(point -> {
            CameraCruisePointRespVO vo = BeanUtils.toBean(point, CameraCruisePointRespVO.class);
            CameraPresetDO preset = finalPresetMap.get(point.getPresetId());
            if (preset != null) {
                vo.setPresetNo(preset.getPresetNo());
                vo.setPresetName(preset.getPresetName());
            }
            return vo;
        }).collect(Collectors.toList());
        
        return success(result);
    }

    @PostMapping("/point/add")
    @Operation(summary = "添加巡航点")
    @PreAuthorize("@ss.hasPermission('iot:camera:cruise:update')")
    public CommonResult<Long> addCruisePoint(@Valid @RequestBody CameraCruisePointSaveReqVO reqVO) {
        return success(cruiseService.addCruisePoint(reqVO));
    }

    @PutMapping("/point/update")
    @Operation(summary = "更新巡航点")
    @PreAuthorize("@ss.hasPermission('iot:camera:cruise:update')")
    public CommonResult<Boolean> updateCruisePoint(@Valid @RequestBody CameraCruisePointSaveReqVO reqVO) {
        cruiseService.updateCruisePoint(reqVO);
        return success(true);
    }

    @DeleteMapping("/point/delete")
    @Operation(summary = "删除巡航点")
    @Parameter(name = "id", description = "巡航点ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:camera:cruise:update')")
    public CommonResult<Boolean> deleteCruisePoint(@RequestParam("id") Long id) {
        cruiseService.deleteCruisePoint(id);
        return success(true);
    }

    // ========== 设备同步 ==========

    @PostMapping("/sync-to-device")
    @Operation(summary = "同步巡航线路到设备")
    @PreAuthorize("@ss.hasPermission('iot:camera:cruise:control')")
    public CommonResult<Boolean> syncCruiseToDevice(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer tourNo = params.get("tourNo") != null ? Integer.valueOf(params.get("tourNo").toString()) : 1;
        cruiseService.syncCruiseToDevice(id, tourNo);
        return success(true);
    }

    @PostMapping("/start-device-cruise")
    @Operation(summary = "启动设备巡航")
    @PreAuthorize("@ss.hasPermission('iot:camera:cruise:control')")
    public CommonResult<Boolean> startDeviceCruise(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer tourNo = params.get("tourNo") != null ? Integer.valueOf(params.get("tourNo").toString()) : 1;
        cruiseService.startDeviceCruise(id, tourNo);
        return success(true);
    }

    @PostMapping("/stop-device-cruise")
    @Operation(summary = "停止设备巡航")
    @PreAuthorize("@ss.hasPermission('iot:camera:cruise:control')")
    public CommonResult<Boolean> stopDeviceCruise(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer tourNo = params.get("tourNo") != null ? Integer.valueOf(params.get("tourNo").toString()) : 1;
        cruiseService.stopDeviceCruise(id, tourNo);
        return success(true);
    }

}
