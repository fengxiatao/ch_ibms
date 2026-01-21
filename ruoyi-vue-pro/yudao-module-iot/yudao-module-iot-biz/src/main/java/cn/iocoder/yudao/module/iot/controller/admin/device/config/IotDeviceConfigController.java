package cn.iocoder.yudao.module.iot.controller.admin.device.config;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.config.vo.*;
import cn.iocoder.yudao.module.iot.service.device.config.IotDeviceConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - IoT 设备配置控制器
 *
 * @author 长辉信息科技有限公司
 */
@Tag(name = "管理后台 - IoT 设备配置")
@RestController
@RequestMapping("/iot/device/config")
@Validated
public class IotDeviceConfigController {

    @Resource
    private IotDeviceConfigService deviceConfigService;

    @GetMapping("/get")
    @Operation(summary = "获取设备配置")
    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<DeviceConfigRespVO> getDeviceConfig(@RequestParam("deviceId") Long deviceId) {
        DeviceConfigRespVO config = deviceConfigService.getDeviceConfig(deviceId);
        return success(config);
    }

    @PutMapping("/network")
    @Operation(summary = "更新设备网络配置")
    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> updateNetworkConfig(
            @RequestParam("deviceId") Long deviceId,
            @Valid @RequestBody DeviceNetworkConfigReqVO reqVO) {
        deviceConfigService.updateNetworkConfig(deviceId, reqVO);
        return success(true);
    }

    @PutMapping("/event")
    @Operation(summary = "更新设备事件配置")
    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> updateEventConfig(
            @RequestParam("deviceId") Long deviceId,
            @Valid @RequestBody DeviceEventConfigReqVO reqVO) {
        deviceConfigService.updateEventConfig(deviceId, reqVO);
        return success(true);
    }

    @PutMapping("/video")
    @Operation(summary = "更新设备视频配置")
    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> updateVideoConfig(
            @RequestParam("deviceId") Long deviceId,
            @Valid @RequestBody DeviceVideoConfigReqVO reqVO) {
        deviceConfigService.updateVideoConfig(deviceId, reqVO);
        return success(true);
    }

    @PostMapping("/sync-from-device")
    @Operation(summary = "从设备同步配置")
    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<DeviceConfigRespVO> syncConfigFromDevice(@RequestParam("deviceId") Long deviceId) {
        DeviceConfigRespVO config = deviceConfigService.syncConfigFromDevice(deviceId);
        return success(config);
    }

    @PostMapping("/apply-to-device")
    @Operation(summary = "应用配置到设备")
    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> applyConfigToDevice(@RequestParam("deviceId") Long deviceId) {
        deviceConfigService.applyConfigToDevice(deviceId);
        return success(true);
    }

    @PostMapping("/reset")
    @Operation(summary = "重置设备配置")
    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> resetDeviceConfig(@RequestParam("deviceId") Long deviceId) {
        deviceConfigService.resetDeviceConfig(deviceId);
        return success(true);
    }

    @GetMapping("/capabilities")
    @Operation(summary = "获取设备能力集")
    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<DeviceCapabilitiesRespVO> getDeviceCapabilities(@RequestParam("deviceId") Long deviceId) {
        DeviceCapabilitiesRespVO capabilities = deviceConfigService.getDeviceCapabilities(deviceId);
        return success(capabilities);
    }
}












