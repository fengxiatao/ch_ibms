package cn.iocoder.yudao.module.iot.controller.admin.gis;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.service.gis.AreaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 区域定时任务配置
 *
 * @author IBMS Team
 */
@Tag(name = "管理后台 - 区域定时任务配置")
@RestController
@RequestMapping("/iot/area-job-config")
public class AreaJobConfigController {

    @Resource
    private AreaService areaService;

    @GetMapping("/get/{id}")
    @Operation(summary = "获取区域的定时任务配置")
    @Parameter(name = "id", description = "区域ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:area:query')")
    public CommonResult<String> getAreaJobConfig(@PathVariable("id") Long id) {
        // 查询区域列表，找到对应的区域
        var areaList = areaService.getAreaWithJobConfig();
        var area = areaList.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        return success(area != null ? area.getJobConfig() : null);
    }

    @PutMapping("/save/{id}")
    @Operation(summary = "更新区域的定时任务配置")
    @Parameter(name = "id", description = "区域ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:area:update')")
    public CommonResult<Boolean> saveAreaJobConfig(
            @PathVariable("id") Long id,
            @RequestBody String jobConfig) {

        // 更新配置
        areaService.updateAreaJobConfig(id, jobConfig);

        return success(true);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除区域的定时任务配置")
    @Parameter(name = "id", description = "区域ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:area:delete')")
    public CommonResult<Boolean> deleteJobConfig(@PathVariable("id") Long id) {

        // 删除配置（设置为NULL）
        areaService.updateAreaJobConfig(id, null);

        return success(true);
    }
}

