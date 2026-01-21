package cn.iocoder.yudao.module.iot.controller.admin.access;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.operationlog.IotAccessOperationLogPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.operationlog.IotAccessOperationLogRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessOperationLogDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO;
import cn.iocoder.yudao.module.iot.service.access.IotAccessChannelService;
import cn.iocoder.yudao.module.iot.service.access.IotAccessOperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 门禁操作日志 Controller
 * 
 * 支持两类操作日志：
 * 1. 门控操作日志：开门、关门、常开、常闭等
 * 2. 授权操作日志：授权下发、授权撤销等（Requirements: 12.1, 12.2, 12.3, 12.4）
 */
@Tag(name = "管理后台 - 门禁操作日志")
@RestController
@RequestMapping("/iot/access/operation-log")
@Validated
public class IotAccessOperationLogController {

    @Resource
    private IotAccessOperationLogService operationLogService;
    
    @Resource
    private IotAccessChannelService channelService;

    @GetMapping("/page")
    @Operation(summary = "获取操作日志分页")
    @PreAuthorize("@ss.hasPermission('iot:access-operation-log:query')")
    public CommonResult<PageResult<IotAccessOperationLogRespVO>> getOperationLogPage(
            @Valid IotAccessOperationLogPageReqVO pageReqVO) {
        PageResult<IotAccessOperationLogDO> pageResult = operationLogService.getOperationLogPage(
                pageReqVO.getDeviceId(),
                pageReqVO.getChannelId(),
                pageReqVO.getOperationType(),
                pageReqVO.getOperatorId(),
                pageReqVO.getResult(),
                pageReqVO.getStartTime(),
                pageReqVO.getEndTime(),
                pageReqVO.getPageNo(),
                pageReqVO.getPageSize()
        );
        return success(convertPageResult(pageResult));
    }

    @GetMapping("/get")
    @Operation(summary = "获取操作日志详情")
    @Parameter(name = "id", description = "日志ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-operation-log:query')")
    public CommonResult<IotAccessOperationLogRespVO> getOperationLog(@RequestParam("id") Long id) {
        IotAccessOperationLogDO log = operationLogService.getOperationLog(id);
        return success(convertToVO(log));
    }
    
    // ========== 授权操作日志接口（Requirements: 12.4） ==========
    
    @GetMapping("/auth/page")
    @Operation(summary = "获取授权操作日志分页（支持按人员、设备、时间范围筛选）")
    @PreAuthorize("@ss.hasPermission('iot:access-operation-log:query')")
    public CommonResult<PageResult<IotAccessOperationLogRespVO>> getAuthOperationLogPage(
            @Valid IotAccessOperationLogPageReqVO pageReqVO) {
        PageResult<IotAccessOperationLogDO> pageResult = operationLogService.getAuthOperationLogPage(pageReqVO);
        return success(convertPageResult(pageResult));
    }
    
