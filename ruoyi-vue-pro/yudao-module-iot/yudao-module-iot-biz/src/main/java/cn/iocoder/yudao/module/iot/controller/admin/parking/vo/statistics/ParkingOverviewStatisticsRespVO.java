package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 停车场概览统计 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingOverviewStatisticsRespVO {

    @Schema(description = "停车场总数", example = "5")
    private Long lotCount;

    @Schema(description = "总车位数", example = "1000")
    private Integer totalSpaces;

    @Schema(description = "在场车辆总数", example = "500")
    private Long presentVehicleCount;

    @Schema(description = "剩余车位数", example = "500")
    private Integer availableSpaces;

    @Schema(description = "整体车位使用率(%)", example = "50.0")
    private Double overallOccupancyRate;

    @Schema(description = "今日入场车辆数", example = "200")
    private Long todayEntryCount;

    @Schema(description = "今日出场车辆数", example = "180")
    private Long todayExitCount;

    @Schema(description = "今日收入金额", example = "5000.00")
    private BigDecimal todayIncome;

    @Schema(description = "月租车总数", example = "100")
    private Long monthlyVehicleCount;

    @Schema(description = "免费车总数", example = "50")
    private Long freeVehicleCount;

    @Schema(description = "各停车场统计")
    private List<LotStatistics> lotStatisticsList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LotStatistics {

        @Schema(description = "停车场ID", example = "1")
        private Long lotId;

        @Schema(description = "停车场名称", example = "A区停车场")
        private String lotName;

        @Schema(description = "总车位数", example = "200")
        private Integer totalSpaces;

        @Schema(description = "在场车辆数", example = "100")
        private Long presentCount;

        @Schema(description = "剩余车位数", example = "100")
        private Integer availableSpaces;

        @Schema(description = "车位使用率(%)", example = "50.0")
        private Double occupancyRate;
    }
}
