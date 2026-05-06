package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.controller.admin.access.vo.dashboard.AccessDashboardStatisticsRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.dashboard.RealTimeAccessRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessEventLogDO;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessEventLogMapper;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceRuntimeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 门禁管理 Dashboard Service 实现类
 *
 * <h3>M2-D 重写记录（2026-05-06）</h3>
 * 历史实现注入 {@code AccessRecordMapper}/{@code AccessAlarmMapper}（DO 标注 {@code @TableName("iot_access_record")}/{@code "iot_access_alarm"}），
 * 但这两张表在数据库中并不存在，运行时所有查询会抛 SQLException 并被 GlobalExceptionHandler 兜底为空响应，
 * 前端永远 fallback 到 mock 数据。详见 docs/ibms-bidirectional-gap.md GAP-001。
 *
 * 本次改为收口到真实表 {@code iot_access_event_log}（{@code IotAccessEventLogDO}/{@code IotAccessEventLogMapper}），
 * 56448 行真实数据，完整支撑 statistics + real-time + trend + device-status + heatmap + abnormal-events 六个端点。
 *
 * @author 智能化系统
 */
@Service
@Slf4j
public class AccessDashboardServiceImpl implements AccessDashboardService {

    /**
     * 视为"告警/异常"的事件类型集合（M2-D，按业务语义合并 ALARM_EVENT_TYPES + ABNORMAL_EVENT_TYPES）。
     * 与 {@link IotAccessEventLogMapper#ALARM_EVENT_TYPES}/{@code ABNORMAL_EVENT_TYPES} 保持一致。
     */
    private static final List<String> ALARM_AND_ABNORMAL_TYPES = Stream
            .concat(IotAccessEventLogMapper.ALARM_EVENT_TYPES.stream(),
                    IotAccessEventLogMapper.ABNORMAL_EVENT_TYPES.stream())
            .distinct()
            .toList();

    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    @Resource
    private IotAccessEventLogMapper eventLogMapper;

    @Resource
    private IbmsDeviceMapper ibmsDeviceMapper;
    @Resource
    private IbmsDeviceRuntimeMapper ibmsDeviceRuntimeMapper;

    @Override
    public AccessDashboardStatisticsRespVO getStatistics() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.atTime(LocalTime.MAX);
        LocalDateTime startOfYesterday = today.minusDays(1).atStartOfDay();
        LocalDateTime endOfYesterday = today.minusDays(1).atTime(LocalTime.MAX);

        // 今日 / 昨日 通行总次数（用于增长率计算）
        Long todayAccessCount = nullToZero(eventLogMapper.countByTimeRange(startOfToday, endOfToday));
        Long yesterdayAccessCount = nullToZero(eventLogMapper.countByTimeRange(startOfYesterday, endOfYesterday));
        // 今日告警/异常数
        Long todayAlarmCount = nullToZero(
                eventLogMapper.countAlarmsByTimeRange(startOfToday, endOfToday, ALARM_AND_ABNORMAL_TYPES));

        // 设备统计：ibms_device 中 systemCode 属于门禁类（AC/IC）
        List<IbmsDeviceDO> devices = ibmsDeviceMapper.selectListAccessLikeDevices();
        Long totalDeviceCount = (long) devices.size();
        Map<Long, Integer> stateMap = ibmsDeviceRuntimeMapper.selectStateMapByDeviceIds(
                devices.stream().map(IbmsDeviceDO::getId).toList());
        Long onlineDeviceCount = devices.stream()
                .filter(d -> IotDeviceStateEnum.isOnline(
                        stateMap.getOrDefault(d.getId(), IotDeviceStateEnum.INACTIVE.getState())))
                .count();

        // 通行类型分布：基于 event_type，落到 employee/visitor/vehicle/elevator 4 个槽位
        // - employee = NORMAL_EVENT_TYPES（正常通行：刷卡/人脸/指纹/密码/二维码/远程/按钮）
        // - visitor = ABNORMAL_EVENT_TYPES（验证失败/无权限/卡过期 等异常通行尝试）
        // - vehicle = 0（车辆数据见 access_parking_* 系列表，本端点不聚合）
        // - elevator = 0（梯控暂未独立分类）
        List<Map<String, Object>> typeDistRows =
                eventLogMapper.selectEventTypeDistribution(startOfToday, endOfToday);
        long employeeCount = sumByEventTypeIn(typeDistRows, IotAccessEventLogMapper.NORMAL_EVENT_TYPES);
        long visitorCount = sumByEventTypeIn(typeDistRows, IotAccessEventLogMapper.ABNORMAL_EVENT_TYPES);

