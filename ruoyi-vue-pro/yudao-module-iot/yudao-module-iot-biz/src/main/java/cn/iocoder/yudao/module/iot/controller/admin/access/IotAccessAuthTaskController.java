package cn.iocoder.yudao.module.iot.controller.admin.access;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.authtask.IotAccessAuthTaskPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.authtask.IotAccessAuthTaskRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessAuthTaskDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessAuthTaskDetailDO;
import cn.iocoder.yudao.module.iot.service.access.IotAccessAuthDispatchService;
import cn.iocoder.yudao.module.iot.service.access.IotAccessAuthTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 门禁授权任务 Controller
 * 
 * 实现授权任务的创建、查询、重试、撤销等功能
 * 
 * Requirements: 1.1, 2.1, 6.1, 6.3, 7.1, 8.1
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - 门禁授权任务")
@RestController
@RequestMapping("/iot/access/auth-task")
@Validated
public class IotAccessAuthTaskController {

    @Resource
    private IotAccessAuthTaskService authTaskService;

    @Resource
    private IotAccessAuthDispatchService authDispatchService;

    // ========== 授权下发接口 (Requirements: 1.1, 2.1) ==========

    @PostMapping("/dispatch/group/{groupId}")
    @Operation(summary = "权限组授权下发", description = "将权限组中的人员权限批量下发到关联设备")
    @Parameter(name = "groupId", description = "权限组ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-auth-task:dispatch')")
    public CommonResult<Long> dispatchByGroup(@PathVariable("groupId") Long groupId) {
        Long taskId = authDispatchService.dispatchPermissionGroup(groupId);
        return success(taskId);
    }

    @PostMapping("/dispatch/person/{personId}")
    @Operation(summary = "单人授权下发", description = "为单个人员下发权限到其关联的所有设备")
    @Parameter(name = "personId", description = "人员ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-auth-task:dispatch')")
    public CommonResult<Long> dispatchByPerson(@PathVariable("personId") Long personId) {
        Long taskId = authDispatchService.dispatchPerson(personId);
        return success(taskId);
    }

    @PostMapping("/dispatch/group/{groupId}/incremental")
    @Operation(summary = "权限组增量下发", description = "只下发新增人员或凭证变更的人员")
    @Parameter(name = "groupId", description = "权限组ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-auth-task:dispatch')")
    public CommonResult<Long> dispatchByGroupIncremental(@PathVariable("groupId") Long groupId) {
        Long taskId = authDispatchService.dispatchPermissionGroupIncremental(groupId);
        return success(taskId);
    }

    @PostMapping("/dispatch/person/{personId}/incremental")
    @Operation(summary = "单人增量下发", description = "只下发变更的凭证类型")
    @Parameter(name = "personId", description = "人员ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-auth-task:dispatch')")
    public CommonResult<Long> dispatchByPersonIncremental(@PathVariable("personId") Long personId) {
        Long taskId = authDispatchService.dispatchPersonIncremental(personId);
        return success(taskId);
    }

    // ========== 权限撤销接口 (Requirements: 8.1) ==========

    @PostMapping("/revoke/person/{personId}")
    @Operation(summary = "撤销人员权限", description = "从所有设备删除该人员的权限")
    @Parameter(name = "personId", description = "人员ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-auth-task:revoke')")
    public CommonResult<Long> revokeByPerson(@PathVariable("personId") Long personId) {
        Long taskId = authDispatchService.revokePerson(personId);
        return success(taskId);
    }

    @PostMapping("/revoke/persons")
    @Operation(summary = "批量撤销人员权限", description = "从所有设备删除多个人员的权限")
    @PreAuthorize("@ss.hasPermission('iot:access-auth-task:revoke')")
    public CommonResult<Long> revokeByPersons(@RequestBody List<Long> personIds) {
        Long taskId = authDispatchService.revokePersons(personIds);
        return success(taskId);
    }

    @PostMapping("/revoke/person/{personId}/group/{groupId}")
    @Operation(summary = "从权限组撤销人员权限", description = "只撤销该权限组关联设备上的权限")
    @Parameter(name = "personId", description = "人员ID", required = true)
    @Parameter(name = "groupId", description = "权限组ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-auth-task:revoke')")
    public CommonResult<Long> revokePersonFromGroup(@PathVariable("personId") Long personId,
                                                     @PathVariable("groupId") Long groupId) {
        Long taskId = authDispatchService.revokePersonFromGroup(personId, groupId);
        return success(taskId);
    }

    // ========== 任务查询接口 (Requirements: 6.1, 6.3) ==========

    @GetMapping("/page")
    @Operation(summary = "获取授权任务分页")
    @PreAuthorize("@ss.hasPermission('iot:access-auth-task:query')")
    public CommonResult<PageResult<IotAccessAuthTaskRespVO>> getTaskPage(@Valid IotAccessAuthTaskPageReqVO pageReqVO) {
        PageResult<IotAccessAuthTaskDO> pageResult = authTaskService.getTaskPage(pageReqVO);
        return success(convertPageResult(pageResult));
    }

