package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.AccessDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.GenericDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import cn.iocoder.yudao.module.iot.enums.device.AccessDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.service.channel.IotDeviceChannelService;
import cn.iocoder.yudao.module.iot.mq.producer.DeviceCommandPublisher;
import cn.iocoder.yudao.module.iot.service.ibms.device.IbmsDeviceRuntimeService;
import cn.iocoder.yudao.module.iot.service.ibms.device.support.IbmsDeviceLedgerRuntimeHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.mq.manager.DeviceCommandResponseManager;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 门禁通道 Service 实现类
 * 
 * <p>通过 DeviceCommandPublisher 发送命令到统一 Topic（DEVICE_SERVICE_INVOKE），
 * 并监听 DEVICE_SERVICE_RESULT 接收响应，实现 biz 和 newgateway 的解耦。</p>
 * 
 * <p>适配说明：</p>
 * <ul>
 *   <li>使用 DeviceCommandPublisher 替代直接发送到 ACCESS_CONTROL_DEVICE_COMMAND</li>
 *   <li>监听 DEVICE_SERVICE_RESULT 替代 ACCESS_CONTROL_DEVICE_RESPONSE</li>
 *   <li>根据设备 supportVideo 字段自动识别设备类型（ACCESS_GEN1/ACCESS_GEN2）</li>
 * </ul>
 * 
 * <p>Requirements: 4.1, 4.2, 4.3</p>
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@Validated
public class IotAccessChannelServiceImpl implements IotAccessChannelService {

    /** 操作类型常量 */
    private static final String OP_OPEN_DOOR = "OPEN_DOOR";
    private static final String OP_CLOSE_DOOR = "CLOSE_DOOR";
    private static final String OP_ALWAYS_OPEN = "ALWAYS_OPEN";
    private static final String OP_ALWAYS_CLOSED = "ALWAYS_CLOSED";
    private static final String OP_CANCEL_ALWAYS = "CANCEL_ALWAYS";
    private static final String OP_QUERY_CHANNELS = "QUERY_CHANNELS";

    /** 命令超时时间（秒） */
    private static final int COMMAND_TIMEOUT_SECONDS = 30;

    @Resource
    private DeviceCommandResponseManager responseManager;

    @Resource
    private IotDeviceChannelService channelService;

    @Resource
    private IbmsDeviceMapper ibmsDeviceMapper;

    @Resource
    private IbmsDeviceRuntimeService ibmsDeviceRuntimeService;

    @Resource
    private DeviceCommandPublisher deviceCommandPublisher;

    @Resource
    private IotAccessOperationLogService operationLogService;

    /**
     * 根据设备配置获取设备类型
     * 
     * @param device 设备信息
     * @return 设备类型（ACCESS_GEN1 或 ACCESS_GEN2）
     */
    private String getDeviceType(IotDeviceDO device) {
        if (device.getConfig() instanceof AccessDeviceConfig) {
            AccessDeviceConfig config = (AccessDeviceConfig) device.getConfig();
            String configDeviceType = config.getAccessDeviceType();
            Boolean supportVideo = config.getSupportVideo();
            return AccessDeviceTypeConstants.resolveDeviceType(configDeviceType, supportVideo);
        }
        // 兼容 GenericDeviceConfig：优先读取 config.deviceType（ACCESS_GEN1/ACCESS_GEN2）
        if (device.getConfig() instanceof GenericDeviceConfig) {
            GenericDeviceConfig cfg = (GenericDeviceConfig) device.getConfig();
            Object deviceTypeObj = cfg.get("deviceType");
            Object supportVideoObj = cfg.get("supportVideo");
            String configDeviceType = deviceTypeObj != null ? deviceTypeObj.toString() : null;
            Boolean supportVideo = (supportVideoObj instanceof Boolean) ? (Boolean) supportVideoObj : null;
            return AccessDeviceTypeConstants.resolveDeviceType(configDeviceType, supportVideo);
        }
        // 默认返回一代门禁
        return AccessDeviceTypeConstants.ACCESS_GEN1;
    }

