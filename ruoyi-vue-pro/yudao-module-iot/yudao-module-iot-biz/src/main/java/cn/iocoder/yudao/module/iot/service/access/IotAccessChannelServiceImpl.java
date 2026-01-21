package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.AccessDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.GenericDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.mysql.channel.IotDeviceChannelMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import cn.iocoder.yudao.module.iot.enums.device.AccessDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.mq.producer.DeviceCommandPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.mq.manager.DeviceCommandResponseManager;
import jakarta.annotation.Resource;
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
    private IotDeviceChannelMapper channelMapper;

    @Resource
    private IotDeviceMapper deviceMapper;

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
            return AccessDeviceTypeConstants.getDeviceType(config.getSupportVideo());
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
        IotDeviceDO device = deviceMapper.selectById(deviceId);
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
        int count = 0;
        for (Map<String, Object> channelInfo : channelList) {
            Integer channelNo = getInteger(channelInfo, "channelNo");
            String channelName = getString(channelInfo, "channelName");
            Integer doorState = getInteger(channelInfo, "doorState");
            String doorStatus = getString(channelInfo, "doorStatus");
            Integer doorMode = getInteger(channelInfo, "doorMode");

            // 检查通道是否已存在
            List<IotDeviceChannelDO> existingChannels = channelMapper.selectListByDeviceId(deviceId);
            IotDeviceChannelDO existingChannel = existingChannels.stream()
                    .filter(ch -> ch.getChannelNo() != null && ch.getChannelNo().equals(channelNo))
                    .findFirst()
                    .orElse(null);
            
            if (existingChannel == null) {
                // 新通道,插入数据库
                IotDeviceChannelDO channel = IotDeviceChannelDO.builder()
                        .deviceId(deviceId)
                        .channelNo(channelNo)
                        .channelName(channelName)
                        .channelType("ACCESS")
                        .deviceType("ACCESS_CONTROL")
                        .onlineStatus(1)
                        .enableStatus(1)
                        .build();
                
                // 保存门状态和模式到config
                if (doorState != null || doorMode != null) {
                    Map<String, Object> config = new HashMap<>();
                    if (doorState != null) {
                        config.put("doorState", doorState);
                        config.put("doorStatus", doorStatus);
                    }
                    if (doorMode != null) {
                        config.put("doorMode", doorMode);
                    }
                    channel.setConfig(config);
                }
                
                channelMapper.insert(channel);
                count++;
                log.info("[discoverChannels] 新增通道: deviceId={}, channelNo={}, channelName={}", 
                        deviceId, channelNo, channelName);
            } else {
                // 已存在的通道,更新状态信息
                updateExistingChannel(existingChannel, channelName, doorState, doorStatus, doorMode);
            }
        }
        return count;
    }

    /**
     * 更新已存在的通道
     */
    private void updateExistingChannel(IotDeviceChannelDO existingChannel, String channelName,
                                        Integer doorState, String doorStatus, Integer doorMode) {
        boolean needUpdate = false;
        Map<String, Object> config = existingChannel.getConfig();
        if (config == null) {
            config = new HashMap<>();
        }
        
        // 更新门状态
        if (doorState != null) {
            config.put("doorState", doorState);
            config.put("doorStatus", doorStatus);
            needUpdate = true;
        }
        
        // 更新门模式
        if (doorMode != null) {
            config.put("doorMode", doorMode);
            needUpdate = true;
        }
        
        // 如果通道名称为空或为默认名称,则更新为SDK返回的名称
        if (existingChannel.getChannelName() == null || 
            existingChannel.getChannelName().isEmpty() ||
            existingChannel.getChannelName().startsWith("通道") ||
            existingChannel.getChannelName().startsWith("Channel")) {
            existingChannel.setChannelName(channelName);
            needUpdate = true;
        }
        
        if (needUpdate) {
            existingChannel.setConfig(config);
            channelMapper.updateById(existingChannel);
            log.info("[discoverChannels] 更新通道状态: deviceId={}, channelNo={}", 
                    existingChannel.getDeviceId(), existingChannel.getChannelNo());
        }
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
        return channelMapper.selectListByDeviceId(deviceId);
    }

    @Override
    public IotDeviceChannelDO getChannel(Long channelId) {
        return channelMapper.selectById(channelId);
    }

    /**
     * 执行门控制操作
     */
    private void executeDoorControl(Long channelId, String operationType, Long operatorId, String operatorName) {
        IotDeviceChannelDO channel = channelMapper.selectById(channelId);
        if (channel == null) {
            throw exception(CHANNEL_NOT_EXISTS);
        }

        IotDeviceDO device = deviceMapper.selectById(channel.getDeviceId());
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
            channel.setConfig(config);
            
            channelMapper.updateById(channel);
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
        IotDeviceChannelDO channel = channelMapper.selectById(channelId);
        if (channel == null) {
            throw exception(ACCESS_CHANNEL_NOT_EXISTS);
        }
        
        // 2. 获取设备信息
        IotDeviceDO device = deviceMapper.selectById(channel.getDeviceId());
        
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