    @GetMapping("/get")
    @Operation(summary = "获取授权任务详情")
    @Parameter(name = "id", description = "任务ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-auth-task:query')")
    public CommonResult<IotAccessAuthTaskRespVO> getTask(@RequestParam("id") Long id) {
        IotAccessAuthTaskDO task = authTaskService.getTask(id);
        IotAccessAuthTaskRespVO vo = convertToVO(task);
        if (vo != null) {
            // 填充任务明细
            List<IotAccessAuthTaskDetailDO> details = authTaskService.getTaskDetails(id);
            vo.setDetails(convertDetails(details));
        }
        return success(vo);
    }

    @GetMapping("/{taskId}/details")
    @Operation(summary = "获取任务明细列表")
    @Parameter(name = "taskId", description = "任务ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-auth-task:query')")
    public CommonResult<List<IotAccessAuthTaskRespVO.DetailVO>> getTaskDetails(@PathVariable("taskId") Long taskId) {
        List<IotAccessAuthTaskDetailDO> details = authTaskService.getTaskDetails(taskId);
        return success(convertDetails(details));
    }

    @GetMapping("/{taskId}/failed-details")
    @Operation(summary = "获取失败的任务明细列表")
    @Parameter(name = "taskId", description = "任务ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-auth-task:query')")
    public CommonResult<List<IotAccessAuthTaskRespVO.DetailVO>> getFailedTaskDetails(@PathVariable("taskId") Long taskId) {
        List<IotAccessAuthTaskDetailDO> details = authTaskService.getFailedTaskDetails(taskId);
        return success(convertDetails(details));
    }

    // ========== 重试接口 (Requirements: 7.1) ==========

    @PostMapping("/{taskId}/retry")
    @Operation(summary = "重试失败任务", description = "只重新下发失败的明细")
    @Parameter(name = "taskId", description = "任务ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-auth-task:retry')")
    public CommonResult<Long> retryTask(@PathVariable("taskId") Long taskId) {
        Long newTaskId = authDispatchService.retryFailedDetails(taskId);
        return success(newTaskId);
    }

    @PostMapping("/cancel/{taskId}")
    @Operation(summary = "取消正在执行的任务")
    @Parameter(name = "taskId", description = "任务ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-auth-task:cancel')")
    public CommonResult<Boolean> cancelTask(@PathVariable("taskId") Long taskId) {
        boolean result = authDispatchService.cancelTask(taskId);
        return success(result);
    }

    // ========== 转换方法 ==========

    private IotAccessAuthTaskRespVO convertToVO(IotAccessAuthTaskDO task) {
        if (task == null) {
            return null;
        }
        IotAccessAuthTaskRespVO vo = new IotAccessAuthTaskRespVO();
        vo.setId(task.getId());
        vo.setTaskType(task.getTaskType());
        vo.setGroupId(task.getGroupId());
        // deviceId 从任务明细中获取，任务本身不存储设备ID
        vo.setTotalCount(task.getTotalCount());
        vo.setSuccessCount(task.getSuccessCount());
        vo.setFailCount(task.getFailCount());
        vo.setStatus(task.getStatus());
        vo.setStartTime(task.getStartTime());
        vo.setEndTime(task.getEndTime());
        vo.setCreateTime(task.getCreateTime());
        // 计算进度
        vo.setProgress(calculateProgress(task));
        return vo;
    }

    /**
     * 计算任务进度百分比
     */
    private Integer calculateProgress(IotAccessAuthTaskDO task) {
        if (task.getTotalCount() == null || task.getTotalCount() <= 0) {
            return 0;
        }
        int completed = (task.getSuccessCount() != null ? task.getSuccessCount() : 0) +
                (task.getFailCount() != null ? task.getFailCount() : 0);
        return completed * 100 / task.getTotalCount();
    }

    private PageResult<IotAccessAuthTaskRespVO> convertPageResult(PageResult<IotAccessAuthTaskDO> pageResult) {
        List<IotAccessAuthTaskRespVO> list = new ArrayList<>();
        if (pageResult.getList() != null) {
            for (IotAccessAuthTaskDO task : pageResult.getList()) {
                list.add(convertToVO(task));
            }
        }
        return new PageResult<>(list, pageResult.getTotal());
    }

    private List<IotAccessAuthTaskRespVO.DetailVO> convertDetails(List<IotAccessAuthTaskDetailDO> details) {
        List<IotAccessAuthTaskRespVO.DetailVO> result = new ArrayList<>();
        if (details != null) {
            for (IotAccessAuthTaskDetailDO detail : details) {
                IotAccessAuthTaskRespVO.DetailVO vo = new IotAccessAuthTaskRespVO.DetailVO();
                vo.setId(detail.getId());
                vo.setPersonId(detail.getPersonId());
                vo.setPersonCode(detail.getPersonCode());
                vo.setPersonName(detail.getPersonName());
                vo.setDeviceId(detail.getDeviceId());
                vo.setDeviceName(detail.getDeviceName());
                vo.setCredentialTypes(detail.getCredentialTypes());
                vo.setStatus(detail.getStatus());
                vo.setErrorMessage(detail.getErrorMessage());
                vo.setExecuteTime(detail.getExecuteTime());
                result.add(vo);
            }
        }
        return result;
    }

}
