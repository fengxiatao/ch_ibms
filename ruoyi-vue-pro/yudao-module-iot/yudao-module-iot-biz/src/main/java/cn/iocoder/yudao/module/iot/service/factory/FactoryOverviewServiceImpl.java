package cn.iocoder.yudao.module.iot.service.factory;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.iot.controller.admin.factory.vo.FactoryOverviewRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alert.IotAlertRecordDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsEnergyStatisticsDailyDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsEnvDataRecordDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsEnvSensorDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsChannelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsSpaceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.alert.IotAlertRecordMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.building.IbmsEnergyStatisticsDailyMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.building.IbmsEnvDataRecordMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.building.IbmsEnvSensorMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsChannelMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceRuntimeMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsSpaceMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Validated
public class FactoryOverviewServiceImpl implements FactoryOverviewService {

    private static final int DEVICE_STATE_INACTIVE = 0;
    private static final int DEVICE_STATE_ONLINE = 1;
    private static final int DEVICE_STATE_OFFLINE = 2;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final Set<String> ONLINE_CHANNEL_STATUS = Set.of("online", "armed", "warning");

    @Resource
    private IbmsDeviceMapper ibmsDeviceMapper;
    @Resource
    private IbmsDeviceRuntimeMapper ibmsDeviceRuntimeMapper;
    @Resource
    private IotAlertRecordMapper iotAlertRecordMapper;
    @Resource
    private IbmsSpaceMapper ibmsSpaceMapper;
    @Resource
    private IbmsChannelMapper ibmsChannelMapper;
    @Resource
    private IbmsEnergyStatisticsDailyMapper ibmsEnergyStatisticsDailyMapper;
    @Resource
    private IbmsEnvSensorMapper ibmsEnvSensorMapper;
    @Resource
    private IbmsEnvDataRecordMapper ibmsEnvDataRecordMapper;

    @Override
    public FactoryOverviewRespVO getOverview() {
        Long tenantId = TenantContextHolder.getTenantId();
        List<IbmsDeviceDO> devices = ibmsDeviceMapper.selectList();
        Map<Long, IbmsDeviceDO> deviceMap = devices.stream()
                .collect(Collectors.toMap(IbmsDeviceDO::getId, Function.identity(), (left, right) -> left));
        Map<Long, Integer> stateMap = ibmsDeviceRuntimeMapper.selectStateMapByDeviceIds(
                devices.stream().map(IbmsDeviceDO::getId).toList());
        List<FactoryOverviewRespVO.FloorItem> floors = getFloors();
        List<IotAlertRecordDO> alerts = iotAlertRecordMapper.selectList(
                new LambdaQueryWrapperX<IotAlertRecordDO>().orderByDesc(IotAlertRecordDO::getCreateTime));
        List<IbmsEnergyStatisticsDailyDO> energyRecords = ibmsEnergyStatisticsDailyMapper.selectList(
                new LambdaQueryWrapperX<IbmsEnergyStatisticsDailyDO>()
                        .eqIfPresent(IbmsEnergyStatisticsDailyDO::getTenantId, tenantId)
                        .orderByDesc(IbmsEnergyStatisticsDailyDO::getStatDate)
                        .orderByDesc(IbmsEnergyStatisticsDailyDO::getId));
        List<IbmsEnvSensorDO> sensors = ibmsEnvSensorMapper.selectList();
        Map<Long, IbmsEnvDataRecordDO> latestEnvRecordMap = getLatestEnvRecordMap(sensors);
        List<IbmsChannelDO> channels = ibmsChannelMapper.selectList(
                new LambdaQueryWrapperX<IbmsChannelDO>()
                        .and(wrapper -> wrapper.eq(IbmsChannelDO::getBusiness, "sa")
                                .or()
                                .eq(IbmsChannelDO::getSystemType, "VI"))
                        .orderByDesc(IbmsChannelDO::getId));

        return FactoryOverviewRespVO.builder()
                .kpis(buildKpis(devices, stateMap, alerts, sensors, latestEnvRecordMap, energyRecords))
                .floors(floors)
                .deviceStatusList(buildDeviceStatusList(devices, stateMap, floors))
                .latestAlerts(buildLatestAlerts(alerts, deviceMap, floors))
                .scene(buildScene(floors))
                .videoSnapshot(buildVideoSnapshot(channels, floors))
                .energyTrend(buildEnergyTrend(energyRecords))
                .environmentSnapshot(buildEnvironmentSnapshot(sensors, latestEnvRecordMap))
                .build();
    }

