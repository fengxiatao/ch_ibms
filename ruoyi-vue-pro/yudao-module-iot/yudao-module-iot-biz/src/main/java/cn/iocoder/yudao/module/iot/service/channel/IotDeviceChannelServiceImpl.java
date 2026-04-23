package cn.iocoder.yudao.module.iot.service.channel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.controller.admin.channel.vo.IotDeviceChannelPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.channel.vo.IotDeviceChannelSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.channel.vo.NvrWithChannelsRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelHistoryDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsChannelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfigHelper;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.GenericDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.channel.IotDeviceChannelHistoryMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsChannelMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.channel.vo.IbmsChannelRespVO;
import cn.iocoder.yudao.module.iot.service.ibms.device.IbmsDeviceRuntimeService;
import cn.iocoder.yudao.module.iot.service.ibms.device.support.IbmsDeviceLedgerRuntimeHelper;
import cn.iocoder.yudao.module.iot.service.channel.support.IotGisSpatialLocationBuilder;
import cn.iocoder.yudao.module.iot.service.ibms.channel.IbmsChannelService;
import cn.iocoder.yudao.module.iot.service.video.nvr.NvrQueryService;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 设备通道 Service 实现类
 *
 * @author IBMS Team
 */
@Service
@Validated
@Slf4j
public class IotDeviceChannelServiceImpl implements IotDeviceChannelService {

    @Resource
    private IbmsChannelMapper ibmsChannelMapper;

    @Resource
    private IotDeviceChannelHistoryMapper channelHistoryMapper;

    @jakarta.annotation.Resource
    @org.springframework.context.annotation.Lazy
    private IbmsChannelService ibmsChannelService;

    @Resource
    private NvrQueryService nvrQueryService;

    @Resource
    private IbmsDeviceMapper ibmsDeviceMapper;

    @Resource
    private IbmsDeviceRuntimeService ibmsDeviceRuntimeService;

