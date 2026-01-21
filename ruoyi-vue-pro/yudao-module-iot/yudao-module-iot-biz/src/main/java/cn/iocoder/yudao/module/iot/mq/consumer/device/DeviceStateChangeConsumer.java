package cn.iocoder.yudao.module.iot.mq.consumer.device;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.gateway.dto.DeviceStateChangeMessage;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmHostDO;
import cn.iocoder.yudao.module.iot.dal.mysql.alarm.IotAlarmHostMapper;
import cn.iocoder.yudao.module.iot.enums.device.AccessDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.enums.device.AlarmDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.enums.device.ChanghuiDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.enums.device.NvrDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.mq.consumer.device.handler.DeviceStateHandler;
import cn.iocoder.yudao.module.iot.mq.producer.DeviceCommandPublisher;
import cn.iocoder.yudao.module.iot.service.access.IotAccessAuthDispatchService;
import cn.iocoder.yudao.module.iot.service.changhui.upgrade.ChanghuiUpgradeService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.activation.DeviceActivationStateManager;
import cn.iocoder.yudao.module.iot.service.device.discovery.DiscoveredDeviceService;
import cn.iocoder.yudao.module.iot.websocket.DeviceMessagePushService;
import cn.iocoder.yudao.module.iot.websocket.IotWebSocketHandler;
import cn.iocoder.yudao.module.iot.websocket.message.AlarmHostStatusMessage;
import cn.iocoder.yudao.module.iot.websocket.message.IotMessage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备状态变更消费者（统一版本）
 * 
 * <p>统一处理所有设备的状态变更消息。</p>
 * 
 * <p>功能：</p>
 * <ul>
 *   <li>订阅 DEVICE_STATE_CHANGED 主题</li>
 *   <li>更新数据库中的设备状态</li>
 *   <li>调用 DeviceMessagePushService 推送状态到前端（统一格式）</li>
 *   <li>根据 deviceType 路由到不同的状态处理器执行设备特定逻辑</li>
 *   <li>门禁设备上线时自动触发通道同步</li>
 *   <li>报警主机设备状态变化时同步更新报警主机表并推送 WebSocket</li>
 *   <li>NVR 设备状态变化时执行 NVR 特定逻辑</li>
 *   <li>长辉设备状态变化时执行长辉特定逻辑</li>
 * </ul>
 * 
 * <p>Requirements: 2.1, 2.2, 2.5, 5.1, 5.2, 5.3, 5.4, 5.5, 8.1, 8.2, 9.6</p>
 *
 * @author 长辉信息科技有限公司
 */
@Component("unifiedDeviceStateChangeConsumer")
@Slf4j
public class DeviceStateChangeConsumer implements IotMessageSubscriber<DeviceStateChangeMessage> {

    /**
     * 消费者组名
     */
    private static final String CONSUMER_GROUP = "iot-biz-device-state";

    /**
     * 查询通道命令
     */
    public static final String CMD_QUERY_CHANNELS = "QUERY_CHANNELS";

    /**
     * 设备上线后延迟同步时间（毫秒）
     */
    private static final long CHANNEL_SYNC_DELAY_MS = 2000;

    private final IotMessageBus messageBus;
    private final IotDeviceService deviceService;
    private final DeviceMessagePushService pushService;
    private final DeviceCommandPublisher commandPublisher;
    private final Map<String, DeviceStateHandler> stateHandlers;
    
    @Resource
    private IotAlarmHostMapper alarmHostMapper;
    
    @Resource(name = "iotWebSocketHandler")
    private IotWebSocketHandler webSocketHandler;
    
    @Resource
    private ChanghuiUpgradeService changhuiUpgradeService;
    
    @Resource
    private IotAccessAuthDispatchService accessAuthDispatchService;
    
    @Resource
    private DeviceActivationStateManager activationStateManager;
    
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private DiscoveredDeviceService discoveredDeviceService;
    
    @Resource
    private cn.iocoder.yudao.module.iot.service.channel.IotDeviceChannelService channelService;

