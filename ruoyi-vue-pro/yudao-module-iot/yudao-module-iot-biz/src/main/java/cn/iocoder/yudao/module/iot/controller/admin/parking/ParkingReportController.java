package cn.iocoder.yudao.module.iot.controller.admin.parking;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.report.ParkingReportOverviewRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingRecordDO;
import cn.iocoder.yudao.module.iot.dal.mysql.parking.ParkingRecordMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 停车统计报表")
@RestController
@RequestMapping("/iot/parking/report")
@Validated
public class ParkingReportController {

    @Resource
    private ParkingRecordMapper parkingRecordMapper;

    @GetMapping("/overview")
    @Operation(summary = "获取停车统计报表概览")
    public CommonResult<ParkingReportOverviewRespVO> getOverview(
            @RequestParam(value = "lotId", required = false) Long lotId,
            @RequestParam("startDate") @Parameter(description = "开始日期，格式：yyyy-MM-dd") @NotBlank String startDate,
            @RequestParam("endDate") @Parameter(description = "结束日期，格式：yyyy-MM-dd") @NotBlank String endDate,
            @RequestParam(value = "granularity", required = false) @Parameter(description = "统计粒度：day/week/month") String granularity) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        if (end.isBefore(start)) {
            LocalDate swap = start;
            start = end;
            end = swap;
        }
        String g = ("week".equalsIgnoreCase(granularity) || "month".equalsIgnoreCase(granularity))
                ? granularity.toLowerCase(Locale.ROOT) : "day";
        LocalDateTime rangeStart = start.atStartOfDay();
        LocalDateTime rangeEndExclusive = end.plusDays(1).atStartOfDay();

        // 时长/车型/收益按“已出场记录”统计；时段流量同时使用入场和出场。
        List<ParkingRecordDO> exitRecords = parkingRecordMapper.selectList(new LambdaQueryWrapperX<ParkingRecordDO>()
                .eqIfPresent(ParkingRecordDO::getLotId, lotId)
                .eq(ParkingRecordDO::getRecordStatus, 2)
                .isNotNull(ParkingRecordDO::getExitTime)
                .ge(ParkingRecordDO::getExitTime, rangeStart)
                .lt(ParkingRecordDO::getExitTime, rangeEndExclusive)
                .orderByAsc(ParkingRecordDO::getExitTime));
        List<ParkingRecordDO> allRecords = parkingRecordMapper.selectList(new LambdaQueryWrapperX<ParkingRecordDO>()
                .eqIfPresent(ParkingRecordDO::getLotId, lotId)
                .isNotNull(ParkingRecordDO::getEntryTime)
                .ge(ParkingRecordDO::getEntryTime, rangeStart)
                .lt(ParkingRecordDO::getEntryTime, rangeEndExclusive));

