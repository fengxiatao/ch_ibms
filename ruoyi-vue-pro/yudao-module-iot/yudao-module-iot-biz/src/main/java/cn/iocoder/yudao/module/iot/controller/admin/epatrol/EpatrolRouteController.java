package cn.iocoder.yudao.module.iot.controller.admin.epatrol;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolRouteDO;
import cn.iocoder.yudao.module.iot.service.epatrol.EpatrolRouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 电子巡更路线
 *
 * @author 长辉信息
 */
@Tag(name = "管理后台 - 电子巡更路线")
@RestController
@RequestMapping("/iot/epatrol/route")
@Validated
public class EpatrolRouteController {

    @Resource
    private EpatrolRouteService routeService;

    @PostMapping("/create")
    @Operation(summary = "创建巡更路线")
    @PreAuthorize("@ss.hasPermission('iot:epatrol-route:create')")
    public CommonResult<Long> createRoute(@Valid @RequestBody EpatrolRouteSaveReqVO createReqVO) {
        return success(routeService.createRoute(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新巡更路线")
    @PreAuthorize("@ss.hasPermission('iot:epatrol-route:update')")
    public CommonResult<Boolean> updateRoute(@Valid @RequestBody EpatrolRouteSaveReqVO updateReqVO) {
        routeService.updateRoute(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除巡更路线")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:epatrol-route:delete')")
    public CommonResult<Boolean> deleteRoute(@RequestParam("id") Long id) {
        routeService.deleteRoute(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得巡更路线详情")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:epatrol-route:query')")
    public CommonResult<EpatrolRouteRespVO> getRoute(@RequestParam("id") Long id) {
        return success(routeService.getRouteDetail(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得巡更路线分页")
    @PreAuthorize("@ss.hasPermission('iot:epatrol-route:query')")
    public CommonResult<PageResult<EpatrolRouteRespVO>> getRoutePage(@Valid EpatrolRoutePageReqVO pageReqVO) {
        PageResult<EpatrolRouteDO> pageResult = routeService.getRoutePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, EpatrolRouteRespVO.class));
    }

    @GetMapping("/list-all-enabled")
    @Operation(summary = "获得所有启用的巡更路线")
    @PreAuthorize("@ss.hasPermission('iot:epatrol-route:query')")
    public CommonResult<List<EpatrolRouteRespVO>> getEnabledRouteList() {
        List<EpatrolRouteDO> list = routeService.getEnabledRouteList();
        return success(BeanUtils.toBean(list, EpatrolRouteRespVO.class));
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新巡更路线状态")
    @PreAuthorize("@ss.hasPermission('iot:epatrol-route:update')")
    public CommonResult<Boolean> updateRouteStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        routeService.updateRouteStatus(id, status);
        return success(true);
    }

}
