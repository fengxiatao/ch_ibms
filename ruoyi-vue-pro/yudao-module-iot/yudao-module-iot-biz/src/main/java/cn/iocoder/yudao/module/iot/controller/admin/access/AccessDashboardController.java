package cn.iocoder.yudao.module.iot.controller.admin.access;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.dashboard.AccessDashboardStatisticsRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.dashboard.RealTimeAccessRespVO;
import cn.iocoder.yudao.module.iot.service.access.AccessDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 门禁管理 Dashboard Controller
 *
 * @author 智能化系统
 */
@Tag(name = "管理后台 - 门禁管理 Dashboard")
@RestController
@RequestMapping("/iot/access/dashboard")
@Validated
public class AccessDashboardController {

    @Resource
    private AccessDashboardService accessDashboardService;

    @GetMapping("/statistics")
    @Operation(summary = "获取门禁管理统计数据")
    @PreAuthorize("@ss.hasPermission('iot:access:query')")
    public CommonResult<AccessDashboardStatisticsRespVO> getStatistics() {
        AccessDashboardStatisticsRespVO statistics = accessDashboardService.getStatistics();
        return success(statistics);
    }

    @GetMapping("/real-time")
    @Operation(summary = "获取实时通行数据")
    @PreAuthorize("@ss.hasPermission('iot:access:query')")
    public CommonResult<RealTimeAccessRespVO> getRealTimeAccess(
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        RealTimeAccessRespVO realTimeData = accessDashboardService.getRealTimeAccess(pageSize);
        return success(realTimeData);
    }

    @GetMapping("/trend")
    @Operation(summary = "获取通行趋势数据")
    @PreAuthorize("@ss.hasPermission('iot:access:query')")
    public CommonResult<?> getAccessTrend(
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            @RequestParam(value = "type", required = false) String type) {
        return success(accessDashboardService.getAccessTrend(startTime, endTime, type));
    }

    @GetMapping("/device-status")
    @Operation(summary = "获取设备状态概览")
    @PreAuthorize("@ss.hasPermission('iot:access:query')")
    public CommonResult<?> getDeviceStatusOverview() {
        return success(accessDashboardService.getDeviceStatusOverview());
    }

    @GetMapping("/heatmap")
    @Operation(summary = "获取热力图数据")
    @PreAuthorize("@ss.hasPermission('iot:access:query')")
    public CommonResult<?> getAccessHeatmap(
            @RequestParam("date") String date,
            @RequestParam(value = "type", required = false) String type) {
        return success(accessDashboardService.getAccessHeatmap(date, type));
    }

    @GetMapping("/abnormal-events")
    @Operation(summary = "获取异常事件列表")
    @PreAuthorize("@ss.hasPermission('iot:access:query')")
    public CommonResult<?> getAbnormalEventList(
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "level", required = false) String level) {
        return success(accessDashboardService.getAbnormalEventList(pageSize, level));
    }

}



