        ParkingReportOverviewRespVO respVO = ParkingReportOverviewRespVO.builder()
                .durationRows(buildDurationRows(exitRecords))
                .durationSummary(buildDurationSummary(exitRecords))
                .peakRows(buildPeakRows(allRecords, exitRecords))
                .peakSummary(buildPeakSummary(allRecords, exitRecords))
                .carTypeRows(buildCarTypeRows(exitRecords))
                .carTypeSummary(buildCarTypeSummary(exitRecords))
                .revenueRows(buildRevenueRows(exitRecords, g))
                .revenueSummary(buildRevenueSummary(exitRecords, start, end))
                .build();
        return success(respVO);
    }

    private List<ParkingReportOverviewRespVO.DurationRow> buildDurationRows(List<ParkingRecordDO> records) {
        List<String> buckets = List.of("0-1h", "1-2h", "2-4h", "4-8h", "8-12h", ">12h");
        Map<String, List<ParkingRecordDO>> grouped = buckets.stream()
                .collect(Collectors.toMap(Function.identity(), k -> new ArrayList<>(), (a, b) -> a));
        for (ParkingRecordDO r : records) {
            grouped.get(toDurationBucket(r.getParkingDuration())).add(r);
        }
        int total = Math.max(records.size(), 1);
        List<ParkingReportOverviewRespVO.DurationRow> rows = new ArrayList<>();
        for (String bucket : buckets) {
            List<ParkingRecordDO> item = grouped.get(bucket);
            BigDecimal income = item.stream().map(this::paidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avgFee = item.isEmpty() ? BigDecimal.ZERO
                    : income.divide(BigDecimal.valueOf(item.size()), 2, RoundingMode.HALF_UP);
            rows.add(ParkingReportOverviewRespVO.DurationRow.builder()
                    .bucket(bucket)
                    .count(item.size())
                    .rate(BigDecimal.valueOf(item.size()).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP).doubleValue())
                    .avgFee(avgFee)
                    .income(income)
                    .build());
        }
        return rows;
    }

    private ParkingReportOverviewRespVO.DurationSummary buildDurationSummary(List<ParkingRecordDO> records) {
        int shortCount = 0, midCount = 0, longCount = 0;
        int totalMinutes = 0;
        int validCount = 0;
        for (ParkingRecordDO r : records) {
            Integer duration = r.getParkingDuration();
            if (duration == null || duration < 0) {
                continue;
            }
            totalMinutes += duration;
            validCount++;
            if (duration <= 120) {
                shortCount++;
            } else if (duration <= 480) {
                midCount++;
            } else {
                longCount++;
            }
        }
        int avgMinutes = validCount == 0 ? 0 : totalMinutes / validCount;
        return ParkingReportOverviewRespVO.DurationSummary.builder()
                .shortCount(shortCount)
                .midCount(midCount)
                .longCount(longCount)
                .avgText(avgMinutes + "分钟")
                .build();
    }

    private List<ParkingReportOverviewRespVO.PeakRow> buildPeakRows(List<ParkingRecordDO> entryRecords,
                                                                    List<ParkingRecordDO> exitRecords) {
        List<ParkingReportOverviewRespVO.PeakRow> rows = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            final int h = hour;
            int inCount = (int) entryRecords.stream()
                    .map(ParkingRecordDO::getEntryTime)
                    .filter(Objects::nonNull)
                    .filter(t -> t.getHour() == h)
                    .count();
            int outCount = (int) exitRecords.stream()
                    .map(ParkingRecordDO::getExitTime)
                    .filter(Objects::nonNull)
                    .filter(t -> t.getHour() == h)
                    .count();
            int avgMinutes = avgMinutesByHour(exitRecords, h);
            rows.add(ParkingReportOverviewRespVO.PeakRow.builder()
                    .period(String.format("%02d:00-%02d:00", hour, (hour + 1) % 24))
                    .inCount(inCount)
                    .outCount(outCount)
                    .net(inCount - outCount)
                    .avgTime(avgMinutes + "分钟")
                    .congestion(toCongestion(inCount + outCount))
                    .build());
        }
        return rows;
    }

    private ParkingReportOverviewRespVO.PeakSummary buildPeakSummary(List<ParkingRecordDO> entryRecords,
                                                                     List<ParkingRecordDO> exitRecords) {
        Map<Integer, Integer> hourFlow = new java.util.HashMap<>();
        for (int i = 0; i < 24; i++) {
            hourFlow.put(i, 0);
        }
        entryRecords.stream().map(ParkingRecordDO::getEntryTime).filter(Objects::nonNull)
                .forEach(t -> hourFlow.computeIfPresent(t.getHour(), (k, v) -> v + 1));
        exitRecords.stream().map(ParkingRecordDO::getExitTime).filter(Objects::nonNull)
                .forEach(t -> hourFlow.computeIfPresent(t.getHour(), (k, v) -> v + 1));

        Comparator<Map.Entry<Integer, Integer>> cmp = Comparator.comparingInt(Map.Entry::getValue);
        int morning = hourFlow.entrySet().stream().filter(e -> e.getKey() >= 7 && e.getKey() <= 10).max(cmp)
                .map(Map.Entry::getKey).orElse(8);
        int evening = hourFlow.entrySet().stream().filter(e -> e.getKey() >= 17 && e.getKey() <= 20).max(cmp)
                .map(Map.Entry::getKey).orElse(18);
        int low = hourFlow.entrySet().stream().min(cmp).map(Map.Entry::getKey).orElse(2);
        return ParkingReportOverviewRespVO.PeakSummary.builder()
                .morning(toHourPeriod(morning))
                .evening(toHourPeriod(evening))
                .normal("10:00-17:00")
                .low(toHourPeriod(low))
                .build();
    }

    private List<ParkingReportOverviewRespVO.CarTypeRow> buildCarTypeRows(List<ParkingRecordDO> records) {
        List<String> categories = List.of("monthly", "temporary", "free");
        int totalCount = Math.max(records.size(), 1);
        BigDecimal totalIncome = records.stream().map(this::paidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<ParkingReportOverviewRespVO.CarTypeRow> rows = new ArrayList<>();
        for (String category : categories) {
            List<ParkingRecordDO> item = records.stream()
                    .filter(r -> category.equalsIgnoreCase(r.getVehicleCategory()))
                    .collect(Collectors.toList());
            int count = item.size();
            BigDecimal income = item.stream().map(this::paidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal monthlyAvg = count == 0 ? BigDecimal.ZERO
                    : income.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
            double rate = BigDecimal.valueOf(count).divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP).doubleValue();
            double incomeRate = totalIncome.compareTo(BigDecimal.ZERO) == 0 ? 0D
                    : income.divide(totalIncome, 4, RoundingMode.HALF_UP).doubleValue();
            rows.add(ParkingReportOverviewRespVO.CarTypeRow.builder()
                    .type(toCategoryLabel(category))
                    .count(count)
                    .rate(rate)
                    .monthlyAvgText("¥" + monthlyAvg)
                    .income(income)
                    .incomeRate(incomeRate)
                    .build());
        }
        return rows;
    }

    private ParkingReportOverviewRespVO.CarTypeSummary buildCarTypeSummary(List<ParkingRecordDO> records) {
        int fixed = (int) records.stream().filter(r -> "monthly".equalsIgnoreCase(r.getVehicleCategory())).count();
        int temp = (int) records.stream().filter(r -> "temporary".equalsIgnoreCase(r.getVehicleCategory())).count();
        int free = (int) records.stream().filter(r -> "free".equalsIgnoreCase(r.getVehicleCategory())).count();
        BigDecimal allIncome = records.stream().map(this::paidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal fixedIncome = records.stream()
                .filter(r -> "monthly".equalsIgnoreCase(r.getVehicleCategory()))
                .map(this::paidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        String fixedIncomeRate = allIncome.compareTo(BigDecimal.ZERO) == 0
                ? "0%"
                : fixedIncome.multiply(BigDecimal.valueOf(100)).divide(allIncome, 0, RoundingMode.HALF_UP) + "%";
        return ParkingReportOverviewRespVO.CarTypeSummary.builder()
                .fixed(fixed)
                .temp(temp)
                .free(free)
                .fixedIncomeRate(fixedIncomeRate)
                .build();
    }

    private List<ParkingReportOverviewRespVO.RevenueRow> buildRevenueRows(List<ParkingRecordDO> records, String granularity) {
        Map<String, BigDecimal> grouped = records.stream().collect(Collectors.groupingBy(
                r -> toPeriodLabel(r.getExitTime(), granularity),
                Collectors.mapping(this::paidAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
        ));
        List<String> periods = grouped.keySet().stream().sorted().collect(Collectors.toList());
        List<ParkingReportOverviewRespVO.RevenueRow> rows = new ArrayList<>();
        BigDecimal prev = BigDecimal.ZERO;
        for (String period : periods) {
            BigDecimal total = grouped.getOrDefault(period, BigDecimal.ZERO);
            String mom = toGrowthText(prev, total);
            rows.add(ParkingReportOverviewRespVO.RevenueRow.builder()
                    .period(period)
                    .total(total)
                    .momText(mom)
                    .yoyText("--")
                    .avgText("¥" + total.setScale(2, RoundingMode.HALF_UP))
                    .build());
            prev = total;
        }
        return rows;
    }

    private ParkingReportOverviewRespVO.RevenueSummary buildRevenueSummary(List<ParkingRecordDO> records,
                                                                           LocalDate start,
                                                                           LocalDate end) {
        BigDecimal totalIncome = records.stream().map(this::paidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        long dayCount = Math.max(1, ChronoUnit.DAYS.between(start, end) + 1L);
        BigDecimal avgIncome = totalIncome.divide(BigDecimal.valueOf(dayCount), 2, RoundingMode.HALF_UP);

        EnumMap<DayOfWeek, BigDecimal> daily = new EnumMap<>(DayOfWeek.class);
        for (ParkingRecordDO record : records) {
            LocalDateTime exit = record.getExitTime();
            if (exit == null) {
                continue;
            }
            daily.merge(exit.getDayOfWeek(), paidAmount(record), BigDecimal::add);
        }
        BigDecimal weekend = daily.getOrDefault(DayOfWeek.SATURDAY, BigDecimal.ZERO)
                .add(daily.getOrDefault(DayOfWeek.SUNDAY, BigDecimal.ZERO));
        BigDecimal workday = BigDecimal.ZERO;
        for (DayOfWeek d : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            workday = workday.add(daily.getOrDefault(d, BigDecimal.ZERO));
        }
        String weekendVsWorkday = workday.compareTo(BigDecimal.ZERO) == 0
                ? "1:1"
                : weekend.divide(workday, 2, RoundingMode.HALF_UP) + ":1";

        return ParkingReportOverviewRespVO.RevenueSummary.builder()
                .avgIncome("¥" + avgIncome)
                .weekendVsWorkday(weekendVsWorkday)
                .holidayGrowth("0%")
                .monthlyGrowth("0%")
                .build();
    }

    private String toDurationBucket(Integer minutes) {
        int m = minutes == null ? 0 : Math.max(minutes, 0);
        if (m < 60) return "0-1h";
        if (m < 120) return "1-2h";
        if (m < 240) return "2-4h";
        if (m < 480) return "4-8h";
        if (m < 720) return "8-12h";
        return ">12h";
    }

    private int avgMinutesByHour(List<ParkingRecordDO> records, int hour) {
        List<Integer> durations = records.stream()
                .filter(r -> r.getExitTime() != null && r.getExitTime().getHour() == hour)
                .map(ParkingRecordDO::getParkingDuration)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (durations.isEmpty()) {
            return 0;
        }
        return durations.stream().mapToInt(Integer::intValue).sum() / durations.size();
    }

    private String toCongestion(int flow) {
        if (flow >= 30) return "拥堵";
        if (flow >= 15) return "较忙";
        if (flow >= 5) return "通畅";
        return "畅通";
    }

    private String toHourPeriod(int hour) {
        return String.format("%02d:00-%02d:00", hour, (hour + 1) % 24);
    }

    private String toCategoryLabel(String category) {
        if ("monthly".equalsIgnoreCase(category)) return "固定车";
        if ("free".equalsIgnoreCase(category)) return "免费车";
        return "临时车";
    }

    private String toPeriodLabel(LocalDateTime time, String granularity) {
        if (time == null) {
            return "";
        }
        if ("month".equals(granularity)) {
            return String.format("%d-%02d", time.getYear(), time.getMonthValue());
        }
        if ("week".equals(granularity)) {
            int week = time.get(WeekFields.ISO.weekOfWeekBasedYear());
            return String.format("%d-W%02d", time.getYear(), week);
        }
        return time.toLocalDate().toString();
    }

    private String toGrowthText(BigDecimal previous, BigDecimal current) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? "+100%" : "0%";
        }
        BigDecimal growth = current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 0, RoundingMode.HALF_UP);
        String sign = growth.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        return sign + growth + "%";
    }

    private BigDecimal paidAmount(ParkingRecordDO record) {
        if (record == null || record.getPaidAmount() == null) {
            return BigDecimal.ZERO;
        }
        return record.getPaidAmount();
    }
}

