package cn.iocoder.yudao.module.iot.controller.admin.patrolplan;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.patrolplan.vo.task.PatrolTaskSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan.IotVideoPatrolTaskDO;
import cn.iocoder.yudao.module.iot.service.patrolplan.PatrolTaskService;
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
 * 管理后台 - 轮巡任务 Controller
 *
 * @author 长辉信息
 */
@Tag(name = "管理后台 - 轮巡任务")
@RestController
@RequestMapping("/iot/patrol-task")
@Validated
public class PatrolTaskController {

    @Resource
    private PatrolTaskService patrolTaskService;

    @PostMapping("/create")
    @Operation(summary = "创建轮巡任务")
    @PreAuthorize("@ss.hasPermission('iot:patrol-task:create')")
    public CommonResult<Long> createPatrolTask(@Valid @RequestBody PatrolTaskSaveReqVO createReqVO) {
        return success(patrolTaskService.createPatrolTask(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新轮巡任务")
    @PreAuthorize("@ss.hasPermission('iot:patrol-task:update')")
    public CommonResult<Boolean> updatePatrolTask(@Valid @RequestBody PatrolTaskSaveReqVO updateReqVO) {
        patrolTaskService.updatePatrolTask(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除轮巡任务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:patrol-task:delete')")
    public CommonResult<Boolean> deletePatrolTask(@RequestParam("id") Long id) {
        patrolTaskService.deletePatrolTask(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得轮巡任务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:patrol-task:query')")
    public CommonResult<IotVideoPatrolTaskDO> getPatrolTask(@RequestParam("id") Long id) {
        IotVideoPatrolTaskDO task = patrolTaskService.getPatrolTask(id);
        return success(task);
    }

    @GetMapping("/list")
    @Operation(summary = "获得任务列表")
    @Parameter(name = "planId", description = "计划ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:patrol-task:query')")
    public CommonResult<List<IotVideoPatrolTaskDO>> getPatrolTaskList(@RequestParam("planId") Long planId) {
        List<IotVideoPatrolTaskDO> list = patrolTaskService.getPatrolTaskListByPlanId(planId);
        return success(list);
    }

    @PostMapping("/start")
    @Operation(summary = "启动轮巡任务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:patrol-task:start')")
    public CommonResult<Boolean> startPatrolTask(@RequestParam("id") Long id) {
        patrolTaskService.startPatrolTask(id);
        return success(true);
    }

    @PostMapping("/pause")
    @Operation(summary = "暂停轮巡任务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:patrol-task:pause')")
    public CommonResult<Boolean> pausePatrolTask(@RequestParam("id") Long id) {
        patrolTaskService.pausePatrolTask(id);
        return success(true);
    }

    @PostMapping("/stop")
    @Operation(summary = "停止轮巡任务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:patrol-task:stop')")
    public CommonResult<Boolean> stopPatrolTask(@RequestParam("id") Long id) {
        patrolTaskService.stopPatrolTask(id);
        return success(true);
    }

}
