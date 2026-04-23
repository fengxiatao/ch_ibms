package cn.iocoder.yudao.module.iot.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceRuntimeDO;
import cn.iocoder.yudao.module.iot.service.ibms.device.IbmsDeviceRuntimeService;
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
    private IbmsDeviceRuntimeService ibmsDeviceRuntimeService;

    @GetMapping("/get/{id}")
    @Operation(summary = "获取设备的定时任务配置")
    @Parameter(name = "id", description = "设备ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<String> getDeviceJobConfig(@PathVariable("id") Long id) {
        IbmsDeviceRuntimeDO rt = ibmsDeviceRuntimeService.getByDeviceId(id);
        return success(rt != null ? rt.getJobConfig() : null);
    }

    @PutMapping("/save/{id}")
    @Operation(summary = "更新设备的定时任务配置")
    @Parameter(name = "id", description = "设备ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> saveDeviceJobConfig(
            @PathVariable("id") Long id,
            @RequestBody String jobConfig) {
        ibmsDeviceRuntimeService.updateJobConfig(id, jobConfig);
        return success(true);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除设备的定时任务配置")
    @Parameter(name = "id", description = "设备ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device:delete')")
    public CommonResult<Boolean> deleteJobConfig(@PathVariable("id") Long id) {
        ibmsDeviceRuntimeService.updateJobConfig(id, null);
        return success(true);
    }
}



