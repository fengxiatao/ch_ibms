package cn.iocoder.yudao.module.iot.controller.admin.videoinspection;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.videoinspection.vo.InspectionTaskRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.videoinspection.vo.InspectionTaskSaveReqVO;
import cn.iocoder.yudao.module.iot.service.videoinspection.VideoInspectionTaskService;
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
 * 视频巡检任务 Controller
 *
 * @author system
 */
@Tag(name = "管理后台 - 视频巡检任务")
@RestController
@RequestMapping("/iot/video-inspection/task")
@Validated
public class VideoInspectionTaskController {

    @Resource
    private VideoInspectionTaskService inspectionTaskService;

    @PostMapping("/create")
    @Operation(summary = "创建视频巡检任务")
    @PreAuthorize("@ss.hasPermission('iot:video-inspection-task:create')")
    public CommonResult<Long> createInspectionTask(@Valid @RequestBody InspectionTaskSaveReqVO createReqVO) {
        return success(inspectionTaskService.createInspectionTask(createReqVO));
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "更新视频巡检任务")
    @Parameter(name = "id", description = "任务ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:video-inspection-task:update')")
    public CommonResult<Boolean> updateInspectionTask(@PathVariable("id") Long id, @Valid @RequestBody InspectionTaskSaveReqVO updateReqVO) {
        updateReqVO.setId(id);
        inspectionTaskService.updateInspectionTask(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除视频巡检任务")
    @Parameter(name = "id", description = "任务ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:video-inspection-task:delete')")
    public CommonResult<Boolean> deleteInspectionTask(@PathVariable("id") Long id) {
        inspectionTaskService.deleteInspectionTask(id);
        return success(true);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获得视频巡检任务")
    @Parameter(name = "id", description = "任务ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:video-inspection-task:query')")
    public CommonResult<InspectionTaskRespVO> getInspectionTask(@PathVariable("id") Long id) {
        InspectionTaskRespVO task = inspectionTaskService.getInspectionTask(id);
        return success(task);
    }

    @GetMapping("/list")
    @Operation(summary = "获得视频巡检任务列表")
    @PreAuthorize("@ss.hasPermission('iot:video-inspection-task:query')")
    public CommonResult<List<InspectionTaskRespVO>> getInspectionTaskList() {
        List<InspectionTaskRespVO> list = inspectionTaskService.getInspectionTaskList();
        return success(list);
    }
}
