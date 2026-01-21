package cn.iocoder.yudao.module.iot.controller.admin.gis;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.service.gis.CampusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 园区定时任务配置
 *
 * @author IBMS Team
 */
@Tag(name = "管理后台 - 园区定时任务配置")
@RestController
@RequestMapping("/iot/campus-job-config")
public class CampusJobConfigController {

    @Resource
    private CampusService campusService;

    @GetMapping("/get/{id}")
    @Operation(summary = "获取园区的定时任务配置")
    @Parameter(name = "id", description = "园区ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:campus:query')")
    public CommonResult<String> getCampusJobConfig(@PathVariable("id") Long id) {
        // 查询园区列表，找到对应的园区
        var campusList = campusService.getCampusList();
        var campus = campusList.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        return success(campus != null ? campus.getJobConfig() : null);
    }

    @PutMapping("/save/{id}")
    @Operation(summary = "更新园区的定时任务配置")
    @Parameter(name = "id", description = "园区ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:campus:update')")
    public CommonResult<Boolean> saveCampusJobConfig(
            @PathVariable("id") Long id,
            @RequestBody String jobConfig) {

        // 更新配置
        campusService.updateCampusJobConfig(id, jobConfig);

        return success(true);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除园区的定时任务配置")
    @Parameter(name = "id", description = "园区ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:campus:delete')")
    public CommonResult<Boolean> deleteJobConfig(@PathVariable("id") Long id) {

        // 删除配置（设置为NULL）
        campusService.updateCampusJobConfig(id, null);

        return success(true);
    }
}