        AccessDashboardStatisticsRespVO respVO = new AccessDashboardStatisticsRespVO();
        respVO.setTodayAccessCount(todayAccessCount);
        respVO.setTodayVisitorCount(visitorCount);
        respVO.setTodayVehicleCount(0L); // TODO M2-E: 接入 access_parking_record
        respVO.setTodayAlarmCount(todayAlarmCount);
        respVO.setOnlineDeviceCount(onlineDeviceCount);
        respVO.setTotalDeviceCount(totalDeviceCount);
        respVO.setCurrentVisitorCount(0L); // TODO M2-E: 实时在场访客
        respVO.setOccupiedParkingSpaces(0L); // TODO M2-E
        respVO.setTotalParkingSpaces(0L);    // TODO M2-E
        respVO.setAccessCountGrowth(percentGrowth(todayAccessCount, yesterdayAccessCount));
        respVO.setVisitorCountGrowth(0.0);
        respVO.setVehicleCountGrowth(0.0);

        AccessDashboardStatisticsRespVO.DeviceStatusDistribution deviceStatus =
                new AccessDashboardStatisticsRespVO.DeviceStatusDistribution();
        deviceStatus.setOnline(onlineDeviceCount);
        deviceStatus.setOffline(Math.max(0L, totalDeviceCount - onlineDeviceCount));
        deviceStatus.setMaintenance(0L);
        deviceStatus.setFault(0L);
        respVO.setDeviceStatusDistribution(deviceStatus);

        AccessDashboardStatisticsRespVO.AccessTypeDistribution accessType =
                new AccessDashboardStatisticsRespVO.AccessTypeDistribution();
        accessType.setEmployee(employeeCount);
        accessType.setVisitor(visitorCount);
        accessType.setVehicle(0L);
        accessType.setElevator(0L);
        respVO.setAccessTypeDistribution(accessType);

