package cn.iocoder.yudao.module.iot.service.visitor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.abnormal.VisitorAbnormalEventPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.appointment.VisitorAppointmentPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.overview.VisitorDashboardRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.overview.VisitorOverviewStatsRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.VisitorAbnormalEventDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.VisitorAppointmentDO;
import cn.iocoder.yudao.module.iot.dal.mysql.visitor.VisitorAbnormalEventMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.visitor.VisitorAppointmentMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Validated
public class VisitorOverviewServiceImpl implements VisitorOverviewService {

    @Resource
    private VisitorAppointmentMapper visitorAppointmentMapper;

    @Resource
    private VisitorAbnormalEventMapper visitorAbnormalEventMapper;

    @Override
    public VisitorOverviewStatsRespVO getStats() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(today, LocalTime.MAX);

        LocalDateTime yesterdayStart = LocalDateTime.of(today.minusDays(1), LocalTime.MIN);
        LocalDateTime yesterdayEnd = LocalDateTime.of(today.minusDays(1), LocalTime.MAX);

        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDateTime monthStart = LocalDateTime.of(firstDayOfMonth, LocalTime.MIN);
        LocalDateTime monthEnd = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

        long currentVisitors = visitorAppointmentMapper.countCurrentVisiting(todayStart, todayEnd);
        long yesterdayVisitors = visitorAppointmentMapper.countCurrentVisiting(yesterdayStart, yesterdayEnd);
        int trend = 0;
        if (yesterdayVisitors > 0) {
            trend = (int) Math.round((currentVisitors - yesterdayVisitors) * 100.0 / yesterdayVisitors);
        } else if (currentVisitors > 0) {
            trend = 100;
        }

        long todayAppointments = visitorAppointmentMapper.countByStatusAndVisitTimeRange(null, todayStart, todayEnd);
        long pendingApproval = visitorAppointmentMapper.countByStatusAndVisitTimeRange("pending", todayStart.minusDays(7), todayEnd.plusDays(7));
        long abnormalCount = visitorAbnormalEventMapper.selectCount();
        long monthlyTotal = visitorAppointmentMapper.countMonthlyTotal(monthStart, monthEnd);
        long todayProcessedCount = visitorAppointmentMapper.countTodayProcessed(todayStart, todayEnd);

