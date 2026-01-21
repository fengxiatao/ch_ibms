package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.AccessDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfigHelper;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.GenericDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import cn.iocoder.yudao.module.iot.enums.device.AccessDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.mq.producer.DeviceCommandPublisher;
import cn.iocoder.yudao.module.iot.websocket.DeviceMessagePushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.mq.manager.DeviceCommandResponseManager;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 门禁设备 Service 实现类
 * 
 * <p>通过 DeviceCommandPublisher 发送命令到统一 Topic（DEVICE_SERVICE_INVOKE），
 * 并监听 DEVICE_SERVICE_RESULT 接收响应。</p>
 * 
 * <p>适配说明：</p>
 * <ul>
 *   <li>使用 DeviceCommandPublisher 替代直接发送到 ACCESS_CONTROL_DEVICE_COMMAND</li>
 *   <li>监听 DEVICE_SERVICE_RESULT 替代 ACCESS_CONTROL_DEVICE_RESPONSE</li>
 *   <li>命令类型: ACTIVATE_DEVICE, DEACTIVATE_DEVICE, CHECK_DEVICE_ONLINE</li>
 * </ul>
 * 
 * <p>Requirements: 4.1, 4.2, 4.3</p>
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@Validated
public class IotAccessDeviceServiceImpl implements IotAccessDeviceService {

    /** 门禁子系统代码 */
    private static final String ACCESS_SUBSYSTEM_CODE = "access";
    
    /** 命令类型常量 */
    private static final String CMD_ACTIVATE_DEVICE = "ACTIVATE_DEVICE";
    private static final String CMD_DEACTIVATE_DEVICE = "DEACTIVATE_DEVICE";
    
    /** 命令超时时间（秒） */
    private static final int COMMAND_TIMEOUT_SECONDS = 30;

    @Resource
    private DeviceCommandResponseManager responseManager;

    @Resource
    private IotDeviceMapper deviceMapper;

    @Resource
    private DeviceCommandPublisher deviceCommandPublisher;

    @Resource
    private IotAccessChannelService channelService;

    @Resource
    private DeviceMessagePushService deviceMessagePushService;

    @Resource
    private IotAccessDeviceCapabilityService capabilityService;

    @Resource
    private cn.iocoder.yudao.module.iot.dal.mysql.product.IotProductMapper productMapper;

    @Resource
    private cn.iocoder.yudao.module.iot.dal.mysql.channel.IotDeviceChannelMapper channelMapper;

    /**
     * 根据设备配置获取设备类型
     */
    private String getDeviceType(IotDeviceDO device) {
        if (device.getConfig() instanceof AccessDeviceConfig) {
            AccessDeviceConfig config = (AccessDeviceConfig) device.getConfig();
            return AccessDeviceTypeConstants.getDeviceType(config.getSupportVideo());
        }
        if (device.getConfig() instanceof GenericDeviceConfig) {
            GenericDeviceConfig cfg = (GenericDeviceConfig) device.getConfig();
            Object deviceTypeObj = cfg.get("deviceType");
            Object supportVideoObj = cfg.get("supportVideo");
            String configDeviceType = deviceTypeObj != null ? deviceTypeObj.toString() : null;
            Boolean supportVideo = (supportVideoObj instanceof Boolean) ? (Boolean) supportVideoObj : null;
            return AccessDeviceTypeConstants.resolveDeviceType(configDeviceType, supportVideo);
        }
        return AccessDeviceTypeConstants.ACCESS_GEN1;
    }

