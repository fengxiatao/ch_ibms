package cn.iocoder.yudao.module.iot.controller.admin.gis;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.AreaRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.PathRequestVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.PathStepRespVO;
import cn.iocoder.yudao.module.iot.service.gis.AreaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - IoT区域 Controller
 *
 * @author ruoyi
 */
@Tag(name = "管理后台 - IoT区域管理")
@RestController
@RequestMapping("/iot/area")
@Validated
@Slf4j
public class IotAreaController {

    @Autowired
    private AreaService areaService;

    // ==================== 基础查询 API ====================

    @GetMapping("/floor/{floorId}/list")
    @Operation(summary = "获取楼层所有区域")
    @PreAuthorize("@ss.hasPermission('iot:area:query')")
    public CommonResult<List<AreaRespVO>> getAreasByFloorId(
            @Parameter(description = "楼层ID", required = true)
            @PathVariable("floorId") Long floorId) {
        List<AreaRespVO> areas = areaService.getAreasByFloorId(floorId);
        return success(areas);
    }

    @GetMapping("/building/{buildingId}/list")
    @Operation(summary = "获取建筑所有区域")
    @PreAuthorize("@ss.hasPermission('iot:area:query')")
    public CommonResult<List<AreaRespVO>> getAreasByBuildingId(
            @Parameter(description = "建筑ID", required = true)
            @PathVariable("buildingId") Long buildingId) {
        List<AreaRespVO> areas = areaService.getAreasByBuildingId(buildingId);
        return success(areas);
    }

    @GetMapping("/floor/{floorId}/type/{areaType}")
    @Operation(summary = "根据楼层和类型查询区域")
    @PreAuthorize("@ss.hasPermission('iot:area:query')")
    public CommonResult<List<AreaRespVO>> getAreasByFloorIdAndType(
            @Parameter(description = "楼层ID", required = true)
            @PathVariable("floorId") Long floorId,
            @Parameter(description = "区域类型", required = true, example = "ROOM")
            @PathVariable("areaType") String areaType) {
        List<AreaRespVO> areas = areaService.getAreasByFloorIdAndType(floorId, areaType);
        return success(areas);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取区域详情")
    @PreAuthorize("@ss.hasPermission('iot:area:query')")
    public CommonResult<AreaRespVO> getArea(
            @Parameter(description = "区域ID", required = true)
            @PathVariable("id") Long id) {
        AreaRespVO area = areaService.getAreaById(id);
        return success(area);
    }

    // ==================== 可视化数据 API ====================

    @GetMapping("/floor/{floorId}/visualization")
    @Operation(summary = "获取楼层可视化数据（JSON格式，包含区域、设备、聚类等）")
    @PreAuthorize("@ss.hasPermission('iot:area:query')")
    public CommonResult<String> getFloorVisualizationData(
            @Parameter(description = "楼层ID", required = true)
            @PathVariable("floorId") Long floorId) {
        String jsonData = areaService.getFloorVisualizationData(floorId);
        return success(jsonData);
    }

    // ==================== 路径规划 API ====================

    @PostMapping("/path/find")
    @Operation(summary = "查找两个区域之间的最短路径")
    @PreAuthorize("@ss.hasPermission('iot:area:query')")
    public CommonResult<List<PathStepRespVO>> findShortestPath(
            @Parameter(description = "路径请求", required = true)
            @Valid @RequestBody PathRequestVO request) {
        List<PathStepRespVO> path = areaService.findShortestPath(request);
        return success(path);
    }

    @GetMapping("/nearby/{areaId}")
    @Operation(summary = "查找附近可导航区域")
    @PreAuthorize("@ss.hasPermission('iot:area:query')")
    public CommonResult<List<Map<String, Object>>> findNearbyAreas(
            @Parameter(description = "区域ID", required = true)
            @PathVariable("areaId") Long areaId,
            @Parameter(description = "最大距离（米）", required = false, example = "50.0")
            @RequestParam(required = false, defaultValue = "50.0") Double maxDistance,
            @Parameter(description = "是否包含垂直连接（电梯、楼梯）", required = false, example = "false")
            @RequestParam(required = false, defaultValue = "false") Boolean includeVertical) {
        List<Map<String, Object>> areas = areaService.findNearbyAreas(areaId, maxDistance, includeVertical);
        return success(areas);
    }

    @GetMapping("/reachability/{areaId}")
    @Operation(summary = "分析区域可达性（N跳内可达的所有区域）")
    @PreAuthorize("@ss.hasPermission('iot:area:query')")
    public CommonResult<List<Map<String, Object>>> analyzeReachability(
            @Parameter(description = "区域ID", required = true)
            @PathVariable("areaId") Long areaId,
            @Parameter(description = "最大跳数", required = false, example = "5")
            @RequestParam(required = false, defaultValue = "5") Integer maxHops) {
        List<Map<String, Object>> areas = areaService.analyzeReachability(areaId, maxHops);
        return success(areas);
    }

    // ==================== 设备定位 API ====================

    @GetMapping("/locate/device/{deviceId}")
    @Operation(summary = "设备定位到区域（根据设备坐标自动判断所属区域）")
    @PreAuthorize("@ss.hasPermission('iot:area:query')")
    public CommonResult<Long> locateDeviceToArea(
            @Parameter(description = "设备ID", required = true)
            @PathVariable("deviceId") Long deviceId) {
        Long areaId = areaService.locateDeviceToArea(deviceId);
        return success(areaId);
    }

}