        VisitorOverviewStatsRespVO respVO = new VisitorOverviewStatsRespVO();
        respVO.setCurrentVisitors(currentVisitors);
        respVO.setVisitTrend(trend);
        respVO.setTodayAppointments(todayAppointments);
        respVO.setPendingConfirm(0L);
        respVO.setPendingApproval(pendingApproval);
        respVO.setAbnormalCount(abnormalCount);
        respVO.setMonthlyTotal(monthlyTotal);
        respVO.setTodayProcessedCount(todayProcessedCount);
        return respVO;
    }

    @Override
    public VisitorDashboardRespVO getDashboard(LocalDate dateFrom, LocalDate dateTo) {
        LocalDateTime start = LocalDateTime.of(dateFrom != null ? dateFrom : LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(dateTo != null ? dateTo : LocalDate.now(), LocalTime.MAX);

        List<VisitorAppointmentDO> appointments = visitorAppointmentMapper.selectList(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<VisitorAppointmentDO>()
                        .between(VisitorAppointmentDO::getVisitTime, start, end));
        List<VisitorAbnormalEventDO> abnormals = visitorAbnormalEventMapper.selectList(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<VisitorAbnormalEventDO>()
                        .between(VisitorAbnormalEventDO::getEventTime, start, end));

        VisitorDashboardRespVO vo = new VisitorDashboardRespVO();

        // 被访人排行：按 host 分组计数，取前10，算占比（dept 取该被访人第一条记录的 hostDept）
        Map<String, Long> hostCount = appointments.stream()
                .filter(a -> a.getHost() != null && !a.getHost().isBlank())
                .collect(Collectors.groupingBy(VisitorAppointmentDO::getHost, Collectors.counting()));
        Map<String, String> hostDeptMap = appointments.stream()
                .filter(a -> a.getHost() != null && !a.getHost().isBlank())
                .collect(Collectors.toMap(VisitorAppointmentDO::getHost, a -> a.getHostDept() != null ? a.getHostDept() : "", (a, b) -> a));
        long totalHost = hostCount.values().stream().mapToLong(Long::longValue).sum();
        List<VisitorDashboardRespVO.HostRankItem> hostRank = hostCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    VisitorDashboardRespVO.HostRankItem item = new VisitorDashboardRespVO.HostRankItem();
                    item.setName(e.getKey());
                    item.setDept(hostDeptMap.getOrDefault(e.getKey(), ""));
                    item.setCount(e.getValue());
                    item.setPercent(totalHost > 0 ? String.format("%.1f%%", e.getValue() * 100.0 / totalHost) : "0%");
                    return item;
                })
                .collect(Collectors.toList());
        vo.setHostRank(hostRank);

        // 来访事由分布
        Map<String, Long> reasonCount = appointments.stream()
                .filter(a -> a.getReason() != null && !a.getReason().isBlank())
                .collect(Collectors.groupingBy(VisitorAppointmentDO::getReason, Collectors.counting()));
        List<Map<String, Object>> reasonDist = reasonCount.entrySet().stream()
                .map(e -> Map.<String, Object>of("name", e.getKey(), "value", e.getValue().intValue()))
                .collect(Collectors.toList());
        vo.setReasonDistribution(reasonDist);

        // 访客趋势：近7天每日签到数
        LocalDate endDay = dateTo != null ? dateTo : LocalDate.now();
        List<Map<String, Object>> trendList = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = endDay.minusDays(i);
            LocalDateTime dayStart = LocalDateTime.of(d, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(d, LocalTime.MAX);
            long count = appointments.stream()
                    .filter(a -> a.getSignInTime() != null && !a.getSignInTime().isBefore(dayStart) && !a.getSignInTime().isAfter(dayEnd))
                    .count();
            trendList.add(Map.of("date", d.toString(), "count", (int) count));
        }
        vo.setTrend(trendList);

        // 异常事件分布
        Map<String, Long> abnormalCount = abnormals.stream()
                .filter(a -> a.getAbnormalType() != null)
                .collect(Collectors.groupingBy(VisitorAbnormalEventDO::getAbnormalType, Collectors.counting()));
        List<Map<String, Object>> abnormalDist = abnormalCount.entrySet().stream()
                .map(e -> Map.<String, Object>of("name", e.getKey(), "value", e.getValue().intValue()))
                .collect(Collectors.toList());
        vo.setAbnormalDistribution(abnormalDist);

        // 来访时段分布：按签到小时分段
        String[] slots = {"8:00前", "8点-10点", "10点-12点", "12点-14点", "14点-16点", "16点-18点", "18点后"};
        int[] slotCounts = new int[7];
        for (VisitorAppointmentDO a : appointments) {
            if (a.getSignInTime() == null) continue;
            int hour = a.getSignInTime().getHour();
            int idx = hour < 8 ? 0 : hour < 10 ? 1 : hour < 12 ? 2 : hour < 14 ? 3 : hour < 16 ? 4 : hour < 18 ? 5 : 6;
            slotCounts[idx]++;
        }
        List<Map<String, Object>> timeDist = new ArrayList<>();
        for (int i = 0; i < slots.length; i++) {
            timeDist.add(Map.of("name", slots[i], "value", slotCounts[i]));
        }
        vo.setTimeDistribution(timeDist);

        return vo;
    }

    @Override
    public PageResult<VisitorAppointmentDO> pageTodayVisiting(VisitorAppointmentPageReqVO reqVO) {
        // 复用分页 VO：按“今天 + 已签到未签离”过滤
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        reqVO.setVisitTime(new LocalDateTime[]{start, end});
        PageResult<VisitorAppointmentDO> page = visitorAppointmentMapper.selectPage(reqVO);
        page.setList(page.getList().stream()
                .filter(it -> it.getSignInTime() != null && it.getSignOutTime() == null)
                .toList());
        page.setTotal((long) page.getList().size());
        return page;
    }

    @Override
    public PageResult<VisitorAppointmentDO> pageHistory(VisitorAppointmentPageReqVO reqVO) {
        PageResult<VisitorAppointmentDO> page = visitorAppointmentMapper.selectPage(reqVO);
        page.setList(page.getList().stream()
                .filter(it -> it.getSignOutTime() != null)
                .toList());
        page.setTotal((long) page.getList().size());
        return page;
    }

    @Override
    public PageResult<VisitorAbnormalEventDO> pageAbnormal(VisitorAbnormalEventPageReqVO reqVO) {
        return visitorAbnormalEventMapper.selectPage(reqVO);
    }
}