    @Resource
    private IotGisSpatialLocationBuilder spatialLocationBuilder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createChannel(IotDeviceChannelSaveReqVO createReqVO) {
        throw cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0(
                410, "已废弃：请改用 `/iot/ibms/channel/create|update|delete|page` 对应接口（IBMS 单台账）。");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChannel(IotDeviceChannelSaveReqVO updateReqVO) {
        throw cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0(
                410, "已废弃：请改用 `/iot/ibms/channel/update` 对应接口（IBMS 单台账）。");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChannel(Long id) {
        throw cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0(
                410, "已废弃：请改用 `/iot/ibms/channel/delete` 对应接口（IBMS 单台账）。");
    }

    @Override
    public IotDeviceChannelDO getChannel(Long id) {
        throw cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0(
                410, "已废弃：请改用 `/iot/ibms/channel/get` 对应接口（IBMS 单台账）。");
    }

    @Override
    public PageResult<IotDeviceChannelDO> getChannelPage(IotDeviceChannelPageReqVO pageReqVO) {
        if (pageReqVO.getDeviceId() != null) {
            IbmsDeviceDO ibmsDevice = ibmsDeviceMapper.selectById(pageReqVO.getDeviceId());
            if (pageReqVO.getChannelType() == null || "VIDEO".equalsIgnoreCase(pageReqVO.getChannelType())) {
                // 仅实现 NVR 视频通道分页：把 legacy 条件映射到 ibms_channel
                cn.iocoder.yudao.module.iot.controller.admin.ibms.channel.vo.IbmsChannelPageReqVO ibmsReq =
                        new cn.iocoder.yudao.module.iot.controller.admin.ibms.channel.vo.IbmsChannelPageReqVO();
                ibmsReq.setPageNo(pageReqVO.getPageNo());
                ibmsReq.setPageSize(pageReqVO.getPageSize());
                ibmsReq.setDeviceId(pageReqVO.getDeviceId());
                ibmsReq.setSpaceId(pageReqVO.getSpaceId());
                ibmsReq.setKeyword(pageReqVO.getChannelName());
                ibmsReq.setTypeCode("VIDEO".equalsIgnoreCase(pageReqVO.getChannelType()) || pageReqVO.getChannelType() == null ? "VT" : null);

                if (pageReqVO.getOnlineStatus() != null) {
                    Integer os = pageReqVO.getOnlineStatus();
                    if (os == 0) {
                        ibmsReq.setStatus("offline");
                    } else if (os == 1) {
                        ibmsReq.setStatus("online");
                    } else if (os == 2) {
                        ibmsReq.setStatus("warning");
                    } else if (os == 3) {
                        ibmsReq.setStatus("armed");
                    }
                }
                ibmsReq.setCreateTime(pageReqVO.getCreateTime());

                PageResult<IbmsChannelDO> page = ibmsChannelMapper.selectPage(ibmsReq);
                List<IotDeviceChannelDO> list = page.getList().stream()
                        .map(r -> convertIbmsVideoChannelToLegacy(r, ibmsDevice))
                        .collect(Collectors.toList());
                return new PageResult<>(list, page.getTotal());
            }
        }
        throw cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0(
                410, "已废弃：请改用 `/iot/ibms/channel/page` 对应接口（IBMS 单台账）。");
    }

    @Override
    public List<IotDeviceChannelDO> getChannelsByDeviceId(Long deviceId) {
        IbmsDeviceDO ibmsDevice = ibmsDeviceMapper.selectById(deviceId);
        if (ibmsDevice != null) {
            List<IbmsChannelDO> rows = ibmsChannelMapper.selectListByDeviceId(deviceId).stream()
                    .filter(ch -> {
                        String tc = StrUtil.blankToDefault(ch.getTypeCode(), "").toUpperCase();
                        return tc.startsWith("VT")
                                || "DR".equalsIgnoreCase(tc)
                                || "DR-READER".equalsIgnoreCase(tc);
                    })
                    .collect(Collectors.toList());
            if (!rows.isEmpty()) {
                return rows.stream().map(ch -> {
                    String tc = StrUtil.blankToDefault(ch.getTypeCode(), "").toUpperCase();
                    if (tc.startsWith("VT")) {
                        return convertIbmsVideoChannelToLegacy(ch, ibmsDevice);
                    }
                    return convertIbmsAccessChannelToLegacy(ch);
                }).collect(Collectors.toList());
            }
        }
        // IBMS 未命中或不存在：本阶段不再回退到 iot_device_channel（G4 清理 iot_ 持久层依赖）
        return List.of();
    }

    @Override
    public IotDeviceChannelDO getChannelByDeviceIdAndChannelNo(Long deviceId, Integer channelNo) {
        IbmsDeviceDO ibmsDevice = ibmsDeviceMapper.selectById(deviceId);
        if (ibmsDevice != null) {
            IbmsChannelDO ibmsChannel = ibmsChannelMapper.selectOne(new LambdaQueryWrapperX<IbmsChannelDO>()
                    .eq(IbmsChannelDO::getDeviceId, deviceId)
                    .eq(IbmsChannelDO::getChannelNo, channelNo));
            if (ibmsChannel != null && StrUtil.isNotBlank(ibmsChannel.getTypeCode())) {
                String tc = ibmsChannel.getTypeCode().toUpperCase();
                if (tc.startsWith("VT")) {
                    return convertIbmsVideoChannelToLegacy(ibmsChannel, ibmsDevice);
                }
                if ("DR".equalsIgnoreCase(tc) || "DR-READER".equalsIgnoreCase(tc)) {
                    return convertIbmsAccessChannelToLegacy(ibmsChannel);
                }
            }
        }

        // IBMS 未命中：本阶段不再回退到 iot_device_channel（G4 清理 iot_ 持久层依赖）
        return null;
    }

    @Override
    public List<IotDeviceChannelDO> getVideoChannels(String deviceType, Integer onlineStatus, Boolean isPatrol, Boolean isMonitor) {
        List<IbmsChannelRespVO> rows = ibmsChannelService.listVideoChannels(deviceType, onlineStatus, isPatrol, isMonitor);
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        return rows.stream()
                .map(r -> {
                    IbmsChannelDO ch = new IbmsChannelDO();
                    ch.setId(r.getId());
                    ch.setSpaceId(r.getSpaceId());
                    ch.setDeviceId(r.getDeviceId());
                    ch.setCode(r.getCode());
                    ch.setChannelNo(r.getChannelNo());
                    ch.setName(r.getName());
                    ch.setStatus(r.getStatus());
                    ch.setExtra(r.getExtra());
                    ch.setSpace(r.getSpace());
                    ch.setDataSource(r.getDataSource());
                    ch.setCreateTime(r.getCreateTime());
                    return convertIbmsVideoChannelToLegacy(ch, null);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<IotDeviceChannelDO> getPatrolChannels() {
        List<IbmsChannelRespVO> rows = ibmsChannelService.listPatrolChannels();
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        return rows.stream()
                .map(r -> {
                    IbmsChannelDO ch = new IbmsChannelDO();
                    ch.setId(r.getId());
                    ch.setSpaceId(r.getSpaceId());
                    ch.setDeviceId(r.getDeviceId());
                    ch.setCode(r.getCode());
                    ch.setChannelNo(r.getChannelNo());
                    ch.setName(r.getName());
                    ch.setStatus(r.getStatus());
                    ch.setExtra(r.getExtra());
                    ch.setSpace(r.getSpace());
                    ch.setDataSource(r.getDataSource());
                    ch.setCreateTime(r.getCreateTime());
                    return convertIbmsVideoChannelToLegacy(ch, null);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<IotDeviceChannelDO> getMonitorChannels() {
        List<IbmsChannelRespVO> rows = ibmsChannelService.listMonitorChannels();
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        return rows.stream()
                .map(r -> {
                    IbmsChannelDO ch = new IbmsChannelDO();
                    ch.setId(r.getId());
                    ch.setSpaceId(r.getSpaceId());
                    ch.setDeviceId(r.getDeviceId());
                    ch.setCode(r.getCode());
                    ch.setChannelNo(r.getChannelNo());
                    ch.setName(r.getName());
                    ch.setStatus(r.getStatus());
                    ch.setExtra(r.getExtra());
                    ch.setSpace(r.getSpace());
                    ch.setDataSource(r.getDataSource());
                    ch.setCreateTime(r.getCreateTime());
                    return convertIbmsVideoChannelToLegacy(ch, null);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer syncDeviceChannels(Long deviceId) {
        // 1. 校验设备存在（单台账：仅 IBMS）
        IbmsDeviceDO ibmsDevice = ibmsDeviceMapper.selectById(deviceId);
        if (ibmsDevice == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        IotDeviceDO deviceShell = IbmsDeviceLedgerRuntimeHelper.buildLegacyOtaDeviceShell(
                ibmsDevice, ibmsDeviceRuntimeService.getByDeviceId(deviceId));

        // 2. 根据设备类型同步通道
        int syncCount = 0;
        boolean isNvr = isIbmsNvr(ibmsDevice);
        if (isNvr) {
            // NVR设备：从SDK获取通道列表
            if (nvrQueryService != null) {
                var sdkChannels = nvrQueryService.refreshChannelsByNvrId(deviceId);
                
                // 获取数据库中现有的通道
                List<IbmsChannelDO> dbChannels = ibmsChannelMapper.selectListByDeviceId(deviceId).stream()
                        .filter(ch -> StrUtil.isNotBlank(ch.getTypeCode()) && ch.getTypeCode().toUpperCase().startsWith("VT"))
                        .collect(Collectors.toList());
                
                // 提取SDK返回的通道号列表
                java.util.Set<Integer> sdkChannelNos = new java.util.HashSet<>();
                for (var ch : sdkChannels) {
                    if (ch.getChannelNo() != null) {
                        sdkChannelNos.add(ch.getChannelNo());
                    }
                }

                IotDeviceDO nvrForSync = IbmsDeviceLedgerRuntimeHelper.buildLegacyNvrDeviceShell(
                        ibmsDevice, ibmsDeviceRuntimeService.getByDeviceId(deviceId));
                
                // 同步通道（新增或更新）
                for (var ch : sdkChannels) {
                    syncNvrChannelToIbms(deviceId, ibmsDevice, nvrForSync, ch.toLegacyChannelDeviceForSync(deviceId));
                    syncCount++;
                }
                
                // 删除SDK中不存在的通道（硬删除 + 记录历史）
                int deletedCount = 0;
                for (IbmsChannelDO dbChannel : dbChannels) {
                    if (!sdkChannelNos.contains(dbChannel.getChannelNo())) {
                        // 硬删除
                        ibmsChannelMapper.deleteById(dbChannel.getId());
                        deletedCount++;
                        log.info("[通道同步] 删除通道: deviceId={}, channelNo={}, name={}", 
                                deviceId, dbChannel.getChannelNo(), dbChannel.getName());
                    }
                }
                
                if (deletedCount > 0) {
                    log.info("[通道同步] 删除了 {} 个不存在的通道", deletedCount);
                }
            }
        } else if (deviceShell != null && "IPC".equals(deviceShell.getDeviceType())) {
            // 球机或普通IPC：通过 ONVIF 查询设备通道数
            syncCount = syncIpcChannelsViaOnvifToIbms(deviceId, ibmsDevice, deviceShell);
        }

        log.info("[通道管理] 同步设备通道完成: deviceId={}, syncCount={}", deviceId, syncCount);
        return syncCount;
    }

    private static boolean isIbmsNvr(IbmsDeviceDO ibms) {
        if (ibms == null) {
            return false;
        }
        if ("NVR".equals(ibms.getDeviceTypeCode()) || Objects.equals(ibms.getIbmsProductId(), 4L)) {
            return true;
        }
        String extra = ibms.getExtra();
        if (extra == null || extra.isEmpty()) {
            return false;
        }
        try {
            return "NVR".equals(cn.hutool.json.JSONUtil.parseObj(extra).getStr("deviceType"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 通过 ONVIF 同步 IPC（非 NVR）视频通道到 {@code ibms_channel}。
     *
     * <p>legacy 的视频字段（PTZ/认证/targetIp/分辨率等）统一放在 {@code ibms_channel.extra}，
     * 以便 {@link #convertIbmsVideoChannelToLegacy(IbmsChannelDO, IbmsDeviceDO)} 回填。</p>
     */
    private int syncIpcChannelsViaOnvifToIbms(Long deviceId, IbmsDeviceDO ibmsDevice, IotDeviceDO device) {
        int syncCount = 0;

        // 1) 删除旧的 ibms_channel（只删除视频 VT*）
        List<IbmsChannelDO> existingChannels = ibmsChannelMapper.selectListByDeviceId(deviceId).stream()
                .filter(ch -> StrUtil.isNotBlank(ch.getTypeCode()) && ch.getTypeCode().toUpperCase().startsWith("VT"))
                .collect(Collectors.toList());
        if (!existingChannels.isEmpty()) {
            log.info("[通道同步] 删除设备旧的 IBMS 视频通道，准备重新同步: deviceId={}, oldChannelCount={}",
                    deviceId, existingChannels.size());
            for (IbmsChannelDO channel : existingChannels) {
                ibmsChannelMapper.deleteById(channel.getId());
            }
        }

        // 2) 解析设备配置：ONVIF 连接认证（沿用旧逻辑）
        String username = "admin";
        String password = "admin123";
        try {
            if (device.getConfig() != null) {
                Map<String, Object> configMap = device.getConfig().toMap();
                if (configMap.containsKey("username")) {
                    Object usernameObj = configMap.get("username");
                    username = usernameObj != null ? usernameObj.toString() : "admin";
                }
                if (configMap.containsKey("password")) {
                    Object passwordObj = configMap.get("password");
                    password = passwordObj != null ? passwordObj.toString() : "admin123";
                }
            }
        } catch (Exception e) {
            log.warn("[通道同步] 解析设备配置失败，使用默认认证信息: deviceId={}", deviceId, e);
        }

        // 3) ONVIF 查询 profiles
        String deviceIp = DeviceConfigHelper.getIpAddress(device);
        log.info("[通道同步] 开始通过ONVIF查询IPC通道: deviceId={}, ip={}", deviceId, deviceIp);

        List<cn.iocoder.yudao.module.iot.service.onvif.OnvifChannelInfo> channelInfoList;
        try {
            cn.iocoder.yudao.module.iot.service.onvif.OnvifClient onvifClient =
                    new cn.iocoder.yudao.module.iot.service.onvif.OnvifClient(deviceIp, username, password);
            channelInfoList = onvifClient.getProfiles();
        } catch (Exception e) {
            log.error("[通道同步] ONVIF查询失败，将使用默认通道配置: deviceId={}, ip={}",
                    deviceId, deviceIp, e);
            channelInfoList = null;
        }

        if (channelInfoList == null || channelInfoList.isEmpty()) {
            log.warn("[通道同步] ONVIF查询返回空结果，使用默认配置: deviceId={}, ip={}", deviceId, deviceIp);
            channelInfoList = createDefaultChannelInfo(device);
        }

        // 4) 写入 ibms_channel（typeCode=VT）
        String dc = StrUtil.blankToDefault(ibmsDevice.getDeviceCode(), "D" + deviceId);
        String business = StrUtil.isNotBlank(ibmsDevice.getGroupCode()) ? ibmsDevice.getGroupCode().toLowerCase() : "security";
        String systemType = StrUtil.isNotBlank(ibmsDevice.getSystemCode()) ? ibmsDevice.getSystemCode() : "VI";

        // device.getState() 沿用旧逻辑：1=在线，2=离线
        String status = (device.getState() != null && device.getState() == 1) ? "online" : "offline";

        for (cn.iocoder.yudao.module.iot.service.onvif.OnvifChannelInfo info : channelInfoList) {
            int channelNo = info.getChannelNo();
            if (channelNo <= 0) {
                continue;
            }

            String channelName = info.getChannelName() != null
                    ? info.getChannelName()
                    : device.getDeviceName() + "-通道" + channelNo;

            IbmsChannelDO channel = new IbmsChannelDO();
            channel.setId(null);
            channel.setTenantId(ibmsDevice.getTenantId());
            channel.setDeviceId(deviceId);
            channel.setChannelNo(channelNo);
            channel.setTypeCode("VT");
            channel.setSystemType(systemType);
            channel.setBusiness(business);
            channel.setDataSource("IPC");
            channel.setCategory("视频通道");
            channel.setCurrentValue("--");
            channel.setIp(deviceIp);
            channel.setStatus(status);
            channel.setName(channelName);
            channel.setCode(dc + "-VT" + String.format("%02d", channelNo));

            cn.hutool.json.JSONObject extra = cn.hutool.json.JSONUtil.createObj();
            extra.set("enableStatus", 1);
            extra.set("isPatrol", 0);
            extra.set("isMonitor", 0);
            extra.set("sort", channelNo);
            extra.set("monitorPosition", Integer.MAX_VALUE);

            extra.set("channelSubType", info.isPtzSupport() ? "PTZ" : "IPC");
            extra.set("ptzSupport", info.isPtzSupport());
            extra.set("audioSupport", info.isAudioSupport());
            if (info.getResolution() != null) {
                extra.set("resolution", info.getResolution());
            }

            extra.set("protocol", "ONVIF");
            extra.set("username", username);
            extra.set("password", password);

            extra.set("targetIp", deviceIp);
            extra.set("targetChannelNo", channelNo);

            extra.set("lastSyncTime", LocalDateTime.now().toString());

            channel.setExtra(extra.toString());
            ibmsChannelMapper.insert(channel);
            syncCount++;
        }

        updateIbmsDevicePointCount(deviceId);
        return syncCount;
    }

    /**
     * 将 IBMS 通道（视频类）转换为旧版 {@link IotDeviceChannelDO}，供 NVR/摄像头链路复用。
     *
     * <p>约定：legacy 字段统一存放在 {@code ibms_channel.extra} 中，关键键位在本类的
     * {@link #syncNvrChannelToIbms(Long, IbmsDeviceDO, IotDeviceDO, IotDeviceDO)} 中写入。</p>
     */
    private IotDeviceChannelDO convertIbmsVideoChannelToLegacy(IbmsChannelDO ibmsChannel, IbmsDeviceDO ibmsDevice) {
        IotDeviceChannelDO c = new IotDeviceChannelDO();
        if (ibmsChannel == null) {
            return c;
        }
        c.setId(ibmsChannel.getId());
        c.setDeviceId(ibmsChannel.getDeviceId());
        // deviceType 影响 RTSP 构建（NVR 使用设备/IP，IPC 使用 targetIp）
        if (ibmsDevice != null) {
            c.setDeviceType(isIbmsNvr(ibmsDevice) ? "NVR" : "IPC");
        } else {
            String ds = ibmsChannel.getDataSource();
            c.setDeviceType("NVR".equalsIgnoreCase(ds) ? "NVR" : "IPC");
        }
        c.setChannelNo(ibmsChannel.getChannelNo());
        c.setChannelName(ibmsChannel.getName());
        c.setChannelCode(ibmsChannel.getCode());
        c.setChannelType("VIDEO");

        // 设备子类型（PTZ/IPC 等）来源于 extra
        String channelSubType = null;
        Integer enableStatus = 1;
        Integer isPatrol = 0;
        Integer isMonitor = 0;
        Integer sort = ibmsChannel.getChannelNo();
        Integer monitorPosition = Integer.MAX_VALUE;
        Boolean ptzSupport = null;
        Boolean audioSupport = null;
        String resolution = null;
        String protocol = null;
        String username = null;
        String password = null;
        String streamUrlMain = null;
        String streamUrlSub = null;
        String snapshotUrl = null;
        String targetIp = null;
        Integer targetChannelNo = 1;
        LocalDateTime lastSyncTime = null;
        Integer onlineStatus = 0;
        Integer alarmStatus = 0;
        java.util.Map<String, Object> capabilities = null;

        String status = ibmsChannel.getStatus();
        if ("offline".equalsIgnoreCase(status)) {
            onlineStatus = 0;
            alarmStatus = 0;
        } else if ("warning".equalsIgnoreCase(status)) {
            onlineStatus = 2;
            alarmStatus = 1;
        } else {
            // online/armed
            onlineStatus = 1;
            alarmStatus = 0;
        }

        cn.hutool.json.JSONObject ex;
        if (StrUtil.isBlank(ibmsChannel.getExtra())) {
            ex = cn.hutool.json.JSONUtil.createObj();
        } else {
            try {
                ex = cn.hutool.json.JSONUtil.parseObj(ibmsChannel.getExtra().trim());
            } catch (Exception e) {
                ex = cn.hutool.json.JSONUtil.createObj();
            }
        }
        channelSubType = ex.getStr("channelSubType");
        if (StrUtil.isBlank(channelSubType)) {
            channelSubType = "IPC";
        }
        c.setChannelSubType(channelSubType);

        Integer vEnable = ex.getInt("enableStatus");
        if (vEnable != null) {
            enableStatus = vEnable;
        }
        isPatrol = ex.getInt("isPatrol") != null ? ex.getInt("isPatrol") : 0;
        isMonitor = ex.getInt("isMonitor") != null ? ex.getInt("isMonitor") : 0;
        sort = ex.getInt("sort") != null ? ex.getInt("sort") : sort;
        Integer mp = ex.getInt("monitorPosition");
        if (mp != null) {
            monitorPosition = mp;
        }
        // 这些字段用于 NVR/云台/区域放大等链路
        ptzSupport = ex.getBool("ptzSupport");
        audioSupport = ex.getBool("audioSupport");
        resolution = ex.getStr("resolution");
        protocol = ex.getStr("protocol");
        username = ex.getStr("username");
        password = ex.getStr("password");
        streamUrlMain = ex.getStr("streamUrlMain");
        streamUrlSub = ex.getStr("streamUrlSub");
        snapshotUrl = ex.getStr("snapshotUrl");
        targetIp = ex.getStr("targetIp");
        Integer tcn = ex.getInt("targetChannelNo");
        if (tcn != null) {
            targetChannelNo = tcn;
        }

        // 记录最后同步时间（用于响应 VO 展示）
        String lastSyncTimeStr = ex.getStr("lastSyncTime");
        if (StrUtil.isNotBlank(lastSyncTimeStr)) {
            try {
                lastSyncTime = LocalDateTime.parse(lastSyncTimeStr);
            } catch (Exception ignore) {
                // ignore
            }
        }

        Object cap = ex.get("capabilities");
        if (cap instanceof java.util.Map) {
            // 直接透传
            capabilities = (java.util.Map<String, Object>) cap;
        }

        c.setOnlineStatus(onlineStatus);
        c.setEnableStatus(enableStatus);
        c.setAlarmStatus(alarmStatus);
        c.setIsPatrol(isPatrol);
        c.setIsMonitor(isMonitor);
        c.setSort(sort);
        c.setMonitorPosition(monitorPosition);
        c.setPtzSupport(ptzSupport);
        c.setAudioSupport(audioSupport);
        c.setResolution(resolution);
        c.setProtocol(protocol);
        c.setUsername(username);
        c.setPassword(password);
        c.setStreamUrlMain(streamUrlMain);
        c.setStreamUrlSub(streamUrlSub);
        c.setSnapshotUrl(snapshotUrl);
        c.setTargetIp(targetIp);
        c.setTargetChannelNo(targetChannelNo);
        c.setLastSyncTime(lastSyncTime);
        c.setCapabilities(capabilities);
        c.setSpaceId(ibmsChannel.getSpaceId());
        c.setLocation(ibmsChannel.getSpace());

        // config 在当前阶段由访问通道（iot）链路维护；视频通道先不做强行映射
        c.setConfig(null);
        c.setCreateTime(ibmsChannel.getCreateTime());
        c.setUpdateTime(ibmsChannel.getUpdateTime());
        return c;
    }

    /**
     * 将 IBMS 通道（门禁/访问类：DR/DR-READER）转换为旧版 {@link IotDeviceChannelDO}，供门禁链路复用。
     *
     * <p>约定：legacy 字段关键配置（doorMode/alwaysMode/alwaysModeCode）来源于 {@code ibms_channel.extra.config}。</p>
     */
    private IotDeviceChannelDO convertIbmsAccessChannelToLegacy(IbmsChannelDO ibmsChannel) {
        IotDeviceChannelDO c = new IotDeviceChannelDO();
        if (ibmsChannel == null) {
            return c;
        }

        c.setId(ibmsChannel.getId());
        c.setDeviceId(ibmsChannel.getDeviceId());
        c.setChannelNo(ibmsChannel.getChannelNo());
        c.setChannelName(ibmsChannel.getName());
        c.setChannelCode(ibmsChannel.getCode());

        c.setDeviceType("ACCESS_CONTROLLER");
        c.setChannelType("ACCESS");
        c.setChannelSubType("DOOR");
        c.setDoorName(ibmsChannel.getName());

        // onlineStatus 映射：offline -> 0；armed -> 1；warning -> 2
        String status = ibmsChannel.getStatus();
        if ("offline".equalsIgnoreCase(status)) {
            c.setOnlineStatus(0);
            c.setAlarmStatus(0);
        } else if ("warning".equalsIgnoreCase(status)) {
            c.setOnlineStatus(2);
            c.setAlarmStatus(1);
        } else {
            c.setOnlineStatus(1);
            // access 点位 currentValue：0/1 等
            c.setAlarmStatus("1".equalsIgnoreCase(StrUtil.blankToDefault(ibmsChannel.getCurrentValue(), "")) ? 1 : 0);
        }

        cn.hutool.json.JSONObject ex;
        if (StrUtil.isBlank(ibmsChannel.getExtra())) {
            ex = cn.hutool.json.JSONUtil.createObj();
        } else {
            try {
                ex = cn.hutool.json.JSONUtil.parseObj(ibmsChannel.getExtra().trim());
            } catch (Exception e) {
                ex = cn.hutool.json.JSONUtil.createObj();
            }
        }

        Integer enableStatus = ex.getInt("enableStatus");
        c.setEnableStatus(enableStatus != null ? enableStatus : 1);

        // doorName/config/capabilities 来源于 extra
        String doorName = ex.getStr("doorName");
        if (StrUtil.isNotBlank(doorName)) {
            c.setDoorName(doorName);
        }

        // 记录最后同步时间（用于展示/审计）
        String lastSyncTimeStr = ex.getStr("lastSyncTime");
        if (StrUtil.isNotBlank(lastSyncTimeStr)) {
            try {
                c.setLastSyncTime(LocalDateTime.parse(lastSyncTimeStr));
            } catch (Exception ignore) {}
        }

        Object cfgObj = ex.get("config");
        c.setConfig(normalizeToMap(cfgObj));

        Object capObj = ex.get("capabilities");
        if (capObj instanceof java.util.Map) {
            c.setCapabilities((java.util.Map<String, Object>) capObj);
        } else if (capObj instanceof cn.hutool.json.JSONObject) {
            cn.hutool.json.JSONObject jo = (cn.hutool.json.JSONObject) capObj;
            c.setCapabilities((java.util.Map<String, Object>) jo);
        }

        c.setCreateTime(ibmsChannel.getCreateTime());
        c.setUpdateTime(ibmsChannel.getUpdateTime());
        return c;
    }

    @SuppressWarnings("unchecked")
    private java.util.Map<String, Object> normalizeToMap(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof java.util.Map) {
            return (java.util.Map<String, Object>) obj;
        }
        if (obj instanceof cn.hutool.json.JSONObject) {
            cn.hutool.json.JSONObject jo = (cn.hutool.json.JSONObject) obj;
            return (java.util.Map<String, Object>) jo;
        }
        if (obj instanceof String) {
            String s = (String) obj;
            if (StrUtil.isBlank(s)) {
                return null;
            }
            try {
                cn.hutool.json.JSONObject jo = cn.hutool.json.JSONUtil.parseObj(s.trim());
                return (java.util.Map<String, Object>) jo;
            } catch (Exception ignore) {
                // ignore
            }
        }
        return null;
    }

    /**
     * 将 {@code ibms_device.point_count} 同步为该设备下 {@code ibms_channel} 总数。
     */
    private void updateIbmsDevicePointCount(Long deviceId) {
        if (deviceId == null) {
            return;
        }
        long count = ibmsChannelMapper.selectCount(new LambdaQueryWrapperX<IbmsChannelDO>()
                .eq(IbmsChannelDO::getDeviceId, deviceId));
        ibmsDeviceMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<IbmsDeviceDO>()
                        .eq(IbmsDeviceDO::getId, deviceId)
                        .set(IbmsDeviceDO::getPointCount, (int) count));
    }
    
    /**
     * 通过 ONVIF 同步 IPC 设备通道（包括球机）
     * 
     * @param deviceId 设备ID
     * @param device 设备信息
     * @return 同步的通道数量
     */
    private int syncIpcChannelsViaOnvif(Long deviceId, IotDeviceDO device) {
        // G4 清理 iot_ 持久层后：IPC 通道不再落库到 iot_device_channel
        // 当前仍走 ibms_device_runtime + ibms_channel 的收敛链路。
        return 0;
    }
    
    /**
     * 创建默认通道信息列表
     */
    private List<cn.iocoder.yudao.module.iot.service.onvif.OnvifChannelInfo> createDefaultChannelInfo(IotDeviceDO device) {
        List<cn.iocoder.yudao.module.iot.service.onvif.OnvifChannelInfo> list = new ArrayList<>();
        cn.iocoder.yudao.module.iot.service.onvif.OnvifChannelInfo info = 
                new cn.iocoder.yudao.module.iot.service.onvif.OnvifChannelInfo();
        info.setChannelNo(1);
        info.setChannelName(device.getDeviceName());
        info.setPtzSupport(false);
        info.setAudioSupport(false);
        list.add(info);
        return list;
    }
    
    /**
     * 创建默认通道（当 ONVIF 查询失败时）
     */
    private int createDefaultChannel(Long deviceId, IotDeviceDO device, String username, String password) {
        IotDeviceChannelDO channel = new IotDeviceChannelDO();
        channel.setDeviceId(deviceId);
        channel.setProductId(device.getProductId());
        channel.setDeviceType(convertDeviceType(device.getDeviceType()));
        channel.setChannelNo(1);
        channel.setChannelName(device.getDeviceName() + "-默认通道");
        channel.setChannelCode("CH-" + device.getDeviceKey() + "-1");
        channel.setChannelType("VIDEO");
        channel.setChannelSubType("IPC");
        channel.setPtzSupport(false);
        channel.setAudioSupport(false);
        
        // 设置目标设备信息
        channel.setTargetDeviceId(deviceId);
        channel.setTargetIp(DeviceConfigHelper.getIpAddress(device));
        channel.setTargetPort(80);
        channel.setTargetChannelNo(1);
        
        // 设置协议和认证信息
        channel.setProtocol("ONVIF");
        channel.setUsername(username);
        channel.setPassword(password);
        
        // 设置状态
        channel.setOnlineStatus(device.getState());
        channel.setEnableStatus(1);
        channel.setAlarmStatus(0);
        channel.setSort(1);
        
        // G4 清理 iot_ 持久层后：默认通道不再落库到 iot_device_channel
        log.info("[通道同步] 创建默认通道: deviceId={}, channelNo=1", deviceId);
        return 1;
    }
    
    /**
     * 将设备类型 Integer 转换为 String
     */
    private String convertDeviceType(Integer deviceType) {
        if (deviceType == null) {
            return "UNKNOWN";
        }
        // 根据枚举值转换
        // 1=直连设备, 2=网关子设备, 3=网关设备
        switch (deviceType) {
            case 1: return "DIRECT";
            case 2: return "GATEWAY_SUB";
            case 3: return "GATEWAY";
            default: return "UNKNOWN";
        }
    }

    /**
     * 同步NVR通道
     * @param nvrId NVR设备ID
     * @param nvrDevice NVR设备信息
     * @param channelInfo 通道信息
     */
    private void syncNvrChannel(Long nvrId, IotDeviceDO nvrDevice, IotDeviceDO channelInfo) {
        // 从config中获取通道号和云台支持信息
        // DeviceConfig 转换为 JSON 字符串
        String configStr = null;
        if (channelInfo.getConfig() != null) {
            try {
                configStr = JsonUtils.toJsonString(channelInfo.getConfig().toMap());
            } catch (Exception e) {
                log.warn("[通道同步] 序列化config失败: {}", e.getMessage());
            }
        }
        if (configStr == null || configStr.isEmpty()) {
            return;
        }
        
        Integer channelNo;
        Boolean ptzSupport = false;
        Boolean audioSupport = false;
        String deviceType = null;
        String resolution = null;
        String sdkChannelName = null;  // ✅ 从SDK获取的通道名称
        try {
            cn.hutool.json.JSONObject cfg = cn.hutool.json.JSONUtil.parseObj(configStr);
            channelNo = cfg.getInt("channel");
            
            // ✅ 从SDK返回的config中获取通道名称
            sdkChannelName = cfg.getStr("channelName");
            
            // ✅ 调试日志：查看SDK返回的config内容
            log.info("[通道同步] SDK返回的config: nvrId={}, channelNo={}, config={}", 
                    nvrId, channelNo, configStr);
            
            // 方式1：SDK直接返回云台支持信息（优先）
            if (cfg.containsKey("ptzSupport")) {
                Object ptzObj = cfg.get("ptzSupport");
                if (ptzObj != null) {
                    // SDK返回的可能是Boolean或String
                    if (ptzObj instanceof Boolean) {
                        ptzSupport = (Boolean) ptzObj;
                    } else {
                        ptzSupport = Boolean.parseBoolean(String.valueOf(ptzObj));
                    }
                    log.info("[通道同步] ✅ 从SDK获取ptzSupport: channelNo={}, ptzObj={}, ptzSupport={}", 
                            channelNo, ptzObj, ptzSupport);
                } else {
                    log.warn("[通道同步] SDK返回ptzSupport=null，尝试其他方式: channelNo={}", channelNo);
                }
            }
            
            // 方式2：如果SDK没有返回或返回null，通过设备类型推断
            if (!ptzSupport && cfg.containsKey("deviceType")) {
                deviceType = cfg.getStr("deviceType");
                if (deviceType != null) {
                    ptzSupport = isPtzDevice(deviceType);
                    log.info("[通道同步] 通过deviceType推断ptzSupport: channelNo={}, deviceType={}, ptzSupport={}", 
                            channelNo, deviceType, ptzSupport);
                }
            }
            
            // 方式3：通过设备名称推断
            if (!ptzSupport) {
                String deviceName = channelInfo.getDeviceName();
                if (deviceName != null) {
                    String nameLower = deviceName.toLowerCase();
                    if (nameLower.contains("ptz") || nameLower.contains("dome") || 
                        nameLower.contains("球机") || nameLower.contains("球") ||
                        nameLower.contains("ipc") || nameLower.contains("speed")) {
                        ptzSupport = true;
                        log.info("[通道同步] 通过设备名称推断ptzSupport: channelNo={}, deviceName={}, ptzSupport=true", 
                                channelNo, deviceName);
                    }
                }
            }
            
            // 方式4：通过 IP 地址推断（同一 IP 多通道可能是球机）
            if (!ptzSupport) {
                cn.hutool.json.JSONObject cfg2 = cn.hutool.json.JSONUtil.parseObj(configStr);
                String ipAddress = cfg2.getStr("ipAddress");
                if (ipAddress != null && isPtzByIpPattern(ipAddress, nvrId)) {
                    ptzSupport = true;
                    log.info("[通道同步] 通过IP模式推断ptzSupport: channelNo={}, ip={}, ptzSupport=true", 
                            channelNo, ipAddress);
                }
            }
            
            // 最终日志
            log.info("[通道同步] 最终ptzSupport值: channelNo={}, ptzSupport={}", channelNo, ptzSupport);
            
            // 其他能力信息
            audioSupport = cfg.getBool("audioSupport", false);
            resolution = cfg.getStr("resolution");
        } catch (Exception e) {
            log.warn("[通道管理] 解析通道配置失败: {}", e.getMessage());
            return;
        }
        
        if (channelNo == null) {
            return;
        }

        // 获取NVR配置信息（用于生成URL）
        String nvrIp = DeviceConfigHelper.getIpAddress(nvrDevice);
        String username = "admin";
        String password = "admin123";
        Integer rtspPort = 554;
        Integer httpPort = 80;
        
        if (nvrDevice.getConfig() != null) {
            try {
                Map<String, Object> nvrConfigMap = nvrDevice.getConfig().toMap();
                username = nvrConfigMap.get("username") != null ? nvrConfigMap.get("username").toString() : "admin";
                password = nvrConfigMap.get("password") != null ? nvrConfigMap.get("password").toString() : "admin123";
                rtspPort = nvrConfigMap.get("rtspPort") != null ? Integer.parseInt(nvrConfigMap.get("rtspPort").toString()) : 554;
                httpPort = nvrConfigMap.get("httpPort") != null ? Integer.parseInt(nvrConfigMap.get("httpPort").toString()) : 80;
            } catch (Exception ignored) {}
        }
        
        // 生成视频流URL（通过NVR访问）
        String streamUrlMain = generateStreamUrl(nvrIp, channelNo, "main", username, password, rtspPort);
        String streamUrlSub = generateStreamUrl(nvrIp, channelNo, "sub", username, password, rtspPort);
        String snapshotUrl = generateSnapshotUrl(nvrIp, channelNo, username, password, httpPort);

        // 查询是否已存在
        final Integer finalChannelNo = channelNo;
        // G4 清理 iot_ 持久层依赖：legacy NVR 通道同步不再写入 iot_device_channel
        List<IotDeviceChannelDO> existingChannels = List.of();
        IotDeviceChannelDO existing = existingChannels.stream()
                .filter(ch -> ch.getChannelNo().equals(finalChannelNo))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            // 更新现有通道
            IotDeviceChannelDO updateObj = new IotDeviceChannelDO();
            updateObj.setId(existing.getId());
            
            // ✅ 使用SDK返回的通道名称更新（优先使用sdkChannelName，如果为空则保留原有名称）
            String existingName = existing.getChannelName();
            // SDK 返回的通道名称优先
            String newName = sdkChannelName;
            // 只有当 SDK 返回了有效的非默认名称时才更新
            boolean hasValidSdkName = newName != null && !newName.isEmpty() 
                    && !newName.matches("^通道\\d+$");  // SDK 返回的不是默认名称
            if (hasValidSdkName && !Objects.equals(newName, existingName)) {
                updateObj.setChannelName(newName);
                log.info("[通道同步] 更新通道名称: channelNo={}, {} -> {}", channelNo, existingName, newName);
            }
            // 如果 SDK 返回的是默认名称或为空，保留数据库中的名称
            
            updateObj.setTargetIp(DeviceConfigHelper.getIpAddress(channelInfo));
            updateObj.setOnlineStatus(channelInfo.getState());
            // ✅ 保留手动设置的 PTZ 支持：数据库已有 true 或 SDK 检测到 true，结果就是 true
            boolean finalPtzSupport = ptzSupport || Boolean.TRUE.equals(existing.getPtzSupport());
            updateObj.setPtzSupport(finalPtzSupport);
            updateObj.setAudioSupport(audioSupport);
            updateObj.setResolution(resolution);
            updateObj.setStreamUrlMain(streamUrlMain);  // ✅ 主码流URL
            updateObj.setStreamUrlSub(streamUrlSub);    // ✅ 子码流URL
            updateObj.setSnapshotUrl(snapshotUrl);      // ✅ 快照URL
            updateObj.setLastSyncTime(LocalDateTime.now());
            // ✅ 同步时自动启用通道（设备在线且能扫描到说明通道是有效的）
            if (existing.getEnableStatus() == null || existing.getEnableStatus() == 0) {
                updateObj.setEnableStatus(1);
                log.info("[通道同步] 自动启用通道: nvrId={}, channelNo={}", nvrId, channelNo);
            }
            // no-op
            // 显示更新后的名称（如果更新了就显示新名称，否则显示原名称）
            String displayName = updateObj.getChannelName() != null ? updateObj.getChannelName() : existingName;
            log.info("[通道同步] 更新通道: nvrId={}, channelNo={}, name={}, onlineStatus={}, ptzSupport={}", 
                    nvrId, channelNo, displayName, channelInfo.getState(), ptzSupport);
        } else {
            // 创建新通道
            IotDeviceChannelDO newChannel = new IotDeviceChannelDO();
            newChannel.setDeviceId(nvrId);
            newChannel.setDeviceType("NVR");
            newChannel.setProductId(4L);
            newChannel.setChannelNo(channelNo);
            // ✅ 优先使用SDK返回的通道名称，否则使用默认名称
            String insertName = (sdkChannelName != null && !sdkChannelName.isEmpty()) 
                    ? sdkChannelName : "通道" + channelNo;
            newChannel.setChannelName(insertName);
            newChannel.setChannelType("VIDEO");
            newChannel.setChannelSubType(deviceType != null ? deviceType : "IPC");
            newChannel.setTargetIp(DeviceConfigHelper.getIpAddress(channelInfo));
            newChannel.setTargetChannelNo(1); // IPC通常只有一个通道，默认为1
            newChannel.setProtocol("RTSP");
            newChannel.setUsername(username);
            newChannel.setPassword(password);
            newChannel.setStreamUrlMain(streamUrlMain);  // ✅ 主码流URL
            newChannel.setStreamUrlSub(streamUrlSub);    // ✅ 子码流URL
            newChannel.setSnapshotUrl(snapshotUrl);      // ✅ 快照URL
            newChannel.setPtzSupport(ptzSupport);
            newChannel.setAudioSupport(audioSupport);
            newChannel.setResolution(resolution);
            newChannel.setOnlineStatus(channelInfo.getState());
            newChannel.setEnableStatus(1);
            newChannel.setLastSyncTime(LocalDateTime.now());
            // no-op
            log.info("[通道同步] 新增通道: nvrId={}, channelNo={}, name={}", nvrId, channelNo, insertName);
        }
    }

    /**
     * 同步NVR通道到 {@code ibms_channel}（legacy 字段写入 extra），供 NVR 视频链路读取。
     *
     * <p>相比 {@link #syncNvrChannel(Long, IotDeviceDO, IotDeviceDO)}，本方法不写入 `iot_device_channel`。</p>
     */
    private void syncNvrChannelToIbms(Long nvrId,
                                      IbmsDeviceDO ibmsNvr,
                                      IotDeviceDO nvrDevice,
                                      IotDeviceDO channelInfo) {
        // 从config中获取通道号和云台支持信息
        String configStr = null;
        if (channelInfo.getConfig() != null) {
            try {
                configStr = JsonUtils.toJsonString(channelInfo.getConfig().toMap());
            } catch (Exception e) {
                log.warn("[通道同步] 序列化config失败: {}", e.getMessage());
            }
        }
        if (configStr == null || configStr.isEmpty()) {
            return;
        }

        Integer channelNo;
        Boolean ptzSupport = false;
        Boolean audioSupport = false;
        String deviceType = null;
        String resolution = null;
        String sdkChannelName = null;

        try {
            cn.hutool.json.JSONObject cfg = cn.hutool.json.JSONUtil.parseObj(configStr);
            channelNo = cfg.getInt("channel");
            sdkChannelName = cfg.getStr("channelName");

            // 云台支持推断（逻辑与 syncNvrChannel 保持一致）
            if (cfg.containsKey("ptzSupport")) {
                Object ptzObj = cfg.get("ptzSupport");
                if (ptzObj != null) {
                    if (ptzObj instanceof Boolean) {
                        ptzSupport = (Boolean) ptzObj;
                    } else {
                        ptzSupport = Boolean.parseBoolean(String.valueOf(ptzObj));
                    }
                }
            }
            if (!ptzSupport && cfg.containsKey("deviceType")) {
                deviceType = cfg.getStr("deviceType");
                if (deviceType != null) {
                    ptzSupport = isPtzDevice(deviceType);
                }
            }
            if (!ptzSupport) {
                String deviceName = channelInfo.getDeviceName();
                if (deviceName != null) {
                    String nameLower = deviceName.toLowerCase();
                    if (nameLower.contains("ptz") || nameLower.contains("dome")
                            || nameLower.contains("球机") || nameLower.contains("球")
                            || nameLower.contains("ipc") || nameLower.contains("speed")) {
                        ptzSupport = true;
                    }
                }
            }
            if (!ptzSupport) {
                cn.hutool.json.JSONObject cfg2 = cn.hutool.json.JSONUtil.parseObj(configStr);
                String ipAddress = cfg2.getStr("ipAddress");
                if (ipAddress != null && isPtzByIpPattern(ipAddress, nvrId)) {
                    ptzSupport = true;
                }
            }

            audioSupport = cfg.getBool("audioSupport", false);
            resolution = cfg.getStr("resolution");
        } catch (Exception e) {
            log.warn("[通道同步] 解析通道配置失败: {}", e.getMessage());
            return;
        }

        if (channelNo == null) {
            return;
        }

        // 获取NVR配置信息（用于生成URL）
        String nvrIp = DeviceConfigHelper.getIpAddress(nvrDevice);
        String username = "admin";
        String password = "admin123";
        Integer rtspPort = 554;
        Integer httpPort = 80;

        if (nvrDevice.getConfig() != null) {
            try {
                Map<String, Object> nvrConfigMap = nvrDevice.getConfig().toMap();
                username = nvrConfigMap.get("username") != null ? nvrConfigMap.get("username").toString() : "admin";
                password = nvrConfigMap.get("password") != null ? nvrConfigMap.get("password").toString() : "admin123";
                rtspPort = nvrConfigMap.get("rtspPort") != null ? Integer.parseInt(nvrConfigMap.get("rtspPort").toString()) : 554;
                httpPort = nvrConfigMap.get("httpPort") != null ? Integer.parseInt(nvrConfigMap.get("httpPort").toString()) : 80;
            } catch (Exception ignored) {}
        }

        String streamUrlMain = generateStreamUrl(nvrIp, channelNo, "main", username, password, rtspPort);
        String streamUrlSub = generateStreamUrl(nvrIp, channelNo, "sub", username, password, rtspPort);
        String snapshotUrl = generateSnapshotUrl(nvrIp, channelNo, username, password, httpPort);

        boolean online = channelInfo.getState() != null && channelInfo.getState() == 1;
        String status = online ? "online" : "offline";

        // 查询是否已存在
        IbmsChannelDO existing = ibmsChannelMapper.selectOne(new LambdaQueryWrapperX<IbmsChannelDO>()
                .eq(IbmsChannelDO::getDeviceId, nvrId)
                .eq(IbmsChannelDO::getChannelNo, channelNo)
                .likeRight(IbmsChannelDO::getTypeCode, "VT"));

        String insertName = (sdkChannelName != null && !sdkChannelName.isEmpty())
                ? sdkChannelName : "通道" + channelNo;

        String nowStr = LocalDateTime.now().toString();

        cn.hutool.json.JSONObject newExtra = cn.hutool.json.JSONUtil.createObj();
        newExtra.set("enableStatus", 1);
        newExtra.set("isPatrol", 0);
        newExtra.set("isMonitor", 0);
        newExtra.set("sort", channelNo);
        newExtra.set("monitorPosition", Integer.MAX_VALUE);
        newExtra.set("ptzSupport", ptzSupport);
        newExtra.set("audioSupport", audioSupport);
        newExtra.set("resolution", resolution);

        newExtra.set("protocol", "RTSP");
        newExtra.set("username", username);
        newExtra.set("password", password);

        newExtra.set("streamUrlMain", streamUrlMain);
        newExtra.set("streamUrlSub", streamUrlSub);
        newExtra.set("snapshotUrl", snapshotUrl);

        newExtra.set("targetIp", DeviceConfigHelper.getIpAddress(channelInfo));
        newExtra.set("targetChannelNo", 1);

        String channelSubType = deviceType != null ? deviceType : "IPC";
        newExtra.set("channelSubType", channelSubType);

        newExtra.set("lastSyncTime", nowStr);

        if (existing == null) {
            String dc = StrUtil.blankToDefault(ibmsNvr.getDeviceCode(), "D" + nvrId);
            IbmsChannelDO channel = new IbmsChannelDO();
            channel.setId(null);
            channel.setDeviceId(nvrId);
            channel.setChannelNo(channelNo);
            channel.setName(insertName);
            channel.setCode(dc + "-VT" + String.format("%02d", channelNo));
            channel.setTypeCode("VT");
            channel.setSystemType(StrUtil.isNotBlank(ibmsNvr.getSystemCode()) ? ibmsNvr.getSystemCode() : "VI");
            channel.setBusiness(StrUtil.isNotBlank(ibmsNvr.getGroupCode()) ? ibmsNvr.getGroupCode().toLowerCase() : "security");
            channel.setCategory("视频通道");
            channel.setDataSource("NVR");
            channel.setCurrentValue("--");
            channel.setIp(nvrIp);
            channel.setStatus(status);
            channel.setExtra(newExtra.toString());
            ibmsChannelMapper.insert(channel);
            log.info("[通道同步] ✅ (IBMS) 新增NVR通道: nvrId={}, channelNo={}, name={}", nvrId, channelNo, insertName);
        } else {
            // 更新现有通道：尽量不覆盖用户配置（patrol/monitor 等）
            cn.hutool.json.JSONObject ex;
            if (StrUtil.isBlank(existing.getExtra())) {
                ex = cn.hutool.json.JSONUtil.createObj();
            } else {
                try {
                    ex = cn.hutool.json.JSONUtil.parseObj(existing.getExtra().trim());
                } catch (Exception e) {
                    ex = cn.hutool.json.JSONUtil.createObj();
                }
            }

            Integer existingEnable = ex.getInt("enableStatus");
            if (existingEnable == null || existingEnable == 0) {
                ex.set("enableStatus", 1);
            }

            // 保持 isPatrol/isMonitor/monitorPosition/sort（如已存在）
            if (ex.get("isPatrol") == null) ex.set("isPatrol", 0);
            if (ex.get("isMonitor") == null) ex.set("isMonitor", 0);
            if (ex.get("monitorPosition") == null) ex.set("monitorPosition", Integer.MAX_VALUE);
            if (ex.get("sort") == null) ex.set("sort", channelNo);

            ex.set("ptzSupport", ptzSupport);
            ex.set("audioSupport", audioSupport);
            ex.set("resolution", resolution);
            ex.set("protocol", "RTSP");
            ex.set("username", username);
            ex.set("password", password);
            ex.set("streamUrlMain", streamUrlMain);
            ex.set("streamUrlSub", streamUrlSub);
            ex.set("snapshotUrl", snapshotUrl);
            ex.set("targetIp", DeviceConfigHelper.getIpAddress(channelInfo));
            ex.set("targetChannelNo", 1);
            ex.set("channelSubType", deviceType != null ? deviceType : ex.getStr("channelSubType"));
            ex.set("lastSyncTime", nowStr);

            String existingName = existing.getName();
            boolean hasValidSdkName = sdkChannelName != null && !sdkChannelName.isEmpty()
                    && !sdkChannelName.matches("^通道\\d+$");
            String newName = hasValidSdkName ? sdkChannelName : existingName;

            IbmsChannelDO update = new IbmsChannelDO();
            update.setId(existing.getId());
            update.setName(newName);
            update.setStatus(status);
            update.setExtra(ex.toString());
            ibmsChannelMapper.updateById(update);
            log.info("[通道同步] 更新NVR通道(IBMS): nvrId={}, channelNo={}, name={}", nvrId, channelNo, newName);
        }
    }
    
    /**
     * 生成RTSP流地址
     * 大华格式：rtsp://admin:admin123@192.168.1.200:80/cam/realmonitor?channel=5&subtype=0
     * - channel: NVR 通道号（从 SDK 获取的通道号，已经是从 1 开始）
     * - subtype: 0=主码流, 1=辅码流1, 2=辅码流2
     */
    private String generateStreamUrl(String ip, Integer channelNo, String streamType, String username, String password, Integer rtspPort) {
        if (ip == null || channelNo == null) {
            return null;
        }
        // subtype: 0=主码流, 1=辅码流1, 2=辅码流2
        int rtspChannel = channelNo - 1;
        if (rtspChannel < 0) rtspChannel = 0;
        int subtype = "sub".equals(streamType) ? 1 : 0;
        // 大华 RTSP 使用 HTTP 端口（80），不是 RTSP 端口（554）
        int port = 80;
        return String.format("rtsp://%s:%s@%s:%d/cam/realmonitor?channel=%d&subtype=%d", 
                           username, password, ip, port, channelNo, subtype);
    }
    
    /**
     * 生成快照地址
     * 大华格式：http://admin:admin123@192.168.1.200/cgi-bin/snapshot.cgi?channel=5
     */
    private String generateSnapshotUrl(String ip, Integer channelNo, String username, String password, Integer httpPort) {
        if (ip == null || channelNo == null) {
            return null;
        }
        // channelNo 已经是从 1 开始的，直接使用
        if (httpPort == 80) {
            return String.format("http://%s:%s@%s/cgi-bin/snapshot.cgi?channel=%d", username, password, ip, channelNo);
        }
        return String.format("http://%s:%s@%s:%d/cgi-bin/snapshot.cgi?channel=%d", username, password, ip, httpPort, channelNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchEnableChannels(List<Long> channelIds) {
        channelIds.forEach(id -> {
            IbmsChannelDO row = ibmsChannelMapper.selectById(id);
            if (row != null && StrUtil.isNotBlank(row.getTypeCode()) && row.getTypeCode().toUpperCase().startsWith("VT")) {
                cn.hutool.json.JSONObject ex = StrUtil.isBlank(row.getExtra())
                        ? cn.hutool.json.JSONUtil.createObj()
                        : cn.hutool.json.JSONUtil.parseObj(row.getExtra());
                ex.set("enableStatus", 1);
                IbmsChannelDO update = new IbmsChannelDO();
                update.setId(id);
                update.setExtra(ex.toString());
                ibmsChannelMapper.updateById(update);
                return;
            }

            // G4 清理 iot_ 持久层依赖：非 VT 通道不再写入 iot_device_channel
        });
        log.info("[通道管理] 批量启用通道: count={}", channelIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDisableChannels(List<Long> channelIds) {
        channelIds.forEach(id -> {
            IbmsChannelDO row = ibmsChannelMapper.selectById(id);
            if (row != null && StrUtil.isNotBlank(row.getTypeCode()) && row.getTypeCode().toUpperCase().startsWith("VT")) {
                cn.hutool.json.JSONObject ex = StrUtil.isBlank(row.getExtra())
                        ? cn.hutool.json.JSONUtil.createObj()
                        : cn.hutool.json.JSONUtil.parseObj(row.getExtra());
                ex.set("enableStatus", 0);
                IbmsChannelDO update = new IbmsChannelDO();
                update.setId(id);
                update.setExtra(ex.toString());
                ibmsChannelMapper.updateById(update);
                return;
            }

            // G4 清理 iot_ 持久层依赖：非 VT 通道不再写入 iot_device_channel
        });
        log.info("[通道管理] 批量禁用通道: count={}", channelIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChannelConfig(Long channelId, java.util.Map<String, Object> config) {
        if (channelId == null) {
            return;
        }

        // 优先处理 IBMS 门禁/访问点位（DR/DR-READER）
        IbmsChannelDO ibmsChannel = ibmsChannelMapper.selectById(channelId);
        if (ibmsChannel != null && StrUtil.isNotBlank(ibmsChannel.getTypeCode())) {
            String tc = ibmsChannel.getTypeCode();
            if ("DR".equalsIgnoreCase(tc) || "DR-READER".equalsIgnoreCase(tc)) {
                cn.hutool.json.JSONObject ex = StrUtil.isBlank(ibmsChannel.getExtra())
                        ? cn.hutool.json.JSONUtil.createObj()
                        : cn.hutool.json.JSONUtil.parseObj(ibmsChannel.getExtra().trim());

                java.util.Map<String, Object> existingConfig = normalizeToMap(ex.get("config"));
                if (existingConfig == null) {
                    existingConfig = new java.util.HashMap<>();
                }
                if (config != null) {
                    existingConfig.putAll(config);
                }

                ex.set("config", existingConfig);

                IbmsChannelDO update = new IbmsChannelDO();
                update.setId(channelId);
                update.setExtra(ex.toString());
                ibmsChannelMapper.updateById(update);

                log.info("[通道管理] 更新 IBMS 门禁通道配置: channelId={}, config={}", channelId, config);
                return;
            }
        }

        // G4 清理 iot_ 持久层依赖：非门禁点位配置更新已废弃
        log.warn("[通道管理] 更新通道配置已废弃（仅 IBMS 门禁通道支持）：channelId={}, config={}", channelId, config);
        return;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSetPatrol(List<Long> channelIds, Boolean isPatrol) {
        int v = isPatrol != null && isPatrol ? 1 : 0;
        channelIds.forEach(id -> {
            IbmsChannelDO row = ibmsChannelMapper.selectById(id);
            if (row != null && StrUtil.isNotBlank(row.getTypeCode()) && row.getTypeCode().toUpperCase().startsWith("VT")) {
                cn.hutool.json.JSONObject ex = StrUtil.isBlank(row.getExtra())
                        ? cn.hutool.json.JSONUtil.createObj()
                        : cn.hutool.json.JSONUtil.parseObj(row.getExtra());
                ex.set("isPatrol", v);
                IbmsChannelDO update = new IbmsChannelDO();
                update.setId(id);
                update.setExtra(ex.toString());
                ibmsChannelMapper.updateById(update);
                return;
            }

            // G4 清理 iot_ 持久层依赖：非 VT 通道不再支持巡更配置更新
        });
        log.info("[通道管理] 批量设置巡更: count={}, isPatrol={}", channelIds.size(), isPatrol);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSetMonitor(List<Long> channelIds, Boolean isMonitor) {
        int v = isMonitor != null && isMonitor ? 1 : 0;
        channelIds.forEach(id -> {
            IbmsChannelDO row = ibmsChannelMapper.selectById(id);
            if (row != null && StrUtil.isNotBlank(row.getTypeCode()) && row.getTypeCode().toUpperCase().startsWith("VT")) {
                cn.hutool.json.JSONObject ex = StrUtil.isBlank(row.getExtra())
                        ? cn.hutool.json.JSONUtil.createObj()
                        : cn.hutool.json.JSONUtil.parseObj(row.getExtra());
                ex.set("isMonitor", v);
                IbmsChannelDO update = new IbmsChannelDO();
                update.setId(id);
                update.setExtra(ex.toString());
                ibmsChannelMapper.updateById(update);
                return;
            }

            // G4 清理 iot_ 持久层依赖：非 VT 通道不再支持监控墙配置更新
        });
        log.info("[通道管理] 批量设置监控墙: count={}, isMonitor={}", channelIds.size(), isMonitor);
    }

    @Override
    public void batchAssignSpatial(List<Long> channelIds, Long campusId, Long buildingId, Long floorId, Long areaId) {
        if (channelIds == null || channelIds.isEmpty()) {
            return;
        }

        // 兼容：VT 视频通道走 ibms_channel；其余（如 IPC/门禁）仍走 iot_device_channel（后续 B5/ G4 收口时再迁移）
        List<Long> ibmsChannelIds = new ArrayList<>();
        List<Long> iotChannelIds = new ArrayList<>();
        for (Long id : channelIds) {
            IbmsChannelDO row = ibmsChannelMapper.selectById(id);
            if (row != null && StrUtil.isNotBlank(row.getTypeCode()) && row.getTypeCode().toUpperCase().startsWith("VT")) {
                ibmsChannelIds.add(id);
            } else {
                iotChannelIds.add(id);
            }
        }

        if (!ibmsChannelIds.isEmpty() && ibmsChannelService != null) {
            // 让 IBMS 侧负责 extra.gis* 和 spaceId 映射
            ibmsChannelService.batchAssignSpatial(ibmsChannelIds, campusId, buildingId, floorId, areaId);
        }

        if (!iotChannelIds.isEmpty()) {
            // G4 清理 iot_ 持久层依赖：非 VT 通道空间指派已废弃
            log.warn("[通道管理] 非 VT 通道空间指派已废弃（当前跳过）：iotCount={}, campusId={}, buildingId={}, floorId={}, areaId={}",
                    iotChannelIds.size(), campusId, buildingId, floorId, areaId);
        }

        log.info("[通道管理] 批量指派空间: count={}, campusId={}, buildingId={}, floorId={}, ibmsCount={}, iotCount={}",
                channelIds.size(), campusId, buildingId, floorId, ibmsChannelIds.size(), iotChannelIds.size());
    }
    
    /**
     * 在事务中批量指派空间（仅更新MySQL中的通道表）
     */
    /**
     * 批量分配空间（内部事务方法）
     * 注意：private 方法的 @Transactional 不会生效，事务由调用方 batchAssignSpatial 控制
     */
    private void batchAssignSpatialInTransaction(List<Long> channelIds, Long buildingId, Long floorId, Long areaId, String location) {
        // G4 清理 iot_ 持久层依赖：非 VT/IPC/门禁通道的空间指派已废弃
        // （IBMS 侧由 ibms_channel.extra + ibms_space.extra 映射负责）
    }

    // ========== 多屏预览专用 ==========

    @Override
    public List<IotDeviceChannelDO> getNvrChannelsWithAutoSync(Long deviceId, Boolean forceSync) {
        IbmsDeviceDO ibmsDevice = ibmsDeviceMapper.selectById(deviceId);
        if (ibmsDevice != null) {
            // 1) 查询 IBMS 中的视频通道
            List<IotDeviceChannelDO> channels = ibmsChannelMapper.selectListByDeviceId(deviceId).stream()
                    .filter(ch -> StrUtil.isNotBlank(ch.getTypeCode()) && ch.getTypeCode().toUpperCase().startsWith("VT"))
                    .map(ch -> convertIbmsVideoChannelToLegacy(ch, ibmsDevice))
                    .collect(Collectors.toList());

            // 2) 如果没有数据或强制同步，则同步（NVR 分支会落库到 ibms_channel）
            if (channels.isEmpty() || Boolean.TRUE.equals(forceSync)) {
                log.info("[NVR通道] 自动同步: deviceId={}, forceSync={}", deviceId, forceSync);
                syncDeviceChannels(deviceId);
                channels = ibmsChannelMapper.selectListByDeviceId(deviceId).stream()
                        .filter(ch -> StrUtil.isNotBlank(ch.getTypeCode()) && ch.getTypeCode().toUpperCase().startsWith("VT"))
                        .map(ch -> convertIbmsVideoChannelToLegacy(ch, ibmsDevice))
                        .collect(Collectors.toList());
            }
            if (!channels.isEmpty()) {
                return channels;
            }
        }

        // G4 清理 iot_ 持久层依赖：仅返回 ibms_channel（NVR/VT 视频通道）
        return List.of();
    }

    @Override
    public List<NvrWithChannelsRespVO> getAllNvrsWithChannels() {
        // TODO: 实现获取所有NVR及其通道的逻辑
        // 需要在 IotDeviceService 中添加 getDevicesByType 方法
        log.warn("[getAllNvrsWithChannels] 方法待实现");
        return new ArrayList<>();
    }

    @Override
    public SyncResult batchSyncAllNvrChannels() {
        SyncResult result = new SyncResult();
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 获取所有 NVR 设备
            List<IbmsDeviceDO> nvrList = nvrQueryService.getNvrList();
            if (nvrList == null || nvrList.isEmpty()) {
                log.info("[批量同步NVR通道] 没有找到 NVR 设备");
                result.setNvrCount(0);
                result.setDuration(System.currentTimeMillis() - startTime);
                return result;
            }
            
            log.info("[批量同步NVR通道] 开始同步，共 {} 台 NVR", nvrList.size());
            
            int successCount = 0;
            int failCount = 0;
            
            // 2. 对每个 NVR 发送通道同步命令
            for (IbmsDeviceDO nvr : nvrList) {
                try {
                    Integer st = IbmsDeviceLedgerRuntimeHelper.resolveDeviceState(nvr,
                            ibmsDeviceRuntimeService.getByDeviceId(nvr.getId()));
                    // 只对在线的 NVR 发送同步命令
                    if (st != null && st == 1) {
                        // 调用 NvrQueryService 发送通道查询命令到 newgateway
                        // 结果会通过 DeviceServiceResultConsumer 异步处理并更新数据库
                        nvrQueryService.refreshChannelsByNvrId(nvr.getId());
                        successCount++;
                        log.info("[批量同步NVR通道] NVR {} ({}) 同步命令已发送", nvr.getId(), nvr.getName());
                    } else {
                        log.info("[批量同步NVR通道] NVR {} ({}) 不在线，跳过", nvr.getId(), nvr.getName());
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("[批量同步NVR通道] NVR {} 同步失败: {}", nvr.getId(), e.getMessage());
                }
            }
            
            result.setNvrCount(nvrList.size());
            result.setSuccessCount(successCount);
            result.setFailCount(failCount);
            result.setDuration(System.currentTimeMillis() - startTime);
            
            log.info("[批量同步NVR通道] 同步完成: 总数={}, 成功={}, 失败={}, 耗时={}ms",
                    nvrList.size(), successCount, failCount, result.getDuration());
            
        } catch (Exception e) {
            log.error("[批量同步NVR通道] 批量同步异常", e);
            result.setDuration(System.currentTimeMillis() - startTime);
        }
        
        return result;
    }

    /**
     * 根据设备类型判断是否支持云台
     * @param deviceType 设备类型
     * @return true=支持云台, false=不支持
     */
    private boolean isPtzDevice(String deviceType) {
        if (deviceType == null || deviceType.isEmpty()) {
            return false;
        }
        
        // 支持云台的设备类型
        String type = deviceType.toUpperCase();
        return type.contains("PTZ") ||          // 云台类型
               type.contains("DOME") ||         // 球机
               type.contains("SPEED_DOME") ||   // 快球
               type.contains("BALL") ||         // 球机（中文拼音）
               type.contains("IPC") ||          // 网络摄像机（可能支持 PTZ）
               type.contains("球机") ||          // 球机中文
               type.contains("球");              // 球
    }
    
    /**
     * 通过 IP 地址判断是否是 PTZ 设备
     * 说明：如果同一个 IP 有多个通道（如球机的可见光和热成像），通常表示是 PTZ 设备
     * @param targetIp 目标 IP
     * @param deviceId 设备 ID
     * @return true=可能是 PTZ 设备
     */
    private boolean isPtzByIpPattern(String targetIp, Long deviceId) {
        if (targetIp == null || targetIp.isEmpty() || deviceId == null) {
            return false;
        }
        
        // 查询同一 IP 下的通道数量
        List<IbmsChannelDO> channels = ibmsChannelMapper.selectListByDeviceId(deviceId);
        long sameIpCount = channels.stream()
                .map(ch -> {
                    String extra = ch.getExtra();
                    if (StrUtil.isBlank(extra)) {
                        return null;
                    }
                    try {
                        cn.hutool.json.JSONObject jo = cn.hutool.json.JSONUtil.parseObj(extra);
                        return jo.getStr("targetIp");
                    } catch (Exception ignore) {
                        return null;
                    }
                })
                .filter(ip -> targetIp.equals(ip))
                .count();
        
        // 如果同一 IP 有多个通道，可能是球机（如可见光+热成像）
        return sameIpCount > 1;
    }

    /**
     * 生成同步批次ID
     */
    private String generateSyncBatchId() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return "SYNC-" + LocalDateTime.now().format(formatter) + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 保存通道历史记录
     */
    private void saveChannelHistory(IotDeviceChannelDO channel, String operation, String operationDesc, String syncBatchId) {
        try {
            IotDeviceChannelHistoryDO history = new IotDeviceChannelHistoryDO();
            history.setChannelId(channel.getId());
            history.setDeviceId(channel.getDeviceId());
            history.setChannelNo(channel.getChannelNo());
            history.setOperation(operation);
            history.setOperationDesc(operationDesc);
            history.setChannelData(JsonUtils.toJsonString(channel));
            history.setOperator("SYSTEM");
            history.setOperateTime(LocalDateTime.now());
            history.setSyncSource("SDK");
            history.setSyncBatchId(syncBatchId);
            
            channelHistoryMapper.insert(history);
            log.debug("[通道历史] 记录操作: operation={}, channelId={}, channelNo={}", 
                     operation, channel.getId(), channel.getChannelNo());
        } catch (Exception e) {
            log.error("[通道历史] 保存历史记录失败: channelId={}, operation={}", channel.getId(), operation, e);
        }
    }

    // ========== 门禁通道同步 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccessChannelSyncResult syncAccessChannels(Long deviceId, List<AccessChannelSyncInfo> channelList) {
        log.info("[门禁通道同步] 开始同步: deviceId={}, channelCount={}", deviceId, 
                channelList != null ? channelList.size() : 0);
        
        // 1. 参数校验
        if (deviceId == null) {
            return AccessChannelSyncResult.failure("设备ID不能为空");
        }
        
        // 2. 校验设备
        IbmsDeviceDO ibmsAccess = ibmsDeviceMapper.selectById(deviceId);
        if (ibmsAccess == null) {
            return AccessChannelSyncResult.failure("设备不存在: deviceId=" + deviceId);
        }
        
        // 3. 处理空通道列表（设备未配置通道的情况）
        if (channelList == null) {
            channelList = new ArrayList<>();
        }
        
        // 4. 查询数据库中现有的门禁通道（仅 DR/DR-READER）
        List<IbmsChannelDO> existingChannels = ibmsChannelMapper.selectListByDeviceId(deviceId).stream()
                .filter(ch -> StrUtil.isNotBlank(ch.getTypeCode())
                        && ("DR".equalsIgnoreCase(ch.getTypeCode()) || "DR-READER".equalsIgnoreCase(ch.getTypeCode())))
                .collect(Collectors.toList());
        Map<Integer, IbmsChannelDO> existingChannelMap = existingChannels.stream()
                .collect(Collectors.toMap(IbmsChannelDO::getChannelNo, c -> c, (a, b) -> a));
        
        // 5. 收集设备返回的通道号
        Set<Integer> deviceChannelNos = channelList.stream()
                .filter(c -> c.getChannelNo() != null)
                .map(AccessChannelSyncInfo::getChannelNo)
                .collect(Collectors.toSet());
        
        // 6. 执行同步
        int insertedCount = 0;
        int updatedCount = 0;
        int deletedCount = 0;
        LocalDateTime syncTime = LocalDateTime.now();
        
        // 6.1 新增或更新通道
        for (AccessChannelSyncInfo channelInfo : channelList) {
            if (channelInfo.getChannelNo() == null) {
                log.warn("[门禁通道同步] 跳过无效通道（通道号为空）: deviceId={}", deviceId);
                continue;
            }
            
            IbmsChannelDO existingChannel = existingChannelMap.get(channelInfo.getChannelNo());
            
            if (existingChannel != null) {
                // 更新现有通道
                updateAccessChannel(existingChannel, ibmsAccess, channelInfo, syncTime);
                updatedCount++;
                log.debug("[门禁通道同步] 更新通道: deviceId={}, channelNo={}, name={}", 
                        deviceId, channelInfo.getChannelNo(), channelInfo.getChannelName());
            } else {
                // 新增通道
                createAccessChannel(deviceId, ibmsAccess, channelInfo, syncTime);
                insertedCount++;
                log.debug("[门禁通道同步] 新增通道: deviceId={}, channelNo={}, name={}", 
                        deviceId, channelInfo.getChannelNo(), channelInfo.getChannelName());
            }
        }
        
        // 6.2 软删除设备上不存在的通道
        for (IbmsChannelDO existingChannel : existingChannels) {
            if (!deviceChannelNos.contains(existingChannel.getChannelNo())) {
                // 软删除：标记为离线状态并禁用
                softDeleteAccessChannel(existingChannel, syncTime);
                deletedCount++;
                log.info("[门禁通道同步] 软删除通道: deviceId={}, channelNo={}, name={}", 
                        deviceId, existingChannel.getChannelNo(), existingChannel.getName());
            }
        }

        if (insertedCount + deletedCount > 0) {
            updateIbmsDevicePointCount(deviceId);
        }
        
        log.info("[门禁通道同步] 同步完成: deviceId={}, inserted={}, updated={}, deleted={}", 
                deviceId, insertedCount, updatedCount, deletedCount);
        
        return AccessChannelSyncResult.success(insertedCount, updatedCount, deletedCount);
    }

    // ========== NVR 通道同步（来自 newgateway） ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccessChannelSyncResult syncNvrChannels(Long deviceId, List<NvrChannelSyncInfo> channelList) {
        log.info("[NVR通道同步] 开始同步: deviceId={}, channelCount={}",
                deviceId, channelList != null ? channelList.size() : 0);

        // 1. 参数校验
        if (deviceId == null) {
            return AccessChannelSyncResult.failure("设备ID不能为空");
        }

        // 2. 校验设备（IoT 全量或 IBMS NVR 壳）
        IbmsDeviceDO ibmsNvr = ibmsDeviceMapper.selectById(deviceId);
        IotDeviceDO nvrDevice;
        if (ibmsNvr == null) {
            return AccessChannelSyncResult.failure("设备不存在: deviceId=" + deviceId);
        }
        nvrDevice = IbmsDeviceLedgerRuntimeHelper.buildLegacyNvrDeviceShell(ibmsNvr,
                ibmsDeviceRuntimeService.getByDeviceId(deviceId));
        if (nvrDevice == null) {
            return AccessChannelSyncResult.failure("设备不存在: deviceId=" + deviceId);
        }

        if (channelList == null) {
            channelList = new ArrayList<>();
        }

        // 3. 查询数据库中现有的通道（IBMS 视频通道）
        List<IbmsChannelDO> existingChannels = ibmsChannelMapper.selectListByDeviceId(deviceId).stream()
                .filter(ch -> StrUtil.isNotBlank(ch.getTypeCode()) && ch.getTypeCode().toUpperCase().startsWith("VT"))
                .collect(Collectors.toList());
        Map<Integer, IbmsChannelDO> existingChannelMap = existingChannels.stream()
                .collect(Collectors.toMap(IbmsChannelDO::getChannelNo, c -> c, (a, b) -> a));

        // 4. 收集设备返回的通道号
        Set<Integer> deviceChannelNos = channelList.stream()
                .filter(c -> c.getChannelNo() != null)
                .map(NvrChannelSyncInfo::getChannelNo)
                .collect(Collectors.toSet());

        int insertedCount = 0;
        int updatedCount = 0;
        int deletedCount = 0;

        // 5. 新增或更新通道（复用现有的 syncNvrChannel 逻辑生成 URL/能力字段）
        for (NvrChannelSyncInfo channelInfo : channelList) {
            Integer channelNo = channelInfo.getChannelNo();
            if (channelNo == null) {
                log.warn("[NVR通道同步] 跳过无效通道（通道号为空）: deviceId={}", deviceId);
                continue;
            }

            boolean exists = existingChannelMap.containsKey(channelNo);

            IotDeviceDO channelDevice = new IotDeviceDO();
            channelDevice.setDeviceName(StrUtil.blankToDefault(channelInfo.getChannelName(), "通道" + channelNo));
            channelDevice.setState(Boolean.TRUE.equals(channelInfo.getOnline()) ? 1 : 2); // 1=在线,2=离线（与既有逻辑保持一致）

            GenericDeviceConfig cfg = new GenericDeviceConfig();
            cfg.set("channel", channelNo);
            cfg.set("channelName", channelInfo.getChannelName());
            cfg.set("online", channelInfo.getOnline());
            cfg.set("recording", channelInfo.getRecording());
            if (channelInfo.getCapabilities() != null) {
                cfg.set("capabilities", channelInfo.getCapabilities());
                Object ipObj = channelInfo.getCapabilities().getOrDefault("ipAddress", channelInfo.getCapabilities().get("ip"));
                if (ipObj != null) {
                    cfg.set("ipAddress", String.valueOf(ipObj));
                }
                Object ptzObj = channelInfo.getCapabilities().get("ptzSupport");
                if (ptzObj != null) {
                    cfg.set("ptzSupport", ptzObj);
                }
                Object devTypeObj = channelInfo.getCapabilities().get("deviceType");
                if (devTypeObj != null) {
                    cfg.set("deviceType", devTypeObj);
                }
            }
            channelDevice.setConfig(cfg);

            syncNvrChannelToIbms(deviceId, ibmsNvr, nvrDevice, channelDevice);

            if (exists) {
                updatedCount++;
            } else {
                insertedCount++;
            }
        }

        // 6. 删除设备上不存在的通道（硬删除）
        for (IbmsChannelDO existingChannel : existingChannels) {
            if (!deviceChannelNos.contains(existingChannel.getChannelNo())) {
                ibmsChannelMapper.deleteById(existingChannel.getId());
                deletedCount++;
                log.info("[NVR通道同步] 删除通道: deviceId={}, channelNo={}, name={}",
                        deviceId, existingChannel.getChannelNo(), existingChannel.getName());
            }
        }

        log.info("[NVR通道同步] 同步完成: deviceId={}, inserted={}, updated={}, deleted={}",
                deviceId, insertedCount, updatedCount, deletedCount);
        return AccessChannelSyncResult.success(insertedCount, updatedCount, deletedCount);
    }

    /**
     * 创建门禁通道
     */
    private void createAccessChannel(Long deviceId, IbmsDeviceDO ibmsAccess,
                                      AccessChannelSyncInfo channelInfo, LocalDateTime syncTime) {
        IbmsChannelDO channel = new IbmsChannelDO();
        channel.setDeviceId(deviceId);
        channel.setChannelNo(channelInfo.getChannelNo());

        String channelName = StrUtil.blankToDefault(channelInfo.getChannelName(),
                "门" + channelInfo.getChannelNo());
        channel.setName(channelName);

        channel.setBusiness("access");
        channel.setTypeCode("DR");
        channel.setCategory("门禁通行");
        channel.setSystemType(StrUtil.blankToDefault(ibmsAccess.getSystemCode(), "AC"));
        channel.setDataSource(StrUtil.blankToDefault(ibmsAccess.getProtocol(), "CTR"));

        channel.setIp(ibmsAccess.getIp());
        channel.setDeviceSn(ibmsAccess.getSn());
        channel.setDeviceName(ibmsAccess.getName());

        boolean online = channelInfo.getStatus() != null && channelInfo.getStatus() == 1;
        channel.setStatus(online ? "armed" : "offline");
        channel.setCurrentValue(online ? "0" : "--");

        // 只要能唯一标识通道即可（迁移后的历史 code 会被更新覆盖）
        String baseDeviceCode = StrUtil.blankToDefault(ibmsAccess.getDeviceCode(), "AC-" + deviceId);
        channel.setCode(baseDeviceCode + "-DR-" + String.format("%03d", channelInfo.getChannelNo()));

        cn.hutool.json.JSONObject ex = cn.hutool.json.JSONUtil.createObj();
        ex.set("enableStatus", 1);
        ex.set("doorName", channelName);
        ex.set("channelSubType", "DOOR");
        ex.set("config", cn.hutool.json.JSONUtil.createObj());
        ex.set("capabilities", channelInfo.getCapabilities());
        ex.set("lastSyncTime", syncTime.toString());
        channel.setExtra(ex.toString());

        ibmsChannelMapper.insert(channel);
    }

    /**
     * 更新门禁通道
     */
    private void updateAccessChannel(IbmsChannelDO existingChannel,
                                      IbmsDeviceDO ibmsAccess,
                                      AccessChannelSyncInfo channelInfo,
                                      LocalDateTime syncTime) {
        if (existingChannel == null || existingChannel.getId() == null) {
            return;
        }

        String channelName = StrUtil.blankToDefault(channelInfo.getChannelName(), existingChannel.getName());

        boolean online = channelInfo.getStatus() != null && channelInfo.getStatus() == 1;
        String status = online ? "armed" : "offline";
        String currentValue = online ? "0" : "--";

        cn.hutool.json.JSONObject ex;
        if (StrUtil.isBlank(existingChannel.getExtra())) {
            ex = cn.hutool.json.JSONUtil.createObj();
        } else {
            try {
                ex = cn.hutool.json.JSONUtil.parseObj(existingChannel.getExtra().trim());
            } catch (Exception e) {
                ex = cn.hutool.json.JSONUtil.createObj();
            }
        }

        // 不覆盖 config，避免门模式/常开常闭配置丢失
        ex.set("doorName", channelName);
        ex.set("capabilities", channelInfo.getCapabilities());
        ex.set("lastSyncTime", syncTime.toString());
        ex.set("enableStatus", 1);

        IbmsChannelDO update = new IbmsChannelDO();
        update.setId(existingChannel.getId());
        update.setName(channelName);
        update.setStatus(status);
        update.setCurrentValue(currentValue);
        update.setExtra(ex.toString());
        ibmsChannelMapper.updateById(update);
    }

    /**
     * 软删除门禁通道
     * <p>
     * 软删除策略：
     * <ul>
     *     <li>将通道状态设置为离线(0)</li>
     *     <li>记录删除历史</li>
     *     <li>保留通道记录以便追溯</li>
     * </ul>
     */
    private void softDeleteAccessChannel(IbmsChannelDO channel, LocalDateTime syncTime) {
        if (channel == null || channel.getId() == null) {
            return;
        }

        cn.hutool.json.JSONObject ex;
        if (StrUtil.isBlank(channel.getExtra())) {
            ex = cn.hutool.json.JSONUtil.createObj();
        } else {
            try {
                ex = cn.hutool.json.JSONUtil.parseObj(channel.getExtra().trim());
            } catch (Exception e) {
                ex = cn.hutool.json.JSONUtil.createObj();
            }
        }

        ex.set("enableStatus", 0);
        ex.set("lastSyncTime", syncTime.toString());

        IbmsChannelDO update = new IbmsChannelDO();
        update.setId(channel.getId());
        update.setStatus("offline");
        update.setCurrentValue("--");
        update.setExtra(ex.toString());
        ibmsChannelMapper.updateById(update);
    }
}