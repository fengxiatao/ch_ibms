package cn.iocoder.yudao.module.iot.controller.admin.gis;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.building.BuildingPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.building.BuildingRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.building.BuildingSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.gis.BuildingConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.BuildingDO;
import cn.iocoder.yudao.module.iot.service.gis.BuildingService;
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

@Tag(name = "管理后台 - 建筑管理")
@RestController
@RequestMapping("/iot/building")
@Validated
public class BuildingController {

    @Autowired
    private BuildingService buildingService;

    @PostMapping("/create")
    @Operation(summary = "创建建筑")
    @PreAuthorize("@ss.hasPermission('iot:building:create')")
    public CommonResult<Long> createBuilding(@Valid @RequestBody BuildingSaveReqVO createReqVO) {
        return success(buildingService.createBuilding(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新建筑")
    @PreAuthorize("@ss.hasPermission('iot:building:update')")
    public CommonResult<Boolean> updateBuilding(@Valid @RequestBody BuildingSaveReqVO updateReqVO) {
        buildingService.updateBuilding(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除建筑")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:building:delete')")
    public CommonResult<Boolean> deleteBuilding(@RequestParam("id") Long id) {
        buildingService.deleteBuilding(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得建筑")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:building:query')")
    public CommonResult<BuildingRespVO> getBuilding(@RequestParam("id") Long id) {
        BuildingDO building = buildingService.getBuilding(id);
        return success(BuildingConvert.INSTANCE.convert(building));
    }

    @GetMapping("/page")
    @Operation(summary = "获得建筑分页")
    @PreAuthorize("@ss.hasPermission('iot:building:query')")
    public CommonResult<PageResult<BuildingRespVO>> getBuildingPage(@Valid BuildingPageReqVO pageReqVO) {
        PageResult<BuildingDO> pageResult = buildingService.getBuildingPage(pageReqVO);
        return success(BuildingConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/list")
    @Operation(summary = "获得建筑列表")
    @PreAuthorize("@ss.hasPermission('iot:building:query')")
    public CommonResult<List<BuildingRespVO>> getBuildingList() {
        List<BuildingDO> list = buildingService.getBuildingList();
        return success(BuildingConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/list-by-campus")
    @Operation(summary = "根据园区ID获取建筑列表")
    @Parameter(name = "campusId", description = "园区ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:building:query')")
    public CommonResult<List<BuildingRespVO>> getBuildingListByCampusId(@RequestParam("campusId") Long campusId) {
        List<BuildingDO> list = buildingService.getBuildingListByCampusId(campusId);
        return success(BuildingConvert.INSTANCE.convertList(list));
    }

}

