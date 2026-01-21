package cn.iocoder.yudao.module.iot.service.dashboard;

import cn.iocoder.yudao.module.iot.controller.admin.dashboard.vo.AlertStatisticsRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.dashboard.vo.DeviceStatisticsRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.dashboard.vo.RealTimeMonitorRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alert.IotAlertRecordDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceEventLogDO;
import cn.iocoder.yudao.module.iot.dal.mysql.alert.IotAlertRecordMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceEventLogMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * IoT 数据大屏 Service 实现类
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Validated
@Slf4j
public class IotDashboardServiceImpl implements IotDashboardService {

    @Resource
    private IotDeviceMapper deviceMapper;
    @Resource
    private IotAlertRecordMapper alertRecordMapper;
    @Resource
    private IotDeviceEventLogMapper deviceEventLogMapper;

    @Override
    public DeviceStatisticsRespVO getDeviceStatistics() {
        DeviceStatisticsRespVO vo = new DeviceStatisticsRespVO();

        // 查询所有设备
        List<IotDeviceDO> allDevices = deviceMapper.selectList();
        vo.setTotalDevices((long) allDevices.size());

        // 统计各状态设备数
        Map<Integer, Long> statusCountMap = allDevices.stream()
                .filter(device -> device.getState() != null)
                .collect(Collectors.groupingBy(IotDeviceDO::getState, Collectors.counting()));

        long onlineDevices = statusCountMap.getOrDefault(1, 0L); // ONLINE = 1
        long offlineDevices = statusCountMap.getOrDefault(2, 0L); // OFFLINE = 2
        long inactiveDevices = statusCountMap.getOrDefault(0, 0L); // INACTIVE = 0

        vo.setOnlineDevices(onlineDevices);
        vo.setOfflineDevices(offlineDevices);
        vo.setFaultDevices(0L); // 暂不支持故障状态

        // 计算在线率
        if (vo.getTotalDevices() > 0) {
            vo.setOnlineRate(onlineDevices * 100.0 / vo.getTotalDevices());
        } else {
            vo.setOnlineRate(0.0);
        }

        // 各状态设备数量（用于前端展示）
        Map<String, Long> devicesByStatus = new LinkedHashMap<>();
        devicesByStatus.put("在线", onlineDevices);
        devicesByStatus.put("离线", offlineDevices);
        devicesByStatus.put("未激活", inactiveDevices);
        vo.setDevicesByStatus(devicesByStatus);

        // 各产品设备数量分布
        Map<String, Long> devicesByProduct = allDevices.stream()
                .collect(Collectors.groupingBy(
                        device -> device.getProductId() != null ? device.getProductId().toString() : "未知",
                        Collectors.counting()
                ));
        vo.setDevicesByProduct(devicesByProduct);

        // 今日新增设备
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        long todayNewDevices = allDevices.stream()
                .filter(device -> device.getCreateTime() != null && device.getCreateTime().isAfter(todayStart))
                .count();
        vo.setTodayNewDevices(todayNewDevices);

        // 本周新增设备
        LocalDateTime weekStart = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.MIN);
        long weekNewDevices = allDevices.stream()
                .filter(device -> device.getCreateTime() != null && device.getCreateTime().isAfter(weekStart))
                .count();
        vo.setWeekNewDevices(weekNewDevices);

