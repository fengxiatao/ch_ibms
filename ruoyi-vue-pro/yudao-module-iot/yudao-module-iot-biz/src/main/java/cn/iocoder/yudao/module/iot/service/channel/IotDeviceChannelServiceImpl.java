package cn.iocoder.yudao.module.iot.service.channel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.controller.admin.channel.vo.IotDeviceChannelPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.channel.vo.IotDeviceChannelSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.channel.vo.NvrWithChannelsRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelHistoryDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfigHelper;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.GenericDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.mysql.channel.IotDeviceChannelHistoryMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.channel.IotDeviceChannelMapper;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.video.nvr.NvrQueryService;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.CampusMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.BuildingMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.FloorMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.AreaMapper;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.CampusDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.BuildingDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.FloorDO;
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
    private IotDeviceChannelMapper channelMapper;

    @Resource
    private IotDeviceChannelHistoryMapper channelHistoryMapper;

    @Resource
    private IotDeviceService deviceService;

    @Resource
    private NvrQueryService nvrQueryService;
    @Resource
    private CampusMapper campusMapper;
    @Resource
    private BuildingMapper buildingMapper;
    @Resource
    private FloorMapper floorMapper;
    @Resource
    private AreaMapper areaMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createChannel(IotDeviceChannelSaveReqVO createReqVO) {
        // 1. 校验设备存在
        IotDeviceDO device = deviceService.getDevice(createReqVO.getDeviceId());
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }

        // 2. 校验通道号唯一性
        if (channelMapper.existsByDeviceIdAndChannelNo(createReqVO.getDeviceId(), createReqVO.getChannelNo(), null)) {
            throw exception(CHANNEL_NO_EXISTS);
        }

        // 3. 创建通道
        IotDeviceChannelDO channel = new IotDeviceChannelDO();
        BeanUtils.copyProperties(createReqVO, channel);
        channel.setOnlineStatus(0); // 默认离线
        channel.setEnableStatus(1); // 默认启用
        channel.setAlarmStatus(0); // 默认正常
        channelMapper.insert(channel);

        log.info("[通道管理] 创建通道成功: id={}, deviceId={}, channelNo={}, channelName={}",
                channel.getId(), channel.getDeviceId(), channel.getChannelNo(), channel.getChannelName());
        return channel.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChannel(IotDeviceChannelSaveReqVO updateReqVO) {
        // 1. 校验通道存在
        IotDeviceChannelDO channel = channelMapper.selectById(updateReqVO.getId());
        if (channel == null) {
            throw exception(CHANNEL_NOT_EXISTS);
        }

        // 2. 校验通道号唯一性
        if (channelMapper.existsByDeviceIdAndChannelNo(updateReqVO.getDeviceId(), updateReqVO.getChannelNo(), updateReqVO.getId())) {
            throw exception(CHANNEL_NO_EXISTS);
        }

        // 3. 更新通道
        IotDeviceChannelDO updateObj = new IotDeviceChannelDO();
        BeanUtils.copyProperties(updateReqVO, updateObj);
        channelMapper.updateById(updateObj);

        log.info("[通道管理] 更新通道成功: id={}, channelName={}", updateObj.getId(), updateObj.getChannelName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChannel(Long id) {
        // 1. 校验通道存在
        IotDeviceChannelDO channel = channelMapper.selectById(id);
        if (channel == null) {
            throw exception(CHANNEL_NOT_EXISTS);
        }

        // 2. 删除通道
        channelMapper.deleteById(id);

        log.info("[通道管理] 删除通道成功: id={}, channelName={}", id, channel.getChannelName());
    }

    @Override
    public IotDeviceChannelDO getChannel(Long id) {
        return channelMapper.selectById(id);
    }

    @Override
    public PageResult<IotDeviceChannelDO> getChannelPage(IotDeviceChannelPageReqVO pageReqVO) {
        return channelMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotDeviceChannelDO> getChannelsByDeviceId(Long deviceId) {
        return channelMapper.selectListByDeviceId(deviceId);
    }

    @Override
    public IotDeviceChannelDO getChannelByDeviceIdAndChannelNo(Long deviceId, Integer channelNo) {
        return channelMapper.selectOne(new LambdaQueryWrapperX<IotDeviceChannelDO>()
                .eq(IotDeviceChannelDO::getDeviceId, deviceId)
                .eq(IotDeviceChannelDO::getChannelNo, channelNo));
    }

    @Override
    public List<IotDeviceChannelDO> getVideoChannels(String deviceType, Integer onlineStatus, Boolean isPatrol, Boolean isMonitor) {
        return channelMapper.selectVideoChannels(deviceType, onlineStatus, isPatrol, isMonitor);
    }

    @Override
    public List<IotDeviceChannelDO> getPatrolChannels() {
        return channelMapper.selectPatrolChannels();
    }

    @Override
    public List<IotDeviceChannelDO> getMonitorChannels() {
        return channelMapper.selectMonitorChannels();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer syncDeviceChannels(Long deviceId) {
        // 1. 校验设备存在
        IotDeviceDO device = deviceService.getDevice(deviceId);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }

        // 2. 根据设备类型同步通道
        int syncCount = 0;
        if ("NVR".equals(device.getDeviceType()) || device.getProductId() == 4L) {
            // NVR设备：从SDK获取通道列表
            if (nvrQueryService != null) {
                List<IotDeviceDO> sdkChannels = nvrQueryService.refreshChannelsByNvrId(deviceId);
                
                // 获取数据库中现有的通道
                List<IotDeviceChannelDO> dbChannels = channelMapper.selectListByDeviceId(deviceId);
                
                // 提取SDK返回的通道号列表
                java.util.Set<Integer> sdkChannelNos = new java.util.HashSet<>();
                for (IotDeviceDO ch : sdkChannels) {
                    try {
                        // 从 DeviceConfig 接口获取配置
                        if (ch.getConfig() != null) {
                            Map<String, Object> configMap = ch.getConfig().toMap();
                            Object channelNoObj = configMap.get("channel");
                            if (channelNoObj != null) {
                                Integer channelNo = channelNoObj instanceof Integer ? 
                                    (Integer) channelNoObj : Integer.parseInt(channelNoObj.toString());
                                sdkChannelNos.add(channelNo);
                            }
                        }
                    } catch (Exception ignored) {}
                }
                
                // 同步通道（新增或更新）
                for (IotDeviceDO ch : sdkChannels) {
                    syncNvrChannel(deviceId, device, ch);
                    syncCount++;
                }
                
                // 删除SDK中不存在的通道（硬删除 + 记录历史）
                String syncBatchId = generateSyncBatchId();
                int deletedCount = 0;
                for (IotDeviceChannelDO dbChannel : dbChannels) {
                    if (!sdkChannelNos.contains(dbChannel.getChannelNo())) {
                        // 记录删除历史
                        saveChannelHistory(dbChannel, "DELETE", "SDK同步时删除不存在的通道", syncBatchId);
                        
                        // 硬删除
                        channelMapper.deleteById(dbChannel.getId());
                        deletedCount++;
                        log.info("[通道同步] 删除通道: deviceId={}, channelNo={}, name={}", 
                                deviceId, dbChannel.getChannelNo(), dbChannel.getChannelName());
                    }
                }
                
                if (deletedCount > 0) {
                    log.info("[通道同步] 删除了 {} 个不存在的通道", deletedCount);
                }
            }
        } else if ("IPC".equals(device.getDeviceType())) {
            // 球机或普通IPC：通过 ONVIF 查询设备通道数
            syncCount = syncIpcChannelsViaOnvif(deviceId, device);
        }

        log.info("[通道管理] 同步设备通道完成: deviceId={}, syncCount={}", deviceId, syncCount);
        return syncCount;
    }
    
    /**
     * 通过 ONVIF 同步 IPC 设备通道（包括球机）
     * 
     * @param deviceId 设备ID
     * @param device 设备信息
     * @return 同步的通道数量
     */
    private int syncIpcChannelsViaOnvif(Long deviceId, IotDeviceDO device) {
        int syncCount = 0;
        
        // 1. 删除旧通道（如果存在），准备重新同步
        List<IotDeviceChannelDO> existingChannels = channelMapper.selectListByDeviceId(deviceId);
        if (!existingChannels.isEmpty()) {
            log.info("[通道同步] 删除设备旧通道，准备重新同步: deviceId={}, oldChannelCount={}", 
                    deviceId, existingChannels.size());
            for (IotDeviceChannelDO channel : existingChannels) {
                channelMapper.deleteById(channel.getId());
            }
        }
        
        // 2. 从设备配置中获取认证信息
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
        
        // 3. 通过 ONVIF 查询设备的实际通道数和云台能力
        String deviceIp = DeviceConfigHelper.getIpAddress(device);
        log.info("[通道同步] 开始通过ONVIF查询设备通道: deviceId={}, ip={}", deviceId, deviceIp);
        
        try {
            cn.iocoder.yudao.module.iot.service.onvif.OnvifClient onvifClient = 
                    new cn.iocoder.yudao.module.iot.service.onvif.OnvifClient(deviceIp, username, password);
            
            List<cn.iocoder.yudao.module.iot.service.onvif.OnvifChannelInfo> channelInfoList = 
                    onvifClient.getProfiles();
            
            if (channelInfoList == null || channelInfoList.isEmpty()) {
                log.warn("[通道同步] ONVIF查询返回空结果，使用默认配置: deviceId={}, ip={}", deviceId, deviceIp);
                // 使用默认配置：1个通道
                channelInfoList = createDefaultChannelInfo(device);
            }
            
            // 4. 创建通道
            for (cn.iocoder.yudao.module.iot.service.onvif.OnvifChannelInfo info : channelInfoList) {
                IotDeviceChannelDO channel = new IotDeviceChannelDO();
                channel.setDeviceId(deviceId);
                channel.setProductId(device.getProductId());
                channel.setDeviceType(convertDeviceType(device.getDeviceType()));
                channel.setChannelNo(info.getChannelNo());
                channel.setChannelName(info.getChannelName() != null ? info.getChannelName() : device.getDeviceName() + "-通道" + info.getChannelNo());
                channel.setChannelCode("CH-" + device.getDeviceKey() + "-" + info.getChannelNo());
                channel.setChannelType("VIDEO");
                channel.setChannelSubType(info.isPtzSupport() ? "PTZ" : "IPC");
                channel.setPtzSupport(info.isPtzSupport());
                channel.setAudioSupport(info.isAudioSupport());
                
                // 设置分辨率
                if (info.getResolution() != null) {
                    channel.setResolution(info.getResolution());
                }
                
                // 设置目标设备信息（IPC的通道指向自己）
                channel.setTargetDeviceId(deviceId);
                channel.setTargetIp(deviceIp);
                channel.setTargetPort(80); // ONVIF 默认端口
                channel.setTargetChannelNo(info.getChannelNo());
                
                // 设置协议和认证信息
                channel.setProtocol("ONVIF");
                channel.setUsername(username);
                channel.setPassword(password);
                
                // 设置状态（继承设备状态）
                channel.setOnlineStatus(device.getState());
                channel.setEnableStatus(1);
                channel.setAlarmStatus(0);
                
                // 设置排序
                channel.setSort(info.getChannelNo());
                
                channelMapper.insert(channel);
                syncCount++;
                
                log.info("[通道同步] ✅ 创建IPC通道: deviceId={}, channelNo={}, name={}, isPtz={}, hasAudio={}, resolution={}", 
                        deviceId, info.getChannelNo(), channel.getChannelName(), 
                        info.isPtzSupport(), info.isAudioSupport(), info.getResolution());
            }
            
            log.info("[通道同步] IPC设备通道同步完成: deviceId={}, syncCount={}", deviceId, syncCount);
            
        } catch (Exception e) {
            log.error("[通道同步] ONVIF查询失败，使用默认配置: deviceId={}, ip={}", deviceId, deviceIp, e);
            // 出错时使用默认配置
            syncCount = createDefaultChannel(deviceId, device, username, password);
        }
        
        return syncCount;
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
        
        channelMapper.insert(channel);
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
        try {
            cn.hutool.json.JSONObject cfg = cn.hutool.json.JSONUtil.parseObj(configStr);
            channelNo = cfg.getInt("channel");
            
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
        List<IotDeviceChannelDO> existingChannels = channelMapper.selectListByDeviceId(nvrId);
        IotDeviceChannelDO existing = existingChannels.stream()
                .filter(ch -> ch.getChannelNo().equals(finalChannelNo))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            // 更新现有通道
            IotDeviceChannelDO updateObj = new IotDeviceChannelDO();
            updateObj.setId(existing.getId());
            
            // ✅ 保留已有的自定义通道名称，只有当数据库中是默认名称或为空时才更新
            String existingName = existing.getChannelName();
            String newName = channelInfo.getDeviceName();
            boolean isDefaultName = existingName == null || existingName.isEmpty() 
                    || existingName.matches("^通道\\d+$");  // 匹配"通道1"、"通道2"等默认名称
            if (isDefaultName && newName != null && !newName.isEmpty()) {
                updateObj.setChannelName(newName);
                log.info("[通道同步] 更新通道名称: channelNo={}, {} -> {}", channelNo, existingName, newName);
            }
            // 如果已有自定义名称，不覆盖
            
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
            channelMapper.updateById(updateObj);
            log.info("[通道同步] 更新通道: nvrId={}, channelNo={}, name={}, onlineStatus={}, ptzSupport={}", 
                    nvrId, channelNo, existing.getChannelName(), channelInfo.getState(), ptzSupport);
        } else {
            // 创建新通道
            IotDeviceChannelDO newChannel = new IotDeviceChannelDO();
            newChannel.setDeviceId(nvrId);
            newChannel.setDeviceType("NVR");
            newChannel.setProductId(4L);
            newChannel.setChannelNo(channelNo);
            newChannel.setChannelName(channelInfo.getDeviceName());
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
            channelMapper.insert(newChannel);
            log.info("[通道同步] 新增通道: nvrId={}, channelNo={}, name={}", nvrId, channelNo, channelInfo.getDeviceName());
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
            IotDeviceChannelDO updateObj = new IotDeviceChannelDO();
            updateObj.setId(id);
            updateObj.setEnableStatus(1);
            channelMapper.updateById(updateObj);
        });
        log.info("[通道管理] 批量启用通道: count={}", channelIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDisableChannels(List<Long> channelIds) {
        channelIds.forEach(id -> {
            IotDeviceChannelDO updateObj = new IotDeviceChannelDO();
            updateObj.setId(id);
            updateObj.setEnableStatus(0);
            channelMapper.updateById(updateObj);
        });
        log.info("[通道管理] 批量禁用通道: count={}", channelIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSetPatrol(List<Long> channelIds, Boolean isPatrol) {
        channelIds.forEach(id -> {
            IotDeviceChannelDO updateObj = new IotDeviceChannelDO();
            updateObj.setId(id);
            updateObj.setIsPatrol(isPatrol != null && isPatrol ? 1 : 0);
            channelMapper.updateById(updateObj);
        });
        log.info("[通道管理] 批量设置巡更: count={}, isPatrol={}", channelIds.size(), isPatrol);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSetMonitor(List<Long> channelIds, Boolean isMonitor) {
        channelIds.forEach(id -> {
            IotDeviceChannelDO updateObj = new IotDeviceChannelDO();
            updateObj.setId(id);
            updateObj.setIsMonitor(isMonitor != null && isMonitor ? 1 : 0);
            channelMapper.updateById(updateObj);
        });
        log.info("[通道管理] 批量设置监控墙: count={}, isMonitor={}", channelIds.size(), isMonitor);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAssignSpatial(List<Long> channelIds, Long campusId, Long buildingId, Long floorId, Long areaId) {
        if (channelIds == null || channelIds.isEmpty()) {
            return;
        }
        // 先查询空间信息
        SpatialInfo spatialInfo = querySpatialInfo(campusId, buildingId, floorId, areaId);
        
        // 然后更新通道信息
        for (Long id : channelIds) {
            IotDeviceChannelDO updateObj = new IotDeviceChannelDO();
            updateObj.setId(id);
            updateObj.setBuildingId(buildingId);
            updateObj.setFloorId(floorId);
            updateObj.setAreaId(areaId);
            updateObj.setSpaceId(null);
            updateObj.setLocation(spatialInfo.location);
            channelMapper.updateById(updateObj);
        }
        
        log.info("[通道管理] 批量指派空间: count={}, campusId={}, buildingId={}, floorId={}", channelIds.size(), campusId, buildingId, floorId);
    }

    // ========== 多屏预览专用 ==========

    @Override
    public List<IotDeviceChannelDO> getNvrChannelsWithAutoSync(Long deviceId, Boolean forceSync) {
        // 1. 查询数据库中的通道
        List<IotDeviceChannelDO> channels = channelMapper.selectListByDeviceId(deviceId);
        
        // 2. 如果没有数据或强制同步，则同步
        if (channels.isEmpty() || Boolean.TRUE.equals(forceSync)) {
            log.info("[NVR通道] 自动同步: deviceId={}, forceSync={}", deviceId, forceSync);
            syncDeviceChannels(deviceId);
            // 重新查询
            channels = channelMapper.selectListByDeviceId(deviceId);
        }
        
        return channels;
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
            List<IotDeviceDO> nvrList = nvrQueryService.getNvrList();
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
            for (IotDeviceDO nvr : nvrList) {
                try {
                    // 只对在线的 NVR 发送同步命令
                    if (nvr.getState() != null && nvr.getState() == 1) {
                        // 调用 NvrQueryService 发送通道查询命令到 newgateway
                        // 结果会通过 DeviceServiceResultConsumer 异步处理并更新数据库
                        nvrQueryService.refreshChannelsByNvrId(nvr.getId());
                        successCount++;
                        log.info("[批量同步NVR通道] NVR {} ({}) 同步命令已发送", nvr.getId(), nvr.getDeviceName());
                    } else {
                        log.info("[批量同步NVR通道] NVR {} ({}) 不在线，跳过", nvr.getId(), nvr.getDeviceName());
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
        List<IotDeviceChannelDO> channels = channelMapper.selectListByDeviceId(deviceId);
        long sameIpCount = channels.stream()
                .filter(ch -> targetIp.equals(ch.getTargetIp()))
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

    /**
     * 空间信息内部类
     */
    private static class SpatialInfo {
        CampusDO campus;
        BuildingDO building;
        FloorDO floor;
        cn.iocoder.yudao.module.iot.dal.dataobject.gis.AreaDO area;
        String location;
    }

    /**
     * 查询空间信息
     * 注：已移除 @DS("postgresql") 注解，改用默认 MySQL 数据源
     */
    private SpatialInfo querySpatialInfo(Long campusId, Long buildingId, Long floorId, Long areaId) {
        SpatialInfo info = new SpatialInfo();
        
        // 查询空间实体
        info.campus = campusMapper.selectById(campusId);
        info.building = buildingMapper.selectById(buildingId);
        info.floor = floorMapper.selectById(floorId);
        
        // 验证空间实体
        if (info.campus == null) {
            throw exception(CAMPUS_NOT_EXISTS);
        }
        if (info.building == null) {
            throw exception(BUILDING_NOT_EXISTS);
        }
        if (info.floor == null) {
            throw exception(FLOOR_NOT_EXISTS);
        }
        if (!info.building.getCampusId().equals(info.campus.getId())) {
            throw exception(BUILDING_NOT_BELONG_TO_CAMPUS);
        }
        if (!info.floor.getBuildingId().equals(info.building.getId())) {
            throw exception(FLOOR_NOT_BELONG_TO_BUILDING);
        }
        
        // 构建位置字符串
        String loc = String.format("%s/%s/%s", info.campus.getName(), info.building.getName(), info.floor.getName());
        
        // 处理区域信息
        if (areaId != null) {
            info.area = areaMapper.selectById(areaId);
            if (info.area == null) {
                throw exception(AREA_NOT_EXISTS);
            }
            if (!floorId.equals(info.area.getFloorId())) {
                throw exception(AREA_NOT_BELONG_TO_FLOOR);
            }
            if (info.area.getBuildingId() != null && !buildingId.equals(info.area.getBuildingId())) {
                throw exception(AREA_NOT_BELONG_TO_BUILDING);
            }
            String areaName = info.area.getName();
            loc = loc + "/" + (areaName != null ? areaName : "区域");
        }
        
        info.location = loc;
        return info;
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
        
        // 2. 校验设备存在
        IotDeviceDO device = deviceService.getDevice(deviceId);
        if (device == null) {
            return AccessChannelSyncResult.failure("设备不存在: deviceId=" + deviceId);
        }
        
        // 3. 处理空通道列表（设备未配置通道的情况）
        if (channelList == null) {
            channelList = new ArrayList<>();
        }
        
        // 4. 查询数据库中现有的通道
        List<IotDeviceChannelDO> existingChannels = channelMapper.selectListByDeviceId(deviceId);
        Map<Integer, IotDeviceChannelDO> existingChannelMap = existingChannels.stream()
                .collect(Collectors.toMap(IotDeviceChannelDO::getChannelNo, c -> c, (a, b) -> a));
        
        // 5. 收集设备返回的通道号
        Set<Integer> deviceChannelNos = channelList.stream()
                .filter(c -> c.getChannelNo() != null)
                .map(AccessChannelSyncInfo::getChannelNo)
                .collect(Collectors.toSet());
        
        // 6. 执行同步
        int insertedCount = 0;
        int updatedCount = 0;
        int deletedCount = 0;
        String syncBatchId = generateSyncBatchId();
        LocalDateTime syncTime = LocalDateTime.now();
        
        // 6.1 新增或更新通道
        for (AccessChannelSyncInfo channelInfo : channelList) {
            if (channelInfo.getChannelNo() == null) {
                log.warn("[门禁通道同步] 跳过无效通道（通道号为空）: deviceId={}", deviceId);
                continue;
            }
            
            IotDeviceChannelDO existingChannel = existingChannelMap.get(channelInfo.getChannelNo());
            
            if (existingChannel != null) {
                // 更新现有通道
                updateAccessChannel(existingChannel, channelInfo, syncTime);
                updatedCount++;
                log.debug("[门禁通道同步] 更新通道: deviceId={}, channelNo={}, name={}", 
                        deviceId, channelInfo.getChannelNo(), channelInfo.getChannelName());
            } else {
                // 新增通道
                createAccessChannel(deviceId, device, channelInfo, syncTime);
                insertedCount++;
                log.debug("[门禁通道同步] 新增通道: deviceId={}, channelNo={}, name={}", 
                        deviceId, channelInfo.getChannelNo(), channelInfo.getChannelName());
            }
        }
        
        // 6.2 软删除设备上不存在的通道
        for (IotDeviceChannelDO existingChannel : existingChannels) {
            if (!deviceChannelNos.contains(existingChannel.getChannelNo())) {
                // 软删除：标记为离线状态，并记录历史
                softDeleteAccessChannel(existingChannel, syncBatchId, syncTime);
                deletedCount++;
                log.info("[门禁通道同步] 软删除通道: deviceId={}, channelNo={}, name={}", 
                        deviceId, existingChannel.getChannelNo(), existingChannel.getChannelName());
            }
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

        // 2. 校验设备存在
        IotDeviceDO nvrDevice = deviceService.getDevice(deviceId);
        if (nvrDevice == null) {
            return AccessChannelSyncResult.failure("设备不存在: deviceId=" + deviceId);
        }

        if (channelList == null) {
            channelList = new ArrayList<>();
        }

        // 3. 查询数据库中现有的通道
        List<IotDeviceChannelDO> existingChannels = channelMapper.selectListByDeviceId(deviceId);
        Map<Integer, IotDeviceChannelDO> existingChannelMap = existingChannels.stream()
                .collect(Collectors.toMap(IotDeviceChannelDO::getChannelNo, c -> c, (a, b) -> a));

        // 4. 收集设备返回的通道号
        Set<Integer> deviceChannelNos = channelList.stream()
                .filter(c -> c.getChannelNo() != null)
                .map(NvrChannelSyncInfo::getChannelNo)
                .collect(Collectors.toSet());

        int insertedCount = 0;
        int updatedCount = 0;
        int deletedCount = 0;
        String syncBatchId = generateSyncBatchId();

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

            syncNvrChannel(deviceId, nvrDevice, channelDevice);

            if (exists) {
                updatedCount++;
            } else {
                insertedCount++;
            }
        }

        // 6. 删除设备上不存在的通道（硬删除 + 记录历史），保持与既有 NVR 同步策略一致
        for (IotDeviceChannelDO existingChannel : existingChannels) {
            if (!deviceChannelNos.contains(existingChannel.getChannelNo())) {
                saveChannelHistory(existingChannel, "DELETE", "newgateway同步时删除不存在的通道", syncBatchId);
                channelMapper.deleteById(existingChannel.getId());
                deletedCount++;
                log.info("[NVR通道同步] 删除通道: deviceId={}, channelNo={}, name={}",
                        deviceId, existingChannel.getChannelNo(), existingChannel.getChannelName());
            }
        }

        log.info("[NVR通道同步] 同步完成: deviceId={}, inserted={}, updated={}, deleted={}",
                deviceId, insertedCount, updatedCount, deletedCount);
        return AccessChannelSyncResult.success(insertedCount, updatedCount, deletedCount);
    }

    /**
     * 创建门禁通道
     */
    private void createAccessChannel(Long deviceId, IotDeviceDO device, 
                                      AccessChannelSyncInfo channelInfo, LocalDateTime syncTime) {
        IotDeviceChannelDO channel = new IotDeviceChannelDO();
        channel.setDeviceId(deviceId);
        channel.setProductId(device.getProductId());
        channel.setDeviceType("ACCESS");
        channel.setChannelNo(channelInfo.getChannelNo());
        channel.setChannelName(channelInfo.getChannelName() != null ? 
                channelInfo.getChannelName() : "门" + channelInfo.getChannelNo());
        channel.setChannelCode("CH-ACCESS-" + device.getDeviceKey() + "-" + channelInfo.getChannelNo());
        channel.setChannelType(channelInfo.getChannelType() != null ? 
                channelInfo.getChannelType() : "ACCESS");
        channel.setChannelSubType("DOOR");
        
        // 设置状态
        channel.setOnlineStatus(channelInfo.getStatus() != null ? channelInfo.getStatus() : 2);
        channel.setEnableStatus(1);
        channel.setAlarmStatus(0);
        
        // 设置能力信息
        if (channelInfo.getCapabilities() != null) {
            channel.setCapabilities(channelInfo.getCapabilities());
        }
        
        // 设置同步时间
        channel.setLastSyncTime(syncTime);
        channel.setSort(channelInfo.getChannelNo());
        
        channelMapper.insert(channel);
    }

    /**
     * 更新门禁通道
     */
    private void updateAccessChannel(IotDeviceChannelDO existingChannel, 
                                      AccessChannelSyncInfo channelInfo, LocalDateTime syncTime) {
        IotDeviceChannelDO updateObj = new IotDeviceChannelDO();
        updateObj.setId(existingChannel.getId());
        
        // 更新名称（如果有变化）
        if (channelInfo.getChannelName() != null) {
            updateObj.setChannelName(channelInfo.getChannelName());
        }
        
        // 更新状态
        if (channelInfo.getStatus() != null) {
            updateObj.setOnlineStatus(channelInfo.getStatus());
        }
        
        // 更新能力信息
        if (channelInfo.getCapabilities() != null) {
            updateObj.setCapabilities(channelInfo.getCapabilities());
        }
        
        // 更新同步时间
        updateObj.setLastSyncTime(syncTime);
        
        channelMapper.updateById(updateObj);
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
    private void softDeleteAccessChannel(IotDeviceChannelDO channel, String syncBatchId, LocalDateTime syncTime) {
        // 记录删除历史
        saveChannelHistory(channel, "SOFT_DELETE", "设备同步时通道不存在，执行软删除", syncBatchId);
        
        // 更新通道状态为离线
        IotDeviceChannelDO updateObj = new IotDeviceChannelDO();
        updateObj.setId(channel.getId());
        updateObj.setOnlineStatus(0); // 离线
        updateObj.setEnableStatus(0); // 禁用
        updateObj.setLastSyncTime(syncTime);
        
        channelMapper.updateById(updateObj);
    }
}