    /**
     * 构造函数
     * 
     * @param messageBus 消息总线
     * @param deviceService 设备服务
     * @param pushService WebSocket 推送服务
     * @param commandPublisher 命令发布器
     * @param handlerList 设备状态处理器列表（Spring 自动注入所有实现）
     */
    @Autowired
    public DeviceStateChangeConsumer(IotMessageBus messageBus,
                                      IotDeviceService deviceService,
                                      DeviceMessagePushService pushService,
                                      DeviceCommandPublisher commandPublisher,
                                      @Autowired(required = false) List<DeviceStateHandler> handlerList) {
        this.messageBus = messageBus;
        this.deviceService = deviceService;
        this.pushService = pushService;
        this.commandPublisher = commandPublisher;
        this.stateHandlers = new HashMap<>();
        
        // 注册所有状态处理器
        if (handlerList != null) {
            for (DeviceStateHandler handler : handlerList) {
                stateHandlers.put(handler.getDeviceType(), handler);
                log.info("[DeviceStateChangeConsumer] 注册状态处理器: deviceType={}", handler.getDeviceType());
            }
        }
    }

    /**
     * 初始化：注册到消息总线
     */
    @PostConstruct
    public void init() {
        // 打印消息总线类型，便于诊断问题
        String busType = messageBus.getClass().getSimpleName();
        log.info("[DeviceStateChangeConsumer] 消息总线类型: {}", busType);
        
        // 检查是否使用了正确的消息总线
        if ("IotLocalMessageBus".equals(busType)) {
            log.warn("[DeviceStateChangeConsumer] ⚠️ 警告：使用的是本地消息总线，无法接收跨进程消息！" +
                    "请检查配置 yudao.iot.message-bus.type 是否设置为 rocketmq");
        }
        
        messageBus.register(this);
        log.info("[DeviceStateChangeConsumer] 已注册到消息总线: topic={}, group={}, busType={}", 
                getTopic(), getGroup(), busType);
    }

    @Override
    public String getTopic() {
        return IotMessageTopics.DEVICE_STATE_CHANGED;
    }

    @Override
    public String getGroup() {
        return CONSUMER_GROUP;
    }

    @Override
    public void onMessage(DeviceStateChangeMessage message) {
        // 入口日志 - 确保每条消息都被记录
        log.info("[DeviceStateChangeConsumer] ========== 收到RocketMQ消息 ==========");
        
        if (message == null) {
            log.warn("[DeviceStateChangeConsumer] 收到空消息，忽略");
            return;
        }

        Long deviceId = message.getDeviceId();
        Long tenantId = message.getTenantId();
        String deviceType = message.getDeviceType();
        String newStateName = message.getNewStateName();

        log.info("[DeviceStateChangeConsumer] 收到状态变更: deviceId={}, deviceType={}, " +
                        "previousState={}, newState={}, reason={}, tenantId={}",
                deviceId, deviceType, message.getPreviousStateName(),
                newStateName, message.getReason(), tenantId);

        try {
            // 在租户上下文中执行业务逻辑
            Runnable processLogic = () -> processStateChange(message);

            if (tenantId != null) {
                TenantUtils.execute(tenantId, processLogic);
            } else {
                log.warn("[DeviceStateChangeConsumer] 消息中无租户ID，将忽略租户过滤: deviceId={}", deviceId);
                TenantUtils.executeIgnore(processLogic);
            }
        } catch (Exception e) {
            log.error("[DeviceStateChangeConsumer] 处理状态变更失败: deviceId={}, deviceType={}, error={}",
                    deviceId, deviceType, e.getMessage(), e);
        }
    }