        return respVO;
    }

    @Override
    public RealTimeAccessRespVO getRealTimeAccess(Integer pageSize) {
        int limit = pageSize == null || pageSize <= 0 ? 10 : pageSize;
        List<IotAccessEventLogDO> records = eventLogMapper.selectRecentList(limit);

        RealTimeAccessRespVO respVO = new RealTimeAccessRespVO();
        respVO.setRecords(convertList(records, ev -> {
            RealTimeAccessRespVO.RealTimeAccessItem item = new RealTimeAccessRespVO.RealTimeAccessItem();
            item.setId(ev.getId());
            item.setType(IotAccessEventLogMapper.ALARM_EVENT_TYPES.contains(ev.getEventType())
                    || IotAccessEventLogMapper.ABNORMAL_EVENT_TYPES.contains(ev.getEventType())
                    ? "alarm" : "access");
            item.setUserName(ev.getPersonName());
            item.setDeviceName(ev.getDeviceName());
            item.setLocation(ev.getChannelName());
            item.setTime(ev.getEventTime());
            // verify_result 大量 NULL：用 success 字段或 ABNORMAL 类型兜底判定
            String result;
            if (ev.getVerifyResult() != null) {
                result = ev.getVerifyResult() == 1 ? "success" : "failed";
            } else if (Boolean.TRUE.equals(ev.getSuccess())) {
                result = "success";
            } else if (IotAccessEventLogMapper.ABNORMAL_EVENT_TYPES.contains(ev.getEventType())) {
                result = "failed";
            } else {
                result = "success";
            }
            item.setResult(result);
            item.setPhoto(ev.getSnapshotUrl() != null ? ev.getSnapshotUrl() : ev.getCaptureUrl());
            return item;
        }));
        return respVO;
    }

    /**
     * 通行趋势 - M2-D 实现
     *
     * 输入 startTime/endTime（ISO_LOCAL_DATE_TIME 或 ISO_LOCAL_DATE 都接受）。
     * 同日返回 24 小时槽位（hour 0~23），跨日返回逐日槽位。
     *
     * 返回结构：
     *   {
     *     granularity: "hour" | "day",
     *     labels: ["0:00", "1:00", ...] | ["2026-04-15", ...],
     *     accessData: [...],   // 每槽位通行总次数
     *     rejectData: [...],   // 每槽位告警/异常次数
     *     inData:     [...],   // 进门 (direction=1) 次数；当数据库 direction 全空时回退到 accessData
     *     outData:    [...]    // 出门 (direction=2)
     *   }
     */
    @Override
    public Object getAccessTrend(String startTime, String endTime, String type) {
        LocalDateTime start = parseDateTimeFlexible(startTime, true);
        LocalDateTime end = parseDateTimeFlexible(endTime, false);
        if (start == null || end == null || end.isBefore(start)) {
            log.warn("[getAccessTrend] 参数解析失败 startTime={}, endTime={}", startTime, endTime);
            return Map.of("granularity", "day", "labels", List.of(),
                    "accessData", List.of(), "rejectData", List.of(),
                    "inData", List.of(), "outData", List.of());
        }
        boolean sameDay = start.toLocalDate().equals(end.toLocalDate());
        Map<String, Object> resp = new HashMap<>();
        if (sameDay) {
            List<Map<String, Object>> rows = eventLogMapper.selectHourlyTrend(start, end, ALARM_AND_ABNORMAL_TYPES);
            Map<Integer, Map<String, Object>> byHour = rows.stream().collect(
                    Collectors.toMap(r -> ((Number) r.get("hour")).intValue(), r -> r, (a, b) -> a));
            List<String> labels = new ArrayList<>(24);
            List<Long> access = new ArrayList<>(24);
            List<Long> alarm = new ArrayList<>(24);
            List<Long> in = new ArrayList<>(24);
            List<Long> out = new ArrayList<>(24);
            for (int h = 0; h < 24; h++) {
                labels.add(h + ":00");
                Map<String, Object> r = byHour.get(h);
                long ac = r == null ? 0L : ((Number) r.get("accessCount")).longValue();
                long al = r == null ? 0L : ((Number) r.getOrDefault("alarmCount", 0)).longValue();
                long ic = r == null ? 0L : ((Number) r.getOrDefault("inCount", 0)).longValue();
                long oc = r == null ? 0L : ((Number) r.getOrDefault("outCount", 0)).longValue();
                access.add(ac);
                alarm.add(al);
                // direction 大量 NULL：当 in+out=0 时用 access 兜底
                in.add(ic + oc == 0 ? ac : ic);
                out.add(oc);
            }
            resp.put("granularity", "hour");
            resp.put("labels", labels);
            resp.put("accessData", access);
            resp.put("rejectData", alarm);
            resp.put("inData", in);
            resp.put("outData", out);
        } else {
            List<Map<String, Object>> rows = eventLogMapper.selectDailyTrend(start, end, ALARM_AND_ABNORMAL_TYPES);
            // 行键可能是 java.sql.Date / LocalDate / String，统一转 ISO_DATE
            Map<String, Map<String, Object>> byDate = new HashMap<>();
            for (Map<String, Object> r : rows) {
                Object dateObj = r.get("date");
                String key = dateObj instanceof java.sql.Date d ? d.toLocalDate().format(ISO_DATE)
                        : dateObj instanceof LocalDate ld ? ld.format(ISO_DATE)
                        : String.valueOf(dateObj);
                byDate.put(key, r);
            }
            List<String> labels = new ArrayList<>();
            List<Long> access = new ArrayList<>();
            List<Long> alarm = new ArrayList<>();
            List<Long> in = new ArrayList<>();
            List<Long> out = new ArrayList<>();
            LocalDate cur = start.toLocalDate();
            LocalDate stop = end.toLocalDate();
            while (!cur.isAfter(stop)) {
                String key = cur.format(ISO_DATE);
                labels.add(key);
                Map<String, Object> r = byDate.get(key);
                long ac = r == null ? 0L : ((Number) r.get("accessCount")).longValue();
                long al = r == null ? 0L : ((Number) r.getOrDefault("alarmCount", 0)).longValue();
                long ic = r == null ? 0L : ((Number) r.getOrDefault("inCount", 0)).longValue();
                long oc = r == null ? 0L : ((Number) r.getOrDefault("outCount", 0)).longValue();
                access.add(ac);
                alarm.add(al);
                in.add(ic + oc == 0 ? ac : ic);
                out.add(oc);
                cur = cur.plusDays(1);
            }
            resp.put("granularity", "day");
            resp.put("labels", labels);
            resp.put("accessData", access);
            resp.put("rejectData", alarm);
            resp.put("inData", in);
            resp.put("outData", out);
        }
        return resp;
    }

    /**
     * 设备状态概览 - M2-D 实现
     * 复用 getStatistics 的设备聚合逻辑，独立成端点便于前端按需轮询。
     */
    @Override
    public Object getDeviceStatusOverview() {
        List<IbmsDeviceDO> devices = ibmsDeviceMapper.selectListAccessLikeDevices();
        long total = devices.size();
        Map<Long, Integer> stateMap = ibmsDeviceRuntimeMapper.selectStateMapByDeviceIds(
                devices.stream().map(IbmsDeviceDO::getId).toList());
        long online = devices.stream()
                .filter(d -> IotDeviceStateEnum.isOnline(
                        stateMap.getOrDefault(d.getId(), IotDeviceStateEnum.INACTIVE.getState())))
                .count();
        // 按 systemCode 分类（AC/IC）
        Map<String, Long> bySystemCode = devices.stream()
                .collect(Collectors.groupingBy(d -> nullToEmpty(d.getSystemCode()), Collectors.counting()));
        Map<String, Object> resp = new HashMap<>();
        resp.put("total", total);
        resp.put("online", online);
        resp.put("offline", Math.max(0L, total - online));
        resp.put("maintenance", 0L);
        resp.put("fault", 0L);
        resp.put("bySystemCode", bySystemCode);
        return resp;
    }

    /**
     * 热力图 - M2-D 实现
     * 返回 [{deviceId, deviceName, hour, count}]，前端可直接渲染。
     */
    @Override
    public Object getAccessHeatmap(String date, String type) {
        LocalDate target;
        try {
            target = date == null || date.isBlank() ? LocalDate.now() : LocalDate.parse(date, ISO_DATE);
        } catch (Exception e) {
            log.warn("[getAccessHeatmap] date 参数解析失败 date={}", date);
            target = LocalDate.now();
        }
        return eventLogMapper.selectHeatmap(target);
    }

    /**
     * 异常事件列表 - M2-D 实现
     * level 暂未细分，按 ALARM+ABNORMAL 类型联合返回最新 N 条。
     */
    @Override
    public Object getAbnormalEventList(Integer pageSize, String level) {
        int limit = pageSize == null || pageSize <= 0 ? 10 : pageSize;
        List<IotAccessEventLogDO> rows = eventLogMapper.selectAbnormalList(ALARM_AND_ABNORMAL_TYPES, limit);
        return rows.stream().map(ev -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", ev.getId());
            m.put("deviceId", ev.getDeviceId());
            m.put("deviceName", ev.getDeviceName());
            m.put("channelName", ev.getChannelName());
            m.put("eventType", ev.getEventType());
            m.put("eventDesc", ev.getEventDesc());
            m.put("eventTime", ev.getEventTime());
            m.put("failReason", ev.getFailReason());
            m.put("personName", ev.getPersonName());
            // level 推断：ALARM_TYPES → "high"（强制开门、防拆等），ABNORMAL → "medium"
            String lvl = IotAccessEventLogMapper.ALARM_EVENT_TYPES.contains(ev.getEventType()) ? "high" : "medium";
            m.put("level", lvl);
            return m;
        }).collect(Collectors.toList());
    }

    // ===== helpers =====

    private static long nullToZero(Long v) {
        return v == null ? 0L : v;
    }

    private static String nullToEmpty(String v) {
        return v == null ? "" : v;
    }

    private static double percentGrowth(long today, long yesterday) {
        if (yesterday == 0L) return today == 0L ? 0.0 : 100.0;
        return Math.round((today - yesterday) * 1000.0 / yesterday) / 10.0;
    }

    private static long sumByEventTypeIn(List<Map<String, Object>> rows, List<String> types) {
        long s = 0L;
        for (Map<String, Object> r : rows) {
            if (types.contains(String.valueOf(r.get("eventType")))) {
                s += ((Number) r.getOrDefault("cnt", 0)).longValue();
            }
        }
        return s;
    }

    /**
     * 接受 ISO 日期或日期时间；toStart=true 时日期补 00:00:00，否则补 23:59:59。
     */
    private static LocalDateTime parseDateTimeFlexible(String s, boolean toStart) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDateTime.parse(s, ISO_DATE_TIME);
        } catch (Exception ignore) {
            // try as date
        }
        try {
            LocalDate d = LocalDate.parse(s, ISO_DATE);
            return toStart ? d.atStartOfDay() : d.atTime(LocalTime.MAX);
        } catch (Exception ignore) {
            return null;
        }
    }

}