    @Override
    public void activateDevice(Long deviceId) {
        IotDeviceDO device = validateAccessDevice(deviceId);
        String deviceType = getDeviceType(device);
        
        // 解析设备配置获取连接信息
        String ip = DeviceConfigHelper.getIpAddress(device);
        Integer port = 37777;
        String username = "admin";
        String password = "admin123";
        
        if (device.getConfig() != null) {
            if (device.getConfig() instanceof AccessDeviceConfig) {
                AccessDeviceConfig config = (AccessDeviceConfig) device.getConfig();
                if (config.getPort() != null) port = config.getPort();
                if (config.getUsername() != null) username = config.getUsername();
                if (config.getPassword() != null) password = config.getPassword();
            } else if (device.getConfig() instanceof GenericDeviceConfig) {
                // 兼容 GenericDeviceConfig（deviceType=GENERIC 的情况）
                GenericDeviceConfig config = (GenericDeviceConfig) device.getConfig();
                Object portObj = config.get("port");
                Object usernameObj = config.get("username");
                Object passwordObj = config.get("password");
                if (portObj instanceof Integer) port = (Integer) portObj;
                else if (portObj instanceof Number) port = ((Number) portObj).intValue();
                if (usernameObj instanceof String) username = (String) usernameObj;
                if (passwordObj instanceof String) password = (String) passwordObj;
            }
        }
        
        // 构建命令参数
        Map<String, Object> params = new HashMap<>();
        params.put("ipAddress", ip);
        params.put("port", port);
        params.put("username", username);
        params.put("password", password);
        params.put("tenantId", device.getTenantId());
        
        // 使用 DeviceCommandPublisher 发送激活命令
        IotDeviceMessage response = sendCommandAndWait(deviceType, deviceId, CMD_ACTIVATE_DEVICE, params);
        
        if (response == null || !isSuccess(response)) {
            String errorMsg = response != null ? response.getMsg() : "命令超时";
            log.error("[activateDevice] 设备激活失败: deviceId={}, error={}", deviceId, errorMsg);
            throw exception(ACCESS_DEVICE_ACTIVATE_FAILED);
        }
        
        // 更新设备状态为在线
        IotDeviceDO updateDevice = new IotDeviceDO();
        updateDevice.setId(deviceId);
        updateDevice.setState(IotDeviceStateEnum.ONLINE.getState());
        updateDevice.setOnlineTime(LocalDateTime.now());
        updateDevice.setActiveTime(LocalDateTime.now());
        deviceMapper.updateById(updateDevice);
        
        // 自动发现通道
        try {
            channelService.discoverChannels(deviceId);
        } catch (Exception e) {
            log.warn("[activateDevice] 发现通道失败: deviceId={}, error={}", deviceId, e.getMessage());
        }
        
        // 推送设备上线WebSocket消息
        deviceMessagePushService.pushDeviceStatus(deviceId, deviceType, "ONLINE", System.currentTimeMillis());

        // 激活成功后立即刷新能力并写回 iot_device.config（供业务侧按能力精准操作）
        try {
            Long tenantId = device.getTenantId() != null ? device.getTenantId() : 1L;
            TenantUtils.execute(tenantId, () -> capabilityService.refreshCapability(deviceId));
        } catch (Exception e) {
            log.warn("[activateDevice] 激活后刷新能力失败（不影响主流程）: deviceId={}, error={}", deviceId, e.getMessage());
        }
        
        log.info("[activateDevice] 门禁设备激活成功: deviceId={}, ip={}", deviceId, ip);
    }

    @Override
    public void deactivateDevice(Long deviceId) {
        IotDeviceDO device = validateAccessDevice(deviceId);
        String deviceType = getDeviceType(device);
        
        String ip = DeviceConfigHelper.getIpAddress(device);
        Integer port = 37777;
        if (device.getConfig() != null && device.getConfig() instanceof AccessDeviceConfig) {
            AccessDeviceConfig config = (AccessDeviceConfig) device.getConfig();
            if (config.getPort() != null) port = config.getPort();
        }
        
        // 构建命令参数
        Map<String, Object> params = new HashMap<>();
        params.put("ipAddress", ip);
        params.put("port", port);
        params.put("tenantId", device.getTenantId());
        
        // 使用 DeviceCommandPublisher 发送停用命令（不等待响应）
        deviceCommandPublisher.publishCommand(deviceType, deviceId, CMD_DEACTIVATE_DEVICE, params);
        
        // 更新设备状态为离线
        IotDeviceDO updateDevice = new IotDeviceDO();
        updateDevice.setId(deviceId);
        updateDevice.setState(IotDeviceStateEnum.OFFLINE.getState());
        updateDevice.setOfflineTime(LocalDateTime.now());
        deviceMapper.updateById(updateDevice);
        
        // 推送设备离线WebSocket消息
        deviceMessagePushService.pushDeviceStatus(deviceId, deviceType, "OFFLINE", System.currentTimeMillis());
        
        log.info("[deactivateDevice] 门禁设备停用成功: deviceId={}, ip={}", deviceId, ip);
    }

