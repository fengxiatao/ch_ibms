package cn.iocoder.yudao.module.iot.controller.admin.access;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.device.IotAccessDeviceActivateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.service.access.IotAccessDeviceService;
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
 * 门禁设备管理 Controller
 */
@Tag(name = "管理后台 - 门禁设备管理")
@RestController
@RequestMapping("/iot/access/device")
@Validated
public class IotAccessDeviceController {

    @Resource
    private IotAccessDeviceService accessDeviceService;

    @PostMapping("/activate")
    @Operation(summary = "激活设备")
    @PreAuthorize("@ss.hasPermission('iot:access-device:activate')")
    public CommonResult<Boolean> activateDevice(@Valid @RequestBody IotAccessDeviceActivateReqVO reqVO) {
        accessDeviceService.activateDevice(reqVO.getDeviceId());
        return success(true);
    }

    @PostMapping("/deactivate")
    @Operation(summary = "停用设备")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-device:deactivate')")
    public CommonResult<Boolean> deactivateDevice(@RequestParam("deviceId") Long deviceId) {
        accessDeviceService.deactivateDevice(deviceId);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获取门禁设备列表")
    @PreAuthorize("@ss.hasPermission('iot:access-device:query')")
    public CommonResult<List<IotDeviceDO>> getAccessDevices() {
        return success(accessDeviceService.getAccessDevices());
    }

    @GetMapping("/online")
    @Operation(summary = "获取在线门禁设备列表")
    @PreAuthorize("@ss.hasPermission('iot:access-device:query')")
    public CommonResult<List<IotDeviceDO>> getOnlineAccessDevices() {
        return success(accessDeviceService.getOnlineAccessDevices());
    }

    @GetMapping("/get")
    @Operation(summary = "获取设备详情")
    @Parameter(name = "id", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-device:query')")
    public CommonResult<IotDeviceDO> getDevice(@RequestParam("id") Long id) {
        List<IotDeviceDO> devices = accessDeviceService.getAccessDevices();
        for (IotDeviceDO device : devices) {
            if (device.getId().equals(id)) {
                return success(device);
            }
        }
        return success(null);
    }

    @GetMapping("/config/{id}")
    @Operation(summary = "获取设备完整配置信息")
    @Parameter(name = "id", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-device:query')")
    public CommonResult<cn.iocoder.yudao.module.iot.controller.admin.access.vo.device.IotAccessDeviceConfigRespVO> getDeviceConfig(@PathVariable("id") Long id) {
        return success(accessDeviceService.getDeviceConfig(id));
    }

}