    /**
     * 处理设备状态变更
     * 
     * <p>这是设备状态变更的统一入口，所有设备状态变更都通过此方法处理。</p>
     * 
     * @param message 状态变更消息
     */
    void processStateChange(DeviceStateChangeMessage message) {
        Long deviceId = message.getDeviceId();
        Integer newState = message.getNewState();
        String deviceType = message.getDeviceType();
        boolean isOnline = isOnlineState(newState);

        try {
            // 1. 更新数据库设备状态（所有设备类型通用）
            // Requirements: 5.2 - 更新数据库中的设备状态
            updateDatabaseStatus(deviceId, newState, message.getTimestamp());

            // 2. 推送状态到前端（统一格式）
            // Requirements: 5.3, 8.1, 8.2 - 通过 WebSocket 推送状态变更
            pushStatusToFrontend(deviceId, deviceType, newState, message.getTimestamp());

            // 3. 路由到设备类型特定的状态处理器
            // Requirements: 2.5 - 根据 deviceType 执行不同的状态更新逻辑
            routeToStateHandler(deviceType, message);

            // 4. 处理设备上线相关逻辑（统一入口）
            if (isOnline) {
                handleDeviceOnlineUnified(deviceId, deviceType);
            } else if (isOfflineState(newState)) {
                // 5. 处理设备离线相关逻辑（统一入口）
                handleDeviceOfflineUnified(deviceId, message.getReason());
            }
            
            // 6. 报警主机设备状态变化时同步更新报警主机表
            // Requirements: 2.1, 2.2, 8.1, 8.2 - 从 DeviceOnlineConsumer/DeviceOfflineConsumer 迁移
            if (isAlarmDevice(deviceType)) {
                syncAlarmHostStatus(deviceId, isOnline);
            }
            
            // 7. NVR 设备状态变化时执行 NVR 特定逻辑
            if (isNvrDevice(deviceType)) {
                handleNvrStateChange(deviceId, newState, message);
            }
            
            // 8. 长辉设备状态变化时执行长辉特定逻辑
            if (isChanghuiDevice(deviceType)) {
                handleChanghuiStateChange(deviceId, newState, message);
            }

            log.info("[DeviceStateChangeConsumer] 状态变更处理完成: deviceId={}, deviceType={}, newState={}",
                    deviceId, deviceType, message.getNewStateName());

        } catch (Exception e) {
            log.error("[DeviceStateChangeConsumer] 处理状态变更失败: deviceId={}, newState={}, error={}",
                    deviceId, newState, e.getMessage(), e);
        }
    }
    
    /**
     * 统一处理设备上线逻辑
     * 
     * <p>这是设备上线的统一入口，取代原来分散在 IotDeviceActivationServiceImpl 中的逻辑。</p>
     * 
     * <p>处理流程：</p>
     * <ol>
     *   <li>更新激活状态为完成</li>
     *   <li>清理设备发现记录</li>
     *   <li>触发通道同步（NVR/PTZ/门禁设备）</li>
     *   <li>执行待撤销授权（门禁设备）</li>
     * </ol>
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     */
    private void handleDeviceOnlineUnified(Long deviceId, String deviceType) {
        log.info("[DeviceStateChangeConsumer] 统一处理设备上线: deviceId={}, deviceType={}", deviceId, deviceType);
        
        // 1. 更新激活状态为完成
        activationStateManager.completeActivation(deviceId);
        
        // 2. 清理设备发现记录
        cleanupDiscoveryRecord(deviceId);
        
        // 3. NVR 设备：不再由 Biz 层触发通道同步
        // 原因：Gateway 层的 NvrPlugin.login() 成功后会自动调用 asyncQueryChannelsAfterLogin()
        //       直接通过 SDK 查询通道并发布结果，更可靠且避免时序问题
        // 如果 Biz 层也触发，会导致：
        //   - 重复查询（Gateway 已查询一次）
        //   - 时序问题（Biz 发送的 SCAN_CHANNELS 命令到达时设备可能还未就绪）
        if (activationStateManager.checkAndClearNeedsSyncChannel(deviceId)) {
            if (isNvrDevice(deviceType)) {
                log.info("[DeviceStateChangeConsumer] NVR设备由Gateway层自动同步通道，Biz层跳过: deviceId={}", deviceId);
            } else {
                // 非 NVR 设备（如 IPC/PTZ）仍可能需要同步
                log.info("[DeviceStateChangeConsumer] 设备需要通道同步: deviceId={}", deviceId);
                triggerNvrChannelSync(deviceId);
            }
        }
        
        // 4. 门禁设备上线时触发通道同步和执行待撤销授权
        if (isAccessDevice(deviceType)) {
            triggerChannelSync(deviceId, deviceType);
            executePendingRevokesOnDeviceOnline(deviceId);
        }
    }
    
    /**
     * 统一处理设备离线逻辑
     * 
     * <p>这是设备离线的统一入口，取代原来分散在 IotDeviceActivationServiceImpl 中的逻辑。</p>
     * 
     * @param deviceId 设备ID
     * @param reason 离线原因
     */
    private void handleDeviceOfflineUnified(Long deviceId, String reason) {
        log.info("[DeviceStateChangeConsumer] 统一处理设备离线: deviceId={}, reason={}", deviceId, reason);
        
        // 更新激活状态为失败（如果正在激活中）
        activationStateManager.failActivation(deviceId, reason);
    }
    
