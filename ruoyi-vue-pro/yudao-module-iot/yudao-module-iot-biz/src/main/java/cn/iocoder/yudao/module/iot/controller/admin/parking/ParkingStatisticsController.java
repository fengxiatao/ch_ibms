package cn.iocoder.yudao.module.iot.controller.admin.parking;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.statistics.ParkingOverviewStatisticsRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.statistics.ParkingPresentStatisticsRespVO;
import cn.iocoder.yudao.module.iot.service.parking.ParkingStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 停车场统计")
@RestController
@RequestMapping("/iot/parking/statistics")
@Validated
public class ParkingStatisticsController {

    @Resource
    private ParkingStatisticsService parkingStatisticsService;

    @GetMapping("/present")
    @Operation(summary = "获取在场车辆统计")
    @Parameter(name = "lotId", description = "停车场ID（可选）")
    @PreAuthorize("@ss.hasPermission('iot:parking:lot:query-btn')")
    public CommonResult<ParkingPresentStatisticsRespVO> getPresentStatistics(
            @RequestParam(value = "lotId", required = false) Long lotId) {
        return success(parkingStatisticsService.getPresentStatistics(lotId));
    }

    @GetMapping("/overview")
    @Operation(summary = "获取停车场概览统计")
    @PreAuthorize("@ss.hasPermission('iot:parking:lot:query-btn')")
    public CommonResult<ParkingOverviewStatisticsRespVO> getOverviewStatistics() {
        return success(parkingStatisticsService.getOverviewStatistics());
    }
}
