package cn.iocoder.yudao.module.iot.controller.admin.epatrol;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolPlanDO;
import cn.iocoder.yudao.module.iot.service.epatrol.EpatrolPlanService;
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
 * 管理后台 - 电子巡更计划
 *
 * @author 长辉信息
 */
@Tag(name = "管理后台 - 电子巡更计划")
@RestController
@RequestMapping("/iot/epatrol/plan")
@Validated
public class EpatrolPlanController {

    @Resource
    private EpatrolPlanService planService;

    @PostMapping("/create")
    @Operation(summary = "创建巡更计划")
    @PreAuthorize("@ss.hasPermission('iot:epatrol-plan:create')")
    public CommonResult<Long> createPlan(@Valid @RequestBody EpatrolPlanSaveReqVO createReqVO) {
        return success(planService.createPlan(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新巡更计划")
    @PreAuthorize("@ss.hasPermission('iot:epatrol-plan:update')")
    public CommonResult<Boolean> updatePlan(@Valid @RequestBody EpatrolPlanSaveReqVO updateReqVO) {
        planService.updatePlan(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除巡更计划")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:epatrol-plan:delete')")
    public CommonResult<Boolean> deletePlan(@RequestParam("id") Long id) {
        planService.deletePlan(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得巡更计划详情")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:epatrol-plan:query')")
    public CommonResult<EpatrolPlanRespVO> getPlan(@RequestParam("id") Long id) {
        return success(planService.getPlanDetail(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得巡更计划分页")
    @PreAuthorize("@ss.hasPermission('iot:epatrol-plan:query')")
    public CommonResult<PageResult<EpatrolPlanRespVO>> getPlanPage(@Valid EpatrolPlanPageReqVO pageReqVO) {
        PageResult<EpatrolPlanDO> pageResult = planService.getPlanPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, EpatrolPlanRespVO.class));
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新巡更计划状态")
    @PreAuthorize("@ss.hasPermission('iot:epatrol-plan:update')")
    public CommonResult<Boolean> updatePlanStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        planService.updatePlanStatus(id, status);
        return success(true);
    }

    @PostMapping("/generate-tasks")
    @Operation(summary = "手动生成每日任务")
    @PreAuthorize("@ss.hasPermission('iot:epatrol-plan:create')")
    public CommonResult<Boolean> generateDailyTasks() {
        planService.generateDailyTasks();
        return success(true);
    }

}
