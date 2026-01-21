package cn.iocoder.yudao.module.iot.controller.admin.access;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.personpermission.*;
import cn.iocoder.yudao.module.iot.service.access.IotAccessPersonPermissionService;
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
 * 门禁人员权限配置 Controller
 */
@Tag(name = "管理后台 - 门禁人员权限配置")
@RestController
@RequestMapping("/iot/access/person-permission")
@Validated
public class IotAccessPersonPermissionController {

    @Resource
    private IotAccessPersonPermissionService personPermissionService;

    @PostMapping("/assign-by-group")
    @Operation(summary = "按权限组分配权限")
    @PreAuthorize("@ss.hasPermission('iot:access-person-permission:assign')")
    public CommonResult<Boolean> assignByGroup(@Valid @RequestBody IotAccessPersonPermissionAssignReqVO reqVO) {
        personPermissionService.assignByGroup(reqVO.getPersonId(), reqVO.getGroupIds());
        return success(true);
    }

    @PostMapping("/assign-by-group-with-dispatch")
    @Operation(summary = "按权限组分配权限并触发授权下发", description = "分配权限组后，会向权限组关联的设备下发该人员的凭证")
    @PreAuthorize("@ss.hasPermission('iot:access-person-permission:assign')")
    public CommonResult<Long> assignByGroupWithDispatch(@Valid @RequestBody IotAccessPersonPermissionAssignReqVO reqVO) {
        Long taskId = personPermissionService.assignByGroupWithDispatch(reqVO.getPersonId(), reqVO.getGroupIds());
        return success(taskId);
    }

    @PostMapping("/assign-by-device")
    @Operation(summary = "按设备分配权限")
    @PreAuthorize("@ss.hasPermission('iot:access-person-permission:assign')")
    public CommonResult<Boolean> assignByDevice(@Valid @RequestBody IotAccessPersonPermissionAssignReqVO reqVO) {
        personPermissionService.assignByDevice(reqVO.getPersonId(), reqVO.getDeviceIds());
        return success(true);
    }

    @PostMapping("/batch-assign-by-group")
    @Operation(summary = "批量按权限组分配权限")
    @PreAuthorize("@ss.hasPermission('iot:access-person-permission:assign')")
    public CommonResult<Boolean> batchAssignByGroup(@Valid @RequestBody IotAccessPersonPermissionBatchAssignReqVO reqVO) {
        personPermissionService.batchAssignByGroup(reqVO.getPersonIds(), reqVO.getGroupIds());
        return success(true);
    }

    @PostMapping("/batch-assign-by-device")
    @Operation(summary = "批量按设备分配权限")
    @PreAuthorize("@ss.hasPermission('iot:access-person-permission:assign')")
    public CommonResult<Boolean> batchAssignByDevice(@Valid @RequestBody IotAccessPersonPermissionBatchAssignReqVO reqVO) {
        personPermissionService.batchAssignByDevice(reqVO.getPersonIds(), reqVO.getDeviceIds());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取人员权限")
    @Parameter(name = "personId", description = "人员ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-person-permission:query')")
    public CommonResult<IotAccessPersonPermissionRespVO> getPersonPermission(@RequestParam("personId") Long personId) {
        return success(personPermissionService.getPersonPermission(personId));
    }

    @DeleteMapping("/remove-groups")
    @Operation(summary = "移除人员权限组")
    @PreAuthorize("@ss.hasPermission('iot:access-person-permission:assign')")
    public CommonResult<Boolean> removeGroups(@Valid @RequestBody IotAccessPersonPermissionAssignReqVO reqVO) {
        personPermissionService.removeGroups(reqVO.getPersonId(), reqVO.getGroupIds());
        return success(true);
    }

    @DeleteMapping("/remove-groups-with-revoke")
    @Operation(summary = "移除人员权限组并触发权限撤销", description = "移除权限组后，会从权限组关联的设备撤销该人员的凭证")
    @PreAuthorize("@ss.hasPermission('iot:access-person-permission:assign')")
    public CommonResult<Long> removeGroupsWithRevoke(@Valid @RequestBody IotAccessPersonPermissionAssignReqVO reqVO) {
        Long taskId = personPermissionService.removeGroupsWithRevoke(reqVO.getPersonId(), reqVO.getGroupIds());
        return success(taskId);
    }

    @DeleteMapping("/remove-devices")
    @Operation(summary = "移除人员设备权限")
    @PreAuthorize("@ss.hasPermission('iot:access-person-permission:assign')")
    public CommonResult<Boolean> removeDevices(@Valid @RequestBody IotAccessPersonPermissionAssignReqVO reqVO) {
        personPermissionService.removeDevices(reqVO.getPersonId(), reqVO.getDeviceIds());
        return success(true);
    }

    @PostMapping("/trigger-auth-task")
    @Operation(summary = "触发权限下发任务")
    @Parameter(name = "personId", description = "人员ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-person-permission:assign')")
    public CommonResult<Boolean> triggerAuthTask(@RequestParam("personId") Long personId) {
        personPermissionService.triggerAuthTask(personId);
        return success(true);
    }

}
