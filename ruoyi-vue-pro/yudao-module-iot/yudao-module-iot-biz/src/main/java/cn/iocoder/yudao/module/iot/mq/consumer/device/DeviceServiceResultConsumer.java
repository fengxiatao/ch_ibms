package cn.iocoder.yudao.module.iot.mq.consumer.device;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.mq.consumer.device.handler.DeviceResultHandler;
import cn.iocoder.yudao.module.iot.mq.manager.DeviceCommandResponseManager;
import cn.iocoder.yudao.module.iot.service.channel.ChannelSyncRetryTemplate;
import cn.iocoder.yudao.module.iot.service.channel.IotDeviceChannelService;
import cn.iocoder.yudao.module.iot.service.channel.IotDeviceChannelService.AccessChannelSyncInfo;
import cn.iocoder.yudao.module.iot.service.channel.IotDeviceChannelService.AccessChannelSyncResult;
import cn.iocoder.yudao.module.iot.service.channel.IotDeviceChannelService.NvrChannelSyncInfo;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.websocket.DeviceMessagePushService;
import cn.iocoder.yudao.module.iot.websocket.message.unified.UnifiedCommandResultMessage;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备服务结果消费者
 * 
 * <p>统一处理所有设备命令的响应。</p>
 * 
 * <p>功能：</p>
 * <ul>
 *   <li>订阅 DEVICE_SERVICE_RESULT 主题</li>
 *   <li>根据 deviceType 路由到对应的处理器</li>
 *   <li>根据 serviceIdentifier 路由到特定处理逻辑（如 QUERY_CHANNELS）</li>
 *   <li>调用 DeviceMessagePushService 推送结果到前端</li>
 *   <li>通道同步失败时使用指数退避策略重试（最多3次）</li>
 * </ul>
 * 
 * <p>Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 9.7, 11.4, 11.5</p>
 *
 * @author 长辉信息科技有限公司
 */
@Component
@Slf4j
public class DeviceServiceResultConsumer implements IotMessageSubscriber<IotDeviceMessage> {

    /**
     * 消费者组名
     */
    private static final String CONSUMER_GROUP = "iot-biz-device-result";

    /**
     * 服务标识：查询通道
     */
    public static final String SERVICE_QUERY_CHANNELS = "QUERY_CHANNELS";
    public static final String SERVICE_SCAN_CHANNELS = "SCAN_CHANNELS";
    public static final String SERVICE_DISCOVER_CHANNELS = "DISCOVER_CHANNELS";
    public static final String SERVICE_LIST_CHANNELS = "LIST_CHANNELS";
    public static final String SERVICE_GET_CHANNELS = "GET_CHANNELS";

    /**
     * 服务标识：在线检测
     */
    public static final String SERVICE_CHECK_DEVICE_ONLINE = "CHECK_DEVICE_ONLINE";

    private final IotMessageBus messageBus;
    private final DeviceMessagePushService pushService;
    private final Map<String, DeviceResultHandler> handlers;
    private final IotDeviceChannelService channelService;
    private final ChannelSyncRetryTemplate channelSyncRetryTemplate;
    private final IotDeviceService deviceService;
    private final DeviceCommandResponseManager responseManager;

    /**
     * 构造函数
     * 
     * @param messageBus 消息总线
     * @param pushService WebSocket 推送服务
     * @param handlerList 设备结果处理器列表（Spring 自动注入所有实现）
     * @param channelService 设备通道服务（用于通道同步）
     * @param channelSyncRetryTemplate 通道同步重试模板
     * @param deviceService 设备服务
     * @param responseManager 命令响应管理器（用于同步请求-响应模式）
     */
    @Autowired
    public DeviceServiceResultConsumer(IotMessageBus messageBus,
                                        DeviceMessagePushService pushService,
                                        @Autowired(required = false) List<DeviceResultHandler> handlerList,
                                        @Autowired(required = false) IotDeviceChannelService channelService,
                                        @Autowired(required = false) ChannelSyncRetryTemplate channelSyncRetryTemplate,
                                        @Autowired(required = false) IotDeviceService deviceService,
                                        @Autowired(required = false) DeviceCommandResponseManager responseManager) {
        this.messageBus = messageBus;
        this.pushService = pushService;
        this.channelService = channelService;
        this.channelSyncRetryTemplate = channelSyncRetryTemplate;
        this.deviceService = deviceService;
        this.responseManager = responseManager;
        this.handlers = new HashMap<>();
        
        // 注册所有处理器
        if (handlerList != null) {
            for (DeviceResultHandler handler : handlerList) {
                handlers.put(handler.getDeviceType(), handler);
                log.info("[DeviceServiceResultConsumer] 注册处理器: deviceType={}", handler.getDeviceType());
            }
        }
    }

