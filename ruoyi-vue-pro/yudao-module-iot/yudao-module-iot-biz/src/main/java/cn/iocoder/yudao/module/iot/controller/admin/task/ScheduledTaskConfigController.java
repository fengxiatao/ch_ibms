package cn.iocoder.yudao.module.iot.controller.admin.task;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.task.vo.config.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.task.ScheduledTaskConfigDO;
import cn.iocoder.yudao.module.iot.service.task.ScheduledTaskConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT定时任务配置")
@RestController
@RequestMapping("/iot/task-config")
@Validated
@Slf4j
public class ScheduledTaskConfigController {

    @Resource
    private ScheduledTaskConfigService taskConfigService;

    @PostMapping("/add")
    @Operation(summary = "创建任务配置")
    @PreAuthorize("@ss.hasPermission('iot:task:config:create')")
    public CommonResult<Long> addTask(@Valid @RequestBody ScheduledTaskConfigSaveReqVO createReqVO) {
        return success(taskConfigService.createTask(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新任务配置")
    @PreAuthorize("@ss.hasPermission('iot:task:config:update')")
    public CommonResult<Boolean> updateTask(@Valid @RequestBody ScheduledTaskConfigSaveReqVO updateReqVO) {
        log.info("[Controller] 接收到更新请求，enabled值: {}, 类型: {}", 
                updateReqVO.getEnabled(), 
                updateReqVO.getEnabled() != null ? updateReqVO.getEnabled().getClass().getName() : "null");
        taskConfigService.updateTask(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除任务配置")
    @Parameter(name = "id", description = "任务配置ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:task:config:delete')")
    public CommonResult<Boolean> deleteTask(@PathVariable("id") Long id) {
        taskConfigService.deleteTask(id);
        return success(true);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获得任务配置")
    @Parameter(name = "id", description = "任务配置ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:task:config:query')")
    public CommonResult<ScheduledTaskConfigDO> getTask(@PathVariable("id") Long id) {
        return success(taskConfigService.getTask(id));
    }

    @GetMapping("/list")
    @Operation(summary = "获取实体的任务配置列表")
    @PreAuthorize("@ss.hasPermission('iot:task:config:query')")
    public CommonResult<List<ScheduledTaskConfigDO>> getTaskListByEntity(
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") Long entityId) {
        return success(taskConfigService.getTaskListByEntity(entityType, entityId));
    }

    @GetMapping("/monitor/list")
    @Operation(summary = "获取任务监控列表")
    @PreAuthorize("@ss.hasPermission('iot:task:monitor:query')")
    public CommonResult<PageResult<TaskMonitorRespVO>> getTaskMonitorList(@Valid TaskMonitorPageReqVO pageReqVO) {
        return success(taskConfigService.getTaskMonitorPage(pageReqVO));
    }

    @GetMapping("/monitor/statistics")
    @Operation(summary = "获取任务统计数据")
    @PreAuthorize("@ss.hasPermission('iot:task:monitor:query')")
    public CommonResult<TaskStatisticsRespVO> getStatistics() {
        return success(taskConfigService.getStatistics());
    }

    @PutMapping("/toggle/{id}")
    @Operation(summary = "启用/禁用任务")
    @Parameter(name = "id", description = "任务配置ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:task:config:update')")
    public CommonResult<Boolean> toggleTask(
            @PathVariable("id") Long id,
            @RequestParam("enabled") Boolean enabled) {
        taskConfigService.toggleTask(id, enabled);
        return success(true);
    }

    @PostMapping("/execute/{id}")
    @Operation(summary = "立即执行任务")
    @Parameter(name = "id", description = "任务配置ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:task:config:execute')")
    public CommonResult<Boolean> executeTaskNow(@PathVariable("id") Long id) {
        taskConfigService.executeTaskNow(id);
        return success(true);
    }

}