    @GetMapping("/auth/by-person")
    @Operation(summary = "获取人员的授权操作日志")
    @Parameter(name = "personId", description = "人员ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-operation-log:query')")
    public CommonResult<List<IotAccessOperationLogRespVO>> getAuthLogsByPerson(
            @RequestParam("personId") Long personId,
            @RequestParam(value = "startTime", required = false) 
            @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) 
            @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime endTime) {
        List<IotAccessOperationLogDO> logs = operationLogService.getAuthLogsByPerson(personId, startTime, endTime);
        return success(logs.stream().map(this::convertToVO).collect(Collectors.toList()));
    }
    
    @GetMapping("/auth/by-device")
    @Operation(summary = "获取设备的授权操作日志")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-operation-log:query')")
    public CommonResult<List<IotAccessOperationLogRespVO>> getAuthLogsByDevice(
            @RequestParam("deviceId") Long deviceId,
            @RequestParam(value = "startTime", required = false) 
            @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) 
            @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime endTime) {
        List<IotAccessOperationLogDO> logs = operationLogService.getAuthLogsByDevice(deviceId, startTime, endTime);
        return success(logs.stream().map(this::convertToVO).collect(Collectors.toList()));
    }
    
    @GetMapping("/auth/by-task")
    @Operation(summary = "获取授权任务的操作日志")
    @Parameter(name = "taskId", description = "授权任务ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-operation-log:query')")
    public CommonResult<List<IotAccessOperationLogRespVO>> getAuthLogsByTaskId(
            @RequestParam("taskId") Long taskId) {
        List<IotAccessOperationLogDO> logs = operationLogService.getAuthLogsByTaskId(taskId);
        return success(logs.stream().map(this::convertToVO).collect(Collectors.toList()));
    }

    // ========== 转换方法 ==========

    private IotAccessOperationLogRespVO convertToVO(IotAccessOperationLogDO log) {
        if (log == null) {
            return null;
        }
        IotAccessOperationLogRespVO vo = new IotAccessOperationLogRespVO();
        vo.setId(log.getId());
        vo.setOperationType(log.getOperationType());
        vo.setDeviceId(log.getDeviceId());
        vo.setDeviceName(log.getDeviceName());
        vo.setChannelId(log.getChannelId());
        vo.setChannelName(log.getChannelName());
        
        // 如果通道名称为空，尝试从数据库查询
        if (vo.getChannelName() == null && log.getChannelId() != null) {
            try {
                IotDeviceChannelDO channel = channelService.getChannel(log.getChannelId());
                if (channel != null) {
                    vo.setChannelName(channel.getChannelName());
                }
            } catch (Exception e) {
                // 忽略查询异常，通道名称保持为空
            }
        }
        
        vo.setOperatorId(log.getOperatorId());
        vo.setOperatorName(log.getOperatorName());
        vo.setResult(log.getResult());
        vo.setResultDesc(log.getResultDesc());
        vo.setErrorMessage(log.getErrorMessage());
        vo.setOperationTime(log.getOperationTime());
        vo.setCreateTime(log.getCreateTime());
        // 设置操作类型名称
        vo.setOperationTypeName(getOperationTypeName(log.getOperationType()));
        
        // 授权操作扩展字段
        vo.setTargetPersonId(log.getTargetPersonId());
        vo.setTargetPersonCode(log.getTargetPersonCode());
        vo.setTargetPersonName(log.getTargetPersonName());
        vo.setPermissionGroupId(log.getPermissionGroupId());
        vo.setPermissionGroupName(log.getPermissionGroupName());
        vo.setAuthTaskId(log.getAuthTaskId());
        vo.setCredentialTypes(log.getCredentialTypes());
        vo.setSuccessCredentialCount(log.getSuccessCredentialCount());
        vo.setFailedCredentialCount(log.getFailedCredentialCount());
        vo.setSdkErrorCode(log.getSdkErrorCode());
        
        return vo;
    }

    private PageResult<IotAccessOperationLogRespVO> convertPageResult(PageResult<IotAccessOperationLogDO> pageResult) {
        List<IotAccessOperationLogRespVO> list = new ArrayList<>();
        if (pageResult.getList() != null && !pageResult.getList().isEmpty()) {
            // 批量查询通道名称，避免N+1问题
            Set<Long> channelIds = pageResult.getList().stream()
                    .filter(log -> log.getChannelId() != null && log.getChannelName() == null)
                    .map(IotAccessOperationLogDO::getChannelId)
                    .collect(Collectors.toSet());
            
            Map<Long, String> channelNameMap = new java.util.HashMap<>();
            if (!channelIds.isEmpty()) {
                for (Long channelId : channelIds) {
                    try {
                        IotDeviceChannelDO channel = channelService.getChannel(channelId);
                        if (channel != null) {
                            channelNameMap.put(channelId, channel.getChannelName());
                        }
                    } catch (Exception e) {
                        // 忽略查询异常
                    }
                }
            }
            
            for (IotAccessOperationLogDO log : pageResult.getList()) {
                IotAccessOperationLogRespVO vo = convertToVO(log);
                // 如果通道名称仍为空，从批量查询结果中获取
                if (vo.getChannelName() == null && log.getChannelId() != null) {
                    vo.setChannelName(channelNameMap.get(log.getChannelId()));
                }
                list.add(vo);
            }
        }
        return new PageResult<>(list, pageResult.getTotal());
    }

    private String getOperationTypeName(String operationType) {
        if (operationType == null) {
            return "未知";
        }
        switch (operationType) {
            // 门控操作
            case "open_door": return "远程开门";
            case "close_door": return "远程关门";
            case "always_open": return "设置常开";
            case "always_closed": return "设置常闭";
            case "cancel_always": return "取消常开/常闭";
            // 授权操作
            case "auth_dispatch_group": return "权限组下发";
            case "auth_dispatch_person": return "单人下发";
            case "auth_revoke": return "授权撤销";
            case "auth_retry": return "授权重试";
            case "add_user": return "添加用户";
            case "delete_user": return "删除用户";
            case "add_card": return "添加卡片";
            case "delete_card": return "删除卡片";
            case "add_face": return "录入人脸";
            case "delete_face": return "删除人脸";
            case "add_fingerprint": return "录入指纹";
            case "delete_fingerprint": return "删除指纹";
            default: return operationType;
        }
    }

}
