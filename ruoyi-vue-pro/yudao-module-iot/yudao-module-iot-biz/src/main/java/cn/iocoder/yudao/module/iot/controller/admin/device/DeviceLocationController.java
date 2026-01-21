package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.*;
import cn.iocoder.yudao.module.iot.service.device.DeviceLocationService;
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

@Tag(name = "管理后台 - 设备位置管理")
@RestController
@RequestMapping("/iot/device-location")
@Validated
public class DeviceLocationController {

    @Resource
    private DeviceLocationService deviceLocationService;

    @PostMapping("/create")
    @Operation(summary = "创建设备位置信息")
    @PreAuthorize("@ss.hasPermission('iot:device:create')")
    public CommonResult<Long> createDeviceLocation(@Valid @RequestBody DeviceLocationSaveReqVO createReqVO) {
        return success(deviceLocationService.createDeviceLocation(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备位置信息")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> updateDeviceLocation(@Valid @RequestBody DeviceLocationSaveReqVO updateReqVO) {
        deviceLocationService.updateDeviceLocation(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除设备位置信息")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device:delete')")
    public CommonResult<Boolean> deleteDeviceLocation(@RequestParam("id") Long id) {
        deviceLocationService.deleteDeviceLocation(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得设备位置信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<DeviceLocationRespVO> getDeviceLocation(@RequestParam("id") Long id) {
        DeviceLocationRespVO deviceLocation = deviceLocationService.getDeviceLocation(id);
        return success(deviceLocation);
    }

    @GetMapping("/by-device")
    @Operation(summary = "根据设备ID获取位置信息")
    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "1001")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<DeviceLocationRespVO> getDeviceLocationByDeviceId(@RequestParam("deviceId") Long deviceId) {
        DeviceLocationRespVO deviceLocation = deviceLocationService.getDeviceLocationByDeviceId(deviceId);
        return success(deviceLocation);
    }

    @PostMapping("/batch-update")
    @Operation(summary = "批量更新设备位置")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> batchUpdateDeviceLocation(@Valid @RequestBody DeviceLocationBatchUpdateReqVO batchUpdateReqVO) {
        deviceLocationService.batchUpdateDeviceLocation(batchUpdateReqVO);
        return success(true);
    }

    @GetMapping("/floor/{floorId}/devices")
    @Operation(summary = "获取楼层内的所有设备（包含位置信息）")
    @Parameter(name = "floorId", description = "楼层ID", required = true, example = "10")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<List<DeviceWithLocationRespVO>> getDevicesInFloor(@PathVariable("floorId") Long floorId) {
        List<DeviceWithLocationRespVO> devices = deviceLocationService.getDevicesInFloor(floorId);
        return success(devices);
    }

    @GetMapping("/area/{areaId}/devices")
    @Operation(summary = "获取区域内的所有设备（包含位置信息）")
    @Parameter(name = "areaId", description = "区域ID", required = true, example = "100")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<List<DeviceWithLocationRespVO>> getDevicesInArea(@PathVariable("areaId") Long areaId) {
        List<DeviceWithLocationRespVO> devices = deviceLocationService.getDevicesInArea(areaId);
        return success(devices);
    }

    @GetMapping("/building/{buildingId}/devices")
    @Operation(summary = "获取建筑内的所有设备（包含位置信息）")
    @Parameter(name = "buildingId", description = "建筑ID", required = true, example = "5")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<List<DeviceWithLocationRespVO>> getDevicesInBuilding(@PathVariable("buildingId") Long buildingId) {
        List<DeviceWithLocationRespVO> devices = deviceLocationService.getDevicesInBuilding(buildingId);
        return success(devices);
    }

    @PostMapping("/auto-assign-area")
    @Operation(summary = "自动分配设备到区域")
    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "1001")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> autoAssignDeviceToArea(@RequestParam("deviceId") Long deviceId) {
        boolean success = deviceLocationService.autoAssignDeviceToArea(deviceId);
        return success(success);
    }

    @PostMapping("/batch-auto-assign-area")
    @Operation(summary = "批量自动分配设备到区域")
    @Parameter(name = "floorId", description = "楼层ID", required = true, example = "10")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Integer> batchAutoAssignDeviceToArea(@RequestParam("floorId") Long floorId) {
        int count = deviceLocationService.batchAutoAssignDeviceToArea(floorId);
        return success(count);
    }

    @GetMapping("/global-coordinate")
    @Operation(summary = "获取设备的全局坐标（经纬度）")
    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "1001")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<DeviceLocationService.GlobalCoordinateDTO> getDeviceGlobalCoordinate(@RequestParam("deviceId") Long deviceId) {
        DeviceLocationService.GlobalCoordinateDTO coordinate = deviceLocationService.getDeviceGlobalCoordinate(deviceId);
        return success(coordinate);
    }

    @PostMapping("/move-device")
    @Operation(summary = "移动设备到新位置")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> moveDevice(@Valid @RequestBody MoveDeviceReqVO reqVO) {
        deviceLocationService.moveDevice(
            reqVO.getDeviceId(),
            reqVO.getTargetFloorId(),
            reqVO.getTargetAreaId(),
            reqVO.getLocalX(),
            reqVO.getLocalY(),
            reqVO.getLocalZ()
        );
        return success(true);
    }

    @PostMapping("/validate")
    @Operation(summary = "验证设备位置是否有效")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<DeviceLocationService.ValidationResultDTO> validateDeviceLocation(@Valid @RequestBody ValidateLocationReqVO reqVO) {
        DeviceLocationService.ValidationResultDTO result = deviceLocationService.validateDeviceLocation(
            reqVO.getFloorId(),
            reqVO.getLocalX(),
            reqVO.getLocalY()
        );
        return success(result);
    }

    // ========== 内部类：请求VO ==========

    @io.swagger.v3.oas.annotations.media.Schema(description = "移动设备请求VO")
    @lombok.Data
    public static class MoveDeviceReqVO {
        @io.swagger.v3.oas.annotations.media.Schema(description = "设备ID", required = true, example = "1001")
        @jakarta.validation.constraints.NotNull(message = "设备ID不能为空")
        private Long deviceId;

        @io.swagger.v3.oas.annotations.media.Schema(description = "目标楼层ID", example = "10")
        private Long targetFloorId;

        @io.swagger.v3.oas.annotations.media.Schema(description = "目标区域ID", example = "100")
        private Long targetAreaId;

        @io.swagger.v3.oas.annotations.media.Schema(description = "本地X坐标", required = true, example = "15.5")
        @jakarta.validation.constraints.NotNull(message = "本地X坐标不能为空")
        private BigDecimal localX;

        @io.swagger.v3.oas.annotations.media.Schema(description = "本地Y坐标", required = true, example = "20.3")
        @jakarta.validation.constraints.NotNull(message = "本地Y坐标不能为空")
        private BigDecimal localY;

        @io.swagger.v3.oas.annotations.media.Schema(description = "本地Z坐标", example = "1.5")
        private BigDecimal localZ;
    }

    @io.swagger.v3.oas.annotations.media.Schema(description = "验证位置请求VO")
    @lombok.Data
    public static class ValidateLocationReqVO {
        @io.swagger.v3.oas.annotations.media.Schema(description = "楼层ID", required = true, example = "10")
        @jakarta.validation.constraints.NotNull(message = "楼层ID不能为空")
        private Long floorId;

        @io.swagger.v3.oas.annotations.media.Schema(description = "本地X坐标", required = true, example = "15.5")
        @jakarta.validation.constraints.NotNull(message = "本地X坐标不能为空")
        private BigDecimal localX;

        @io.swagger.v3.oas.annotations.media.Schema(description = "本地Y坐标", required = true, example = "20.3")
        @jakarta.validation.constraints.NotNull(message = "本地Y坐标不能为空")
        private BigDecimal localY;
    }

}


















