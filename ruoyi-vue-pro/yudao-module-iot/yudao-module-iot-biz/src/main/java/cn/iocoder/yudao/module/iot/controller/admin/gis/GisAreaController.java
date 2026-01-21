package cn.iocoder.yudao.module.iot.controller.admin.gis;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.area.AreaPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.area.AreaRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.area.AreaSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.gis.AreaConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.AreaDO;
import cn.iocoder.yudao.module.iot.service.gis.IotGisAreaService;
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

@Tag(name = "管理后台 - GIS区域管理")
@RestController
@RequestMapping("/iot/gis/area")
@Validated
public class GisAreaController {

    @Autowired
    private IotGisAreaService areaService;

    @PostMapping("/create")
    @Operation(summary = "创建区域")
    @PreAuthorize("@ss.hasPermission('iot:area:create')")
    public CommonResult<Long> createArea(@Valid @RequestBody AreaSaveReqVO createReqVO) {
        AreaDO area = AreaConvert.INSTANCE.convert(createReqVO);
        Long id = areaService.createArea(area);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新区域")
    @PreAuthorize("@ss.hasPermission('iot:area:update')")
    public CommonResult<Boolean> updateArea(@Valid @RequestBody AreaSaveReqVO updateReqVO) {
        AreaDO area = AreaConvert.INSTANCE.convert(updateReqVO);
        areaService.updateArea(area);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除区域")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:area:delete')")
    public CommonResult<Boolean> deleteArea(@RequestParam("id") Long id) {
        areaService.deleteArea(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得区域")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:area:query')")
    public CommonResult<AreaRespVO> getArea(@RequestParam("id") Long id) {
        AreaDO area = areaService.getArea(id);
        return success(AreaConvert.INSTANCE.convert(area));
    }

    @GetMapping("/page")
    @Operation(summary = "获得区域分页")
    @PreAuthorize("@ss.hasPermission('iot:area:query')")
    public CommonResult<PageResult<AreaRespVO>> getAreaPage(@Valid AreaPageReqVO pageReqVO) {
        PageResult<AreaDO> pageResult = areaService.getAreaPage(pageReqVO);
        return success(AreaConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/list")
    @Operation(summary = "获得区域列表")
    @PreAuthorize("@ss.hasPermission('iot:area:query')")
    public CommonResult<List<AreaRespVO>> getAreaList() {
        List<AreaDO> list = areaService.getAreaList();
        return success(AreaConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/list-by-floor")
    @Operation(summary = "根据楼层ID获得区域列表")
    @Parameter(name = "floorId", description = "楼层ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:area:query')")
    public CommonResult<List<AreaRespVO>> getAreaListByFloorId(@RequestParam("floorId") Long floorId) {
        List<AreaDO> list = areaService.getAreaListByFloorId(floorId);
        return success(AreaConvert.INSTANCE.convertList(list));
    }

    @PostMapping("/batch-create")
    @Operation(summary = "批量创建区域（从DXF识别结果）")
    @PreAuthorize("@ss.hasPermission('iot:area:create')")
    public CommonResult<List<Long>> batchCreateAreas(@Valid @RequestBody List<AreaSaveReqVO> createReqVOList) {
        List<Long> ids = new java.util.ArrayList<>();
        for (AreaSaveReqVO createReqVO : createReqVOList) {
            AreaDO area = AreaConvert.INSTANCE.convert(createReqVO);
            Long id = areaService.createArea(area);
            ids.add(id);
        }
        return success(ids);
    }

}

