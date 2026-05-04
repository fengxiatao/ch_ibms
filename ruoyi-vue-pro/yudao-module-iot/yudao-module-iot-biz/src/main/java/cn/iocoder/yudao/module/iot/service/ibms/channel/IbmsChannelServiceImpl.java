package cn.iocoder.yudao.module.iot.service.ibms.channel;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.channel.vo.IbmsChannelPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.channel.vo.IbmsChannelRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.channel.vo.IbmsChannelSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.channel.vo.IbmsDeviceTreeNodeRespVO;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsChannelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceRuntimeDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsChannelMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import cn.iocoder.yudao.module.iot.mq.manager.DeviceCommandResponseManager;
import cn.iocoder.yudao.module.iot.mq.producer.DeviceCommandPublisher;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsSpaceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsSpaceMapper;
import cn.iocoder.yudao.module.iot.service.channel.IotDeviceChannelService;
import cn.iocoder.yudao.module.iot.service.channel.SyncResult;
import cn.iocoder.yudao.module.iot.service.channel.support.IotGisSpatialLocationBuilder;
import cn.iocoder.yudao.module.iot.service.ibms.device.IbmsDeviceRuntimeService;
import cn.iocoder.yudao.module.iot.service.ibms.device.support.IbmsDeviceLedgerRuntimeHelper;
import cn.iocoder.yudao.module.iot.service.ibms.facade.IbmsBusinessMappingHelper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * IBMS 通道管理 Service 实现
 */