    @Override
    public int discoverChannels(Long deviceId) {
        IbmsDeviceDO ibms = ibmsDeviceMapper.selectById(deviceId);
        if (ibms == null) {
            throw exception(ACCESS_DEVICE_NOT_EXISTS);
        }

        var runtime = ibmsDeviceRuntimeService.getByDeviceId(deviceId);
        IotDeviceDO device = IbmsDeviceLedgerRuntimeHelper.buildLegacyAccessDeviceShell(ibms, runtime);
        if (device == null) {
            throw exception(ACCESS_DEVICE_NOT_EXISTS);
        }

        String ip = device.getConfig() != null ? device.getConfig().getIpAddress() : null;
        Integer port = 37777;
        String username = "admin";
        String password = "admin123";

        if (device.getConfig() instanceof AccessDeviceConfig) {
            AccessDeviceConfig config = (AccessDeviceConfig) device.getConfig();
            if (config.getPort() != null) port = config.getPort();
            if (config.getUsername() != null) username = config.getUsername();
            if (config.getPassword() != null) password = config.getPassword();
        }

        // 获取设备类型
        String deviceType = getDeviceType(device);

        // 构建命令参数
        Map<String, Object> params = new HashMap<>();
        params.put("ipAddress", ip);
        params.put("port", port);
        params.put("username", username);
        params.put("password", password);
        params.put("tenantId", device.getTenantId());

        // 使用 DeviceCommandPublisher 发送命令到统一 Topic
        IotDeviceMessage response = sendCommandAndWait(deviceType, deviceId, OP_QUERY_CHANNELS, params);
        
        if (response == null || !isSuccess(response)) {
            String errorMsg = response != null ? response.getMsg() : "命令超时";
            log.warn("[discoverChannels] 查询通道失败: deviceId={}, error={}", deviceId, errorMsg);
            return 0;
        }

        // 处理查询到的通道
        int count = 0;
        Object data = response.getData();
        if (data instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) data;
            Object channelListObj = dataMap.get("channelList");
            if (channelListObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> channelList = (List<Map<String, Object>>) channelListObj;
                count = processChannelList(deviceId, channelList);
            }
        }
        
