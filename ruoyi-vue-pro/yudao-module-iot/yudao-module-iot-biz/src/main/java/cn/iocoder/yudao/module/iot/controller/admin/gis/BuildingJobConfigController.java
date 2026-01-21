package cn.iocoder.yudao.module.iot.controller.admin.gis;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.service.gis.BuildingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 建筑定时任务配置
 *
 * @author IBMS Team
 */
@Tag(name = "管理后台 - 建筑定时任务配置")
@RestController
@RequestMapping("/iot/building-job-config")
public class BuildingJobConfigController {

    @Resource
    private BuildingService buildingService;

    @GetMapping("/get/{id}")
    @Operation(summary = "获取建筑的定时任务配置")
    @Parameter(name = "id", description = "建筑ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:building:query')")
    public CommonResult<String> getBuildingJobConfig(@PathVariable("id") Long id) {
        // 查询建筑列表，找到对应的建筑
        var buildingList = buildingService.getBuildingList();
        var building = buildingList.stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        return success(building != null ? building.getJobConfig() : null);
    }

    @PutMapping("/save/{id}")
    @Operation(summary = "更新建筑的定时任务配置")
    @Parameter(name = "id", description = "建筑ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:building:update')")
    public CommonResult<Boolean> saveBuildingJobConfig(
            @PathVariable("id") Long id,
            @RequestBody String jobConfig) {

        // 更新配置
        buildingService.updateBuildingJobConfig(id, jobConfig);

        return success(true);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除建筑的定时任务配置")
    @Parameter(name = "id", description = "建筑ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:building:delete')")
    public CommonResult<Boolean> deleteJobConfig(@PathVariable("id") Long id) {

        // 删除配置（设置为NULL）
        buildingService.updateBuildingJobConfig(id, null);

        return success(true);
    }
}

