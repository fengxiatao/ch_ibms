package cn.iocoder.yudao.module.iot.controller.admin.dashboard;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.dashboard.vo.AlertStatisticsRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.dashboard.vo.DeviceStatisticsRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.dashboard.vo.RealTimeMonitorRespVO;
import cn.iocoder.yudao.module.iot.service.dashboard.IotDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - IoT 数据大屏
 *
 * @author 长辉信息科技有限公司
 */
@Tag(name = "管理后台 - IoT 数据大屏")
@RestController
@RequestMapping("/iot/dashboard")
@Validated
@Slf4j
public class IotDashboardController {

    @Resource
    private IotDashboardService dashboardService;

    @GetMapping("/device-statistics")
    @Operation(summary = "获取设备统计数据")
    @PermitAll
    public CommonResult<DeviceStatisticsRespVO> getDeviceStatistics() {
        return success(dashboardService.getDeviceStatistics());
    }

    @GetMapping("/alert-statistics")
    @Operation(summary = "获取告警统计数据")
    @PermitAll
    public CommonResult<AlertStatisticsRespVO> getAlertStatistics() {
        return success(dashboardService.getAlertStatistics());
    }

    @GetMapping("/real-time-monitor")
    @Operation(summary = "获取实时监控数据")
    @PermitAll
    public CommonResult<RealTimeMonitorRespVO> getRealTimeMonitor() {
        return success(dashboardService.getRealTimeMonitor());
    }
}












