package cn.iocoder.yudao.module.iot.controller.admin.gis;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.campus.CampusPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.campus.CampusRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.campus.CampusSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.gis.CampusConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.CampusDO;
import cn.iocoder.yudao.module.iot.service.gis.CampusService;
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

@Tag(name = "管理后台 - 园区管理")
@RestController
@RequestMapping("/iot/campus")
@Validated
public class CampusController {

    @Autowired
    private CampusService campusService;

    @PostMapping("/create")
    @Operation(summary = "创建园区")
    @PreAuthorize("@ss.hasPermission('iot:campus:create')")
    public CommonResult<Long> createCampus(@Valid @RequestBody CampusSaveReqVO createReqVO) {
        return success(campusService.createCampus(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新园区")
    @PreAuthorize("@ss.hasPermission('iot:campus:update')")
    public CommonResult<Boolean> updateCampus(@Valid @RequestBody CampusSaveReqVO updateReqVO) {
        campusService.updateCampus(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除园区")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:campus:delete')")
    public CommonResult<Boolean> deleteCampus(@RequestParam("id") Long id) {
        campusService.deleteCampus(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得园区")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:campus:query')")
    public CommonResult<CampusRespVO> getCampus(@RequestParam("id") Long id) {
        CampusDO campus = campusService.getCampus(id);
        return success(CampusConvert.INSTANCE.convert(campus));
    }

    @GetMapping("/page")
    @Operation(summary = "获得园区分页")
    @PreAuthorize("@ss.hasPermission('iot:campus:query')")
    public CommonResult<PageResult<CampusRespVO>> getCampusPage(@Valid CampusPageReqVO pageReqVO) {
        PageResult<CampusDO> pageResult = campusService.getCampusPage(pageReqVO);
        return success(CampusConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/list")
    @Operation(summary = "获得园区列表")
    @PreAuthorize("@ss.hasPermission('iot:campus:query')")
    public CommonResult<List<CampusRespVO>> getCampusList() {
        List<CampusDO> list = campusService.getCampusList();
        return success(CampusConvert.INSTANCE.convertList(list));
    }

}

