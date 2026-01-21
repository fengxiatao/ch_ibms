package cn.iocoder.yudao.module.iot.controller.admin.gis;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.*;
import cn.iocoder.yudao.module.iot.service.gis.IotGisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * IoT GIS 地图控制器
 *
 * @author IBMS Team
 */
@Tag(name = "管理后台 - IoT GIS 地图")
@RestController
@RequestMapping("/iot/gis")
@Validated
public class IotGisController {

    @Resource
    private IotGisService gisService;

    @GetMapping("/statistics")
    @Operation(summary = "获取 GIS 统计信息")
    @PreAuthorize("@ss.hasPermission('iot:gis:query')")
    public CommonResult<IotGisStatisticsRespVO> getStatistics() {
        IotGisStatisticsRespVO statistics = gisService.getStatistics();
        return success(statistics);
    }

    @GetMapping("/layer-bounds")
    @Operation(summary = "获取图层边界")
    @Parameter(name = "layer", description = "图层名称", required = true, example = "device")
    @PreAuthorize("@ss.hasPermission('iot:gis:query')")
    public CommonResult<IotGisLayerBoundsRespVO> getLayerBounds(@RequestParam("layer") String layer) {
        IotGisLayerBoundsRespVO bounds = gisService.getLayerBounds(layer);
        return success(bounds);
    }

    @GetMapping("/search")
    @Operation(summary = "空间搜索")
    @PreAuthorize("@ss.hasPermission('iot:gis:query')")
    public CommonResult<PageResult<IotGisFeatureRespVO>> searchFeatures(@Valid IotGisSearchReqVO searchReqVO) {
        PageResult<IotGisFeatureRespVO> pageResult = gisService.searchFeatures(searchReqVO);
        return success(pageResult);
    }

    @GetMapping("/nearby")
    @Operation(summary = "获取附近的设备")
    @PreAuthorize("@ss.hasPermission('iot:gis:query')")
    public CommonResult<List<IotGisDeviceRespVO>> getNearbyDevices(@Valid IotGisNearbyReqVO nearbyReqVO) {
        List<IotGisDeviceRespVO> devices = gisService.getNearbyDevices(nearbyReqVO);
        return success(devices);
    }

    @GetMapping("/devices-in-bounds")
    @Operation(summary = "获取边界范围内的设备")
    @PreAuthorize("@ss.hasPermission('iot:gis:query')")
    public CommonResult<List<IotGisDeviceRespVO>> getDevicesInBounds(@Valid IotGisDevicesInBoundsReqVO boundsReqVO) {
        List<IotGisDeviceRespVO> devices = gisService.getDevicesInBounds(boundsReqVO);
        return success(devices);
    }

    @GetMapping("/device-location")
    @Operation(summary = "获取设备位置信息")
    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:gis:query')")
    public CommonResult<IotGisDeviceLocationRespVO> getDeviceLocation(@RequestParam("deviceId") Long deviceId) {
        IotGisDeviceLocationRespVO location = gisService.getDeviceLocation(deviceId);
        return success(location);
    }

    @PostMapping("/update-device-location")
    @Operation(summary = "更新设备位置")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> updateDeviceLocation(@Valid @RequestBody IotGisUpdateLocationReqVO updateReqVO) {
        gisService.updateDeviceLocation(updateReqVO);
        return success(true);
    }

    @GetMapping("/spatial-relation")
    @Operation(summary = "获取设备的空间关系")
    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:gis:query')")
    public CommonResult<IotGisSpatialRelationRespVO> getSpatialRelation(@RequestParam("deviceId") Long deviceId) {
        IotGisSpatialRelationRespVO relation = gisService.getSpatialRelation(deviceId);
        return success(relation);
    }

    @GetMapping("/heatmap-data")
    @Operation(summary = "获取热力图数据")
    @PreAuthorize("@ss.hasPermission('iot:gis:query')")
    public CommonResult<List<IotGisHeatmapPointRespVO>> getHeatmapData(@Valid IotGisHeatmapReqVO heatmapReqVO) {
        List<IotGisHeatmapPointRespVO> heatmapData = gisService.getHeatmapData(heatmapReqVO);
        return success(heatmapData);
    }

    @GetMapping("/cluster-data")
    @Operation(summary = "获取聚合数据")
    @PreAuthorize("@ss.hasPermission('iot:gis:query')")
    public CommonResult<List<IotGisClusterRespVO>> getClusterData(@Valid IotGisClusterReqVO clusterReqVO) {
        List<IotGisClusterRespVO> clusterData = gisService.getClusterData(clusterReqVO);
        return success(clusterData);
    }

    @GetMapping("/measure-distance")
    @Operation(summary = "测量两点之间的距离")
    @PreAuthorize("@ss.hasPermission('iot:gis:query')")
    public CommonResult<IotGisDistanceRespVO> measureDistance(
            @RequestParam("lon1") BigDecimal lon1,
            @RequestParam("lat1") BigDecimal lat1,
            @RequestParam("lon2") BigDecimal lon2,
            @RequestParam("lat2") BigDecimal lat2
    ) {
        IotGisDistanceRespVO distance = gisService.measureDistance(lon1, lat1, lon2, lat2);
        return success(distance);
    }

    @GetMapping("/layer-features")
    @Operation(summary = "获取图层要素列表")
    @PreAuthorize("@ss.hasPermission('iot:gis:query')")
    public CommonResult<PageResult<IotGisFeatureRespVO>> getLayerFeatures(@Valid IotGisLayerFeaturesReqVO reqVO) {
        PageResult<IotGisFeatureRespVO> pageResult = gisService.getLayerFeatures(reqVO);
        return success(pageResult);
    }

    @GetMapping("/building/code/{code}")
    @Operation(summary = "根据建筑编码查询建筑信息")
    @Parameter(name = "code", description = "建筑编码", required = true, example = "BUILD_A01")
    @PreAuthorize("@ss.hasPermission('iot:gis:query')")
    public CommonResult<IotGisBuildingRespVO> getBuildingByCode(@PathVariable("code") String code) {
        IotGisBuildingRespVO building = gisService.getBuildingByCode(code);
        return success(building);
    }
}

