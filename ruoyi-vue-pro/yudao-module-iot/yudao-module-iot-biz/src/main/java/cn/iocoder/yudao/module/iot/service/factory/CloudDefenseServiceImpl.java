package cn.iocoder.yudao.module.iot.service.factory;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.factory.vo.CloudDefenseOverviewRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmZoneDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceEventLogDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.factory.CloudDefenseAreaDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.factory.CloudDefenseAreaDeviceRelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.factory.CloudDefenseModeDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.factory.CloudDefensePointDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.factory.CloudDefenseScoreLogDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsChannelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.alarm.IotAlarmZoneMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceEventLogMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.factory.CloudDefenseAreaDeviceRelMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.factory.CloudDefenseAreaMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.factory.CloudDefenseModeMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.factory.CloudDefensePointMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.factory.CloudDefenseScoreLogMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsChannelMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Validated
public class CloudDefenseServiceImpl implements CloudDefenseService {

    private static final Set<String> ONLINE_CHANNEL_STATUS = Set.of("online", "armed", "warning");
    private static final String DEFAULT_ACTIVE_MODE = "perimeter-defense";

    @Resource
    private CloudDefenseAreaMapper cloudDefenseAreaMapper;
    @Resource
    private CloudDefensePointMapper cloudDefensePointMapper;
    @Resource
    private CloudDefenseModeMapper cloudDefenseModeMapper;
    @Resource
    private CloudDefenseAreaDeviceRelMapper cloudDefenseAreaDeviceRelMapper;
    @Resource
    private CloudDefenseScoreLogMapper cloudDefenseScoreLogMapper;
    @Resource
    private IbmsDeviceMapper ibmsDeviceMapper;
    @Resource
    private IbmsChannelMapper ibmsChannelMapper;
    @Resource
    private IotAlarmZoneMapper iotAlarmZoneMapper;
    @Resource
    private IotDeviceEventLogMapper iotDeviceEventLogMapper;