    /**
     * 清理设备发现记录
     * 
     * @param deviceId 设备ID
     */
    private void cleanupDiscoveryRecord(Long deviceId) {
        if (discoveredDeviceService == null) {
            return;
        }
        
        try {
            // 通过设备ID获取IP地址，然后标记为已激活
            cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO device = 
                    deviceService.getDevice(deviceId);
            if (device != null) {
                String deviceIp = cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfigHelper
                        .getIpAddress(device);
                if (deviceIp != null) {
                    discoveredDeviceService.markAsActivated(deviceIp, deviceId);
                    log.info("[DeviceStateChangeConsumer] 已清理设备发现记录: ip={}, deviceId={}", 
                            deviceIp, deviceId);
                }
            }
        } catch (Exception e) {
            log.error("[DeviceStateChangeConsumer] 清理设备发现记录失败: deviceId={}, error={}", 
                    deviceId, e.getMessage(), e);
        }
    }
    
    /**
     * 触发 NVR/PTZ 设备通道同步
     * 
     * <p>延迟2秒后调用通道服务进行同步</p>
     * 
     * @param deviceId 设备ID
     */
    @Async
    void triggerNvrChannelSync(Long deviceId) {
        try {
            Thread.sleep(CHANNEL_SYNC_DELAY_MS);
            
            // 调用通道服务同步
            Integer syncCount = channelService.syncDeviceChannels(deviceId);
            log.info("[DeviceStateChangeConsumer] NVR/PTZ 通道同步完成: deviceId={}, syncCount={}", 
                    deviceId, syncCount);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[DeviceStateChangeConsumer] NVR 通道同步被中断: deviceId={}", deviceId);
        } catch (Exception e) {
            log.error("[DeviceStateChangeConsumer] NVR 通道同步失败: deviceId={}, error={}", 
                    deviceId, e.getMessage(), e);
        }
    }
    
    /**
     * 路由到设备类型特定的状态处理器
     * 
     * <p>Requirements: 2.5 - 根据 deviceType 执行不同的状态更新逻辑</p>
     * 
     * @param deviceType 设备类型
     * @param message 状态变更消息
     */
    void routeToStateHandler(String deviceType, DeviceStateChangeMessage message) {
        DeviceStateHandler handler = stateHandlers.get(deviceType);
        if (handler != null) {
            try {
                handler.handleStateChange(message);
                log.debug("[DeviceStateChangeConsumer] 状态处理器执行完成: deviceType={}", deviceType);
            } catch (Exception e) {
                log.error("[DeviceStateChangeConsumer] 状态处理器执行失败: deviceType={}, error={}",
                        deviceType, e.getMessage(), e);
            }
        } else {
            log.debug("[DeviceStateChangeConsumer] 未找到状态处理器: deviceType={}", deviceType);
        }
    }

    /**
     * 更新数据库设备状态
     * 
     * @param deviceId 设备ID
     * @param newState 新状态
     * @param timestamp 时间戳
     */
    void updateDatabaseStatus(Long deviceId, Integer newState, Long timestamp) {
        try {
            deviceService.updateDeviceStateWithTimestamp(deviceId, newState, timestamp);
            log.debug("[DeviceStateChangeConsumer] 数据库状态更新成功: deviceId={}, newState={}", 
                    deviceId, newState);
        } catch (Exception e) {
            log.error("[DeviceStateChangeConsumer] 数据库状态更新失败: deviceId={}, newState={}, error={}",
                    deviceId, newState, e.getMessage(), e);
            // 数据库更新失败不影响 WebSocket 推送
        }
    }

    /**
     * 推送状态到前端
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param newState 新状态
     * @param timestamp 时间戳
     */
    void pushStatusToFrontend(Long deviceId, String deviceType, Integer newState, Long timestamp) {
        try {
            String status = convertStateToStatus(newState);
            pushService.pushDeviceStatus(deviceId, deviceType, status, timestamp);
            log.debug("[DeviceStateChangeConsumer] WebSocket推送成功: deviceId={}, status={}", 
                    deviceId, status);
        } catch (Exception e) {
            log.error("[DeviceStateChangeConsumer] WebSocket推送失败: deviceId={}, error={}",
                    deviceId, e.getMessage(), e);
            // WebSocket 推送失败不影响主流程
        }
    }

    /**
     * 将状态码转换为状态字符串
     * 
     * @param state 状态码
     * @return 状态字符串
     */
    String convertStateToStatus(Integer state) {
        if (state == null) {
            return "UNKNOWN";
        }
        IotDeviceStateEnum stateEnum = IotDeviceStateEnum.fromState(state);
        if (stateEnum == null) {
            return "UNKNOWN";
        }
        return stateEnum.name();
    }

