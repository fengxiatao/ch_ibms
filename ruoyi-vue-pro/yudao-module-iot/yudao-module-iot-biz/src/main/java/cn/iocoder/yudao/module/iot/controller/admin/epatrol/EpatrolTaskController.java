package cn.iocoder.yudao.module.iot.controller.admin.epatrol;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolTaskDO;
import cn.iocoder.yudao.module.iot.service.epatrol.EpatrolTaskService;
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
 * 管理后台 - 电子巡更任务
 *
 * @author 长辉信息
 */
@Tag(name = "管理后台 - 电子巡更任务")
@RestController
@RequestMapping("/iot/epatrol/task")
@Validated
public class EpatrolTaskController {

    @Resource
    private EpatrolTaskService taskService;

    @GetMapping("/get")
    @Operation(summary = "获得巡更任务详情")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:epatrol-task:query')")
    public CommonResult<EpatrolTaskRespVO> getTask(@RequestParam("id") Long id) {
        return success(taskService.getTaskDetail(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得巡更任务分页")
    @PreAuthorize("@ss.hasPermission('iot:epatrol-task:query')")
    public CommonResult<PageResult<EpatrolTaskRespVO>> getTaskPage(@Valid EpatrolTaskPageReqVO pageReqVO) {
        PageResult<EpatrolTaskDO> pageResult = taskService.getTaskPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, EpatrolTaskRespVO.class));
    }

    @PostMapping("/submit")
    @Operation(summary = "提交巡更结果")
    @PreAuthorize("@ss.hasPermission('iot:epatrol-task:submit')")
    public CommonResult<Boolean> submitTask(@Valid @RequestBody EpatrolTaskSubmitReqVO submitReqVO) {
        taskService.submitTask(submitReqVO);
        return success(true);
    }

}