@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class IbmsChannelServiceImpl implements IbmsChannelService {

    private static final String DEVICE_TYPE_NVR = "NVR";
    private static final int SYNC_TIMEOUT_SECONDS = 10;

    private final IbmsChannelMapper channelMapper;
    private final IbmsDeviceMapper deviceMapper;
    private final DeviceCommandPublisher deviceCommandPublisher;
    private final DeviceCommandResponseManager responseManager;
    private final IbmsDeviceRuntimeService ibmsDeviceRuntimeService;
    private final IotDeviceChannelService iotDeviceChannelService;
    private final IotGisSpatialLocationBuilder spatialLocationBuilder;
    private final IbmsSpaceMapper ibmsSpaceMapper;
    private final IbmsBusinessMappingHelper businessMappingHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createChannel(IbmsChannelSaveReqVO reqVO) {
        IbmsChannelDO channel = BeanUtils.toBean(reqVO, IbmsChannelDO.class);
        channel.setId(null);
        deriveBusinessFromSystemType(channel);
        channelMapper.insert(channel);
        return channel.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChannel(IbmsChannelSaveReqVO reqVO) {
        if (reqVO.getId() == null) {
            throw ServiceExceptionUtil.exception0(400, "通道 ID 不能为空");
        }
        IbmsChannelDO exist = channelMapper.selectById(reqVO.getId());
        if (exist == null) {
            throw ServiceExceptionUtil.exception0(404, "通道不存在");
        }
        IbmsChannelDO update = BeanUtils.toBean(reqVO, IbmsChannelDO.class);
        deriveBusinessFromSystemType(update);
        channelMapper.updateById(update);
    }

    /**
     * 根据 system_type 自动回填 business（业务大类码，小写；与 ibms_group.value 一致但存储为小写）。
     * 前端若手填了 business 且与推导结果不一致，则以推导值覆盖并 WARN。
     */
    private void deriveBusinessFromSystemType(IbmsChannelDO channel) {
        String systemType = channel.getSystemType();
        if (StrUtil.isBlank(systemType)) {
            return;
        }
        String resolvedGroup = businessMappingHelper.resolveGroupBySystem(systemType);
        if (StrUtil.isBlank(resolvedGroup)) {
            return;
        }
        String expected = resolvedGroup.toLowerCase();
        String current = channel.getBusiness();
        if (StrUtil.isNotBlank(current) && !expected.equalsIgnoreCase(current.trim())) {
            log.warn("[IbmsChannel] business 与 system_type 不自洽，以字典推导值覆盖: systemType={}, inputBusiness={}, resolved={}",
                    systemType, current, expected);
        }
        channel.setBusiness(expected);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChannel(Long id) {
        IbmsChannelDO exist = channelMapper.selectById(id);
        if (exist == null) {
            return;
        }
        channelMapper.deleteById(id);
    }

    @Override
    public IbmsChannelRespVO getChannel(Long id) {
        IbmsChannelDO channel = channelMapper.selectById(id);
        if (channel == null) {
            return null;
        }
        return BeanUtils.toBean(channel, IbmsChannelRespVO.class);
    }

    @Override
    public PageResult<IbmsChannelRespVO> getChannelPage(IbmsChannelPageReqVO reqVO) {
        PageResult<IbmsChannelDO> page = channelMapper.selectPage(reqVO);
        return BeanUtils.toBean(page, IbmsChannelRespVO.class);
    }

    @Override
    public List<IbmsChannelRespVO> listChannelsByDeviceId(Long deviceId) {
        List<IbmsChannelDO> list = channelMapper.selectListByDeviceId(deviceId);
        return BeanUtils.toBean(list, IbmsChannelRespVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<IbmsChannelRespVO> syncChannelsFromDevice(Long deviceId) {
        IbmsDeviceDO ibmsDevice = deviceMapper.selectById(deviceId);
        if (ibmsDevice == null) {
            throw ServiceExceptionUtil.exception0(404, "设备不存在");
        }

        try {
            syncViaCommandBus(deviceId, ibmsDevice);
        } catch (Exception e) {
            log.error("[IbmsChannelService] QUERY_CHANNELS 同步失败: deviceId={}", deviceId, e);
            throw ServiceExceptionUtil.exception0(500, "同步通道失败: " + e.getMessage());
        }

        return listChannelsByDeviceId(deviceId);
    }

    /**
     * 通过命令总线向 NVR 插件发送 QUERY_CHANNELS；消息中的 deviceId 与 ibms_device.id 一致。
     */
    private void syncViaCommandBus(Long ibmsDeviceId, IbmsDeviceDO ibmsDevice) throws Exception {
        String requestId = java.util.UUID.randomUUID().toString();
        CompletableFuture<IotDeviceMessage> future = responseManager.registerRequest(requestId);

        Map<String, Object> params = buildNvrCommandParams(ibmsDevice);
        deviceCommandPublisher.publishCommand(DEVICE_TYPE_NVR, ibmsDeviceId, "QUERY_CHANNELS", params, requestId);

        IotDeviceMessage response = responseManager.waitForResponse(requestId, future, SYNC_TIMEOUT_SECONDS);
        if (response == null || (response.getCode() != null && response.getCode() != 0)) {
            throw new RuntimeException("NVR 返回失败: " + (response != null ? response.getMsg() : "null"));
        }

        List<Map<String, Object>> rawChannels = extractChannels(response.getData());
        upsertChannelsFromRaw(ibmsDeviceId, ibmsDevice, rawChannels);
    }

    /** NVR 命令附加参数；接入信息以 extra JSON 为准，列字段 ip 作补充 */
    private Map<String, Object> buildNvrCommandParams(IbmsDeviceDO d) {
        Map<String, Object> params = new HashMap<>();
        String host = firstHostFromExtra(d);
        if (StrUtil.isBlank(host)) {
            host = d.getIp();
        }
        if (StrUtil.isNotBlank(host)) {
            params.put("ip", host.trim());
        }
        return params;
    }

    private static String firstHostFromExtra(IbmsDeviceDO d) {
        if (StrUtil.isBlank(d.getExtra())) {
            return null;
        }
        try {
            JSONObject j = JSONUtil.parseObj(d.getExtra().trim());
            String ip = j.getStr("ip");
            if (StrUtil.isNotBlank(ip)) {
                return ip.trim();
            }
            String h = j.getStr("host");
            if (StrUtil.isNotBlank(h)) {
                return h.trim();
            }
        } catch (Exception ignored) {
            // ignore
        }
        return null;
    }

    /** 解析命令响应中的 channels 列表 */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractChannels(Object data) {
        if (data instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) data;
            Object channelsObj = dataMap.get("channels");
            if (channelsObj instanceof List) {
                return (List<Map<String, Object>>) channelsObj;
            }
        }
        if (data instanceof List) {
            return (List<Map<String, Object>>) data;
        }
        return new ArrayList<>();
    }

    /** 将 NVR SDK 返回的原始通道数据 upsert 到 ibms_channel */
    @Transactional(rollbackFor = Exception.class)
    protected void upsertChannelsFromRaw(Long ibmsDeviceId, IbmsDeviceDO ibmsDevice,
                                         List<Map<String, Object>> rawChannels) {
        if (rawChannels == null || rawChannels.isEmpty()) {
            return;
        }
        for (Map<String, Object> raw : rawChannels) {
            Integer channelNo = toInteger(raw.get("channelNo"));
            if (channelNo == null) {
                continue;
            }
            String channelName = toStr(raw.get("channelName"), "通道" + channelNo);
            boolean online = Boolean.TRUE.equals(raw.get("online"));

            IbmsChannelDO exist = findExistingChannel(ibmsDeviceId, channelNo);
            if (exist == null) {
                IbmsChannelDO channel = buildNewChannel(ibmsDeviceId, ibmsDevice, channelNo, channelName,
                        ibmsDevice.getIp(), online ? "online" : "offline");
                deriveBusinessFromSystemType(channel);
                channelMapper.insert(channel);
            } else {
                channelMapper.update(null, new LambdaUpdateWrapper<IbmsChannelDO>()
                        .eq(IbmsChannelDO::getId, exist.getId())
                        .set(IbmsChannelDO::getName, channelName)
                        .set(IbmsChannelDO::getStatus, online ? "online" : "offline"));
            }
        }
        updateDevicePointCount(ibmsDeviceId);
    }

    private IbmsChannelDO findExistingChannel(Long deviceId, Integer channelNo) {
        return channelMapper.selectOne(new LambdaUpdateWrapper<IbmsChannelDO>()
                .eq(IbmsChannelDO::getDeviceId, deviceId)
                .eq(IbmsChannelDO::getChannelNo, channelNo));
    }

    private IbmsChannelDO buildNewChannel(Long deviceId, IbmsDeviceDO device,
                                          Integer channelNo, String name,
                                          String ip, String status) {
        IbmsChannelDO ch = new IbmsChannelDO();
        ch.setDeviceId(deviceId);
        ch.setChannelNo(channelNo);
        ch.setName(name);
        ch.setIp(ip);
        ch.setStatus(status);
        ch.setDeviceName(device.getName());
        ch.setDeviceSn(device.getSn());
        // 默认 IBMS 语义字段：NVR 通道 = 视频通道
        ch.setBusiness(StrUtil.isNotBlank(device.getGroupCode()) ? device.getGroupCode().toLowerCase() : "security");
        ch.setTypeCode("VT");
        ch.setSystemType(StrUtil.isNotBlank(device.getSystemCode()) ? device.getSystemCode() : "VI");
        ch.setDataSource("NVR");
        ch.setCategory("视频通道");
        ch.setCurrentValue("--");
        String dc = StrUtil.blankToDefault(device.getDeviceCode(), "D" + deviceId);
        ch.setCode(dc + "-VT" + String.format("%02d", channelNo));
        return ch;
    }

    /** 同步设备的通道总数到 ibms_device.point_count */
    private void updateDevicePointCount(Long deviceId) {
        long count = channelMapper.selectCount(new LambdaUpdateWrapper<IbmsChannelDO>()
                .eq(IbmsChannelDO::getDeviceId, deviceId));
        // 只更新 point_count，不触发其他字段变更
        deviceMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<IbmsDeviceDO>()
                .eq(IbmsDeviceDO::getId, deviceId)
                .set(IbmsDeviceDO::getPointCount, (int) count));
    }

    private Integer toInteger(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Number) return ((Number) obj).intValue();
        try { return Integer.parseInt(obj.toString()); } catch (Exception e) { return null; }
    }

    private String toStr(Object obj, String fallback) {
        if (obj == null) return fallback;
        String s = obj.toString().trim();
        return s.isEmpty() ? fallback : s;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncRuntimeByDevice(Long deviceId, boolean online, Integer alarmCount) {
        if (deviceId == null) {
            return;
        }
        List<IbmsChannelDO> channels = channelMapper.selectListByDeviceId(deviceId);
        if (channels == null || channels.isEmpty()) {
            return;
        }
        boolean hasAlarm = alarmCount != null && alarmCount > 0;
        for (IbmsChannelDO channel : channels) {
            String status = resolveStatus(channel.getTypeCode(), online, hasAlarm);
            String currentValue = resolveCurrentValue(channel.getTypeCode(), status, hasAlarm);
            channelMapper.update(null, new LambdaUpdateWrapper<IbmsChannelDO>()
                    .eq(IbmsChannelDO::getId, channel.getId())
                    .set(IbmsChannelDO::getStatus, status)
                    .set(IbmsChannelDO::getCurrentValue, currentValue));
        }
    }

    private String resolveStatus(String typeCode, boolean online, boolean hasAlarm) {
        if (!online) {
            return "offline";
        }
        String t = safeType(typeCode);
        if (isAlarmType(t) && hasAlarm) {
            return "warning";
        }
        if (isAccessType(t)) {
            return "armed";
        }
        return "online";
    }

    private String resolveCurrentValue(String typeCode, String status, boolean hasAlarm) {
        String t = safeType(typeCode);
        if ("offline".equals(status)) {
            return "--";
        }
        if ("warning".equals(status)) {
            return "告警";
        }
        return switch (t) {
            case "VT", "VT-SUB", "VT-IN", "VT-OUT" -> "在线";
            case "DI", "DR", "DR-READER", "FP", "AI" -> hasAlarm ? "1" : "0";
            case "DO", "AO", "AO-AN", "AO_AN", "LT", "BC" -> "ON";
            case "PM", "AI-AN", "AI_AN" -> "正常";
            default -> "在线";
        };
    }

    private boolean isAlarmType(String typeCode) {
        return "DI".equals(typeCode) || "FP".equals(typeCode) || "AI".equals(typeCode);
    }

    private boolean isAccessType(String typeCode) {
        return "DR".equals(typeCode) || "DR-READER".equals(typeCode);
    }

    private String safeType(String typeCode) {
        return typeCode == null ? "" : typeCode.trim().toUpperCase();
    }

    @Override
    public List<IbmsChannelRespVO> listVideoChannels(String deviceTypeCode, Integer onlineStatus,
                                                      Boolean isPatrol, Boolean isMonitor) {
        List<IbmsChannelDO> rows = channelMapper.selectListVideoOrientedChannels();
        Map<Long, IbmsDeviceDO> dm = loadDeviceMapForChannels(rows);
        return rows.stream()
                .filter(this::isExtraEnabled)
                .filter(c -> matchesDeviceType(c, deviceTypeCode, dm))
                .filter(c -> matchesChannelOnlineFilter(c, onlineStatus))
                .filter(c -> matchesExtraBoolFilter(c, "isPatrol", isPatrol))
                .filter(c -> matchesExtraBoolFilter(c, "isMonitor", isMonitor))
                .sorted(Comparator.comparingInt(this::sortKeyFromExtra))
                .map(c -> BeanUtils.toBean(c, IbmsChannelRespVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<IbmsChannelRespVO> listPatrolChannels() {
        return channelMapper.selectListVideoOrientedChannels().stream()
                .filter(this::isExtraEnabled)
                .filter(c -> "online".equalsIgnoreCase(StrUtil.blankToDefault(c.getStatus(), "")))
                .filter(c -> readExtraBool(c, "isPatrol"))
                .sorted(Comparator.comparingInt(this::sortKeyFromExtra))
                .map(c -> BeanUtils.toBean(c, IbmsChannelRespVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<IbmsChannelRespVO> listMonitorChannels() {
        return channelMapper.selectListVideoOrientedChannels().stream()
                .filter(this::isExtraEnabled)
                .filter(c -> "online".equalsIgnoreCase(StrUtil.blankToDefault(c.getStatus(), "")))
                .filter(c -> readExtraBool(c, "isMonitor"))
                .sorted(Comparator.comparingInt(this::monitorPositionFromExtra))
                .map(c -> BeanUtils.toBean(c, IbmsChannelRespVO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchEnableChannels(List<Long> channelIds) {
        if (channelIds == null || channelIds.isEmpty()) {
            return;
        }
        channelIds.forEach(id -> mergeChannelExtraField(id, "enableStatus", 1));
        log.info("[IbmsChannel] 批量启用: count={}", channelIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDisableChannels(List<Long> channelIds) {
        if (channelIds == null || channelIds.isEmpty()) {
            return;
        }
        channelIds.forEach(id -> mergeChannelExtraField(id, "enableStatus", 0));
        log.info("[IbmsChannel] 批量禁用: count={}", channelIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSetPatrol(List<Long> channelIds, Boolean isPatrol) {
        if (channelIds == null || channelIds.isEmpty()) {
            return;
        }
        int v = Boolean.TRUE.equals(isPatrol) ? 1 : 0;
        channelIds.forEach(id -> mergeChannelExtraField(id, "isPatrol", v));
        log.info("[IbmsChannel] 批量巡更: count={}, isPatrol={}", channelIds.size(), isPatrol);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSetMonitor(List<Long> channelIds, Boolean isMonitor) {
        if (channelIds == null || channelIds.isEmpty()) {
            return;
        }
        int v = Boolean.TRUE.equals(isMonitor) ? 1 : 0;
        channelIds.forEach(id -> mergeChannelExtraField(id, "isMonitor", v));
        log.info("[IbmsChannel] 批量监控墙: count={}, isMonitor={}", channelIds.size(), isMonitor);
    }

    @Override
    public SyncResult batchSyncAllNvrChannels() {
        return iotDeviceChannelService.batchSyncAllNvrChannels();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAssignSpatial(List<Long> channelIds, Long campusId, Long buildingId, Long floorId, Long areaId) {
        if (channelIds == null || channelIds.isEmpty()) {
            return;
        }
        String location = spatialLocationBuilder.buildValidatedLocationPath(campusId, buildingId, floorId, areaId);
        Long tenantId = TenantContextHolder.getTenantId();
        Long resolvedSpaceId = resolveIbmsSpaceIdFromGis(tenantId, campusId, buildingId, floorId, areaId);
        for (Long id : channelIds) {
            IbmsChannelDO ch = channelMapper.selectById(id);
            if (ch == null) {
                continue;
            }
            JSONObject ex = StrUtil.isBlank(ch.getExtra()) ? JSONUtil.createObj() : JSONUtil.parseObj(ch.getExtra());
            ex.set("gisCampusId", campusId);
            ex.set("gisBuildingId", buildingId);
            ex.set("gisFloorId", floorId);
            if (areaId != null) {
                ex.set("gisAreaId", areaId);
            } else {
                ex.remove("gisAreaId");
            }
            channelMapper.update(null, new LambdaUpdateWrapper<IbmsChannelDO>()
                    .eq(IbmsChannelDO::getId, id)
                    .set(IbmsChannelDO::getSpace, location)
                    .set(IbmsChannelDO::getSpaceId, resolvedSpaceId)
                    .set(IbmsChannelDO::getExtra, ex.toString()));
        }
        log.info("[IbmsChannel] 批量指派空间: count={}, ibmsSpaceId={}", channelIds.size(), resolvedSpaceId);
    }

    private Long resolveIbmsSpaceIdFromGis(Long tenantId, Long campusId, Long buildingId, Long floorId, Long areaId) {
        if (tenantId == null) {
            return null;
        }
        if (areaId != null) {
            IbmsSpaceDO sp = ibmsSpaceMapper.selectByExtraGisId(tenantId, "gisAreaId", areaId);
            if (sp != null) {
                return sp.getId();
            }
        }
        if (floorId != null) {
            IbmsSpaceDO sp = ibmsSpaceMapper.selectByExtraGisId(tenantId, "gisFloorId", floorId);
            if (sp != null) {
                return sp.getId();
            }
        }
        if (buildingId != null) {
            IbmsSpaceDO sp = ibmsSpaceMapper.selectByExtraGisId(tenantId, "gisBuildingId", buildingId);
            if (sp != null) {
                return sp.getId();
            }
        }
        if (campusId != null) {
            IbmsSpaceDO sp = ibmsSpaceMapper.selectByExtraGisId(tenantId, "gisCampusId", campusId);
            if (sp != null) {
                return sp.getId();
            }
        }
        return null;
    }

    @Override
    public List<IbmsDeviceTreeNodeRespVO> getDeviceTree(String deviceTypeCode, String channelTypeCode,
                                                      Integer onlineStatus, String keyword) {
        LambdaQueryWrapperX<IbmsDeviceDO> dw = new LambdaQueryWrapperX<IbmsDeviceDO>()
                .eq(IbmsDeviceDO::getSystemCode, "VI");
        if (StrUtil.isNotBlank(deviceTypeCode)) {
            dw.eq(IbmsDeviceDO::getDeviceTypeCode, deviceTypeCode.trim());
        }
        if (StrUtil.isNotBlank(keyword)) {
            String k = keyword.trim();
            dw.and(w -> w.like(IbmsDeviceDO::getName, k).or().like(IbmsDeviceDO::getDeviceCode, k));
        }
        dw.orderByAsc(IbmsDeviceDO::getId);
        List<IbmsDeviceTreeNodeRespVO> roots = new ArrayList<>();
        for (IbmsDeviceDO d : deviceMapper.selectList(dw)) {
            IbmsDeviceRuntimeDO rt = ibmsDeviceRuntimeService.getByDeviceId(d.getId());
            Integer st = IbmsDeviceLedgerRuntimeHelper.resolveDeviceState(d, rt);
            int devOnline = (st != null && IotDeviceStateEnum.isOnline(st)) ? 1 : 0;

            IbmsDeviceTreeNodeRespVO dn = new IbmsDeviceTreeNodeRespVO();
            dn.setId("d-" + d.getId());
            dn.setLabel(StrUtil.blankToDefault(d.getName(), d.getDeviceCode()));
            dn.setType("device");
            dn.setDeviceId(d.getId());
            dn.setDeviceType(d.getDeviceTypeCode());
            dn.setOnlineStatus(devOnline);
            dn.setRaw(d);

            List<IbmsDeviceTreeNodeRespVO> children = new ArrayList<>();
            for (IbmsChannelDO ch : channelMapper.selectListByDeviceId(d.getId())) {
                if (StrUtil.isNotBlank(channelTypeCode)
                        && !channelTypeCode.equalsIgnoreCase(StrUtil.blankToDefault(ch.getTypeCode(), ""))) {
                    continue;
                }
                int chOnline = "online".equalsIgnoreCase(StrUtil.blankToDefault(ch.getStatus(), "")) ? 1 : 0;
                if (onlineStatus != null && !onlineStatus.equals(chOnline)) {
                    continue;
                }
                IbmsDeviceTreeNodeRespVO cn = new IbmsDeviceTreeNodeRespVO();
                cn.setId("c-" + ch.getId());
                cn.setLabel(ch.getName());
                cn.setType("channel");
                cn.setDeviceId(d.getId());
                cn.setChannelId(ch.getId());
                cn.setChannelNo(ch.getChannelNo());
                cn.setDeviceType(d.getDeviceTypeCode());
                cn.setChannelType(ch.getTypeCode());
                cn.setOnlineStatus(chOnline);
                cn.setRaw(ch);
                children.add(cn);
            }
            dn.setChildren(children);
            roots.add(dn);
        }
        return roots;
    }

    private Map<Long, IbmsDeviceDO> loadDeviceMapForChannels(List<IbmsChannelDO> rows) {
        List<Long> ids = rows.stream().map(IbmsChannelDO::getDeviceId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<IbmsDeviceDO> list = deviceMapper.selectList(
                new LambdaQueryWrapperX<IbmsDeviceDO>().in(IbmsDeviceDO::getId, ids));
        Map<Long, IbmsDeviceDO> m = new HashMap<>(list.size());
        for (IbmsDeviceDO d : list) {
            m.put(d.getId(), d);
        }
        return m;
    }

    private boolean matchesDeviceType(IbmsChannelDO c, String deviceTypeCode, Map<Long, IbmsDeviceDO> dm) {
        if (StrUtil.isBlank(deviceTypeCode)) {
            return true;
        }
        if (c.getDeviceId() == null) {
            return false;
        }
        IbmsDeviceDO d = dm.get(c.getDeviceId());
        return d != null && deviceTypeCode.equalsIgnoreCase(StrUtil.blankToDefault(d.getDeviceTypeCode(), ""));
    }

    private boolean matchesChannelOnlineFilter(IbmsChannelDO c, Integer onlineStatus) {
        if (onlineStatus == null) {
            return true;
        }
        int v = "online".equalsIgnoreCase(StrUtil.blankToDefault(c.getStatus(), "")) ? 1 : 0;
        return Objects.equals(onlineStatus, v);
    }

    private boolean matchesExtraBoolFilter(IbmsChannelDO c, String key, Boolean want) {
        if (want == null) {
            return true;
        }
        return want.equals(readExtraBool(c, key));
    }

    private boolean isExtraEnabled(IbmsChannelDO c) {
        if (StrUtil.isBlank(c.getExtra())) {
            return true;
        }
        try {
            Object o = JSONUtil.parseObj(c.getExtra()).get("enableStatus");
            if (o == null) {
                return true;
            }
            if (o instanceof Number) {
                return ((Number) o).intValue() == 1;
            }
            String s = o.toString();
            return "1".equals(s) || "true".equalsIgnoreCase(s);
        } catch (Exception e) {
            return true;
        }
    }

    private boolean readExtraBool(IbmsChannelDO c, String key) {
        if (StrUtil.isBlank(c.getExtra())) {
            return false;
        }
        try {
            Object o = JSONUtil.parseObj(c.getExtra()).get(key);
            if (o == null) {
                return false;
            }
            if (o instanceof Boolean) {
                return (Boolean) o;
            }
            if (o instanceof Number) {
                return ((Number) o).intValue() != 0;
            }
            String s = o.toString();
            return "1".equals(s) || "true".equalsIgnoreCase(s);
        } catch (Exception e) {
            return false;
        }
    }

    private int sortKeyFromExtra(IbmsChannelDO c) {
        if (StrUtil.isBlank(c.getExtra())) {
            return c.getId() != null ? c.getId().intValue() : 0;
        }
        try {
            Integer s = JSONUtil.parseObj(c.getExtra()).getInt("sort");
            return s != null ? s : (c.getId() != null ? c.getId().intValue() : 0);
        } catch (Exception e) {
            return c.getId() != null ? c.getId().intValue() : 0;
        }
    }

    private int monitorPositionFromExtra(IbmsChannelDO c) {
        if (StrUtil.isBlank(c.getExtra())) {
            return Integer.MAX_VALUE;
        }
        try {
            Integer p = JSONUtil.parseObj(c.getExtra()).getInt("monitorPosition");
            return p != null ? p : Integer.MAX_VALUE;
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    private void mergeChannelExtraField(Long channelId, String key, Object value) {
        IbmsChannelDO row = channelMapper.selectById(channelId);
        if (row == null) {
            return;
        }
        JSONObject ex = StrUtil.isBlank(row.getExtra()) ? JSONUtil.createObj() : JSONUtil.parseObj(row.getExtra());
        ex.set(key, value);
        IbmsChannelDO u = new IbmsChannelDO();
        u.setId(channelId);
        u.setExtra(ex.toString());
        channelMapper.updateById(u);
    }
}