    private List<FactoryOverviewRespVO.FloorItem> getFloors() {
        return ibmsSpaceMapper.selectList(new LambdaQueryWrapperX<IbmsSpaceDO>()
                        .eq(IbmsSpaceDO::getType, "floor")
                        .orderByAsc(IbmsSpaceDO::getSort)
                        .orderByAsc(IbmsSpaceDO::getId))
                .stream()
                .map(space -> FactoryOverviewRespVO.FloorItem.builder()
                        .id(space.getId())
                        .code(space.getCode())
                        .name(space.getName())
                        .sort(space.getSort())
                        .build())
                .toList();
    }

    private Map<Long, IbmsEnvDataRecordDO> getLatestEnvRecordMap(List<IbmsEnvSensorDO> sensors) {
        Map<Long, IbmsEnvDataRecordDO> recordMap = new LinkedHashMap<>();
        for (IbmsEnvSensorDO sensor : sensors) {
            IbmsEnvDataRecordDO latestRecord = ibmsEnvDataRecordMapper.selectLatestBySensorId(sensor.getId());
            if (latestRecord != null) {
                recordMap.put(sensor.getId(), latestRecord);
            }
        }
        return recordMap;
    }

    private FactoryOverviewRespVO.Kpis buildKpis(List<IbmsDeviceDO> devices, Map<Long, Integer> stateMap,
                                                 List<IotAlertRecordDO> alerts, List<IbmsEnvSensorDO> sensors,
                                                 Map<Long, IbmsEnvDataRecordDO> latestEnvRecordMap,
                                                 List<IbmsEnergyStatisticsDailyDO> energyRecords) {
        long totalDevices = devices.size();
        long onlineDevices = countDeviceState(stateMap.values(), DEVICE_STATE_ONLINE);
        long offlineDevices = countDeviceState(stateMap.values(), DEVICE_STATE_OFFLINE);
        long inactiveDevices = Math.max(totalDevices - onlineDevices - offlineDevices, 0);
        long totalAlerts = alerts.size();
        long unhandledAlerts = alerts.stream().filter(alert -> !Boolean.TRUE.equals(alert.getProcessStatus())).count();
        long handledAlerts = totalAlerts - unhandledAlerts;
        long qualifiedSensors = latestEnvRecordMap.values().stream().filter(this::isEnvironmentQualified).count();
        long totalSensorRecords = latestEnvRecordMap.size();
        LocalDate latestEnergyDate = energyRecords.stream()
                .map(IbmsEnergyStatisticsDailyDO::getStatDate)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);
        LocalDate previousEnergyDate = energyRecords.stream()
                .map(IbmsEnergyStatisticsDailyDO::getStatDate)
                .filter(Objects::nonNull)
                .filter(date -> latestEnergyDate != null && date.isBefore(latestEnergyDate))
                .max(LocalDate::compareTo)
                .orElse(null);
        Map<Integer, BigDecimal> latestEnergy = sumEnergyByType(energyRecords, latestEnergyDate);
        Map<Integer, BigDecimal> previousEnergy = sumEnergyByType(energyRecords, previousEnergyDate);