        log.info("[discoverChannels] 发现 {} 个新通道: deviceId={}", count, deviceId);
        return count;
    }

    /**
     * 处理通道列表
     */
    private int processChannelList(Long deviceId, List<Map<String, Object>> channelList) {
        if (channelList == null || channelList.isEmpty()) {
            return 0;
        }

        // 1) 先把门禁“通道行”收口到 ibms_channel（DR/DR-READER）
        List<IotDeviceChannelService.AccessChannelSyncInfo> syncInfos = new ArrayList<>();
        // 2) 再把门禁配置（doorState/doorStatus/doorMode）写入 extra.config
        Map<Integer, Map<String, Object>> configByChannelNo = new HashMap<>();

        for (Map<String, Object> channelInfo : channelList) {
            Integer channelNo = getInteger(channelInfo, "channelNo");
            String channelName = getString(channelInfo, "channelName");
            Integer doorState = getInteger(channelInfo, "doorState");
            String doorStatus = getString(channelInfo, "doorStatus");
            Integer doorMode = getInteger(channelInfo, "doorMode");

            if (channelNo == null) {
                continue;
            }

            syncInfos.add(IotDeviceChannelService.AccessChannelSyncInfo.builder()
                    .channelNo(channelNo)
                    .channelName(channelName)
                    .channelType("ACCESS")
                    // discovery 场景未知“在线/离线”，这里默认开启（armed）
                    .status(1)
                    .capabilities(null)
                    .build());

            boolean hasCfg = doorState != null || doorMode != null || doorStatus != null;
            if (hasCfg) {
                Map<String, Object> cfg = configByChannelNo.computeIfAbsent(channelNo, k -> new HashMap<>());
                if (doorState != null) {
                    cfg.put("doorState", doorState);
                }
                if (doorStatus != null) {
                    cfg.put("doorStatus", doorStatus);
                }
                if (doorMode != null) {
                    cfg.put("doorMode", doorMode);
                }
            }
        }

        if (syncInfos.isEmpty()) {
            return 0;
        }

        var syncResult = channelService.syncAccessChannels(deviceId, syncInfos);
        int inserted = syncResult != null ? syncResult.getInsertedCount() : 0;

        // 将门禁状态/模式写入 extra.config（避免后续 G4 删除 iot_device_channel 断链）
        for (Map.Entry<Integer, Map<String, Object>> entry : configByChannelNo.entrySet()) {
            Integer channelNo = entry.getKey();
            Map<String, Object> cfg = entry.getValue();
            if (channelNo == null || cfg == null || cfg.isEmpty()) {
                continue;
            }
            IotDeviceChannelDO channel = channelService.getChannelByDeviceIdAndChannelNo(deviceId, channelNo);
            if (channel == null || channel.getId() == null) {
                continue;
            }
            channelService.updateChannelConfig(channel.getId(), cfg);
        }

        return inserted;
    }

    /**
     * 更新已存在的通道
     */
    private void updateExistingChannel(IotDeviceChannelDO existingChannel, String channelName,
                                        Integer doorState, String doorStatus, Integer doorMode) {
        if (existingChannel == null || existingChannel.getId() == null) {
            return;
        }
        Map<String, Object> config = existingChannel.getConfig();
        if (config == null) {
            config = new HashMap<>();
        }
        if (doorState != null) {
            config.put("doorState", doorState);
            config.put("doorStatus", doorStatus);
        }
        if (doorMode != null) {
            config.put("doorMode", doorMode);
        }
        channelService.updateChannelConfig(existingChannel.getId(), config);
    }

    /**
     * 从 Map 中获取 Integer 值
     */
    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 从 Map 中获取 String 值
     */
    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    @Override
    public void openDoor(Long channelId, Long operatorId, String operatorName) {
        executeDoorControl(channelId, OP_OPEN_DOOR, operatorId, operatorName);
    }

    @Override
    public void closeDoor(Long channelId, Long operatorId, String operatorName) {
        executeDoorControl(channelId, OP_CLOSE_DOOR, operatorId, operatorName);
    }

    @Override
    public void setAlwaysOpen(Long channelId, Long operatorId, String operatorName) {
        executeDoorControl(channelId, OP_ALWAYS_OPEN, operatorId, operatorName);
    }

    @Override
    public void setAlwaysClosed(Long channelId, Long operatorId, String operatorName) {
        executeDoorControl(channelId, OP_ALWAYS_CLOSED, operatorId, operatorName);
    }

    @Override
    public void cancelAlwaysState(Long channelId, Long operatorId, String operatorName) {
        executeDoorControl(channelId, OP_CANCEL_ALWAYS, operatorId, operatorName);
    }

    @Override
    public List<IotDeviceChannelDO> getChannelsByDeviceId(Long deviceId) {
        List<IotDeviceChannelDO> all = channelService.getChannelsByDeviceId(deviceId);
        if (all == null || all.isEmpty()) {
            return List.of();
        }
        List<IotDeviceChannelDO> access = new ArrayList<>();
        for (IotDeviceChannelDO c : all) {
            if (c != null && "ACCESS".equalsIgnoreCase(c.getChannelType())) {
                access.add(c);
            }
        }
        return access;
    }

    @Override
    public IotDeviceChannelDO getChannel(Long channelId) {
        return channelService.getChannel(channelId);
    }

    /**
     * 执行门控制操作
     */
    private void executeDoorControl(Long channelId, String operationType, Long operatorId, String operatorName) {
        IotDeviceChannelDO channel = channelService.getChannel(channelId);
        if (channel == null) {
            throw exception(CHANNEL_NOT_EXISTS);
        }

        IbmsDeviceDO ibms = ibmsDeviceMapper.selectById(channel.getDeviceId());
        if (ibms == null) {
            throw exception(ACCESS_DEVICE_NOT_EXISTS);
        }
        var runtime = ibmsDeviceRuntimeService.getByDeviceId(channel.getDeviceId());
        IotDeviceDO device = IbmsDeviceLedgerRuntimeHelper.buildLegacyAccessDeviceShell(ibms, runtime);
        if (device == null) {
            throw exception(ACCESS_DEVICE_NOT_EXISTS);
        }

        String ip = device.getConfig() != null ? device.getConfig().getIpAddress() : null;
        Integer port = 37777;
        if (device.getConfig() instanceof AccessDeviceConfig) {
            AccessDeviceConfig config = (AccessDeviceConfig) device.getConfig();
            if (config.getPort() != null) port = config.getPort();
        }

        // 获取设备类型
        String deviceType = getDeviceType(device);

        // 构建命令参数
        Map<String, Object> params = new HashMap<>();
        params.put("deviceType", deviceType);  // 【关键】传递正确的设备类型
        params.put("channelId", channelId);
        params.put("channelNo", channel.getChannelNo());
        params.put("ipAddress", ip);
        params.put("port", port);
        params.put("tenantId", device.getTenantId());

        // 使用 DeviceCommandPublisher 发送命令到统一 Topic
        IotDeviceMessage response = sendCommandAndWait(deviceType, device.getId(), operationType, params);

        // 获取设备名称和通道名称用于日志记录
        String deviceName = device.getDeviceName();
        String channelName = channel.getChannelName();

        if (response == null || !isSuccess(response)) {
            String errorMsg = response != null ? response.getMsg() : "命令超时";
            operationLogService.logOperation(device.getId(), deviceName, channelId, channelName,
                    operationType, operatorId, operatorName, 0, errorMsg);
            throw exception(ACCESS_DEVICE_OFFLINE);
        }

        log.info("[executeDoorControl] 执行门控制成功: channelId={}, operationType={}, operator={}",
                channelId, operationType, operatorName);

        // 如果是常开/常闭/取消操作，更新通道的 alwaysMode 配置
        if (OP_ALWAYS_OPEN.equals(operationType) || OP_ALWAYS_CLOSED.equals(operationType) || OP_CANCEL_ALWAYS.equals(operationType)) {
            updateChannelAlwaysMode(channel, operationType);
        }

        // 记录成功日志
        operationLogService.logOperation(device.getId(), deviceName, channelId, channelName,
                operationType, operatorId, operatorName, 1, null);
    }

    /**
     * 更新通道的常开/常闭模式
     */
    private void updateChannelAlwaysMode(IotDeviceChannelDO channel, String operationType) {
        try {
            Map<String, Object> config = channel.getConfig();
            if (config == null) {
                config = new HashMap<>();
            }
            
            int alwaysModeCode;
            String alwaysMode;
            switch (operationType) {
                case OP_ALWAYS_OPEN:
                    alwaysModeCode = 1;
                    alwaysMode = "常开";
                    break;
                case OP_ALWAYS_CLOSED:
                    alwaysModeCode = 2;
                    alwaysMode = "常闭";
                    break;
                default: // OP_CANCEL_ALWAYS
                    alwaysModeCode = 0;
                    alwaysMode = "正常";
                    break;
            }
            
            config.put("alwaysMode", alwaysMode);
            config.put("alwaysModeCode", alwaysModeCode);
            channelService.updateChannelConfig(channel.getId(), config);
            log.info("[updateChannelAlwaysMode] 更新通道常开/常闭模式成功: channelId={}, alwaysMode={}", 
                    channel.getId(), alwaysMode);
        } catch (Exception e) {
            log.error("[updateChannelAlwaysMode] 更新通道常开/常闭模式失败: channelId={}, error={}", 
                    channel.getId(), e.getMessage(), e);
        }
    }

    @Override
    public cn.iocoder.yudao.module.iot.controller.admin.access.vo.device.IotAccessChannelDetailRespVO getChannelDetail(Long channelId) {
        // 1. 获取通道基本信息
        IotDeviceChannelDO channel = channelService.getChannel(channelId);
        if (channel == null) {
            throw exception(ACCESS_CHANNEL_NOT_EXISTS);
        }
        
        // 2. 获取设备信息
        IotDeviceDO device = null;
        IbmsDeviceDO ibms = ibmsDeviceMapper.selectById(channel.getDeviceId());
        if (ibms != null) {
            var runtime = ibmsDeviceRuntimeService.getByDeviceId(channel.getDeviceId());
            device = IbmsDeviceLedgerRuntimeHelper.buildLegacyAccessDeviceShell(ibms, runtime);
        }

        // 3. 构建响应VO
        cn.iocoder.yudao.module.iot.controller.admin.access.vo.device.IotAccessChannelDetailRespVO respVO =
                new cn.iocoder.yudao.module.iot.controller.admin.access.vo.device.IotAccessChannelDetailRespVO();
        
        // 基本信息
        respVO.setId(channel.getId());
        respVO.setDeviceId(channel.getDeviceId());
        respVO.setDeviceName(device != null ? device.getDeviceName() : null);
        respVO.setChannelNo(channel.getChannelNo());
        respVO.setChannelIndex(channel.getChannelNo() != null ? channel.getChannelNo() - 1 : 0);
        respVO.setChannelName(channel.getChannelName());
        respVO.setChannelCode(channel.getChannelCode());
        respVO.setChannelType(channel.getChannelType());
        respVO.setChannelSubType(channel.getChannelSubType());
        respVO.setLocation(channel.getLocation());
        
        // 门禁通道专用字段
        respVO.setDoorName(channel.getDoorName());
        respVO.setDoorDirection(channel.getDoorDirection());
        respVO.setCardReaderType(channel.getCardReaderType());
        respVO.setLockType(channel.getLockType());
        
        // 从config中解析配置信息
        if (channel.getConfig() != null) {
            try {
                // 开门时长和报警时长
                Integer openDuration = (Integer) channel.getConfig().get("openDuration");
                Integer alarmDuration = (Integer) channel.getConfig().get("alarmDuration");
                Boolean timeoutAlarmEnabled = (Boolean) channel.getConfig().get("timeoutAlarmEnabled");
                Boolean forceAlarmEnabled = (Boolean) channel.getConfig().get("forceAlarmEnabled");
                
                respVO.setOpenDuration(openDuration != null ? openDuration : 5);
                respVO.setAlarmDuration(alarmDuration != null ? alarmDuration : 30);
                respVO.setTimeoutAlarmEnabled(timeoutAlarmEnabled != null ? timeoutAlarmEnabled : true);
                respVO.setForceAlarmEnabled(forceAlarmEnabled != null ? forceAlarmEnabled : true);
                
                // 实时状态
                Object doorStatus = channel.getConfig().get("doorStatus");
                Object doorStatusCode = channel.getConfig().get("doorStatusCode");
                Object lockStatus = channel.getConfig().get("lockStatus");
                Object lockStatusCode = channel.getConfig().get("lockStatusCode");
                Object alwaysMode = channel.getConfig().get("alwaysMode");
                Object alwaysModeCode = channel.getConfig().get("alwaysModeCode");
                
                respVO.setDoorStatus(doorStatus != null ? doorStatus.toString() : "未知");
                respVO.setDoorStatusCode(doorStatusCode != null ? (Integer) doorStatusCode : 0);
                respVO.setLockStatus(lockStatus != null ? lockStatus.toString() : "未知");
                respVO.setLockStatusCode(lockStatusCode != null ? (Integer) lockStatusCode : 0);
                respVO.setAlwaysMode(alwaysMode != null ? alwaysMode.toString() : "正常");
                respVO.setAlwaysModeCode(alwaysModeCode != null ? (Integer) alwaysModeCode : 0);
                
                // 序列化config为JSON字符串
                respVO.setConfig(cn.hutool.json.JSONUtil.toJsonStr(channel.getConfig()));
            } catch (Exception e) {
                log.warn("[getChannelDetail] 解析通道配置失败: channelId={}, error={}", channelId, e.getMessage());
                // 设置默认值
                respVO.setOpenDuration(5);
                respVO.setAlarmDuration(30);
                respVO.setTimeoutAlarmEnabled(true);
                respVO.setForceAlarmEnabled(true);
                respVO.setDoorStatus("未知");
                respVO.setDoorStatusCode(0);
                respVO.setLockStatus("未知");
                respVO.setLockStatusCode(0);
                respVO.setAlwaysMode("正常");
                respVO.setAlwaysModeCode(0);
            }
        } else {
            // 设置默认值
            respVO.setOpenDuration(5);
            respVO.setAlarmDuration(30);
            respVO.setTimeoutAlarmEnabled(true);
            respVO.setForceAlarmEnabled(true);
            respVO.setDoorStatus("未知");
            respVO.setDoorStatusCode(0);
            respVO.setLockStatus("未知");
            respVO.setLockStatusCode(0);
            respVO.setAlwaysMode("正常");
            respVO.setAlwaysModeCode(0);
        }
        
        // 状态信息
        respVO.setOnlineStatus(channel.getOnlineStatus());
        respVO.setOnlineStatusDesc(getOnlineStatusDesc(channel.getOnlineStatus()));
        respVO.setEnableStatus(channel.getEnableStatus());
        respVO.setEnableStatusDesc(getEnableStatusDesc(channel.getEnableStatus()));
        respVO.setAlarmStatus(channel.getAlarmStatus());
        respVO.setAlarmStatusDesc(getAlarmStatusDesc(channel.getAlarmStatus()));
        respVO.setLastOnlineTime(channel.getLastOnlineTime());
        respVO.setLastSyncTime(channel.getLastSyncTime());
        
        // 其他信息
        respVO.setDescription(channel.getDescription());
        respVO.setCreateTime(channel.getCreateTime());
        respVO.setUpdateTime(channel.getUpdateTime());
        
        return respVO;
    }

    private String getOnlineStatusDesc(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "离线";
            case 1: return "在线";
            case 2: return "故障";
            default: return "未知";
        }
    }

    private String getEnableStatusDesc(Integer status) {
        if (status == null) return "未知";
        return status == 1 ? "启用" : "禁用";
    }

    private String getAlarmStatusDesc(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "正常";
            case 1: return "报警";
            case 2: return "故障";
            default: return "未知";
        }
    }

    /**
     * 发送命令并等待响应
     * 
     * @param deviceType  设备类型（ACCESS_GEN1 或 ACCESS_GEN2）
     * @param deviceId    设备ID
     * @param commandType 命令类型
     * @param params      命令参数
     * @return 响应消息，超时返回 null
     */
    /**
     * 发送命令并等待响应
     * 
     * <p>使用统一的 DeviceCommandResponseManager 来等待响应，
     * 响应由 DeviceServiceResultConsumer 统一处理并通知管理器。</p>
     */
    private IotDeviceMessage sendCommandAndWait(String deviceType, Long deviceId, 
                                                  String commandType, Map<String, Object> params) {
        try {
            // 1. 发送命令并获取 requestId
            String requestId = deviceCommandPublisher.publishCommand(deviceType, deviceId, commandType, params);
            
            // 2. 注册请求到响应管理器
            responseManager.registerRequest(requestId);
            
            // 3. 等待响应
            return responseManager.waitForResponse(requestId, COMMAND_TIMEOUT_SECONDS);
        } catch (TimeoutException e) {
            log.error("[sendCommandAndWait] 命令超时: deviceType={}, deviceId={}, commandType={}", 
                    deviceType, deviceId, commandType);
            return null;
        } catch (Exception e) {
            log.error("[sendCommandAndWait] 命令执行失败: deviceType={}, deviceId={}, commandType={}, error={}", 
                    deviceType, deviceId, commandType, e.getMessage());
            return null;
        }
    }

    /**
     * 判断响应是否成功
     */
    private boolean isSuccess(IotDeviceMessage response) {
        Integer code = response.getCode();
        return code != null && code == 0;
    }

}