    /**
     * 初始化：注册到消息总线
     */
    @PostConstruct
    public void init() {
        messageBus.register(this);
        log.info("[DeviceServiceResultConsumer] 已注册到消息总线: topic={}, group={}", 
                getTopic(), getGroup());
    }

    @Override
    public String getTopic() {
        return IotMessageTopics.DEVICE_SERVICE_RESULT;
    }

    @Override
    public String getGroup() {
        return CONSUMER_GROUP;
    }

    @Override
    public void onMessage(IotDeviceMessage message) {
        if (message == null) {
            log.warn("[DeviceServiceResultConsumer] 收到空消息，忽略");
            return;
        }

        String deviceType = extractDeviceType(message);
        Long deviceId = message.getDeviceId();
        String requestId = message.getRequestId();
        boolean success = isSuccess(message);
        String serviceIdentifier = extractServiceIdentifier(message);

        log.info("[DeviceServiceResultConsumer] 收到响应: deviceType={}, deviceId={}, requestId={}, success={}, serviceIdentifier={}",
                deviceType, deviceId, requestId, success, serviceIdentifier);

        try {
            // 0. 通知响应管理器（用于同步请求-响应模式）
            // 这是统一的响应匹配入口，所有需要同步等待响应的服务都通过 DeviceCommandResponseManager 来等待
            if (responseManager != null && requestId != null) {
                responseManager.completeRequest(requestId, message);
            }

            // 1. 检查是否是“通道查询/扫描”响应，需要特殊处理
            if (isChannelQuery(serviceIdentifier)) {
                handleChannelQueryResult(message);
            }

            // 1.1 在线检测结果：补齐“刷新在线状态”闭环（即使 newgateway 没有额外发送 DEVICE_STATE_CHANGED）
            if (SERVICE_CHECK_DEVICE_ONLINE.equals(serviceIdentifier)) {
                handleOnlineCheckResult(message);
            }

            // 2. 路由到对应的处理器
            routeToHandler(deviceType, message);

            // 3. 推送到前端
            pushToFrontend(deviceId, deviceType, requestId, success, message);

        } catch (Exception e) {
            log.error("[DeviceServiceResultConsumer] 处理响应失败: deviceType={}, deviceId={}, requestId={}, error={}",
                    deviceType, deviceId, requestId, e.getMessage(), e);
        }
    }

