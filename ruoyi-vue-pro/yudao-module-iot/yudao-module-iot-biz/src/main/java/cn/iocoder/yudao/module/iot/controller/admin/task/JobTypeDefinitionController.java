package cn.iocoder.yudao.module.iot.controller.admin.task;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.dal.dataobject.task.JobTypeDefinitionDO;
import cn.iocoder.yudao.module.iot.service.task.JobTypeDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 定时任务类型定义 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - 定时任务类型定义")
@RestController
@RequestMapping("/iot/job-type-definition")
@Validated
public class JobTypeDefinitionController {

    @Resource
    private JobTypeDefinitionService jobTypeDefinitionService;

    @GetMapping("/applicable")
    @Operation(summary = "获取适用于指定实体类型的任务类型列表")
    @Parameter(name = "entityType", description = "实体类型", required = true)
    @PreAuthorize("@ss.hasPermission('iot:task-config:query')")
    public CommonResult<List<JobTypeDefinitionDO>> getApplicableJobTypes(@RequestParam("entityType") String entityType) {
        List<JobTypeDefinitionDO> list = jobTypeDefinitionService.getApplicableJobTypes(entityType);
        return success(list);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取任务类型详情")
    @Parameter(name = "id", description = "任务类型ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:task-config:query')")
    public CommonResult<JobTypeDefinitionDO> getJobTypeDefinition(@PathVariable("id") Long id) {
        JobTypeDefinitionDO jobType = jobTypeDefinitionService.getJobTypeDefinition(id);
        return success(jobType);
    }

    @GetMapping("/get-by-code")
    @Operation(summary = "根据code获取任务类型")
    @Parameter(name = "code", description = "任务编码", required = true)
    @PreAuthorize("@ss.hasPermission('iot:task-config:query')")
    public CommonResult<JobTypeDefinitionDO> getJobTypeByCode(@RequestParam("code") String code) {
        JobTypeDefinitionDO jobType = jobTypeDefinitionService.getJobTypeByCode(code);
        return success(jobType);
    }

}




