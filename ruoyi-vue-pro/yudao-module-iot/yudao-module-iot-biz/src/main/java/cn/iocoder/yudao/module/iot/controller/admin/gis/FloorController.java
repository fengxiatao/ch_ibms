package cn.iocoder.yudao.module.iot.controller.admin.gis;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.floor.FloorPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.floor.FloorRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.floor.FloorSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.gis.FloorConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.FloorDO;
import cn.iocoder.yudao.module.iot.service.gis.IotGisFloorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 楼层管理")
@RestController
@RequestMapping("/iot/floor")
@Validated
public class FloorController {

    @Autowired
    private IotGisFloorService floorService;

    @PostMapping("/create")
    @Operation(summary = "创建楼层")
    @PreAuthorize("@ss.hasPermission('iot:floor:create')")
    public CommonResult<Long> createFloor(@Valid @RequestBody FloorSaveReqVO createReqVO) {
        FloorDO floor = FloorConvert.INSTANCE.convert(createReqVO);
        Long id = floorService.createFloor(floor);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新楼层")
    @PreAuthorize("@ss.hasPermission('iot:floor:update')")
    public CommonResult<Boolean> updateFloor(@Valid @RequestBody FloorSaveReqVO updateReqVO) {
        FloorDO floor = FloorConvert.INSTANCE.convert(updateReqVO);
        floorService.updateFloor(floor);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除楼层")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:floor:delete')")
    public CommonResult<Boolean> deleteFloor(@RequestParam("id") Long id) {
        floorService.deleteFloor(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得楼层")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:floor:query')")
    public CommonResult<FloorRespVO> getFloor(@RequestParam("id") Long id) {
        FloorDO floor = floorService.getFloor(id);
        return success(FloorConvert.INSTANCE.convert(floor));
    }

    @GetMapping("/page")
    @Operation(summary = "获得楼层分页")
    @PreAuthorize("@ss.hasPermission('iot:floor:query')")
    public CommonResult<PageResult<FloorRespVO>> getFloorPage(@Valid FloorPageReqVO pageReqVO) {
        PageResult<FloorDO> pageResult = floorService.getFloorPage(pageReqVO);
        return success(FloorConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/list")
    @Operation(summary = "获得楼层列表")
    // @PreAuthorize("@ss.hasPermission('iot:floor:query')")  // 临时注释，方便测试
    public CommonResult<List<FloorRespVO>> getFloorList() {
        List<FloorDO> list = floorService.getFloorList();
        return success(FloorConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/list-by-building")
    @Operation(summary = "根据建筑ID获得楼层列表")
    @Parameter(name = "buildingId", description = "建筑ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:floor:query')")
    public CommonResult<List<FloorRespVO>> getFloorListByBuildingId(@RequestParam("buildingId") Long buildingId) {
        List<FloorDO> list = floorService.getFloorListByBuildingId(buildingId);
        return success(FloorConvert.INSTANCE.convertList(list));
    }

}

