package cn.iocoder.yudao.module.iot.controller.admin.task;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.task.vo.log.TaskLogPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.task.vo.log.TaskLogRespVO;
import cn.iocoder.yudao.module.iot.service.task.TaskLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT任务执行日志")
@RestController
@RequestMapping("/iot/task-log")
@Validated
@Slf4j
public class TaskLogController {

    @Resource
    private TaskLogService taskLogService;

    @GetMapping("/page")
    @Operation(summary = "获取任务执行日志分页")
    public CommonResult<PageResult<TaskLogRespVO>> getLogPage(@Valid TaskLogPageReqVO pageReqVO) {
        PageResult<TaskLogRespVO> pageResult = taskLogService.getLogPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取任务执行日志详情")
    @Parameter(name = "id", description = "日志ID", required = true, example = "1024")
    public CommonResult<TaskLogRespVO> getLogDetail(@PathVariable("id") Long id) {
        TaskLogRespVO log = taskLogService.getLogDetail(id);
        return success(log);
    }

}
























































