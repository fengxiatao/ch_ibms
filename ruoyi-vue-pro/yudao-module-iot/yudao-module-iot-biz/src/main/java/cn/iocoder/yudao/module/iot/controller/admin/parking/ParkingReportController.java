package cn.iocoder.yudao.module.iot.controller.admin.parking;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.report.ParkingReportOverviewRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Collections;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 停车统计报表")
@RestController
@RequestMapping("/iot/parking/report")
@Validated
public class ParkingReportController {

    @GetMapping("/overview")
    @Operation(summary = "获取停车统计报表概览（演示版，仅返回示例数据）")
    public CommonResult<ParkingReportOverviewRespVO> getOverview(
            @RequestParam(value = "lotId", required = false) Long lotId,
            @RequestParam("startDate") @Parameter(description = "开始日期，格式：yyyy-MM-dd") @NotBlank String startDate,
            @RequestParam("endDate") @Parameter(description = "结束日期，格式：yyyy-MM-dd") @NotBlank String endDate,
            @RequestParam(value = "granularity", required = false) @Parameter(description = "统计粒度：day/week/month") String granularity) {

        // 目前先返回一份简单的示例数据，保证前端页面可用。
        ParkingReportOverviewRespVO.DurationRow sampleDurationRow =
                ParkingReportOverviewRespVO.DurationRow.builder()
                        .bucket("0-1h")
                        .count(0)
                        .rate(0D)
                        .avgFee(BigDecimal.ZERO)
                        .income(BigDecimal.ZERO)
                        .build();

        ParkingReportOverviewRespVO.DurationSummary durationSummary =
                ParkingReportOverviewRespVO.DurationSummary.builder()
                        .shortCount(0)
                        .midCount(0)
                        .longCount(0)
                        .avgText("0分钟")
                        .build();

        ParkingReportOverviewRespVO.PeakSummary peakSummary =
                ParkingReportOverviewRespVO.PeakSummary.builder()
                        .morning("")
                        .evening("")
                        .normal("")
                        .low("")
                        .build();

        ParkingReportOverviewRespVO.CarTypeSummary carTypeSummary =
                ParkingReportOverviewRespVO.CarTypeSummary.builder()
                        .fixed(0)
                        .temp(0)
                        .free(0)
                        .fixedIncomeRate("0%")
                        .build();

        ParkingReportOverviewRespVO.RevenueSummary revenueSummary =
                ParkingReportOverviewRespVO.RevenueSummary.builder()
                        .avgIncome("¥0.00")
                        .weekendVsWorkday("1:1")
                        .holidayGrowth("0%")
                        .monthlyGrowth("0%")
                        .build();

        ParkingReportOverviewRespVO respVO = ParkingReportOverviewRespVO.builder()
                .durationRows(Collections.singletonList(sampleDurationRow))
                .durationSummary(durationSummary)
                .peakRows(Collections.emptyList())
                .peakSummary(peakSummary)
                .carTypeRows(Collections.emptyList())
                .carTypeSummary(carTypeSummary)
                .revenueRows(Collections.emptyList())
                .revenueSummary(revenueSummary)
                .build();

        return success(respVO);
    }
}