    @Override
    public List<IotDeviceDO> getAccessDevices() {
        return deviceMapper.selectList(new LambdaQueryWrapperX<IotDeviceDO>()
                .eq(IotDeviceDO::getSubsystemCode, ACCESS_SUBSYSTEM_CODE)
                .orderByDesc(IotDeviceDO::getId));
    }

    @Override
    public List<IotDeviceDO> getOnlineAccessDevices() {
        return deviceMapper.selectList(new LambdaQueryWrapperX<IotDeviceDO>()
                .eq(IotDeviceDO::getSubsystemCode, ACCESS_SUBSYSTEM_CODE)
                .eq(IotDeviceDO::getState, IotDeviceStateEnum.ONLINE.getState())
                .orderByDesc(IotDeviceDO::getId));
    }

    @Override
    public boolean isDeviceOnline(Long deviceId) {
        IotDeviceDO device = deviceMapper.selectById(deviceId);
        return device != null && IotDeviceStateEnum.ONLINE.getState().equals(device.getState());
    }

    @Override
    public IotDeviceDO getAccessDevice(Long deviceId) {
        return deviceMapper.selectById(deviceId);
    }

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

    @Override
    public cn.iocoder.yudao.module.iot.controller.admin.access.vo.device.IotAccessDeviceConfigRespVO getDeviceConfig(Long deviceId) {
        // 1. 获取设备基本信息
        IotDeviceDO device = validateAccessDevice(deviceId);
        
        // 2. 获取产品信息
        String productName = null;
        if (device.getProductId() != null) {
            cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO product = 
                    productMapper.selectById(device.getProductId());
            if (product != null) {
                productName = product.getName();
            }
        }
        
        // 3. 解析设备配置
        Integer port = 37777;
        String username = "admin";
        if (device.getConfig() != null && device.getConfig() instanceof AccessDeviceConfig) {
            AccessDeviceConfig config = (AccessDeviceConfig) device.getConfig();
            if (config.getPort() != null) port = config.getPort();
            if (config.getUsername() != null) username = config.getUsername();
        }
        
        // 4. 计算在线时长
        Long onlineDuration = null;
        if (IotDeviceStateEnum.ONLINE.getState().equals(device.getState()) && device.getOnlineTime() != null) {
            onlineDuration = java.time.Duration.between(device.getOnlineTime(), LocalDateTime.now()).getSeconds();
        }
        
        // 5. 获取通道列表
        List<cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO> channels = 
                channelMapper.selectList(new LambdaQueryWrapperX<cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO>()
                        .eq(cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO::getDeviceId, deviceId)
                        .orderByAsc(cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO::getChannelNo));
        
        // 6. 构建响应VO
        cn.iocoder.yudao.module.iot.controller.admin.access.vo.device.IotAccessDeviceConfigRespVO respVO = 
                new cn.iocoder.yudao.module.iot.controller.admin.access.vo.device.IotAccessDeviceConfigRespVO();
        respVO.setId(device.getId());
        respVO.setDeviceName(device.getDeviceName());
        respVO.setDeviceCode(device.getDeviceKey());
        respVO.setProductId(device.getProductId());
        respVO.setProductName(productName);
        respVO.setIpAddress(DeviceConfigHelper.getIpAddress(device));
        respVO.setPort(port);
        respVO.setUsername(username);
        respVO.setState(device.getState());
        respVO.setStateDesc(getStateDesc(device.getState()));
        respVO.setOnlineDuration(onlineDuration);
        respVO.setOnlineTime(device.getOnlineTime());
        respVO.setOfflineTime(device.getOfflineTime());
        respVO.setActiveTime(device.getActiveTime());
        // 将DeviceConfig对象转换为JSON字符串
        if (device.getConfig() != null) {
            respVO.setConfig(cn.hutool.json.JSONUtil.toJsonStr(device.getConfig().toMap()));
        }
        respVO.setCreateTime(device.getCreateTime());
        
        // 7. 设置登录句柄状态（简化版，实际应该从Gateway查询）
        if (IotDeviceStateEnum.ONLINE.getState().equals(device.getState())) {
            respVO.setLoginHandleStatus("已连接");
        } else {
            respVO.setLoginHandleStatus("未连接");
        }
        
        // 8. 设置设备能力集（默认值，实际应该从设备查询）
        cn.iocoder.yudao.module.iot.controller.admin.access.vo.device.IotAccessDeviceConfigRespVO.DeviceCapabilities capabilities = 
                new cn.iocoder.yudao.module.iot.controller.admin.access.vo.device.IotAccessDeviceConfigRespVO.DeviceCapabilities();
        capabilities.setSupportPassword(true);
        capabilities.setSupportCard(true);
        capabilities.setSupportFingerprint(true);
        capabilities.setSupportFace(true);
        capabilities.setSupportQrCode(false);
        capabilities.setSupportRemoteOpen(true);
        capabilities.setSupportAlwaysMode(true);
        capabilities.setMaxUsers(10000);
        capabilities.setMaxCards(10000);
        capabilities.setMaxFingerprints(3000);
        capabilities.setMaxFaces(3000);
        respVO.setCapabilities(capabilities);
        
        // 9. 设置通道数量
        respVO.setChannelCount(channels.size());
        
        // 10. 转换通道信息
        List<cn.iocoder.yudao.module.iot.controller.admin.access.vo.device.IotAccessDeviceConfigRespVO.ChannelInfo> channelInfos = 
                new java.util.ArrayList<>();
        for (cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO channel : channels) {
            cn.iocoder.yudao.module.iot.controller.admin.access.vo.device.IotAccessDeviceConfigRespVO.ChannelInfo channelInfo = 
                    new cn.iocoder.yudao.module.iot.controller.admin.access.vo.device.IotAccessDeviceConfigRespVO.ChannelInfo();
            channelInfo.setId(channel.getId());
            channelInfo.setChannelIndex(channel.getChannelNo() != null ? channel.getChannelNo() - 1 : 0);
            channelInfo.setChannelName(channel.getChannelName());
            channelInfo.setDoorStatus(getDoorStatus(channel));
            channelInfo.setLockStatus(getLockStatus(channel));
            channelInfo.setAlwaysMode(getAlwaysMode(channel));
            
            // 从config中获取开门时长和报警时长
            if (channel.getConfig() != null) {
                try {
                    Integer openDuration = (Integer) channel.getConfig().get("openDuration");
                    Integer alarmDuration = (Integer) channel.getConfig().get("alarmDuration");
                    channelInfo.setOpenDuration(openDuration != null ? openDuration : 5);
                    channelInfo.setAlarmDuration(alarmDuration != null ? alarmDuration : 30);
                } catch (Exception e) {
                    channelInfo.setOpenDuration(5);
                    channelInfo.setAlarmDuration(30);
                }
            } else {
                channelInfo.setOpenDuration(5);
                channelInfo.setAlarmDuration(30);
            }
            
            channelInfos.add(channelInfo);
        }
        respVO.setChannels(channelInfos);
        
        return respVO;
    }

