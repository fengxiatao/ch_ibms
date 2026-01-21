package cn.iocoder.yudao.module.iot.controller.admin.patrolplan;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.patrolplan.vo.plan.PatrolPlanPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.patrolplan.vo.plan.PatrolPlanSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan.IotVideoPatrolPlanDO;
import cn.iocoder.yudao.module.iot.service.patrolplan.PatrolPlanService;
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
 * 管理后台 - 轮巡计划 Controller
 *
 * @author 长辉信息
 */
@Tag(name = "管理后台 - 轮巡计划")
@RestController
@RequestMapping("/iot/patrol-plan")
@Validated
public class PatrolPlanController {

    @Resource
    private PatrolPlanService patrolPlanService;

    @PostMapping("/create")
    @Operation(summary = "创建轮巡计划")
    @PreAuthorize("@ss.hasPermission('iot:patrol-plan:create')")
    public CommonResult<Long> createPatrolPlan(@Valid @RequestBody PatrolPlanSaveReqVO createReqVO) {
        return success(patrolPlanService.createPatrolPlan(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新轮巡计划")
    @PreAuthorize("@ss.hasPermission('iot:patrol-plan:update')")
    public CommonResult<Boolean> updatePatrolPlan(@Valid @RequestBody PatrolPlanSaveReqVO updateReqVO) {
        patrolPlanService.updatePatrolPlan(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除轮巡计划")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:patrol-plan:delete')")
    public CommonResult<Boolean> deletePatrolPlan(@RequestParam("id") Long id) {
        patrolPlanService.deletePatrolPlan(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得轮巡计划")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:patrol-plan:query')")
    public CommonResult<IotVideoPatrolPlanDO> getPatrolPlan(@RequestParam("id") Long id) {
        IotVideoPatrolPlanDO plan = patrolPlanService.getPatrolPlan(id);
        return success(plan);
    }

    @GetMapping("/page")
    @Operation(summary = "获得轮巡计划分页")
    @PreAuthorize("@ss.hasPermission('iot:patrol-plan:query')")
    public CommonResult<PageResult<IotVideoPatrolPlanDO>> getPatrolPlanPage(@Valid PatrolPlanPageReqVO pageReqVO) {
        PageResult<IotVideoPatrolPlanDO> pageResult = patrolPlanService.getPatrolPlanPage(pageReqVO);
        return success(pageResult);
    }

    @PostMapping("/start")
    @Operation(summary = "启动轮巡计划")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:patrol-plan:start')")
    public CommonResult<Boolean> startPatrolPlan(@RequestParam("id") Long id) {
        patrolPlanService.startPatrolPlan(id);
        return success(true);
    }

    @PostMapping("/pause")
    @Operation(summary = "暂停轮巡计划")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:patrol-plan:pause')")
    public CommonResult<Boolean> pausePatrolPlan(@RequestParam("id") Long id) {
        patrolPlanService.pausePatrolPlan(id);
        return success(true);
    }

    @PostMapping("/stop")
    @Operation(summary = "停止轮巡计划")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:patrol-plan:stop')")
    public CommonResult<Boolean> stopPatrolPlan(@RequestParam("id") Long id) {
        patrolPlanService.stopPatrolPlan(id);
        return success(true);
    }

}
