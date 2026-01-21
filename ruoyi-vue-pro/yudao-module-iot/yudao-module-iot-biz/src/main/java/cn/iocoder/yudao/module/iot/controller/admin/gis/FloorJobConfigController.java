package cn.iocoder.yudao.module.iot.controller.admin.gis;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.service.gis.IotGisFloorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 楼层定时任务配置
 *
 * @author IBMS Team
 */
@Tag(name = "管理后台 - 楼层定时任务配置")
@RestController
@RequestMapping("/iot/floor-job-config")
public class FloorJobConfigController {

    @Resource
    private IotGisFloorService floorService;

    @GetMapping("/get/{id}")
    @Operation(summary = "获取楼层的定时任务配置")
    @Parameter(name = "id", description = "楼层ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:floor:query')")
    public CommonResult<String> getFloorJobConfig(@PathVariable("id") Long id) {
        // 查询楼层列表，找到对应的楼层
        var floorList = floorService.getFloorWithJobConfig();
        var floor = floorList.stream()
                .filter(f -> f.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        return success(floor != null ? floor.getJobConfig() : null);
    }

    @PutMapping("/save/{id}")
    @Operation(summary = "更新楼层的定时任务配置")
    @Parameter(name = "id", description = "楼层ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:floor:update')")
    public CommonResult<Boolean> saveFloorJobConfig(
            @PathVariable("id") Long id,
            @RequestBody String jobConfig) {

        // 更新配置
        floorService.updateFloorJobConfig(id, jobConfig);

        return success(true);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除楼层的定时任务配置")
    @Parameter(name = "id", description = "楼层ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:floor:delete')")
    public CommonResult<Boolean> deleteJobConfig(@PathVariable("id") Long id) {

        // 删除配置（设置为NULL）
        floorService.updateFloorJobConfig(id, null);

        return success(true);
    }
}

