package cn.iocoder.yudao.module.iot.controller.admin.gis;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.FloorDetailVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.FloorVO;
import cn.iocoder.yudao.module.iot.service.gis.IotGisFloorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * IoT GIS 楼层 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - IoT GIS 楼层")
@RestController
@RequestMapping("/iot/gis/floor")
@Validated
public class IotGisFloorController {

    @Autowired
    private IotGisFloorService floorService;

    @GetMapping("/building/{buildingId}/floors")
    @Operation(summary = "获取建筑的所有楼层", description = "返回虚拟坐标系统的楼层数据")
    @Parameter(name = "buildingId", description = "建筑ID", required = true, example = "1")
    public CommonResult<List<FloorVO>> getBuildingFloors(@PathVariable("buildingId") Long buildingId) {
        List<FloorVO> floors = floorService.getBuildingFloors(buildingId);
        return success(floors);
    }

    @GetMapping("/{floorId}/detail")
    @Operation(summary = "获取楼层详情", description = "包含房间和设备的完整信息")
    @Parameter(name = "floorId", description = "楼层ID", required = true, example = "101")
    public CommonResult<FloorDetailVO> getFloorDetail(@PathVariable("floorId") Long floorId) {
        FloorDetailVO detail = floorService.getFloorDetail(floorId);
        return success(detail);
    }

    @GetMapping("/{floorId}/info")
    @Operation(summary = "获取楼层基本信息")
    @Parameter(name = "floorId", description = "楼层ID", required = true, example = "101")
    public CommonResult<FloorVO> getFloorInfo(@PathVariable("floorId") Long floorId) {
        FloorVO floor = floorService.getFloorInfo(floorId);
        return success(floor);
    }

}