    private String getStateDesc(Integer state) {
        if (state == null) return "未知";
        if (state.equals(IotDeviceStateEnum.ONLINE.getState())) return "在线";
        if (state.equals(IotDeviceStateEnum.OFFLINE.getState())) return "离线";
        if (state.equals(IotDeviceStateEnum.INACTIVE.getState())) return "未激活";
        return "未知";
    }

    private String getDoorStatus(cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO channel) {
        if (channel.getConfig() != null && channel.getConfig().containsKey("doorStatus")) {
            Object status = channel.getConfig().get("doorStatus");
            if (status != null) return status.toString();
        }
        return "未知";
    }

    private String getLockStatus(cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO channel) {
        if (channel.getConfig() != null && channel.getConfig().containsKey("lockStatus")) {
            Object status = channel.getConfig().get("lockStatus");
            if (status != null) return status.toString();
        }
        return "未知";
    }

    private String getAlwaysMode(cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO channel) {
        if (channel.getConfig() != null && channel.getConfig().containsKey("alwaysMode")) {
            Object mode = channel.getConfig().get("alwaysMode");
            if (mode != null) return mode.toString();
        }
        return "正常";
    }

    private IotDeviceDO validateAccessDevice(Long deviceId) {
        IotDeviceDO device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw exception(ACCESS_DEVICE_NOT_EXISTS);
        }
        if (!ACCESS_SUBSYSTEM_CODE.equals(device.getSubsystemCode())) {
            throw exception(ACCESS_DEVICE_NOT_EXISTS);
        }
        return device;
    }

}