        return vo;
    }

    @Override
    public AlertStatisticsRespVO getAlertStatistics() {
        AlertStatisticsRespVO vo = new AlertStatisticsRespVO();

        // 查询所有告警记录
        List<IotAlertRecordDO> allAlerts = alertRecordMapper.selectList();
        vo.setTotalAlerts((long) allAlerts.size());

        // 今日告警数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        long todayAlerts = allAlerts.stream()
                .filter(alert -> alert.getCreateTime() != null && alert.getCreateTime().isAfter(todayStart))
                .count();
        vo.setTodayAlerts(todayAlerts);

        // 本周告警数
        LocalDateTime weekStart = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.MIN);
        long weekAlerts = allAlerts.stream()
                .filter(alert -> alert.getCreateTime() != null && alert.getCreateTime().isAfter(weekStart))
                .count();
        vo.setWeekAlerts(weekAlerts);

        // 本月告警数
        LocalDateTime monthStart = LocalDateTime.of(LocalDate.now().minusDays(30), LocalTime.MIN);
        long monthAlerts = allAlerts.stream()
                .filter(alert -> alert.getCreateTime() != null && alert.getCreateTime().isAfter(monthStart))
                .count();
        vo.setMonthAlerts(monthAlerts);

        // 未处理和已处理告警数
        long unhandledAlerts = allAlerts.stream()
                .filter(alert -> alert.getProcessStatus() != null && !alert.getProcessStatus())
                .count();
        long handledAlerts = allAlerts.stream()
                .filter(alert -> alert.getProcessStatus() != null && alert.getProcessStatus())
                .count();

        vo.setUnhandledAlerts(unhandledAlerts);
        vo.setHandledAlerts(handledAlerts);

        // 告警处理率
        if (vo.getTotalAlerts() > 0) {
            vo.setHandledRate(handledAlerts * 100.0 / vo.getTotalAlerts());
        } else {
            vo.setHandledRate(0.0);
        }

        // 各级别告警数量
        Map<String, Long> alertsByLevel = allAlerts.stream()
                .collect(Collectors.groupingBy(
                        alert -> alert.getConfigLevel() != null ? alert.getConfigLevel().toString() : "未知",
                        Collectors.counting()
                ));
        vo.setAlertsByLevel(alertsByLevel);

        // 各类型告警数量（按配置名称统计）
        Map<String, Long> alertsByType = allAlerts.stream()
                .collect(Collectors.groupingBy(
                        alert -> alert.getConfigName() != null ? alert.getConfigName() : "未知",
                        Collectors.counting()
                ));
        vo.setAlertsByType(alertsByType);

        // 告警趋势（最近7天）
        List<AlertStatisticsRespVO.TrendData> trendDataList = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);

            long count = allAlerts.stream()
                    .filter(alert -> alert.getCreateTime() != null
                            && alert.getCreateTime().isAfter(dayStart)
                            && alert.getCreateTime().isBefore(dayEnd))
                    .count();

            AlertStatisticsRespVO.TrendData trendData = new AlertStatisticsRespVO.TrendData();
            trendData.setDate(date.toString());
            trendData.setCount(count);
            trendDataList.add(trendData);
        }
        vo.setAlertTrend(trendDataList);

        return vo;
    }

    @Override
    public RealTimeMonitorRespVO getRealTimeMonitor() {
        RealTimeMonitorRespVO vo = new RealTimeMonitorRespVO();

        // 最新告警列表（最近10条）
        List<IotAlertRecordDO> latestAlertDOs = alertRecordMapper.selectList();
        latestAlertDOs.sort(Comparator.comparing(IotAlertRecordDO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        
        List<RealTimeMonitorRespVO.LatestAlert> latestAlerts = latestAlertDOs.stream()
                .limit(10)
                .map(alert -> {
                    RealTimeMonitorRespVO.LatestAlert latestAlert = new RealTimeMonitorRespVO.LatestAlert();
                    latestAlert.setId(alert.getId());
                    latestAlert.setAlertName(alert.getConfigName());
                    // 从设备ID获取设备名称（这里简化处理，实际应该查询设备表）
                    latestAlert.setDeviceName(alert.getDeviceId() != null ? "设备-" + alert.getDeviceId() : "未知设备");
                    latestAlert.setLevel(alert.getConfigLevel() != null ? alert.getConfigLevel().toString() : "未知");
                    latestAlert.setAlertTime(alert.getCreateTime());
                    latestAlert.setStatus(alert.getProcessStatus() != null && alert.getProcessStatus() ? 1 : 0);
                    return latestAlert;
                })
                .collect(Collectors.toList());
        vo.setLatestAlerts(latestAlerts);

        // 设备状态变化列表：待对接状态变更日志（未实现时返回空列表，禁止伪造数据）
        List<RealTimeMonitorRespVO.DeviceStatusChange> deviceStatusChanges = new ArrayList<>();
        // TODO: 实现设备状态变化查询
        vo.setDeviceStatusChanges(deviceStatusChanges);

        // 最新事件列表（最近10条）
        List<IotDeviceEventLogDO> latestEventDOs = deviceEventLogMapper.selectList();
        latestEventDOs.sort(Comparator.comparing(IotDeviceEventLogDO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        
        List<RealTimeMonitorRespVO.LatestEvent> latestEvents = latestEventDOs.stream()
                .limit(10)
                .map(event -> {
                    RealTimeMonitorRespVO.LatestEvent latestEvent = new RealTimeMonitorRespVO.LatestEvent();
                    latestEvent.setId(event.getId());
                    latestEvent.setEventType(event.getEventType());
                    latestEvent.setDeviceName(event.getDeviceName());
                    latestEvent.setEventTime(event.getCreateTime());
                    latestEvent.setEventData(event.getEventData());
                    return latestEvent;
                })
                .collect(Collectors.toList());
        vo.setLatestEvents(latestEvents);

        // 系统负载信息
        RealTimeMonitorRespVO.SystemLoad systemLoad = new RealTimeMonitorRespVO.SystemLoad();
        
        // CPU使用率
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double cpuUsage = osBean.getSystemLoadAverage();
        systemLoad.setCpuUsage(cpuUsage > 0 ? cpuUsage * 10 : 0.0); // 转换为百分比

        // 内存使用率
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
        double memoryUsage = maxMemory > 0 ? (usedMemory * 100.0 / maxMemory) : 0.0;
        systemLoad.setMemoryUsage(memoryUsage);

        // 磁盘使用率（取当前应用所在磁盘分区的真实数据）
        try {
            Path rootPath = Paths.get(System.getProperty("user.dir")).getRoot();
            java.io.File root = (rootPath != null ? rootPath.toFile() : new java.io.File("/"));
            long total = root.getTotalSpace();
            long free = root.getUsableSpace();
            if (total > 0) {
                systemLoad.setDiskUsage((1.0 - (free * 1.0 / total)) * 100.0);
            }
        } catch (Exception ignored) {
            systemLoad.setDiskUsage(null);
        }

        // 消息队列积压数 / 数据库连接数：未接入真实监控指标时不返回伪造值
        systemLoad.setMessageQueueBacklog(null);
        systemLoad.setDatabaseConnections(null);

        vo.setSystemLoad(systemLoad);

        return vo;
    }
}

