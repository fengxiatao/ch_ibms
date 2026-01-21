package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 设备定时任务配置")
@RestController
@RequestMapping("/iot/device-job-config")
public class DeviceJobConfigController {

    @Resource
    private IotDeviceService deviceService;

    @GetMapping("/get/{id}")
    @Operation(summary = "获取设备的定时任务配置")
    @Parameter(name = "id", description = "设备ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<String> getDeviceJobConfig(@PathVariable("id") Long id) {
        // 查询设备列表，找到对应的设备
        var device = deviceService.getDevice(id);
        return success(device != null ? device.getJobConfig() : null);
    }

    @PutMapping("/save/{id}")
    @Operation(summary = "更新设备的定时任务配置")
    @Parameter(name = "id", description = "设备ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> saveDeviceJobConfig(
            @PathVariable("id") Long id,
            @RequestBody String jobConfig) {

        // 更新配置
        deviceService.updateDeviceJobConfig(id, jobConfig);

        return success(true);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除设备的定时任务配置")
    @Parameter(name = "id", description = "设备ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device:delete')")
    public CommonResult<Boolean> deleteJobConfig(@PathVariable("id") Long id) {

        // 删除配置（设置为NULL）
        deviceService.updateDeviceJobConfig(id, null);

        return success(true);
    }
}



