package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 停车场统计报表概览 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingReportOverviewRespVO {

    @Schema(description = "停车时长分布明细")
    private List<DurationRow> durationRows;

    @Schema(description = "停车时长分布汇总")
    private DurationSummary durationSummary;

    @Schema(description = "时段流量明细")
    private List<PeakRow> peakRows;

    @Schema(description = "时段流量汇总")
    private PeakSummary peakSummary;

    @Schema(description = "车辆类型明细")
    private List<CarTypeRow> carTypeRows;

    @Schema(description = "车辆类型汇总")
    private CarTypeSummary carTypeSummary;

    @Schema(description = "收益趋势明细")
    private List<RevenueRow> revenueRows;

    @Schema(description = "收益趋势汇总")
    private RevenueSummary revenueSummary;

    // ========== 停车时长 ==========

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DurationRow {

        @Schema(description = "时长区间", example = "0-1h")
        private String bucket;

        @Schema(description = "车辆数量", example = "10")
        private Integer count;

        @Schema(description = "占比，0-1 之间的小数", example = "0.25")
        private Double rate;

        @Schema(description = "平均收费金额", example = "5.00")
        private BigDecimal avgFee;

        @Schema(description = "收入金额", example = "50.00")
        private BigDecimal income;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DurationSummary {

        @Schema(description = "短时停车数量", example = "20")
        private Integer shortCount;

        @Schema(description = "中时停车数量", example = "30")
        private Integer midCount;

        @Schema(description = "长时停车数量", example = "10")
        private Integer longCount;

        @Schema(description = "平均停车时长描述", example = "2.5小时")
        private String avgText;
    }

    // ========== 时段流量 ==========

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeakRow {

        @Schema(description = "时段", example = "08:00-09:00")
        private String period;

        @Schema(description = "入场车辆数", example = "30")
        private Integer inCount;

        @Schema(description = "离场车辆数", example = "20")
        private Integer outCount;

        @Schema(description = "净增车辆数", example = "10")
        private Integer net;

        @Schema(description = "平均通行时间", example = "2分钟")
        private String avgTime;

        @Schema(description = "拥堵程度描述", example = "较为通畅")
        private String congestion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeakSummary {

        @Schema(description = "早高峰时段描述", example = "08:00-09:00")
        private String morning;

        @Schema(description = "晚高峰时段描述", example = "17:30-18:30")
        private String evening;

        @Schema(description = "平峰时段描述", example = "10:00-17:00")
        private String normal;

        @Schema(description = "低谷时段描述", example = "00:00-06:00")
        private String low;
    }

    // ========== 车辆类型 ==========

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CarTypeRow {

        @Schema(description = "车辆类型", example = "临时车")
        private String type;

        @Schema(description = "数量", example = "50")
        private Integer count;

        @Schema(description = "占比，0-1 之间的小数", example = "0.5")
        private Double rate;

        @Schema(description = "月均消费描述", example = "¥200")
        private String monthlyAvgText;

        @Schema(description = "收入金额", example = "10000.00")
        private BigDecimal income;

        @Schema(description = "收入占比，0-1 之间的小数", example = "0.6")
        private Double incomeRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CarTypeSummary {

        @Schema(description = "固定车数量", example = "80")
        private Integer fixed;

        @Schema(description = "临时车数量", example = "120")
        private Integer temp;

        @Schema(description = "免费车数量", example = "10")
        private Integer free;

        @Schema(description = "固定车收入占比描述", example = "40%")
        private String fixedIncomeRate;
    }

    // ========== 收益趋势 ==========

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueRow {

        @Schema(description = "统计周期", example = "2025-01-01")
        private String period;

        @Schema(description = "总收入", example = "1000.00")
        private BigDecimal total;

        @Schema(description = "环比增长描述", example = "+10%")
        private String momText;

        @Schema(description = "同比增长描述", example = "+5%")
        private String yoyText;

        @Schema(description = "日均收入描述", example = "¥200")
        private String avgText;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueSummary {

        @Schema(description = "平均每日收入", example = "¥500.00")
        private String avgIncome;

        @Schema(description = "周末 vs 工作日 对比描述", example = "1.2 : 1")
        private String weekendVsWorkday;

        @Schema(description = "节假日增长描述", example = "+30%")
        private String holidayGrowth;

        @Schema(description = "月度增长率描述", example = "+8%")
        private String monthlyGrowth;
    }
}