        return FactoryOverviewRespVO.Kpis.builder()
                .deviceOnlineRate(FactoryOverviewRespVO.MetricItem.builder()
                        .value(rate(onlineDevices, totalDevices))
                        .unit("%")
                        .trend(null)
                        .total(totalDevices)
                        .online(onlineDevices)
                        .offline(offlineDevices)
                        .inactive(inactiveDevices)
                        .build())
                .alarmCount(FactoryOverviewRespVO.MetricItem.builder()
                        .value(BigDecimal.valueOf(totalAlerts))
                        .unit("条")
                        .trend(null)
                        .total(totalAlerts)
                        .unhandled(unhandledAlerts)
                        .handled(handledAlerts)
                        .build())
                .environmentComplianceRate(FactoryOverviewRespVO.MetricItem.builder()
                        .value(rate(qualifiedSensors, totalSensorRecords))
                        .unit("%")
                        .trend(null)
                        .qualified(qualifiedSensors)
                        .total(totalSensorRecords)
                        .build())
                .todayEnergy(FactoryOverviewRespVO.MetricItem.builder()
                        .value(latestEnergy.getOrDefault(1, BigDecimal.ZERO).setScale(1, RoundingMode.HALF_UP))
                        .unit("kWh")
                        .trend(calculateTrend(latestEnergy.get(1), previousEnergy.get(1)))
                        .electricity(scale(latestEnergy.get(1), 1))
                        .water(scale(latestEnergy.get(2), 1))
                        .gas(scale(latestEnergy.get(3), 1))
                        .statDate(latestEnergyDate)
                        .build())
                .build();
    }

    private List<FactoryOverviewRespVO.DeviceStatusItem> buildDeviceStatusList(List<IbmsDeviceDO> devices,
                                                                               Map<Long, Integer> stateMap,
                                                                               List<FactoryOverviewRespVO.FloorItem> floors) {
        return devices.stream()
                .sorted(Comparator
                        .comparing((IbmsDeviceDO device) -> statusOrder(stateMap.getOrDefault(device.getId(), DEVICE_STATE_INACTIVE)))
                        .thenComparing(device -> StrUtil.blankToDefault(device.getNickname(), device.getName()), String.CASE_INSENSITIVE_ORDER))
                .limit(8)
                .map(device -> {
                    FactoryOverviewRespVO.FloorItem floor = resolveFloor(device.getSpace(), floors);
                    int state = stateMap.getOrDefault(device.getId(), DEVICE_STATE_INACTIVE);
                    return FactoryOverviewRespVO.DeviceStatusItem.builder()
                            .id(device.getId())
                            .name(device.getName())
                            .nickname(device.getNickname())
                            .location(StrUtil.blankToDefault(device.getSpace(), "未配置空间"))
                            .floorId(floor != null ? floor.getId() : null)
                            .floorCode(floor != null ? floor.getCode() : null)
                            .floorName(floor != null ? floor.getName() : null)
                            .status(resolveDeviceStatusLabel(state))
                            .online(state == DEVICE_STATE_ONLINE)
                            .systemCode(device.getSystemCode())
                            .deviceTypeCode(device.getDeviceTypeCode())
                            .build();
                })
                .toList();
    }

    private List<FactoryOverviewRespVO.AlertItem> buildLatestAlerts(List<IotAlertRecordDO> alerts,
                                                                    Map<Long, IbmsDeviceDO> deviceMap,
                                                                    List<FactoryOverviewRespVO.FloorItem> floors) {
        return alerts.stream()
                .sorted(Comparator.comparing(IotAlertRecordDO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(6)
                .map(alert -> {
                    IbmsDeviceDO device = alert.getDeviceId() != null ? deviceMap.get(alert.getDeviceId()) : null;
                    String deviceName = device != null ? StrUtil.blankToDefault(device.getNickname(), device.getName()) :
                            (alert.getDeviceId() != null ? "设备-" + alert.getDeviceId() : "未关联设备");
                    String location = device != null ? StrUtil.blankToDefault(device.getSpace(), "未配置空间") : "未配置空间";
                    FactoryOverviewRespVO.FloorItem floor = resolveFloor(location, floors);
                    return FactoryOverviewRespVO.AlertItem.builder()
                            .id(alert.getId())
                            .title(StrUtil.blankToDefault(alert.getConfigName(), "告警事件"))
                            .level(alert.getConfigLevel())
                            .levelLabel(resolveAlertLevelLabel(alert.getConfigLevel()))
                            .deviceId(device != null ? device.getId() : alert.getDeviceId())
                            .deviceName(deviceName)
                            .location(location)
                            .floorId(floor != null ? floor.getId() : null)
                            .floorCode(floor != null ? floor.getCode() : null)
                            .floorName(floor != null ? floor.getName() : null)
                            .handled(Boolean.TRUE.equals(alert.getProcessStatus()))
                            .alarmTime(alert.getCreateTime())
                            .build();
                })
                .toList();
    }

    private FactoryOverviewRespVO.SceneInfo buildScene(List<FactoryOverviewRespVO.FloorItem> floors) {
        FactoryOverviewRespVO.FloorItem currentFloor = floors.isEmpty() ? null : floors.get(0);
        return FactoryOverviewRespVO.SceneInfo.builder()
                .currentFloorId(currentFloor != null ? currentFloor.getId() : null)
                .currentFloorName(currentFloor != null ? currentFloor.getName() : "未配置楼层")
                .title("工厂主视图区")
                .description("当前阶段先承接真实楼层、告警和视频联动信息，3D 场景仍按真实数据可用性逐步接入。")
                .actions(List.of(
                        FactoryOverviewRespVO.SceneAction.builder()
                                .key("reset")
                                .label("重置视角")
                                .enabled(false)
                                .actionType("message")
                                .target("一期暂未接入真实视角控制")
                                .build(),
                        FactoryOverviewRespVO.SceneAction.builder()
                                .key("focus")
                                .label("设备定位")
                                .enabled(false)
                                .actionType("message")
                                .target("一期暂未接入真实设备定位")
                                .build(),
                        FactoryOverviewRespVO.SceneAction.builder()
                                .key("patrol")
                                .label("巡检漫游")
                                .enabled(false)
                                .actionType("message")
                                .target("一期暂未接入真实巡检漫游")
                                .build(),
                        FactoryOverviewRespVO.SceneAction.builder()
                                .key("video")
                                .label("视频融合")
                                .enabled(true)
                                .actionType("route")
                                .target("/factory/dashboard/video")
                                .build()))
                .build();
    }

    private FactoryOverviewRespVO.VideoSnapshot buildVideoSnapshot(List<IbmsChannelDO> channels,
                                                                  List<FactoryOverviewRespVO.FloorItem> floors) {
        List<IbmsChannelDO> filteredChannels = channels.stream()
                .filter(channel -> isVideoChannel(channel.getBusiness(), channel.getSystemType()))
                .toList();
        long onlineCount = filteredChannels.stream().filter(channel -> isOnlineChannel(channel.getStatus())).count();
        List<FactoryOverviewRespVO.VideoSource> sources = filteredChannels.stream()
                .filter(channel -> isOnlineChannel(channel.getStatus()))
                .limit(4)
                .map(channel -> {
                    FactoryOverviewRespVO.FloorItem floor = resolveFloor(channel.getSpace(), floors);
                    return FactoryOverviewRespVO.VideoSource.builder()
                            .id(channel.getId())
                            .deviceId(channel.getDeviceId())
                            .name(StrUtil.blankToDefault(channel.getName(), "未命名视频源"))
                            .location(StrUtil.blankToDefault(channel.getSpace(), "未配置空间"))
                            .floorId(floor != null ? floor.getId() : null)
                            .floorCode(floor != null ? floor.getCode() : null)
                            .floorName(floor != null ? floor.getName() : null)
                            .status(channel.getStatus())
                            .build();
                })
                .toList();
        return FactoryOverviewRespVO.VideoSnapshot.builder()
                .total((long) filteredChannels.size())
                .online(onlineCount)
                .primarySource(sources.isEmpty() ? null : sources.get(0))
                .sources(sources)
                .build();
    }

    private List<FactoryOverviewRespVO.EnergyTrendItem> buildEnergyTrend(List<IbmsEnergyStatisticsDailyDO> energyRecords) {
        List<LocalDate> latestDates = energyRecords.stream()
                .map(IbmsEnergyStatisticsDailyDO::getStatDate)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .limit(7)
                .sorted()
                .toList();
        return latestDates.stream()
                .map(date -> {
                    Map<Integer, BigDecimal> energyByType = sumEnergyByType(energyRecords, date);
                    return FactoryOverviewRespVO.EnergyTrendItem.builder()
                            .date(date)
                            .electricity(scale(energyByType.get(1), 1))
                            .water(scale(energyByType.get(2), 1))
                            .gas(scale(energyByType.get(3), 1))
                            .build();
                })
                .toList();
    }

    private FactoryOverviewRespVO.EnvironmentSnapshot buildEnvironmentSnapshot(List<IbmsEnvSensorDO> sensors,
                                                                               Map<Long, IbmsEnvDataRecordDO> latestEnvRecordMap) {
        List<IbmsEnvDataRecordDO> records = new ArrayList<>(latestEnvRecordMap.values());
        IbmsEnvDataRecordDO latestRecord = records.stream()
                .max(Comparator.comparing(IbmsEnvDataRecordDO::getCollectTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
        Map<Long, IbmsEnvSensorDO> sensorMap = sensors.stream()
                .collect(Collectors.toMap(IbmsEnvSensorDO::getId, Function.identity(), (left, right) -> left));
        String location = latestRecord != null && sensorMap.containsKey(latestRecord.getSensorId())
                ? StrUtil.blankToDefault(sensorMap.get(latestRecord.getSensorId()).getLocation(), sensorMap.get(latestRecord.getSensorId()).getAreaName())
                : "未配置环境点位";
        long qualified = records.stream().filter(this::isEnvironmentQualified).count();
        return FactoryOverviewRespVO.EnvironmentSnapshot.builder()
                .temperature(average(records, IbmsEnvDataRecordDO::getTemperature, 1))
                .humidity(average(records, IbmsEnvDataRecordDO::getHumidity, 1))
                .pm25(average(records, IbmsEnvDataRecordDO::getPm25, 1))
                .co2(average(records, IbmsEnvDataRecordDO::getCo2, 1))
                .differentialPressure(null)
                .cleanliness(null)
                .qualified(qualified)
                .total((long) records.size())
                .collectedAt(latestRecord != null ? latestRecord.getCollectTime() : null)
                .location(location)
                .build();
    }

    private long countDeviceState(Collection<Integer> states, int targetState) {
        return states.stream().filter(state -> state != null && state == targetState).count();
    }

    private Map<Integer, BigDecimal> sumEnergyByType(List<IbmsEnergyStatisticsDailyDO> energyRecords, LocalDate statDate) {
        Map<Integer, BigDecimal> result = new LinkedHashMap<>();
        if (statDate == null) {
            return result;
        }
        for (IbmsEnergyStatisticsDailyDO record : energyRecords) {
            if (!statDate.equals(record.getStatDate())) {
                continue;
            }
            if (record.getMeterType() == null) {
                continue;
            }
            result.merge(record.getMeterType(), defaultDecimal(record.getConsumption()), BigDecimal::add);
        }
        return result;
    }

    private FactoryOverviewRespVO.FloorItem resolveFloor(String location, List<FactoryOverviewRespVO.FloorItem> floors) {
        if (StrUtil.isBlank(location)) {
            return null;
        }
        String normalizedLocation = location.trim().toUpperCase();
        for (FactoryOverviewRespVO.FloorItem floor : floors) {
            if (StrUtil.isBlank(floor.getCode())) {
                continue;
            }
            String code = floor.getCode().toUpperCase();
            if (normalizedLocation.startsWith(code)) {
                return floor;
            }
        }
        return null;
    }

    private String resolveDeviceStatusLabel(int state) {
        if (state == DEVICE_STATE_ONLINE) {
            return "在线";
        }
        if (state == DEVICE_STATE_OFFLINE) {
            return "离线";
        }
        return "未激活";
    }

    private String resolveAlertLevelLabel(Integer level) {
        if (level == null) {
            return "低";
        }
        if (level >= 3) {
            return "高";
        }
        if (level == 2) {
            return "中";
        }
        return "低";
    }

    private int statusOrder(int state) {
        if (state == DEVICE_STATE_ONLINE) {
            return 0;
        }
        if (state == DEVICE_STATE_OFFLINE) {
            return 1;
        }
        return 2;
    }

    private boolean isOnlineChannel(String status) {
        return StrUtil.isNotBlank(status) && ONLINE_CHANNEL_STATUS.contains(status.toLowerCase());
    }

    private boolean isVideoChannel(String business, String systemType) {
        // business 取 sa（智慧安防大类码），子系统 VI 兜底
        return "sa".equalsIgnoreCase(StrUtil.blankToDefault(business, ""))
                || "VI".equalsIgnoreCase(StrUtil.blankToDefault(systemType, ""));
    }

    private boolean isEnvironmentQualified(IbmsEnvDataRecordDO record) {
        return isWithin(record.getTemperature(), BigDecimal.valueOf(18), BigDecimal.valueOf(28))
                && isWithin(record.getHumidity(), BigDecimal.valueOf(40), BigDecimal.valueOf(70))
                && isMax(record.getPm25(), BigDecimal.valueOf(75))
                && isMax(record.getCo2(), BigDecimal.valueOf(1000));
    }

    private boolean isWithin(BigDecimal value, BigDecimal min, BigDecimal max) {
        return value != null && value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }

    private boolean isMax(BigDecimal value, BigDecimal max) {
        return value != null && value.compareTo(max) <= 0;
    }

    private BigDecimal rate(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(numerator)
                .multiply(ONE_HUNDRED)
                .divide(BigDecimal.valueOf(denominator), 1, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTrend(BigDecimal current, BigDecimal previous) {
        if (current == null || previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return current.subtract(previous)
                .multiply(ONE_HUNDRED)
                .divide(previous, 1, RoundingMode.HALF_UP);
    }

    private BigDecimal average(List<IbmsEnvDataRecordDO> records,
                               Function<IbmsEnvDataRecordDO, BigDecimal> extractor,
                               int scale) {
        List<BigDecimal> values = records.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .toList();
        if (values.isEmpty()) {
            return null;
        }
        BigDecimal total = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(values.size()), scale, RoundingMode.HALF_UP);
    }

    private BigDecimal scale(BigDecimal value, int scale) {
        return value == null ? BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP) : value.setScale(scale, RoundingMode.HALF_UP);
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