    /**
     * 判断是否为在线状态
     * 
     * @param state 状态码
     * @return 是否在线
     */
    boolean isOnlineState(Integer state) {
        return IotDeviceStateEnum.isOnline(state);
    }
    
    /**
     * 判断是否为离线状态
     * 
     * @param state 状态码
     * @return 是否离线
     */
    boolean isOfflineState(Integer state) {
        return state != null && state.equals(IotDeviceStateEnum.OFFLINE.getState());
    }

    /**
     * 判断是否为门禁设备
     * 
     * <p>Requirements: 9.6 - 判断设备类型是否为门禁设备</p>
     * 
     * @param deviceType 设备类型
     * @return 是否为门禁设备
     */
    boolean isAccessDevice(String deviceType) {
        if (deviceType == null) {
            return false;
        }
        return AccessDeviceTypeConstants.ACCESS_GEN1.equals(deviceType) 
                || AccessDeviceTypeConstants.ACCESS_GEN2.equals(deviceType);
    }

    /**
     * 触发通道同步
     * 
     * <p>Requirements: 9.6, 9.9 - 门禁设备上线时自动触发通道同步</p>
     * <p>延迟2秒等待设备就绪后发送 QUERY_CHANNELS 命令</p>
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     */
    @Async
    void triggerChannelSync(Long deviceId, String deviceType) {
        try {
            // 延迟等待设备就绪
            Thread.sleep(CHANNEL_SYNC_DELAY_MS);

            // 发送 QUERY_CHANNELS 命令
            String requestId = commandPublisher.publishCommand(deviceType, deviceId, CMD_QUERY_CHANNELS);

            log.info("[DeviceStateChangeConsumer] 已触发通道同步: deviceId={}, deviceType={}, requestId={}",
                    deviceId, deviceType, requestId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[DeviceStateChangeConsumer] 通道同步被中断: deviceId={}", deviceId);
        } catch (Exception e) {
            log.error("[DeviceStateChangeConsumer] 触发通道同步失败: deviceId={}, deviceType={}, error={}",
                    deviceId, deviceType, e.getMessage(), e);
        }
    }
    
    /**
     * 门禁设备上线时执行待撤销的授权
     * 
     * <p>Requirements: 7.4 - WHEN 授权状态为待撤销 THEN 系统 SHALL 在设备上线后自动执行撤销</p>
     * 
     * @param deviceId 设备ID
     */
    void executePendingRevokesOnDeviceOnline(Long deviceId) {
        try {
            log.info("[DeviceStateChangeConsumer] 门禁设备上线，检查并执行待撤销授权: deviceId={}", deviceId);
            
            // 调用授权下发服务执行待撤销任务（异步执行）
            accessAuthDispatchService.executePendingRevokes(deviceId);
            
        } catch (Exception e) {
            // 待撤销执行失败不应影响设备上线流程，只记录日志
            log.error("[DeviceStateChangeConsumer] 执行待撤销授权失败: deviceId={}, error={}",
                    deviceId, e.getMessage(), e);
        }
    }

    /**
     * 获取命令发布器（仅用于测试）
     * 
     * @return 命令发布器
     */
    DeviceCommandPublisher getCommandPublisher() {
        return commandPublisher;
    }
    
    /**
     * 判断是否为报警设备
     * 
     * <p>Requirements: 2.1, 2.2 - 判断设备类型是否为报警设备</p>
     * 
     * @param deviceType 设备类型
     * @return 是否为报警设备
     */
    boolean isAlarmDevice(String deviceType) {
        if (deviceType == null) {
            return false;
        }
        return AlarmDeviceTypeConstants.ALARM.equals(deviceType);
    }
    
    /**
     * 判断是否为 NVR 设备
     * 
     * <p>Requirements: 2.5 - 判断设备类型是否为 NVR 设备</p>
     * 
     * @param deviceType 设备类型
     * @return 是否为 NVR 设备
     */
    boolean isNvrDevice(String deviceType) {
        if (deviceType == null) {
            return false;
        }
        return NvrDeviceTypeConstants.NVR.equals(deviceType);
    }
    
    /**
     * 判断是否为长辉设备
     * 
     * <p>Requirements: 2.5 - 判断设备类型是否为长辉设备</p>
     * 
     * @param deviceType 设备类型
     * @return 是否为长辉设备
     */
    boolean isChanghuiDevice(String deviceType) {
        if (deviceType == null) {
            return false;
        }
        return ChanghuiDeviceTypeConstants.CHANGHUI.equals(deviceType);
    }
    
    /**
     * 处理 NVR 设备状态变化
     * 
     * <p>Requirements: 2.5, 8.1, 8.2 - NVR 设备状态变化时执行特定逻辑</p>
     * 
     * @param deviceId 设备ID
     * @param newState 新状态
     * @param message 状态变更消息
     */
    void handleNvrStateChange(Long deviceId, Integer newState, DeviceStateChangeMessage message) {
        try {
            boolean isOnline = isOnlineState(newState);
            log.info("[DeviceStateChangeConsumer] NVR 设备状态变化: deviceId={}, isOnline={}", 
                    deviceId, isOnline);
            
            // NVR 设备上线时可以触发通道扫描等操作
            // 目前仅记录日志，后续可扩展
            if (isOnline) {
                log.debug("[DeviceStateChangeConsumer] NVR 设备上线，可触发通道扫描: deviceId={}", deviceId);
            }
        } catch (Exception e) {
            log.error("[DeviceStateChangeConsumer] 处理 NVR 状态变化失败: deviceId={}, error={}",
                    deviceId, e.getMessage(), e);
        }
    }
    
    /**
     * 处理长辉设备状态变化
     * 
     * <p>Requirements: 2.5, 8.1, 8.2 - 长辉设备状态变化时执行特定逻辑</p>
     * 
     * @param deviceId 设备ID
     * @param newState 新状态
     * @param message 状态变更消息
     */
    void handleChanghuiStateChange(Long deviceId, Integer newState, DeviceStateChangeMessage message) {
        try {
            boolean isOnline = isOnlineState(newState);
            log.info("[DeviceStateChangeConsumer] 长辉设备状态变化: deviceId={}, isOnline={}", 
                    deviceId, isOnline);
            
            if (isOnline) {
                // 设备上线时，触发待执行的升级任务
                log.info("[DeviceStateChangeConsumer] 长辉设备上线，检查待执行升级任务: deviceId={}", deviceId);
                try {
                    changhuiUpgradeService.triggerPendingUpgradeOnDeviceOnline(deviceId);
                } catch (Exception e) {
                    log.error("[DeviceStateChangeConsumer] 触发待执行升级任务失败: deviceId={}, error={}",
                            deviceId, e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("[DeviceStateChangeConsumer] 处理长辉设备状态变化失败: deviceId={}, error={}",
                    deviceId, e.getMessage(), e);
        }
    }
    
    /**
     * 同步报警主机状态
     * 
     * <p>Requirements: 2.1, 2.2, 8.1, 8.2 - 从 DeviceOnlineConsumer/DeviceOfflineConsumer 迁移</p>
     * <p>当报警主机设备状态变化时，同步更新报警主机表并推送 WebSocket</p>
     * 
     * <p>注意：此方法只会被报警设备的状态变化触发。</p>
     * 
     * @param deviceId 设备ID
     * @param isOnline 是否在线
     */
    void syncAlarmHostStatus(Long deviceId, boolean isOnline) {
        try {
            List<IotAlarmHostDO> hosts = alarmHostMapper.selectListByDeviceId(deviceId);
            if (hosts == null || hosts.isEmpty()) {
                log.debug("[DeviceStateChangeConsumer] 设备无关联的报警主机记录: deviceId={}", deviceId);
                return;
            }
            
            int onlineStatus = isOnline ? 1 : 0;
            for (IotAlarmHostDO host : hosts) {
                // 更新数据库
                IotAlarmHostDO update = new IotAlarmHostDO();
                update.setId(host.getId());
                update.setOnlineStatus(onlineStatus);
                alarmHostMapper.updateById(update);
                
                // 推送 WebSocket
                AlarmHostStatusMessage statusMsg = AlarmHostStatusMessage.builder()
                        .hostId(host.getId())
                        .account(host.getAccount())
                        .onlineStatus(onlineStatus)
                        .build();
                webSocketHandler.broadcast(IotMessage.alarmHostStatus(statusMsg));
                
                log.info("[DeviceStateChangeConsumer] 已同步并推送报警主机{}状态: hostId={}, onlineStatus={}",
                        isOnline ? "上线" : "离线", host.getId(), onlineStatus);
            }
        } catch (Exception ex) {
            log.error("[DeviceStateChangeConsumer] 同步报警主机状态失败: deviceId={}, isOnline={}, error={}",
                    deviceId, isOnline, ex.getMessage(), ex);
        }
    }
}