    /**
     * 处理在线检测结果
     *
     * <p>场景：前端点击“刷新状态”，biz 发出 CHECK_DEVICE_ONLINE 命令。</p>
     * <p>newgateway 可能仅回传 DEVICE_SERVICE_RESULT(data.isOnline)，不一定额外发送 DEVICE_STATE_CHANGED。</p>
     * <p>因此 biz 在此兜底：落库更新设备 state，并通过统一 WS 推送 DEVICE_STATUS，保证前端即时可见。</p>
     */
    void handleOnlineCheckResult(IotDeviceMessage message) {
        if (message == null) {
            return;
        }
        if (!isSuccess(message)) {
            return;
        }
        if (deviceService == null) {
            log.warn("[DeviceServiceResultConsumer] IotDeviceService 未注入，跳过在线检测结果落库/推送");
            return;
        }

        Long deviceId = message.getDeviceId();
        String deviceType = extractDeviceType(message);
        Object dataObj = message.getData();

        Boolean isOnline = null;
        if (dataObj instanceof Map) {
            Object v = ((Map<?, ?>) dataObj).get("isOnline");
            if (v == null) {
                v = ((Map<?, ?>) dataObj).get("online");
            }
            if (v instanceof Boolean) {
                isOnline = (Boolean) v;
            } else if (v != null) {
                isOnline = Boolean.parseBoolean(v.toString());
            }
        }

        if (isOnline == null) {
            log.debug("[DeviceServiceResultConsumer] 在线检测结果无 isOnline 字段，跳过: deviceId={}, data={}", deviceId, dataObj);
            return;
        }

        Integer newState = Boolean.TRUE.equals(isOnline)
                ? IotDeviceStateEnum.ONLINE.getState()
                : IotDeviceStateEnum.OFFLINE.getState();
        Long ts = System.currentTimeMillis();

        // 1) 落库：更新 state/onlineTime/offlineTime
        // 说明：状态同步/批量刷新/在线检测可能并发更新同一行 iot_device，偶发 lock wait timeout。
        // 在线检测属于“观测型”操作，落库失败不应阻塞 MQ 消费线程或打断后续处理。
        boolean updated = false;
        for (int i = 0; i < 3; i++) {
            try {
                deviceService.updateDeviceStateWithTimestamp(deviceId, newState, ts);
                updated = true;
                break;
            } catch (CannotAcquireLockException e) {
                // 快速退避重试，避免长时间占用消费线程
                try {
                    Thread.sleep(100L * (i + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
                if (i == 2) {
                    log.warn("[DeviceServiceResultConsumer] 在线检测落库失败（锁等待超时，已放弃）: deviceId={}, newState={}, error={}",
                            deviceId, newState, e.getMessage());
                }
            }
        }

        // 2) WS 推送：统一设备状态消息（DEVICE_STATUS）
        pushService.pushDeviceStatus(deviceId, deviceType, Boolean.TRUE.equals(isOnline) ? "ONLINE" : "OFFLINE", ts);

        log.info("[DeviceServiceResultConsumer] 在线检测结果已推送{}: deviceId={}, deviceType={}, isOnline={}, newState={}",
                updated ? "并落库" : "（未落库）", deviceId, deviceType, isOnline, newState);
    }

    /**
     * 从消息中提取服务标识
     * 
     * @param message 设备消息
     * @return 服务标识，如果无法提取则返回 null
     */
    String extractServiceIdentifier(IotDeviceMessage message) {
        // 优先从 method 字段获取（作为服务标识）
        String method = message.getMethod();
        if (method != null && !method.isEmpty()) {
            return method;
        }
        
        // 从 params 中获取 commandType 作为备选
        Object params = message.getParams();
        if (params instanceof Map) {
            Object commandType = ((Map<?, ?>) params).get("commandType");
            if (commandType != null) {
                return commandType.toString();
            }
        }
        return null;
    }

    /**
     * 处理通道查询结果
     * <p>
     * 当收到 serviceIdentifier=QUERY_CHANNELS 的消息时，
     * 解析通道列表并调用 IotDeviceChannelService.syncAccessChannels 同步到数据库。
     * 同步失败时使用指数退避策略重试（最多3次）。
     * </p>
     * 
     * @param message 设备消息
     */
    void handleChannelQueryResult(IotDeviceMessage message) {
        Long deviceId = message.getDeviceId();
        boolean success = isSuccess(message);
        String deviceType = extractDeviceType(message);
        
        log.info("[DeviceServiceResultConsumer] 处理通道查询结果: deviceId={}, deviceType={}, success={}",
                deviceId, deviceType, success);
        
        // 如果查询失败，记录日志并返回
        if (!success) {
            log.warn("[DeviceServiceResultConsumer] 通道查询失败: deviceId={}, code={}, msg={}", 
                    deviceId, message.getCode(), message.getMsg());
            return;
        }
        
        // 检查 channelService 是否可用
        if (channelService == null) {
            log.warn("[DeviceServiceResultConsumer] IotDeviceChannelService 未注入，跳过通道同步");
            return;
        }
        
        try {
            // 解析通道列表 + 调用同步（按设备类型分流）
            AccessChannelSyncResult syncResult;

            // 门禁设备：沿用既有通道同步结构（AccessChannelSyncInfo）
            if (deviceType != null && (deviceType.startsWith("ACCESS") || "ACCESS".equalsIgnoreCase(deviceType))) {
                List<AccessChannelSyncInfo> channelList = parseChannelList(message);
                log.info("[DeviceServiceResultConsumer] 解析到 {} 个门禁通道: deviceId={}", channelList.size(), deviceId);

                if (channelSyncRetryTemplate != null) {
                    syncResult = channelSyncRetryTemplate.executeWithRetry(
                            () -> channelService.syncAccessChannels(deviceId, channelList),
                            deviceId
                    );
                } else {
                    log.debug("[DeviceServiceResultConsumer] ChannelSyncRetryTemplate 未注入，不使用重试");
                    syncResult = channelService.syncAccessChannels(deviceId, channelList);
                }
            } else if ("NVR".equalsIgnoreCase(deviceType)) {
                List<NvrChannelSyncInfo> channelList = parseNvrChannelList(message);
                log.info("[DeviceServiceResultConsumer] 解析到 {} 个NVR通道: deviceId={}", channelList.size(), deviceId);
                syncResult = channelService.syncNvrChannels(deviceId, channelList);
            } else {
                log.info("[DeviceServiceResultConsumer] 非门禁/NVR设备，跳过通道同步: deviceId={}, deviceType={}",
                        deviceId, deviceType);
                return;
            }
            
            if (syncResult.isSuccess()) {
                log.info("[DeviceServiceResultConsumer] ✅ 通道同步成功: deviceId={}, inserted={}, updated={}, deleted={}", 
                        deviceId, syncResult.getInsertedCount(), syncResult.getUpdatedCount(), syncResult.getDeletedCount());
            } else {
                log.error("[DeviceServiceResultConsumer] 通道同步失败: deviceId={}, error={}", 
                        deviceId, syncResult.getErrorMessage());
            }
            
        } catch (Exception e) {
            log.error("[DeviceServiceResultConsumer] 处理通道查询结果异常: deviceId={}", deviceId, e);
        }
    }

    /**
     * 是否为“通道查询/扫描”类服务标识
     */
    private boolean isChannelQuery(String serviceIdentifier) {
        if (serviceIdentifier == null || serviceIdentifier.isEmpty()) {
            return false;
        }
        return SERVICE_QUERY_CHANNELS.equals(serviceIdentifier)
                || SERVICE_SCAN_CHANNELS.equals(serviceIdentifier)
                || SERVICE_DISCOVER_CHANNELS.equals(serviceIdentifier)
                || SERVICE_LIST_CHANNELS.equals(serviceIdentifier)
                || SERVICE_GET_CHANNELS.equals(serviceIdentifier);
    }

    /**
     * 从消息中解析通道列表
     * 
     * @param message 设备消息
     * @return 通道信息列表，如果解析失败返回 null
     */
    @SuppressWarnings("unchecked")
    List<AccessChannelSyncInfo> parseChannelList(IotDeviceMessage message) {
        Object data = message.getData();
        if (data == null) {
            return new ArrayList<>();
        }
        
        // data 可能是 Map 或直接是 List
        List<Map<String, Object>> channelListRaw = null;
        
        if (data instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) data;
            Object channelListObj = dataMap.get("channelList");
            if (channelListObj instanceof List) {
                channelListRaw = (List<Map<String, Object>>) channelListObj;
            }
            // 兼容部分设备返回 channels 字段
            if (channelListRaw == null) {
                Object channelsObj = dataMap.get("channels");
                if (channelsObj instanceof List) {
                    channelListRaw = (List<Map<String, Object>>) channelsObj;
                }
            }
        } else if (data instanceof List) {
            channelListRaw = (List<Map<String, Object>>) data;
        }
        
        if (channelListRaw == null) {
            return new ArrayList<>();
        }
        
        // 转换为 AccessChannelSyncInfo 列表
        List<AccessChannelSyncInfo> result = new ArrayList<>();
        for (Map<String, Object> channelMap : channelListRaw) {
            AccessChannelSyncInfo info = AccessChannelSyncInfo.builder()
                    .channelNo(getInteger(channelMap, "channelNo"))
                    .channelName(getString(channelMap, "channelName"))
                    .channelType(getString(channelMap, "channelType"))
                    .status(getInteger(channelMap, "status"))
                    .capabilities(getMap(channelMap, "capabilities"))
                    .build();
            result.add(info);
        }
        
        return result;
    }

    /**
     * 解析 NVR 通道列表（兼容 newgateway NvrPlugin 的返回结构）
     */
    @SuppressWarnings("unchecked")
    List<NvrChannelSyncInfo> parseNvrChannelList(IotDeviceMessage message) {
        Object data = message.getData();
        if (data == null) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> raw = null;
        if (data instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) data;
            Object channelsObj = dataMap.get("channels");
            if (channelsObj instanceof List) {
                raw = (List<Map<String, Object>>) channelsObj;
            } else {
                // 兼容 channelList
                Object channelListObj = dataMap.get("channelList");
                if (channelListObj instanceof List) {
                    raw = (List<Map<String, Object>>) channelListObj;
                }
            }
        } else if (data instanceof List) {
            raw = (List<Map<String, Object>>) data;
        }

        if (raw == null) {
            return new ArrayList<>();
        }

        List<NvrChannelSyncInfo> result = new ArrayList<>();
        for (Map<String, Object> m : raw) {
            Integer channelNo = getInteger(m, "channelNo");
            String channelName = getString(m, "channelName");
            if (channelName == null) {
                channelName = getString(m, "name");
            }
            Boolean online = null;
            Object onlineObj = m.get("online");
            if (onlineObj instanceof Boolean) {
                online = (Boolean) onlineObj;
            } else if (onlineObj != null) {
                online = Boolean.parseBoolean(String.valueOf(onlineObj));
            }
            Boolean recording = null;
            Object recordingObj = m.get("recording");
            if (recordingObj instanceof Boolean) {
                recording = (Boolean) recordingObj;
            } else if (recordingObj != null) {
                recording = Boolean.parseBoolean(String.valueOf(recordingObj));
            }

            result.add(NvrChannelSyncInfo.builder()
                    .channelNo(channelNo)
                    .channelName(channelName)
                    .online(online)
                    .recording(recording)
                    .capabilities(getMap(m, "capabilities"))
                    .build());
        }
        return result;
    }

    /**
     * 从 Map 中获取 Integer 值
     */
    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
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

    /**
     * 从 Map 中获取 Map 值
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
    }

    /**
     * 从消息中提取设备类型
     * 
     * @param message 设备消息
     * @return 设备类型，如果无法提取则返回 "UNKNOWN"
     */
    String extractDeviceType(IotDeviceMessage message) {
        Object params = message.getParams();
        if (params instanceof Map) {
            Object deviceType = ((Map<?, ?>) params).get("deviceType");
            if (deviceType != null) {
                return deviceType.toString();
            }
        }
        return "UNKNOWN";
    }

    /**
     * 判断命令是否执行成功
     * 
     * @param message 设备消息
     * @return 是否成功
     */
    boolean isSuccess(IotDeviceMessage message) {
        Integer code = message.getCode();
        return code != null && code == 0;
    }

    /**
     * 路由到对应的处理器
     * 
     * @param deviceType 设备类型
     * @param message 设备消息
     */
    void routeToHandler(String deviceType, IotDeviceMessage message) {
        DeviceResultHandler handler = handlers.get(deviceType);
        if (handler != null) {
            try {
                handler.handleResult(message);
                log.debug("[DeviceServiceResultConsumer] 处理器执行完成: deviceType={}", deviceType);
            } catch (Exception e) {
                log.error("[DeviceServiceResultConsumer] 处理器执行失败: deviceType={}, error={}",
                        deviceType, e.getMessage(), e);
            }
        } else {
            log.debug("[DeviceServiceResultConsumer] 未找到处理器: deviceType={}", deviceType);
        }
    }

    /**
     * 推送结果到前端
     * 
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param requestId 请求ID
     * @param success 是否成功
     * @param message 原始消息
     */
    void pushToFrontend(Long deviceId, String deviceType, String requestId, 
                               boolean success, IotDeviceMessage message) {
        try {
            UnifiedCommandResultMessage resultMessage = UnifiedCommandResultMessage.of(
                    requestId,
                    deviceId,
                    deviceType,
                    success,
                    message.getMsg(),
                    message.getData()
            );
            
            pushService.pushCommandResult(resultMessage);
            log.debug("[DeviceServiceResultConsumer] 已推送到前端: deviceId={}, requestId={}", 
                    deviceId, requestId);
        } catch (Exception e) {
            log.error("[DeviceServiceResultConsumer] 推送到前端失败: deviceId={}, requestId={}, error={}",
                    deviceId, requestId, e.getMessage(), e);
        }
    }

    /**
     * 获取已注册的处理器数量（用于测试）
     * 
     * @return 处理器数量
     */
    int getHandlerCount() {
        return handlers.size();
    }

    /**
     * 检查是否有指定设备类型的处理器（用于测试）
     * 
     * @param deviceType 设备类型
     * @return 是否存在处理器
     */
    boolean hasHandler(String deviceType) {
        return handlers.containsKey(deviceType);
    }

    /**
     * 检查通道服务是否可用（用于测试）
     * 
     * @return 是否可用
     */
    boolean hasChannelService() {
        return channelService != null;
    }

    /**
     * 检查通道同步重试模板是否可用（用于测试）
     * 
     * @return 是否可用
     */
    boolean hasChannelSyncRetryTemplate() {
        return channelSyncRetryTemplate != null;
    }
}