    @Override
    public CloudDefenseOverviewRespVO getOverview() {
        List<CloudDefenseAreaDO> areas = cloudDefenseAreaMapper.selectEnabledList();
        List<CloudDefensePointDO> points = cloudDefensePointMapper.selectEnabledList();
        List<CloudDefenseModeDO> modes = cloudDefenseModeMapper.selectEnabledList();
        List<CloudDefenseAreaDeviceRelDO> areaDeviceRels = cloudDefenseAreaDeviceRelMapper.selectListByAreaIds(
                areas.stream().map(CloudDefenseAreaDO::getId).filter(Objects::nonNull).toList());
        Map<Long, CloudDefenseAreaDO> areaMap = areas.stream()
                .collect(Collectors.toMap(CloudDefenseAreaDO::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Set<Long> deviceIds = new LinkedHashSet<>();
        Set<Long> channelIds = new LinkedHashSet<>();
        areaDeviceRels.forEach(rel -> {
            if (rel.getDeviceId() != null) {
                deviceIds.add(rel.getDeviceId());
            }
            if (rel.getChannelId() != null) {
                channelIds.add(rel.getChannelId());
            }
        });
        points.forEach(point -> {
            if (point.getDeviceId() != null) {
                deviceIds.add(point.getDeviceId());
            }
            if (point.getChannelId() != null) {
                channelIds.add(point.getChannelId());
            }
        });
        List<IbmsDeviceDO> devices = deviceIds.isEmpty() ? Collections.emptyList() : ibmsDeviceMapper.selectBatchIds(deviceIds);
        List<IbmsChannelDO> channels = loadChannels(deviceIds, channelIds);
        List<IotAlarmZoneDO> zones = loadZones(areas);
        List<IotDeviceEventLogDO> todayAlerts = loadTodayAlerts(deviceIds);
        CloudDefenseScoreLogDO latestScore = cloudDefenseScoreLogMapper.selectLatest();

        Map<Long, IbmsDeviceDO> deviceMap = devices.stream()
                .collect(Collectors.toMap(IbmsDeviceDO::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, IbmsChannelDO> channelMap = channels.stream()
                .collect(Collectors.toMap(IbmsChannelDO::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, List<IbmsChannelDO>> channelsByDeviceId = channels.stream()
                .filter(channel -> channel.getDeviceId() != null)
                .collect(Collectors.groupingBy(IbmsChannelDO::getDeviceId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, List<CloudDefenseAreaDeviceRelDO>> relsByAreaId = areaDeviceRels.stream()
                .collect(Collectors.groupingBy(CloudDefenseAreaDeviceRelDO::getAreaId, LinkedHashMap::new, Collectors.toList()));
        Map<String, List<IotAlarmZoneDO>> zonesByAreaName = zones.stream()
                .filter(zone -> StrUtil.isNotBlank(zone.getAreaLocation()))
                .collect(Collectors.groupingBy(IotAlarmZoneDO::getAreaLocation, LinkedHashMap::new, Collectors.toList()));

        List<CloudDefenseOverviewRespVO.ZoneCardItem> zoneList = buildZoneCards(
                areas, relsByAreaId, deviceMap, channelsByDeviceId, zonesByAreaName);
        Map<Long, CloudDefenseOverviewRespVO.ZoneCardItem> zoneCardMap = zoneList.stream()
                .collect(Collectors.toMap(CloudDefenseOverviewRespVO.ZoneCardItem::getAreaId,
                        Function.identity(), (left, right) -> left, LinkedHashMap::new));

        return CloudDefenseOverviewRespVO.builder()
                .updatedAt(LocalDateTime.now())
                .metrics(buildMetrics(zoneList, todayAlerts, latestScore))
                .modes(buildModes(modes))
                .activeModeCode(resolveActiveModeCode(modes))
                .topology(buildTopology(areas, points, channelMap, zoneCardMap))
                .deviceList(buildDeviceList(areas, relsByAreaId, deviceMap, channelsByDeviceId))
                .zoneList(zoneList)
                .build();
    }

    private List<IbmsChannelDO> loadChannels(Set<Long> deviceIds, Set<Long> channelIds) {
        if (deviceIds.isEmpty() && channelIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapperX<IbmsChannelDO> wrapper = new LambdaQueryWrapperX<IbmsChannelDO>();
        if (!channelIds.isEmpty()) {
            wrapper.in(IbmsChannelDO::getId, channelIds);
        }
        if (!deviceIds.isEmpty()) {
            if (!channelIds.isEmpty()) {
                wrapper.or();
            }
            wrapper.in(IbmsChannelDO::getDeviceId, deviceIds);
        }
        return ibmsChannelMapper.selectList(wrapper.orderByAsc(IbmsChannelDO::getId));
    }

    private List<IotAlarmZoneDO> loadZones(List<CloudDefenseAreaDO> areas) {
        List<String> areaNames = areas.stream()
                .map(CloudDefenseAreaDO::getAreaName)
                .filter(StrUtil::isNotBlank)
                .toList();
        if (areaNames.isEmpty()) {
            return Collections.emptyList();
        }
        return iotAlarmZoneMapper.selectList(new LambdaQueryWrapperX<IotAlarmZoneDO>()
                .in(IotAlarmZoneDO::getAreaLocation, areaNames)
                .orderByAsc(IotAlarmZoneDO::getId));
    }

    private List<IotDeviceEventLogDO> loadTodayAlerts(Set<Long> deviceIds) {
        if (deviceIds.isEmpty()) {
            return Collections.emptyList();
        }
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        return iotDeviceEventLogMapper.selectList(new LambdaQueryWrapperX<IotDeviceEventLogDO>()
                .in(IotDeviceEventLogDO::getDeviceId, deviceIds)
                .eq(IotDeviceEventLogDO::getEventType, "alert")
                .between(IotDeviceEventLogDO::getEventTime, start, end)
                .orderByDesc(IotDeviceEventLogDO::getEventTime));
    }

    private CloudDefenseOverviewRespVO.MetricSummary buildMetrics(
            List<CloudDefenseOverviewRespVO.ZoneCardItem> zoneList,
            List<IotDeviceEventLogDO> todayAlerts,
            CloudDefenseScoreLogDO latestScore
    ) {
        long armedAreaCount = zoneList.stream()
                .filter(item -> item.getArmedZoneCount() != null && item.getArmedZoneCount() > 0)
                .count();
        long totalAreaCount = zoneList.size();
        long onlineDeviceCount = zoneList.stream()
                .map(CloudDefenseOverviewRespVO.ZoneCardItem::getOnlineDeviceCount)
                .filter(Objects::nonNull)
                .reduce(0L, Long::sum);
        long totalDeviceCount = zoneList.stream()
                .map(CloudDefenseOverviewRespVO.ZoneCardItem::getDeviceCount)
                .filter(Objects::nonNull)
                .reduce(0L, Long::sum);
        return CloudDefenseOverviewRespVO.MetricSummary.builder()
                .armedAreaCount(armedAreaCount)
                .totalAreaCount(totalAreaCount)
                .onlineDeviceCount(onlineDeviceCount)
                .totalDeviceCount(totalDeviceCount)
                .todayAlertCount((long) todayAlerts.size())
                .safetyScore(latestScore != null ? latestScore.getScore() : 0)
                .safetyLevel(latestScore != null ? StrUtil.blankToDefault(latestScore.getScoreLevel(), "待评估") : "待评估")
                .build();
    }

    private List<CloudDefenseOverviewRespVO.ModeItem> buildModes(List<CloudDefenseModeDO> modes) {
        return modes.stream()
                .map(mode -> CloudDefenseOverviewRespVO.ModeItem.builder()
                        .id(mode.getId())
                        .code(mode.getModeCode())
                        .name(mode.getModeName())
                        .icon(mode.getIcon())
                        .statusText(StrUtil.blankToDefault(mode.getStatusText(), "待接入"))
                        .enabled(mode.getEnabled() != null && mode.getEnabled() == 1)
                        .build())
                .toList();
    }

    private String resolveActiveModeCode(List<CloudDefenseModeDO> modes) {
        return modes.stream()
                .map(CloudDefenseModeDO::getModeCode)
                .filter(DEFAULT_ACTIVE_MODE::equals)
                .findFirst()
                .orElseGet(() -> modes.stream()
                        .map(CloudDefenseModeDO::getModeCode)
                        .findFirst()
                        .orElse(DEFAULT_ACTIVE_MODE));
    }

    private CloudDefenseOverviewRespVO.Topology buildTopology(
            List<CloudDefenseAreaDO> areas,
            List<CloudDefensePointDO> points,
            Map<Long, IbmsChannelDO> channelMap,
            Map<Long, CloudDefenseOverviewRespVO.ZoneCardItem> zoneCardMap
    ) {
        return CloudDefenseOverviewRespVO.Topology.builder()
                .title("周界防护态势图")
                .legends(List.of(
                        CloudDefenseOverviewRespVO.LegendItem.builder().key("online").label("在线设备").color("#2f7cff").build(),
                        CloudDefenseOverviewRespVO.LegendItem.builder().key("alarm").label("告警设备").color("#ff6b5b").build(),
                        CloudDefenseOverviewRespVO.LegendItem.builder().key("armed").label("已设防").color("#2bd38b").build(),
                        CloudDefenseOverviewRespVO.LegendItem.builder().key("zone").label("周界围栏").color("#63b3ff").build()
                ))
                .areas(areas.stream()
                        .map(area -> {
                            CloudDefenseOverviewRespVO.ZoneCardItem zoneCard = zoneCardMap.get(area.getId());
                            return CloudDefenseOverviewRespVO.AreaItem.builder()
                                    .id(area.getId())
                                    .code(area.getAreaCode())
                                    .name(area.getAreaName())
                                    .type(area.getAreaType())
                                    .x(area.getLayoutX())
                                    .y(area.getLayoutY())
                                    .width(area.getLayoutWidth())
                                    .height(area.getLayoutHeight())
                                    .armed(zoneCard != null && zoneCard.getArmedZoneCount() != null && zoneCard.getArmedZoneCount() > 0)
                                    .alarming(zoneCard != null && zoneCard.getAlarmingZoneCount() != null && zoneCard.getAlarmingZoneCount() > 0)
                                    .deviceCount(zoneCard != null ? zoneCard.getDeviceCount() : 0L)
                                    .zoneCount(zoneCard != null ? zoneCard.getZoneCount() : 0L)
                                    .detailText(StrUtil.blankToDefault(area.getDetailText(),
                                            zoneCard != null ? StrUtil.format("{}设备 / {}防区", zoneCard.getDeviceCount(), zoneCard.getZoneCount()) : "待配置"))
                                    .build();
                        })
                        .toList())
                .points(points.stream()
                        .map(point -> {
                            IbmsChannelDO channel = point.getChannelId() != null ? channelMap.get(point.getChannelId()) : null;
                            boolean online = isOnline(point.getOnlineStatus()) || isOnline(channel != null ? channel.getStatus() : null);
                            boolean alarming = isAlarming(point.getAlarmStatus()) || isWarning(channel != null ? channel.getStatus() : null);
                            return CloudDefenseOverviewRespVO.PointItem.builder()
                                    .id(point.getId())
                                    .areaId(point.getAreaId())
                                    .deviceId(point.getDeviceId())
                                    .channelId(point.getChannelId())
                                    .code(point.getPointCode())
                                    .name(point.getPointName())
                                    .type(point.getPointType())
                                    .x(point.getLayoutX())
                                    .y(point.getLayoutY())
                                    .armed(isArmed(point.getArmedStatus()))
                                    .alarming(alarming)
                                    .online(online)
                                    .build();
                        })
                        .toList())
                .build();
    }

    private List<CloudDefenseOverviewRespVO.DeviceItem> buildDeviceList(
            List<CloudDefenseAreaDO> areas,
            Map<Long, List<CloudDefenseAreaDeviceRelDO>> relsByAreaId,
            Map<Long, IbmsDeviceDO> deviceMap,
            Map<Long, List<IbmsChannelDO>> channelsByDeviceId
    ) {
        List<CloudDefenseOverviewRespVO.DeviceItem> items = new ArrayList<>();
        for (CloudDefenseAreaDO area : areas) {
            for (CloudDefenseAreaDeviceRelDO rel : relsByAreaId.getOrDefault(area.getId(), Collections.emptyList())) {
                IbmsDeviceDO device = rel.getDeviceId() != null ? deviceMap.get(rel.getDeviceId()) : null;
                if (device == null) {
                    continue;
                }
                List<IbmsChannelDO> deviceChannels = channelsByDeviceId.getOrDefault(device.getId(), Collections.emptyList());
                items.add(CloudDefenseOverviewRespVO.DeviceItem.builder()
                        .id(device.getId())
                        .areaId(area.getId())
                        .areaName(area.getAreaName())
                        .name(StrUtil.blankToDefault(device.getNickname(), device.getName()))
                        .typeLabel(resolveDeviceTypeLabel(device, deviceChannels))
                        .location(StrUtil.blankToDefault(device.getSpace(), area.getAreaName()))
                        .online(resolveDeviceOnline(device, deviceChannels))
                        .capabilityTags(resolveCapabilityTags(device, deviceChannels))
                        .build());
            }
        }
        return items.stream()
                .sorted(Comparator
                        .comparing((CloudDefenseOverviewRespVO.DeviceItem item) -> Boolean.TRUE.equals(item.getOnline()) ? 0 : 1)
                        .thenComparing(CloudDefenseOverviewRespVO.DeviceItem::getAreaName, Comparator.nullsLast(String::compareTo))
                        .thenComparing(CloudDefenseOverviewRespVO.DeviceItem::getName, Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    private List<CloudDefenseOverviewRespVO.ZoneCardItem> buildZoneCards(
            List<CloudDefenseAreaDO> areas,
            Map<Long, List<CloudDefenseAreaDeviceRelDO>> relsByAreaId,
            Map<Long, IbmsDeviceDO> deviceMap,
            Map<Long, List<IbmsChannelDO>> channelsByDeviceId,
            Map<String, List<IotAlarmZoneDO>> zonesByAreaName
    ) {
        return areas.stream()
                .map(area -> {
                    List<CloudDefenseAreaDeviceRelDO> rels = relsByAreaId.getOrDefault(area.getId(), Collections.emptyList());
                    List<IotAlarmZoneDO> areaZones = zonesByAreaName.getOrDefault(area.getAreaName(), Collections.emptyList());
                    long onlineDeviceCount = rels.stream()
                            .map(CloudDefenseAreaDeviceRelDO::getDeviceId)
                            .map(deviceMap::get)
                            .filter(Objects::nonNull)
                            .filter(device -> resolveDeviceOnline(device, channelsByDeviceId.getOrDefault(device.getId(), Collections.emptyList())))
                            .count();
                    long armedZoneCount = areaZones.stream().filter(zone -> isArmed(zone.getArmStatus())).count();
                    long alarmingZoneCount = areaZones.stream().filter(zone -> isAlarming(zone.getAlarmStatus())).count();
                    long zoneCount = areaZones.size();
                    return CloudDefenseOverviewRespVO.ZoneCardItem.builder()
                            .id(area.getId())
                            .areaId(area.getId())
                            .name(area.getAreaName())
                            .deviceCount((long) rels.size())
                            .onlineDeviceCount(onlineDeviceCount)
                            .zoneCount(zoneCount)
                            .armedZoneCount(armedZoneCount)
                            .alarmingZoneCount(alarmingZoneCount)
                            .statusText(resolveZoneCardStatus(zoneCount, armedZoneCount))
                            .healthText(alarmingZoneCount > 0 ? alarmingZoneCount + "告警" : "正常")
                            .actionText(armedZoneCount > 0 ? "撤防" : "设防")
                            .build();
                })
                .toList();
    }

    private String resolveZoneCardStatus(long zoneCount, long armedZoneCount) {
        if (zoneCount == 0) {
            return "待配置";
        }
        if (armedZoneCount >= zoneCount) {
            return "正常";
        }
        if (armedZoneCount > 0) {
            return "部分布防";
        }
        return "未设防";
    }

    private boolean resolveDeviceOnline(IbmsDeviceDO device, List<IbmsChannelDO> channels) {
        if (device.getPointsOnline() != null && device.getPointsOnline() > 0) {
            return true;
        }
        return channels.stream().anyMatch(channel -> isOnline(channel.getStatus()));
    }

    private String resolveDeviceTypeLabel(IbmsDeviceDO device, List<IbmsChannelDO> channels) {
        String source = StrUtil.blankToDefault(device.getName(), "");
        if (source.contains("人脸")) {
            return "人脸识别";
        }
        if (source.contains("车牌")) {
            return "车牌识别";
        }
        if (source.contains("热成像")) {
            return "热成像";
        }
        if (source.contains("全景")) {
            return "全景鹰眼";
        }
        if (source.contains("球机")) {
            return "云台球机";
        }
        if (source.contains("枪机")) {
            return "高清枪机";
        }
        String systemCode = StrUtil.blankToDefault(device.getSystemCode(), "");
        if ("AC".equalsIgnoreCase(systemCode)) {
            return "门禁设备";
        }
        if ("AL".equalsIgnoreCase(systemCode)) {
            return "报警设备";
        }
        if ("VI".equalsIgnoreCase(systemCode) || channels.stream().anyMatch(channel -> "VI".equalsIgnoreCase(channel.getSystemType()))) {
            return "视频设备";
        }
        return "云防设备";
    }

    private List<String> resolveCapabilityTags(IbmsDeviceDO device, List<IbmsChannelDO> channels) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        if ("VI".equalsIgnoreCase(device.getSystemCode()) || channels.stream().anyMatch(channel -> "VI".equalsIgnoreCase(channel.getSystemType()))) {
            tags.add("视频");
        }
        if ("AC".equalsIgnoreCase(device.getSystemCode()) || channels.stream().anyMatch(channel -> "access".equalsIgnoreCase(channel.getBusiness()))) {
            tags.add("门禁");
        }
        if ("AL".equalsIgnoreCase(device.getSystemCode()) || channels.stream().anyMatch(channel -> "alarm".equalsIgnoreCase(channel.getBusiness()))) {
            tags.add("云防");
        }
        if (channels.stream().anyMatch(channel -> StrUtil.containsIgnoreCase(channel.getName(), "云台") || StrUtil.containsIgnoreCase(channel.getName(), "球机"))) {
            tags.add("PTZ");
        }
        if (StrUtil.containsIgnoreCase(device.getName(), "人脸")) {
            tags.add("人脸");
        }
        if (StrUtil.containsIgnoreCase(device.getName(), "车牌")) {
            tags.add("车牌");
        }
        if (tags.isEmpty()) {
            tags.add("监测");
        }
        return tags.stream().limit(3).toList();
    }

    private boolean isOnline(String channelStatus) {
        return ONLINE_CHANNEL_STATUS.contains(StrUtil.blankToDefault(channelStatus, "").toLowerCase());
    }

    private boolean isOnline(Integer onlineStatus) {
        return onlineStatus != null && onlineStatus == 1;
    }

    private boolean isArmed(Integer armedStatus) {
        return armedStatus != null && armedStatus == 1;
    }

    private boolean isAlarming(Integer alarmStatus) {
        return alarmStatus != null && alarmStatus > 0;
    }

    private boolean isWarning(String channelStatus) {
        return "warning".equalsIgnoreCase(channelStatus);
    }
}
