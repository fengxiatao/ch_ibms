package cn.iocoder.yudao.module.iot.controller.admin.access;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.service.access.IotAccessDeviceSyncService;
import cn.iocoder.yudao.module.iot.service.access.dto.DeviceSyncCheckResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 门禁设备人员同步 Controller
 * 
 * 提供设备人员对账、清理、同步等 API
 *
 * @author 长辉信息科技
 */
@Tag(name = "管理后台 - 门禁设备人员同步")
@RestController
@RequestMapping("/iot/access/device-sync")
@Validated
public class IotAccessDeviceSyncController {

    @Resource
    private IotAccessDeviceSyncService syncService;

    @GetMapping("/check")
    @Operation(summary = "对账检查", description = "对比设备实际用户与系统权限组人员的差异")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access:device-sync')")
    public CommonResult<DeviceSyncCheckResult> checkDevice(@RequestParam("deviceId") Long deviceId) {
        return success(syncService.checkDevice(deviceId));
    }

    @GetMapping("/check-batch")
    @Operation(summary = "批量对账检查", description = "批量对比多个设备的人员差异")
    @Parameter(name = "deviceIds", description = "设备ID列表", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access:device-sync')")
    public CommonResult<List<DeviceSyncCheckResult>> checkDevices(@RequestParam("deviceIds") List<Long> deviceIds) {
        return success(syncService.checkDevices(deviceIds));
    }

    @GetMapping("/check-group")
    @Operation(summary = "对账权限组设备", description = "对账权限组关联的所有设备")
    @Parameter(name = "groupId", description = "权限组ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access:device-sync')")
    public CommonResult<List<DeviceSyncCheckResult>> checkByGroup(@RequestParam("groupId") Long groupId) {
        return success(syncService.checkByPermissionGroup(groupId));
    }

    @PostMapping("/clean")
    @Operation(summary = "清理多余用户", description = "删除设备上存在但系统权限组中不存在的用户")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access:device-sync')")
    public CommonResult<DeviceSyncCheckResult> cleanExtraUsers(@RequestParam("deviceId") Long deviceId) {
        return success(syncService.cleanDeviceExtraUsers(deviceId));
    }

    @PostMapping("/repair")
    @Operation(summary = "补发缺失用户", description = "将系统权限组中存在但设备上不存在的人员下发到设备")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access:device-sync')")
    public CommonResult<DeviceSyncCheckResult> repairMissingUsers(@RequestParam("deviceId") Long deviceId) {
        return success(syncService.repairMissingUsers(deviceId));
    }

    @PostMapping("/full-sync")
    @Operation(summary = "全量同步", description = "清空设备所有用户后重新下发权限组人员")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access:device-sync')")
    public CommonResult<DeviceSyncCheckResult> fullSync(@RequestParam("deviceId") Long deviceId) {
        return success(syncService.fullSync(deviceId));
    }

    @PostMapping("/clean-users")
    @Operation(summary = "清理指定用户", description = "从设备上删除指定的用户")
    @PreAuthorize("@ss.hasPermission('iot:access:device-sync')")
    public CommonResult<DeviceSyncCheckResult> cleanSpecificUsers(
            @RequestParam("deviceId") Long deviceId,
            @RequestParam("userIds") List<String> userIds) {
        return success(syncService.cleanSpecificUsers(deviceId, userIds));
    }

    @GetMapping("/device-users")
    @Operation(summary = "查询设备用户", description = "直接查询设备存储的用户列表")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access:device-sync')")
    public CommonResult<List<DeviceSyncCheckResult.DeviceUserInfo>> queryDeviceUsers(
            @RequestParam("deviceId") Long deviceId) {
        return success(syncService.queryDeviceUsers(deviceId));
    }

    @GetMapping("/system-users")
    @Operation(summary = "查询系统应授权人员", description = "根据设备关联的权限组，获取应该下发到该设备的人员")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access:device-sync')")
    public CommonResult<List<DeviceSyncCheckResult.PersonInfo>> getSystemUsers(
            @RequestParam("deviceId") Long deviceId) {
        return success(syncService.getSystemUsers(deviceId));
    }

}
