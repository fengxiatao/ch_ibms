package cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2;

import cn.iocoder.yudao.module.iot.core.enums.ConnectionMode;
import cn.iocoder.yudao.module.iot.core.gateway.dto.AccessControlEventMessage;
import cn.iocoder.yudao.module.iot.core.gateway.dto.DeviceInfo;
import cn.iocoder.yudao.module.iot.core.gateway.dto.access.NetAccessFaceInfo;
import cn.iocoder.yudao.module.iot.core.gateway.dto.access.NetAccessUserInfo;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.newgateway.core.annotation.DevicePlugin;
import cn.iocoder.yudao.module.iot.newgateway.core.handler.ActiveDeviceHandler;
import cn.iocoder.yudao.module.iot.newgateway.core.lifecycle.DeviceLifecycleManager;
import cn.iocoder.yudao.module.iot.newgateway.core.message.GatewayMessagePublisher;
import cn.iocoder.yudao.module.iot.newgateway.core.model.CommandResult;
import cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceCommand;
import cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceConnectionInfo;
import cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceStatus;
import cn.iocoder.yudao.module.iot.newgateway.core.model.LoginResult;
import cn.iocoder.yudao.module.iot.newgateway.core.session.DeviceSessionRegistry;
import cn.iocoder.yudao.module.iot.newgateway.core.session.SimpleDeviceSession;
import cn.iocoder.yudao.module.iot.newgateway.plugins.PluginConstants;
import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.adapter.AccessGen2AdapterFactory;
import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.adapter.AccessGen2VendorAdapter;
import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.dto.*;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;
import com.sun.jna.Pointer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 门禁二代插件
 * 
 * <p>实现大华门禁二代设备的接入，使用标准 API 进行用户/卡/人脸管理：</p>
 * <ul>
 *     <li>远程开门（OPEN_DOOR）</li>
 *     <li>远程关门（CLOSE_DOOR）</li>
 *     <li>下发授权（DISPATCH_AUTH）- 通过标准 API</li>
 *     <li>撤销授权（REVOKE_AUTH）- 通过标准 API</li>
 *     <li>下发人脸（DISPATCH_FACE）</li>
 *     <li>下发指纹（DISPATCH_FINGERPRINT）</li>
 * </ul>
 * 
 * <h2>设备特点</h2>
 * <p>门禁二代设备使用大华 NetSDK 进行通信，通过标准 API 管理用户、卡号和生物特征。
 * 与门禁一代不同，二代设备支持人脸识别和指纹识别功能。</p>
 * 
 * <h2>连接模式</h2>
 * <p>门禁二代设备采用主动连接模式（ACTIVE），由平台主动登录设备。
 * 登录成功后需要定期执行保活检测，确保连接有效。</p>
 * 
 * @author IoT Gateway Team
 * @see ActiveDeviceHandler
 * @see AccessGen2Config
 */
@DevicePlugin(
    id = PluginConstants.PLUGIN_ID_ACCESS_GEN2,
    name = "门禁二代",
    deviceType = PluginConstants.DEVICE_TYPE_ACCESS_GEN2,
    vendor = "Dahua",
    description = "大华门禁二代设备，使用标准 API 进行用户/卡/人脸管理，支持人脸和指纹识别",
    capabilityRefreshEnabled = true,
    enabledByDefault = true
)
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "access-gen2", havingValue = "true", matchIfMissing = true)
@Slf4j
@RequiredArgsConstructor
public class AccessGen2Plugin implements ActiveDeviceHandler {

    /**
     * 日志前缀
     */
    private static final String LOG_PREFIX = "[AccessGen2Plugin]";


    // ==================== 命令类型常量 ====================

    /**
     * 远程开门命令
     */
    public static final String CMD_OPEN_DOOR = "OPEN_DOOR";

    /**
     * 远程关门命令
     */
    public static final String CMD_CLOSE_DOOR = "CLOSE_DOOR";

    /**
     * 下发授权命令（通过标准 API）
     */
    public static final String CMD_DISPATCH_AUTH = "DISPATCH_AUTH";

    /**
     * 撤销授权命令（通过标准 API）
     */
    public static final String CMD_REVOKE_AUTH = "REVOKE_AUTH";

    /**
     * 查询授权命令
     */
    public static final String CMD_QUERY_AUTH = "QUERY_AUTH";

    /**
     * 清空所有授权命令
     */
    public static final String CMD_CLEAR_ALL_AUTH = "CLEAR_ALL_AUTH";

    /**
     * 下发人脸命令
     */
    public static final String CMD_DISPATCH_FACE = "DISPATCH_FACE";

    /**
     * 删除人脸命令
     */
    public static final String CMD_DELETE_FACE = "DELETE_FACE";

    /**
     * 下发指纹命令
     */
    public static final String CMD_DISPATCH_FINGERPRINT = "DISPATCH_FINGERPRINT";

    /**
     * 删除指纹命令
     */
    public static final String CMD_DELETE_FINGERPRINT = "DELETE_FINGERPRINT";

    /**
     * 查询通道命令
     */
    public static final String CMD_QUERY_CHANNELS = "QUERY_CHANNELS";

    /**
     * 检查设备在线状态命令
     */
    public static final String CMD_CHECK_DEVICE_ONLINE = "CHECK_DEVICE_ONLINE";

    /**
     * 激活设备命令
     */
    public static final String CMD_ACTIVATE_DEVICE = "ACTIVATE_DEVICE";

    /**
     * 获取登录句柄命令
     */
    public static final String CMD_GET_LOGIN_HANDLE = "GET_LOGIN_HANDLE";

    /**
     * 查询设备能力命令
     */
    public static final String CMD_QUERY_DEVICE_CAPABILITY = "QUERY_DEVICE_CAPABILITY";

    /**
     * 常开门命令
     */
    public static final String CMD_ALWAYS_OPEN = "ALWAYS_OPEN";

    /**
     * 常闭门命令
     */
    public static final String CMD_ALWAYS_CLOSED = "ALWAYS_CLOSED";

    /**
     * 取消常开/常闭命令
     */
    public static final String CMD_CANCEL_ALWAYS = "CANCEL_ALWAYS";

    // ==================== 命令类型映射 ====================

    /**
     * 命令类型映射表
     * 将 biz 层的命令类型映射到插件内部命令类型
     */
    private static final Map<String, String> COMMAND_TYPE_MAPPING = Map.ofEntries(
        // 下发命令映射
        Map.entry("DISPATCH_USER", CMD_DISPATCH_AUTH),
        Map.entry("DISPATCH_CARD", CMD_DISPATCH_AUTH),
        Map.entry("ADD_CARD", CMD_DISPATCH_AUTH),
        Map.entry("UPDATE_CARD", CMD_DISPATCH_AUTH),
        // 撤销命令映射
        Map.entry("REVOKE_USER", CMD_REVOKE_AUTH),
        Map.entry("REVOKE_CARD", CMD_REVOKE_AUTH),
        Map.entry("REVOKE_FACE", CMD_DELETE_FACE),
        Map.entry("REVOKE_FINGERPRINT", CMD_DELETE_FINGERPRINT),
        Map.entry("DELETE_CARD", CMD_REVOKE_AUTH),
        // 查询命令映射
        Map.entry("DISCOVER_CHANNELS", CMD_QUERY_CHANNELS),
        Map.entry("LIST_CARDS", CMD_QUERY_AUTH)
    );

    // ==================== 参数名映射 ====================

    /**
     * 参数名映射表
     * 将 biz 层的参数名映射到插件内部参数名
     */
    private static final Map<String, String> PARAM_NAME_MAPPING = Map.of(
        "personId", "userId",
        "personCode", "userId",
        "personName", "userName"
    );

    // ==================== 事件类型常量 ====================
    // 使用 AccessControlEventMessage.EventType 中定义的整数常量

    /**
     * 门禁刷卡事件
     */
    public static final int EVENT_CARD_SWIPE = AccessControlEventMessage.EventType.CARD;

    /**
     * 门禁人脸识别事件
     */
    public static final int EVENT_FACE_RECOGNIZE = AccessControlEventMessage.EventType.FACE;

    /**
     * 门禁指纹识别事件
     */
    public static final int EVENT_FINGERPRINT_RECOGNIZE = AccessControlEventMessage.EventType.FINGERPRINT;

    /**
     * 门禁开门事件（远程开门）
     */
    public static final int EVENT_DOOR_OPEN = AccessControlEventMessage.EventType.REMOTE_OPEN;

    /**
     * 门禁关门事件（门禁状态）
     */
    public static final int EVENT_DOOR_CLOSE = AccessControlEventMessage.EventType.ACCESS_STATUS;

    /**
     * 门禁报警事件（门磁报警）
     */
    public static final int EVENT_ALARM = AccessControlEventMessage.EventType.DOOR_SENSOR_ALARM;

    // ==================== 消息主题常量 ====================

    /**
     * 门禁二代事件主题
     * @deprecated 已迁移到统一事件主题 {@link IotMessageTopics#DEVICE_EVENT_REPORTED}
     */
    @Deprecated
    public static final String TOPIC_ACCESS_GEN2_EVENT = IotMessageTopics.DEVICE_EVENT_REPORTED;

    // ==================== 依赖注入 ====================

    /**
     * 插件配置
     */
    private final AccessGen2Config config;

    /**
     * 连接管理器
     */
    private final AccessGen2ConnectionManager connectionManager;

    /**
     * 生命周期管理器
     */
    private final DeviceLifecycleManager lifecycleManager;

    /**
     * 消息发布器
     */
    private final GatewayMessagePublisher messagePublisher;

    /**
     * 门禁二代厂商适配器工厂（注册式多厂家）
     */
    private final AccessGen2AdapterFactory adapterFactory;

    /**
     * 门禁命令确认服务
     * <p>基于设备事件回调的命令确认机制，解决 SDK 返回值不可靠的问题</p>
     */
    private final cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1.DoorCommandConfirmationService doorCommandConfirmationService;

    private final DeviceSessionRegistry deviceSessionRegistry;

    /**
     * 门禁事件回调（EVENT_IVS_ACCESS_CTL）
     * <p>
     * 必须保持强引用，避免被 GC 导致回调失效
     * </p>
     */
    private final NetSDKLib.fAnalyzerDataCallBack accessCtlCallback = new AccessCtlAnalyzerDataCallBack(this);

    /**
     * 事件去重缓存：key = "deviceId:channelNo:eventType:result"，value = 上次事件时间戳
     * <p>
     * 用于过滤短时间内重复的事件（如远程开门可能触发多次回调）
     * </p>
     */
    private final java.util.concurrent.ConcurrentHashMap<String, Long> eventDedupeCache = new java.util.concurrent.ConcurrentHashMap<>();
    
    /**
     * 事件去重时间窗口（毫秒）
     * <p>
     * 在此时间窗口内，相同的事件（设备+通道+事件类型+结果）将被忽略
     * </p>
     */
    private static final long EVENT_DEDUPE_WINDOW_MS = 500;

    /**
     * 重连调度器
     */
    private final ScheduledExecutorService reconnectScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "AccessGen2-Reconnect");
        t.setDaemon(true);
        return t;
    });

    // ==================== 初始化 ====================

    /**
     * 初始化插件，注册断线监听器
     */
    @PostConstruct
    public void init() {
        log.info("{} 初始化插件，为各厂商适配器注册断线监听...", LOG_PREFIX);
        for (AccessGen2VendorAdapter adapter : adapterFactory.getAllAdapters()) {
            adapter.registerDisconnectListener(this::handleDeviceDisconnect);
        }
        log.info("{} ✅ 断线监听器注册完成", LOG_PREFIX);
    }

    /**
     * 按设备已绑定的厂商适配器解析实现；未绑定时回退大华默认（NetSDK）。
     */
    private AccessGen2VendorAdapter adapterFor(Long deviceId) {
        if (deviceId == null) {
            return adapterFactory.getDefaultAdapter();
        }
        AccessGen2VendorAdapter bound = adapterFactory.getAdapterByDeviceId(deviceId);
        if (bound == null) {
            return adapterFactory.getDefaultAdapter();
        }
        return bound;
    }

    /**
     * 处理设备断线
     *
     * @param loginHandle 登录句柄
     * @param ip          设备IP
     * @param port        设备端口
     */
    private void handleDeviceDisconnect(long loginHandle, String ip, int port) {
        // 通过 loginHandle 反查 deviceId
        Long deviceId = connectionManager.getDeviceIdByLoginHandle(loginHandle);
        
        if (deviceId == null) {
            log.warn("{} 无法找到断线设备的 deviceId: loginHandle={}, ip={}", LOG_PREFIX, loginHandle, ip);
            return;
        }
        
        log.warn("{} 设备断线，开始处理: deviceId={}, ip={}, port={}", LOG_PREFIX, deviceId, ip, port);
        
        // 1. 获取连接信息（用于后续重连）
        AccessGen2ConnectionManager.AccessGen2ConnectionInfo connInfo = connectionManager.getConnectionInfo(deviceId);
        
        // 2. 更新设备生命周期状态为离线
        try {
            lifecycleManager.onDeviceDisconnected(deviceId, "SDK检测到设备断线");
            log.info("{} 设备状态已更新为离线: deviceId={}", LOG_PREFIX, deviceId);
        } catch (Exception e) {
            log.error("{} 更新设备离线状态失败: deviceId={}, error={}", LOG_PREFIX, deviceId, e.getMessage(), e);
        }
        
        // 3. 清理连接管理器中的连接信息
        connectionManager.unregister(deviceId);
        adapterFactory.unbindAdapter(deviceId);
        deviceSessionRegistry.remove(deviceId);

        // 4. 调度自动重连（如果有连接信息）
        if (connInfo != null && connInfo.getIpAddress() != null) {
            scheduleReconnect(deviceId, connInfo);
        } else {
            log.warn("{} 缺少连接信息，无法自动重连: deviceId={}", LOG_PREFIX, deviceId);
        }
    }

    /**
     * 调度自动重连
     *
     * @param deviceId 设备ID
     * @param connInfo 连接信息
     */
    private void scheduleReconnect(Long deviceId, AccessGen2ConnectionManager.AccessGen2ConnectionInfo connInfo) {
        // 检查是否已在重连中
        if (connectionManager.isReconnecting(deviceId)) {
            log.debug("{} 设备已在重连队列中，跳过: deviceId={}", LOG_PREFIX, deviceId);
            return;
        }
        
        // 标记正在重连
        connectionManager.setReconnecting(deviceId, true);
        
        long reconnectInterval = config.getReconnectInterval();
        log.info("{} 调度自动重连: deviceId={}, ip={}, 延迟={}ms", 
                LOG_PREFIX, deviceId, connInfo.getIpAddress(), reconnectInterval);
        
        reconnectScheduler.schedule(() -> {
            attemptReconnect(deviceId, connInfo);
        }, reconnectInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * 尝试重连设备
     *
     * @param deviceId 设备ID
     * @param connInfo 连接信息
     */
    private void attemptReconnect(Long deviceId, AccessGen2ConnectionManager.AccessGen2ConnectionInfo connInfo) {
        log.info("{} 尝试重连设备: deviceId={}, ip={}:{}", 
                LOG_PREFIX, deviceId, connInfo.getIpAddress(), connInfo.getPort());
        
        try {
            // 检查设备是否已经在线（可能被其他方式重连了）
            if (connectionManager.isOnline(deviceId)) {
                log.info("{} 设备已在线，取消重连: deviceId={}", LOG_PREFIX, deviceId);
                connectionManager.setReconnecting(deviceId, false);
                return;
            }
            
            // 构建连接信息
            DeviceConnectionInfo connectionInfo = DeviceConnectionInfo.builder()
                    .deviceId(deviceId)
                    .ipAddress(connInfo.getIpAddress())
                    .port(connInfo.getPort() != null ? connInfo.getPort() : config.getDefaultPort())
                    .username("admin") // 默认用户名，实际应从配置或数据库获取
                    .password("")      // 默认密码，实际应从配置或数据库获取
                    .deviceType(PluginConstants.DEVICE_TYPE_ACCESS_GEN2)
                    .connectionMode(ConnectionMode.ACTIVE)
                    .build();
            
            // 尝试登录
            LoginResult result = login(connectionInfo);
            
            if (result.isSuccess()) {
                log.info("{} ✅ 设备重连成功: deviceId={}, ip={}", 
                        LOG_PREFIX, deviceId, connInfo.getIpAddress());
                connectionManager.setReconnecting(deviceId, false);
            } else {
                log.warn("{} 设备重连失败，将继续重试: deviceId={}, error={}", 
                        LOG_PREFIX, deviceId, result.getErrorMessage());
                // 重新调度下一次重连
                scheduleReconnect(deviceId, connInfo);
            }
        } catch (Exception e) {
            log.error("{} 重连异常: deviceId={}, error={}", LOG_PREFIX, deviceId, e.getMessage(), e);
            // 重新调度下一次重连
            scheduleReconnect(deviceId, connInfo);
        }
    }

    // ==================== DeviceHandler 接口实现 ====================

    @Override
    public String getDeviceType() {
        return PluginConstants.DEVICE_TYPE_ACCESS_GEN2;
    }

    @Override
    public String getVendor() {
        return "Dahua";
    }

    @Override
    public boolean supports(DeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            return false;
        }
        return PluginConstants.DEVICE_TYPE_ACCESS_GEN2.equalsIgnoreCase(deviceInfo.getDeviceType());
    }

    @Override
    public CommandResult executeCommand(Long deviceId, DeviceCommand command) {
        if (deviceId == null || command == null) {
            return CommandResult.failure("参数不能为空");
        }

        String commandType = command.getCommandType();
        // Activation Biz 发来的 method=CONNECT/DISCONNECT，需要走 login/logout，
        // 否则不会触发 lifecycleManager.onDeviceLogin()，activation 状态将一直卡在 activating。
        if (commandType == null || commandType.isEmpty()) {
            return CommandResult.failure("不支持的命令类型: null");
        }
        if ("CONNECT".equalsIgnoreCase(commandType)) {
            DeviceConnectionInfo connectionInfo = DeviceConnectionInfo.builder()
                    .deviceId(deviceId)
                    .ipAddress(command.getStringParam("ip"))
                    .port(command.getIntParam("port"))
                    .username(command.getStringParam("username"))
                    .password(command.getStringParam("password"))
                    .deviceType(PluginConstants.DEVICE_TYPE_ACCESS_GEN2)
                    .vendor(getVendor())
                    .connectionMode(ConnectionMode.ACTIVE)
                    .build();
            LoginResult loginResult = login(connectionInfo);
            if (loginResult == null) {
                return CommandResult.failure("登录结果为空");
            }
            if (loginResult.isSuccess()) {
                return CommandResult.success(Map.of(
                        "loginHandle", loginResult.getLoginHandle(),
                        "deviceInfo", loginResult.getDeviceInfo()
                ));
            }
            return CommandResult.failure(loginResult.getErrorMessage());
        }
        if ("DISCONNECT".equalsIgnoreCase(commandType)) {
            logout(deviceId);
            return CommandResult.success("DISCONNECT success");
        }

        // 映射命令类型
        String mappedCommandType = mapCommandType(commandType);
        // 映射参数
        Map<String, Object> mappedParams = mapParams(command.getParams());
        // 创建映射后的命令
        DeviceCommand mappedCommand = DeviceCommand.builder()
                .commandType(mappedCommandType)
                .params(mappedParams)
                .build();
        
        log.info("{} 执行命令: deviceId={}, commandType={}, mappedType={}", 
                LOG_PREFIX, deviceId, commandType, mappedCommandType);

        try {
            // 根据命令类型分发处理
            switch (mappedCommandType) {
                case CMD_OPEN_DOOR:
                    return executeOpenDoor(deviceId, mappedCommand);
                case CMD_CLOSE_DOOR:
                    return executeCloseDoor(deviceId, mappedCommand);
                case CMD_DISPATCH_AUTH:
                    // 传递原始命令类型，以便区分 DISPATCH_USER 和 DISPATCH_CARD
                    return executeDispatchAuth(deviceId, mappedCommand, commandType);
                case CMD_REVOKE_AUTH:
                    return executeRevokeAuth(deviceId, mappedCommand);
                case CMD_QUERY_AUTH:
                    return executeQueryAuth(deviceId, mappedCommand);
                case CMD_CLEAR_ALL_AUTH:
                    return executeClearAllAuth(deviceId);
                case CMD_DISPATCH_FACE:
                    return executeDispatchFace(deviceId, mappedCommand);
                case CMD_DELETE_FACE:
                    return executeDeleteFace(deviceId, mappedCommand);
                case CMD_DISPATCH_FINGERPRINT:
                    return executeDispatchFingerprint(deviceId, mappedCommand);
                case CMD_DELETE_FINGERPRINT:
                    return executeDeleteFingerprint(deviceId, mappedCommand);
                case CMD_QUERY_CHANNELS:
                    return executeQueryChannels(deviceId, mappedCommand);
                case CMD_CHECK_DEVICE_ONLINE:
                    return executeCheckDeviceOnline(deviceId, mappedCommand);
                case CMD_ACTIVATE_DEVICE:
                    return executeActivateDevice(deviceId, mappedCommand);
                case CMD_GET_LOGIN_HANDLE:
                    return executeGetLoginHandle(deviceId, mappedCommand);
                case CMD_QUERY_DEVICE_CAPABILITY:
                    return executeQueryDeviceCapability(deviceId, mappedCommand);
                case CMD_ALWAYS_OPEN:
                    return executeAlwaysOpen(deviceId, mappedCommand);
                case CMD_ALWAYS_CLOSED:
                    return executeAlwaysClosed(deviceId, mappedCommand);
                case CMD_CANCEL_ALWAYS:
                    return executeCancelAlways(deviceId, mappedCommand);
                default:
                    return CommandResult.failure("不支持的命令类型: " + commandType);
            }
        } catch (Exception e) {
            log.error("{} 执行命令失败: deviceId={}, commandType={}", LOG_PREFIX, deviceId, commandType, e);
            return CommandResult.failure("命令执行异常: " + e.getMessage());
        }
    }

    /**
     * 映射命令类型
     * 将 biz 层的命令类型映射到插件内部命令类型
     *
     * @param commandType 原始命令类型
     * @return 映射后的命令类型
     */
    private String mapCommandType(String commandType) {
        if (commandType == null) {
            return null;
        }
        return COMMAND_TYPE_MAPPING.getOrDefault(commandType, commandType);
    }

    /**
     * 映射参数名
     * 将 biz 层的参数名映射到插件内部参数名
     * 注意：对于映射到 userId/userName 等字符串类型字段的参数，需要将数值类型转换为字符串，
     * 避免后续出现 ClassCastException（Integer cannot be cast to String）
     *
     * @param params 原始参数
     * @return 映射后的参数
     */
    private Map<String, Object> mapParams(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return params != null ? params : new HashMap<>();
        }
        Map<String, Object> mappedParams = new HashMap<>(params);
        
        // 处理 faceInfo 对象，提取 userId 和 faceUrl 参数
        // 新方案：传递 faceUrl 到网关，由网关下载图片（避免消息总线传输大量二进制数据）
        Object faceInfoObj = mappedParams.get("faceInfo");
        
        log.info("{} mapParams 开始处理, faceInfo存在={}, faceInfo类型={}", 
                LOG_PREFIX, faceInfoObj != null, 
                faceInfoObj != null ? faceInfoObj.getClass().getName() : "null");
        
        if (faceInfoObj != null) {
            String userId = null;
            String faceUrl = null;
            
            if (faceInfoObj instanceof NetAccessFaceInfo) {
                // 直接是 NetAccessFaceInfo 对象（本地调用）
                NetAccessFaceInfo faceInfo = (NetAccessFaceInfo) faceInfoObj;
                userId = faceInfo.getUserId();
                faceUrl = faceInfo.getFaceUrl();
                log.info("{} faceInfo 是 NetAccessFaceInfo 对象: userId={}, faceUrl={}", 
                        LOG_PREFIX, userId, faceUrl);
            } else if (faceInfoObj instanceof Map) {
                // 反序列化后变成 Map（通过消息总线传输）
                @SuppressWarnings("unchecked")
                Map<String, Object> faceInfoMap = (Map<String, Object>) faceInfoObj;
                userId = faceInfoMap.get("userId") != null ? faceInfoMap.get("userId").toString() : null;
                faceUrl = faceInfoMap.get("faceUrl") != null ? faceInfoMap.get("faceUrl").toString() : null;
                
                log.info("{} faceInfo 是 Map 类型, keys={}, userId={}, faceUrl={}", 
                        LOG_PREFIX, faceInfoMap.keySet(), userId, faceUrl);
            } else {
                log.warn("{} faceInfo 类型未知: {}", LOG_PREFIX, faceInfoObj.getClass().getName());
            }
            
            // 设置 userId 和 faceUrl 参数
            if (userId != null && !mappedParams.containsKey("userId")) {
                mappedParams.put("userId", userId);
            }
            if (faceUrl != null && !mappedParams.containsKey("faceUrl")) {
                mappedParams.put("faceUrl", faceUrl);
            }
            mappedParams.remove("faceInfo");
            
            log.info("{} mapParams 处理完成, 最终 userId={}, faceUrl={}", 
                    LOG_PREFIX, mappedParams.get("userId"), mappedParams.get("faceUrl"));
        }
        
        // 处理 userInfo 对象，提取 photoUrl 参数（用于 DISPATCH_USER 命令）
        Object userInfoObj = mappedParams.get("userInfo");
        if (userInfoObj != null) {
            String photoUrl = null;
            String password = null;
            
            if (userInfoObj instanceof NetAccessUserInfo) {
                NetAccessUserInfo userInfo = (NetAccessUserInfo) userInfoObj;
                photoUrl = userInfo.getPhotoUrl();
                password = userInfo.getPassword();
                log.info("{} userInfo 是 NetAccessUserInfo 对象: photoUrl={}, passwordLen={}", 
                        LOG_PREFIX, photoUrl, password != null ? password.length() : 0);
            } else if (userInfoObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> userInfoMap = (Map<String, Object>) userInfoObj;
                photoUrl = userInfoMap.get("photoUrl") != null ? userInfoMap.get("photoUrl").toString() : null;
                password = userInfoMap.get("password") != null ? userInfoMap.get("password").toString() : null;
                log.info("{} userInfo 是 Map 类型, keys={}, photoUrl={}, passwordLen={}", 
                        LOG_PREFIX, userInfoMap.keySet(), photoUrl, password != null ? password.length() : 0);
            }
            
            // 设置 photoUrl 和 password 到顶层参数（便于后续处理）
            if (photoUrl != null && !mappedParams.containsKey("photoUrl")) {
                mappedParams.put("photoUrl", photoUrl);
            }
            if (password != null && !mappedParams.containsKey("password")) {
                mappedParams.put("password", password);
            }
        }
        
        PARAM_NAME_MAPPING.forEach((oldKey, newKey) -> {
            if (mappedParams.containsKey(oldKey) && !mappedParams.containsKey(newKey)) {
                Object value = mappedParams.get(oldKey);
                // 对于映射到 userId、userName 等字符串字段的参数，确保值是字符串类型
                // 这可以避免 personId（Long/Integer）被直接复制到 userId 后导致的类型转换问题
                if (value != null && (newKey.equals("userId") || newKey.equals("userName"))) {
                    value = value.toString();
                }
                mappedParams.put(newKey, value);
            }
        });
        return mappedParams;
    }

    @Override
    public DeviceStatus queryStatus(Long deviceId) {
        if (deviceId == null) {
            return DeviceStatus.offline(null);
        }

        // 通过 ConnectionManager 获取实际状态
        if (connectionManager.isOnline(deviceId)) {
            return DeviceStatus.online(deviceId);
        }
        return DeviceStatus.offline(deviceId);
    }

    // ==================== ActiveDeviceHandler 接口实现 ====================

    @Override
    public LoginResult login(DeviceConnectionInfo connectionInfo) {
        if (connectionInfo == null) {
            return LoginResult.failure("连接信息不能为空");
        }

        Long deviceId = connectionInfo.getDeviceId();
        String ipAddress = connectionInfo.getIpAddress();
        Integer port = connectionInfo.getPort();
        String username = connectionInfo.getUsername();
        String password = connectionInfo.getPassword();

        log.info("{} 登录设备: deviceId={}, ip={}, port={}", 
                LOG_PREFIX, deviceId, ipAddress, port);

        // 参数校验
        if (ipAddress == null || ipAddress.isEmpty()) {
            return LoginResult.failure("IP 地址不能为空");
        }
        if (port == null || port <= 0) {
            port = config.getDefaultPort();
        }
        if (username == null || username.isEmpty()) {
            return LoginResult.failure("用户名不能为空");
        }
        if (password == null || password.isEmpty()) {
            return LoginResult.failure("密码不能为空");
        }

        // 检查是否已有连接 - 如果已在线，直接返回成功，避免重复登录导致设备被踢
        if (connectionManager.isOnline(deviceId)) {
            Long existingHandle = connectionManager.getLoginHandle(deviceId);
            log.info("{} 设备已在线，复用现有连接: deviceId={}, handle={}", LOG_PREFIX, deviceId, existingHandle);
            
            // 从连接信息中获取设备信息
            AccessGen2ConnectionManager.AccessGen2ConnectionInfo connInfo = connectionManager.getConnectionInfo(deviceId);
            
            // 构建设备信息返回
            Map<String, Object> deviceInfo = new HashMap<>();
            deviceInfo.put("deviceType", PluginConstants.DEVICE_TYPE_ACCESS_GEN2);
            deviceInfo.put("alreadyOnline", true);
            if (connInfo != null) {
                deviceInfo.put("serialNumber", connInfo.getSerialNumber());
                deviceInfo.put("channelCount", connInfo.getChannelCount());
                deviceInfo.put("supportsFace", connInfo.isSupportsFace());
                deviceInfo.put("supportsFingerprint", connInfo.isSupportsFingerprint());
            }
            
            return LoginResult.success(existingHandle, deviceInfo);
        }

        // 登录前按连接信息选择厂商（不传 deviceId，避免登录失败时在工厂中错误缓存 deviceId→适配器）
        Map<String, Object> selectionInfo = AccessGen2AdapterFactory.buildDeviceInfo(connectionInfo);
        selectionInfo.remove("deviceId");
        AccessGen2VendorAdapter adapter = adapterFactory.getAdapter(selectionInfo);

        if (!adapter.isInitialized()) {
            log.error("{} 厂商适配器未初始化，无法登录: deviceId={}, vendor={}", LOG_PREFIX, deviceId, adapter.getVendorCode());
            return LoginResult.failure("SDK 未初始化");
        }

        try {
            // 调用厂商适配器登录设备
            AccessGen2LoginResult sdkResult = adapter.login(ipAddress, port, username, password);
            
            if (!sdkResult.isSuccess()) {
                log.error("{} 设备登录失败: deviceId={}, error={}", 
                        LOG_PREFIX, deviceId, sdkResult.getErrorMessage());
                return LoginResult.failure(sdkResult.getErrorMessage());
            }
            
            long loginHandle = sdkResult.getLoginHandle();
            String serialNumber = sdkResult.getSerialNumber();
            int channelCount = sdkResult.getChannelCount();
            boolean supportsFace = sdkResult.isSupportsFace();
            boolean supportsFingerprint = sdkResult.isSupportsFingerprint();
            
            // 构建设备信息
            Map<String, Object> deviceInfo = new HashMap<>();
            deviceInfo.put("serialNumber", serialNumber);
            deviceInfo.put("deviceType", PluginConstants.DEVICE_TYPE_ACCESS_GEN2);
            deviceInfo.put("channelCount", channelCount);
            deviceInfo.put("supportsFace", supportsFace);
            deviceInfo.put("supportsFingerprint", supportsFingerprint);
            deviceInfo.put("sdkDeviceType", sdkResult.getDeviceType());
            
            // 注册连接到连接管理器
            connectionManager.register(deviceId, serialNumber, loginHandle);
            connectionManager.updateIpAddress(deviceId, ipAddress);
            connectionManager.updatePort(deviceId, port);
            connectionManager.updateSerialNumber(deviceId, serialNumber);
            connectionManager.updateChannelCount(deviceId, channelCount);
            connectionManager.updateSupportsFace(deviceId, supportsFace);
            connectionManager.updateSupportsFingerprint(deviceId, supportsFingerprint);
            
            // 构建设备连接信息并原子地更新状态（注册 → 直接上线）
            DeviceConnectionInfo loginConnectionInfo = DeviceConnectionInfo.builder()
                    .deviceId(deviceId)
                    .deviceType(PluginConstants.DEVICE_TYPE_ACCESS_GEN2)
                    .vendor(adapter.getVendorName())
                    .connectionMode(ConnectionMode.ACTIVE)
                    .build();
            lifecycleManager.onDeviceLogin(deviceId, loginConnectionInfo);

            adapterFactory.bindAdapter(deviceId, adapter.getVendorCode());

            deviceSessionRegistry.put(SimpleDeviceSession.builder()
                    .deviceId(deviceId)
                    .pluginId(PluginConstants.PLUGIN_ID_ACCESS_GEN2)
                    .vendorKey(adapter.getVendorCode())
                    .lastActiveEpochMillis(System.currentTimeMillis())
                    .build());

            // 订阅门禁事件（按文档：EVENT_IVS_ACCESS_CTL / DEV_EVENT_ACCESS_CTL_INFO）
            Long analyzerHandle = adapter.subscribeAccessCtlEvent(loginHandle, deviceId, accessCtlCallback);
            if (analyzerHandle != null && analyzerHandle > 0) {
                connectionManager.updateAnalyzerHandle(deviceId, analyzerHandle);
            } else {
                log.warn("{} 门禁事件订阅失败（不影响登录，但事件记录将无法实时回推）: deviceId={}", LOG_PREFIX, deviceId);
            }
            
            // 发布设备上线事件
            publishAccessEvent(deviceId, EVENT_DOOR_OPEN, Map.of(
                    "action", "LOGIN",
                    "serialNumber", serialNumber,
                    "channelCount", channelCount,
                    "supportsFace", supportsFace,
                    "supportsFingerprint", supportsFingerprint
            ));
            
            log.info("{} ✅ 设备登录成功: deviceId={}, handle={}, sn={}, channels={}, face={}, fingerprint={}", 
                    LOG_PREFIX, deviceId, loginHandle, serialNumber, channelCount, supportsFace, supportsFingerprint);
            
            return LoginResult.success(loginHandle, deviceInfo);
        } catch (Exception e) {
            log.error("{} 设备登录异常: deviceId={}, ip={}", LOG_PREFIX, deviceId, ipAddress, e);
            return LoginResult.failure("登录异常: " + e.getMessage());
        }
    }


    @Override
    public void logout(Long deviceId) {
        if (deviceId == null) {
            return;
        }

        log.info("{} 登出设备: deviceId={}", LOG_PREFIX, deviceId);

        try {
            AccessGen2VendorAdapter adapter = adapterFor(deviceId);
            // 获取登录句柄
            Long loginHandle = connectionManager.getLoginHandle(deviceId);
            Long analyzerHandle = connectionManager.getAnalyzerHandle(deviceId);
            if (analyzerHandle != null && analyzerHandle > 0) {
                adapter.unsubscribeAccessEvent(analyzerHandle);
            }
            
            if (loginHandle != null && loginHandle > 0) {
                boolean logoutSuccess = adapter.logout(loginHandle);
                if (!logoutSuccess) {
                    log.warn("{} SDK 登出失败，但仍将清理本地状态: deviceId={}", LOG_PREFIX, deviceId);
                }
            }
            
            // 原子地更新设备状态为离线
            lifecycleManager.onDeviceLogout(deviceId, "SDK登出");
            
            // 从连接管理器注销连接
            connectionManager.unregister(deviceId);
            adapterFactory.unbindAdapter(deviceId);
            deviceSessionRegistry.remove(deviceId);
            
            // 发布设备离线事件
            publishAccessEvent(deviceId, EVENT_DOOR_CLOSE, Map.of(
                    "action", "LOGOUT"
            ));
            
            log.info("{} ✅ 设备登出成功: deviceId={}", LOG_PREFIX, deviceId);
        } catch (Exception e) {
            log.error("{} 设备登出异常: deviceId={}", LOG_PREFIX, deviceId, e);
            // 即使异常也要清理本地状态
            lifecycleManager.onDeviceLogout(deviceId, "登出异常");
            connectionManager.unregister(deviceId);
            adapterFactory.unbindAdapter(deviceId);
            deviceSessionRegistry.remove(deviceId);
        }
    }

    @Override
    public boolean keepalive(Long deviceId) {
        if (deviceId == null) {
            return false;
        }

        log.debug("{} 保活检测: deviceId={}", LOG_PREFIX, deviceId);

        try {
            // 检查连接是否有效
            if (!connectionManager.isOnline(deviceId)) {
                log.warn("{} 保活检测失败: 设备未连接, deviceId={}", LOG_PREFIX, deviceId);
                return false;
            }
            
            // 获取登录句柄
            Long loginHandle = connectionManager.getLoginHandle(deviceId);
            if (loginHandle == null || loginHandle <= 0) {
                log.warn("{} 保活检测失败: 无效的登录句柄, deviceId={}", LOG_PREFIX, deviceId);
                return false;
            }
            
            // 通过查询用户列表来验证连接是否有效
            // 这是一种轻量级的保活检测方式
            List<AccessGen2UserInfo> users = adapterFor(deviceId).queryUsers(loginHandle, null);
            
            // 如果查询成功（不抛异常），说明连接有效
            // 更新心跳时间
            connectionManager.updateHeartbeat(deviceId);
            lifecycleManager.updateLastSeen(deviceId);
            
            log.debug("{} 保活检测成功: deviceId={}, userCount={}", LOG_PREFIX, deviceId, users.size());
            return true;
        } catch (Exception e) {
            log.error("{} 保活检测异常: deviceId={}", LOG_PREFIX, deviceId, e);
            return false;
        }
    }

    @Override
    public long getReconnectInterval() {
        return config.getReconnectInterval();
    }

    @Override
    public long getKeepaliveInterval() {
        return config.getKeepaliveInterval();
    }

    // ==================== 命令执行方法 ====================

    /**
     * 确保设备已连接，如果未连接则尝试按需连接
     * 
     * @param deviceId 设备ID
     * @param command 命令（包含连接参数）
     * @return 登录句柄，如果连接失败则返回 null
     */
    private Long ensureConnected(Long deviceId, DeviceCommand command) {
        // 1. 检查是否已连接
        Long loginHandle = connectionManager.getLoginHandle(deviceId);
        if (loginHandle != null && loginHandle > 0) {
            return loginHandle;
        }
        
        // 2. 尝试按需连接（从命令参数中获取连接信息）
        // 使用安全的类型转换方法避免类型转换异常
        String ipAddress = command.getStringParam("ipAddress");
        if (ipAddress == null) {
            ipAddress = command.getStringParam("ip");
        }
        Integer port = command.getIntParam("port");
        String username = command.getStringParam("username");
        String password = command.getStringParam("password");
        
        // 如果没有连接参数，无法按需连接
        if (ipAddress == null || ipAddress.isEmpty()) {
            log.warn("{} 设备未连接且命令中无连接参数: deviceId={}", LOG_PREFIX, deviceId);
            return null;
        }
        
        log.info("{} 设备未连接，尝试按需连接: deviceId={}, ip={}", LOG_PREFIX, deviceId, ipAddress);
        
        // 3. 构建连接信息并登录
        DeviceConnectionInfo connectionInfo = DeviceConnectionInfo.builder()
                .deviceId(deviceId)
                .ipAddress(ipAddress)
                .port(port != null ? port : config.getDefaultPort())
                .username(username != null ? username : "admin")
                .password(password != null ? password : "")
                .deviceType(PluginConstants.DEVICE_TYPE_ACCESS_GEN2)
                .build();
        
        LoginResult loginResult = login(connectionInfo);
        if (loginResult.isSuccess()) {
            log.info("{} ✅ 按需连接成功: deviceId={}", LOG_PREFIX, deviceId);
            return connectionManager.getLoginHandle(deviceId);
        } else {
            log.error("{} 按需连接失败: deviceId={}, error={}", LOG_PREFIX, deviceId, loginResult.getErrorMessage());
            return null;
        }
    }

    /**
     * 执行远程开门命令
     * 
     * <h2>完全事件驱动设计</h2>
     * <p>采用专业门禁产品的标准模式：<b>所有命令都等待设备事件确认</b>，
     * SDK 返回值仅作为"命令是否发出"的参考，不作为成功依据。</p>
     * 
     * <h3>流程</h3>
     * <ol>
     *     <li>注册待确认命令</li>
     *     <li>发送开门命令（SDK 返回值仅用于判断命令是否发出）</li>
     *     <li>等待设备事件回调确认（远程开门事件 command=12673）</li>
     *     <li>收到确认 → 确定成功；超时 → 确定失败</li>
     * </ol>
     */
    private CommandResult executeOpenDoor(Long deviceId, DeviceCommand command) {
        // 获取通道号，默认为1
        Integer channelNo = command.getParam("channelNo");
        if (channelNo == null) {
            channelNo = 1;
        }
        // SDK的channelNo是从0开始的索引，需要减1
        int channelIndex = channelNo - 1;
        if (channelIndex < 0) {
            channelIndex = 0;
        }

        log.info("{} 执行远程开门（事件驱动模式）: deviceId={}, channelNo={}, channelIndex={}", 
                LOG_PREFIX, deviceId, channelNo, channelIndex);

        // 【步骤1】注册待确认命令
        String confirmationKey = doorCommandConfirmationService.registerPendingCommand(
                deviceId, channelNo, 
                cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1.DoorCommandConfirmationService.CMD_OPEN_DOOR);

        try {
            // 确保设备已连接
            Long loginHandle = ensureConnected(deviceId, command);
            if (loginHandle == null || loginHandle <= 0) {
                doorCommandConfirmationService.cancelPendingCommand(confirmationKey);
                return CommandResult.failure("设备未连接");
            }
            
            // 【步骤2】发送开门命令
            // SDK 返回值仅用于判断命令是否发出，不作为执行成功的依据
            AccessGen2OperationResult sdkResult = adapterFor(deviceId).openDoor(loginHandle, channelIndex);
            
            // 检查是否为真正的发送失败（连接断开等）
            if (!sdkResult.isSuccess() && isConnectionError(sdkResult.getMessage())) {
                log.warn("{} 命令发送失败(连接错误)，尝试重连: deviceId={}, error={}", 
                        LOG_PREFIX, deviceId, sdkResult.getMessage());
                
                connectionManager.unregister(deviceId);
                adapterFactory.unbindAdapter(deviceId);
                Long newLoginHandle = ensureConnected(deviceId, command);
                if (newLoginHandle == null || newLoginHandle <= 0) {
                    doorCommandConfirmationService.cancelPendingCommand(confirmationKey);
                    return CommandResult.failure("重连失败，设备无法建立连接");
                }
                
                log.info("{} 重连成功，重试发送: deviceId={}", LOG_PREFIX, deviceId);
                sdkResult = adapterFor(deviceId).openDoor(newLoginHandle, channelIndex);
                
                if (!sdkResult.isSuccess() && isConnectionError(sdkResult.getMessage())) {
                    doorCommandConfirmationService.cancelPendingCommand(confirmationKey);
                    return CommandResult.failure("命令发送失败: " + sdkResult.getMessage());
                }
            }
            
            // 【步骤3】等待设备事件确认（核心逻辑）
            log.info("{} 命令已发送，等待设备事件确认: deviceId={}, channelNo={}, sdkResult={}", 
                    LOG_PREFIX, deviceId, channelNo, sdkResult.isSuccess() ? "SDK_OK" : sdkResult.getMessage());
            
            var confirmResult = doorCommandConfirmationService.waitForConfirmation(confirmationKey, 5);
            
            if (confirmResult.isConfirmed()) {
                // 【确定成功】设备上报了远程开门事件
                log.info("{} ✅ 开门成功（设备已确认）: deviceId={}, channelNo={}, alarmType={}", 
                        LOG_PREFIX, deviceId, channelNo, confirmResult.getAlarmType());
                
                publishAccessEvent(deviceId, EVENT_DOOR_OPEN, Map.of(
                        "channelNo", channelNo,
                        "action", "REMOTE_OPEN",
                        "confirmedBy", "DEVICE_EVENT"
                ));
                
                return CommandResult.success(Map.of(
                        "message", "开门成功",
                        "channelNo", channelNo
                ));
            } else {
                // 【确定失败】超时未收到设备确认
                log.warn("{} ❌ 开门失败（设备未确认）: deviceId={}, channelNo={}, result={}", 
                        LOG_PREFIX, deviceId, channelNo, confirmResult);
                return CommandResult.failure("开门失败: " + confirmResult.getMessage());
            }
            
        } catch (Exception e) {
            doorCommandConfirmationService.cancelPendingCommand(confirmationKey);
            log.error("{} 远程开门异常: deviceId={}, channelNo={}", LOG_PREFIX, deviceId, channelNo, e);
            return CommandResult.failure("开门异常: " + e.getMessage());
        }
    }
    
    /**
     * 判断是否为连接/发送失败错误
     * <p>
     * 在完全事件驱动模式下，此方法仅用于判断命令是否发送失败（需要重连）。
     * 命令是否执行成功由设备事件回调确认，不依赖 SDK 返回值。
     * </p>
     * 
     * @param errorMsg 错误信息
     * @return true=连接错误，命令未发出，需要重连；false=命令已发出
     */
    private boolean isConnectionError(String errorMsg) {
        if (errorMsg == null) {
            return false;
        }
        // 仅匹配真正的连接/发送失败错误
        // 错误码 516 = 数据发送失败
        // 错误码 385 = 获取服务器实例失败（登录句柄已失效）
        return errorMsg.contains("516") 
                || errorMsg.contains("385")
                || errorMsg.contains("数据发送失败") 
                || errorMsg.contains("发送失败")
                || errorMsg.contains("网络")
                || errorMsg.contains("连接断开")
                || errorMsg.contains("服务器实例")
                || errorMsg.contains("实例失败")
                || errorMsg.contains("句柄无效");
    }

    /**
     * 执行远程关门命令
     */
    private CommandResult executeCloseDoor(Long deviceId, DeviceCommand command) {
        // 获取通道号，默认为1（门禁通道从1开始，与旧gateway保持一致）
        Integer channelNo = command.getParam("channelNo");
        if (channelNo == null) {
            channelNo = 1; // 默认通道1
        }
        // SDK的channelNo是从0开始的索引，需要减1
        int channelIndex = channelNo - 1;
        if (channelIndex < 0) {
            channelIndex = 0;
        }

        log.info("{} 执行远程关门: deviceId={}, channelNo={}, channelIndex={}", LOG_PREFIX, deviceId, channelNo, channelIndex);

        try {
            // 确保设备已连接（支持按需连接）
            Long loginHandle = ensureConnected(deviceId, command);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接");
            }
            
            // 调用 SDK 关门（使用转换后的索引）
            AccessGen2OperationResult sdkResult = adapterFor(deviceId).closeDoor(loginHandle, channelIndex);
            
            if (sdkResult.isSuccess()) {
                // 发布关门事件
                publishAccessEvent(deviceId, EVENT_DOOR_CLOSE, Map.of(
                        "channelNo", channelNo,
                        "action", "REMOTE_CLOSE"
                ));
                
                return CommandResult.success(Map.of(
                        "message", "关门命令已发送",
                        "channelNo", channelNo
                ));
            } else {
                return CommandResult.failure("关门失败: " + sdkResult.getMessage());
            }
        } catch (Exception e) {
            log.error("{} 远程关门异常: deviceId={}, channelNo={}", LOG_PREFIX, deviceId, channelNo, e);
            return CommandResult.failure("关门异常: " + e.getMessage());
        }
    }


    /**
     * 执行下发授权命令（通过标准 API）
     * 
     * @param deviceId 设备ID
     * @param command 命令
     * @param originalCommandType 原始命令类型（DISPATCH_USER, DISPATCH_CARD 等），用于区分不同的下发逻辑
     */
    @SuppressWarnings("unchecked")
    private CommandResult executeDispatchAuth(Long deviceId, DeviceCommand command, String originalCommandType) {
        // 支持三种参数格式：
        // 1. 直接参数: cardNo, userId, userName, ...
        // 2. userInfo 对象: { cardNo, userId, userName, ... }
        // 3. cardInfo 对象: { cardNo, userId, ... } (DISPATCH_CARD 命令)
        
        // 判断是否只需要添加卡片（DISPATCH_CARD 命令只需要添加卡片，不需要重新添加用户）
        boolean cardOnlyMode = "DISPATCH_CARD".equals(originalCommandType);
        String cardNo;
        String userId;
        String userName;
        String validStartTime;
        String validEndTime;
        String password;
        int[] doors = null;
        
        // 优先检查 userInfo
        Object userInfoObj = command.getParam("userInfo");
        // 如果没有 userInfo，检查 cardInfo（DISPATCH_CARD 命令会传递 cardInfo）
        if (userInfoObj == null) {
            userInfoObj = command.getParam("cardInfo");
        }
        
        if (userInfoObj instanceof NetAccessUserInfo) {
            // 直接从 NetAccessUserInfo 对象提取参数
            NetAccessUserInfo userInfo = (NetAccessUserInfo) userInfoObj;
            cardNo = userInfo.getCardNo();
            userId = userInfo.getUserId();
            userName = userInfo.getUserName();
            // 使用双参数版本，设置默认有效期
            validStartTime = normalizeValidTime(userInfo.getValidStartTime(), true);
            validEndTime = normalizeValidTime(userInfo.getValidEndTime(), false);
            password = userInfo.getPassword();
            doors = userInfo.getDoors();
        } else if (userInfoObj instanceof Map) {
            // 从 userInfo Map 提取参数
            Map<String, Object> userInfoMap = (Map<String, Object>) userInfoObj;
            cardNo = getStringFromMap(userInfoMap, "cardNo");
            userId = getStringFromMap(userInfoMap, "userId");
            userName = getStringFromMap(userInfoMap, "userName");
            // 使用双参数版本，设置默认有效期
            validStartTime = normalizeValidTime(getStringFromMap(userInfoMap, "validStartTime"), true);
            validEndTime = normalizeValidTime(getStringFromMap(userInfoMap, "validEndTime"), false);
            password = getStringFromMap(userInfoMap, "password");
            
            // 获取门权限
            Object doorsObj = userInfoMap.get("doors");
            if (doorsObj instanceof int[]) {
                doors = (int[]) doorsObj;
            } else if (doorsObj instanceof List) {
                List<?> doorsList = (List<?>) doorsObj;
                doors = doorsList.stream()
                        .mapToInt(d -> d instanceof Number ? ((Number) d).intValue() : 0)
                        .toArray();
            }
        } else {
            // 直接从命令参数获取（向后兼容）
            // 使用 getStringParam 避免类型转换异常（Integer -> String）
            cardNo = command.getStringParam("cardNo");
            userId = command.getStringParam("userId");
            userName = command.getStringParam("userName");
            // 使用双参数版本，设置默认有效期
            validStartTime = normalizeValidTime(command.getStringParam("validStartTime"), true);
            validEndTime = normalizeValidTime(command.getStringParam("validEndTime"), false);
            password = command.getStringParam("password");
            
            Object doorsParam = command.getParam("doors");
            if (doorsParam instanceof int[]) {
                doors = (int[]) doorsParam;
            }
        }
        
        // 二代设备也需要 cardNo，如果没有则使用 userId 作为兜底
        if ((cardNo == null || cardNo.isEmpty()) && (userId != null && !userId.isEmpty())) {
            cardNo = userId;
            log.info("{} 无卡号，使用userId作为cardNo: {}", LOG_PREFIX, cardNo);
        }

        if (cardNo == null || cardNo.isEmpty()) {
            return CommandResult.failure("缺少 cardNo 参数");
        }

        // 详细日志：打印所有参数（密码只显示长度）
        log.info("{} 执行下发授权: deviceId={}, cardNo={}, userId={}, userName={}, password={}, validStart={}, validEnd={}, doors={}, cardOnlyMode={}", 
                LOG_PREFIX, deviceId, cardNo, userId, userName,
                password != null ? "***(" + password.length() + "字符)" : "无",
                validStartTime, validEndTime,
                doors != null ? java.util.Arrays.toString(doors) : "无",
                cardOnlyMode);

        try {
            // 确保设备已连接（支持按需连接）
            Long loginHandle = ensureConnected(deviceId, command);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接");
            }
            
            // 构建用户信息
            AccessGen2UserInfo accessUserInfo = AccessGen2UserInfo.builder()
                    .userId(userId != null ? userId : cardNo)
                    .userName(userName)
                    .cardNo(cardNo)
                    .password(password)
                    .validStartTime(validStartTime)
                    .validEndTime(validEndTime)
                    .build();
            
            // 设置门权限（如果有，已在前面从 userInfo 或命令参数中提取）
            if (doors != null && doors.length > 0) {
                accessUserInfo.setDoors(doors);
            }
            
            AccessGen2OperationResult userResult = AccessGen2OperationResult.success("跳过用户添加");
            
            // 如果不是只添加卡片模式，才执行用户添加和密码设置
            // DISPATCH_CARD 命令只需要添加卡片，不需要重新添加用户（避免覆盖之前设置的密码）
            if (!cardOnlyMode) {
                // 先添加用户（如果失败则尝试更新）
                userResult = adapterFor(deviceId).addUser(loginHandle, accessUserInfo);
                if (!userResult.isSuccess()) {
                    // 添加失败，可能是用户已存在，尝试更新
                    log.info("{} 添加用户失败，尝试更新: userId={}, msg={}", LOG_PREFIX, accessUserInfo.getUserId(), userResult.getMessage());
                    userResult = adapterFor(deviceId).updateUser(loginHandle, accessUserInfo);
                }
                
                // 用户添加/更新成功后，设置密码（密码需要使用专门的API设置）
                if (userResult.isSuccess() && password != null && !password.isEmpty()) {
                    AccessGen2OperationResult pwdResult = adapterFor(deviceId).setPassword(
                            loginHandle, 
                            accessUserInfo.getUserId(), 
                            password, 
                            0  // 门禁通道号，默认0
                    );
                    if (pwdResult.isSuccess()) {
                        log.info("{} 设置密码成功: userId={}", LOG_PREFIX, accessUserInfo.getUserId());
                    } else {
                        // 密码设置失败不影响整体结果，只记录警告
                        log.warn("{} 设置密码失败（不影响用户下发）: userId={}, msg={}", 
                                LOG_PREFIX, accessUserInfo.getUserId(), pwdResult.getMessage());
                    }
                }
            } else {
                log.info("{} 仅添加卡片模式，跳过用户添加和密码设置: userId={}", LOG_PREFIX, accessUserInfo.getUserId());
            }
            
            // 添加卡号（如果失败则尝试删除后重新添加）
            AccessGen2OperationResult cardResult = adapterFor(deviceId).addCard(loginHandle, accessUserInfo);
            if (!cardResult.isSuccess()) {
                // 添加失败，可能是卡号已存在，尝试删除后重新添加
                log.info("{} 添加卡号失败，尝试删除后重新添加: cardNo={}, msg={}", LOG_PREFIX, cardNo, cardResult.getMessage());
                adapterFor(deviceId).deleteCard(loginHandle, cardNo);
                cardResult = adapterFor(deviceId).addCard(loginHandle, accessUserInfo);
            }
            
            // 只要用户或卡号其中一个成功，就算下发成功
            if (userResult.isSuccess() || cardResult.isSuccess()) {
                // 发布授权下发事件
                publishAccessEvent(deviceId, EVENT_CARD_SWIPE, Map.of(
                        "action", "DISPATCH_AUTH",
                        "cardNo", cardNo,
                        "userId", userId != null ? userId : cardNo
                ));
                
                return CommandResult.success(Map.of(
                        "message", "授权下发成功",
                        "cardNo", cardNo,
                        "userId", userId != null ? userId : cardNo
                ));
            } else {
                // 两个都失败，返回错误信息
                String errorMsg = userResult.isSuccess() ? cardResult.getMessage() : userResult.getMessage();
                return CommandResult.failure("下发授权失败: " + errorMsg);
            }
        } catch (Exception e) {
            log.error("{} 下发授权异常: deviceId={}, cardNo={}", LOG_PREFIX, deviceId, cardNo, e);
            return CommandResult.failure("下发授权异常: " + e.getMessage());
        }
    }

    /**
     * 执行撤销授权命令（通过标准 API）
     */
    private CommandResult executeRevokeAuth(Long deviceId, DeviceCommand command) {
        // 使用 getStringParam 避免类型转换异常
        String cardNo = command.getStringParam("cardNo");
        String userId = command.getStringParam("userId");

        if (cardNo == null && userId == null) {
            return CommandResult.failure("缺少 cardNo 或 userId 参数");
        }

        log.info("{} 执行撤销授权: deviceId={}, cardNo={}, userId={}", 
                LOG_PREFIX, deviceId, cardNo, userId);

        try {
            // 确保设备已连接（支持按需连接）
            Long loginHandle = ensureConnected(deviceId, command);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接");
            }
            
            AccessGen2OperationResult sdkResult;
            
            // 优先使用卡号删除，否则使用用户ID删除
            if (cardNo != null && !cardNo.isEmpty()) {
                sdkResult = adapterFor(deviceId).deleteCard(loginHandle, cardNo);
            } else {
                sdkResult = adapterFor(deviceId).deleteUser(loginHandle, userId);
            }
            
            if (sdkResult.isSuccess()) {
                // 发布授权撤销事件
                publishAccessEvent(deviceId, EVENT_CARD_SWIPE, Map.of(
                        "action", "REVOKE_AUTH",
                        "cardNo", cardNo != null ? cardNo : "",
                        "userId", userId != null ? userId : ""
                ));
                
                return CommandResult.success(Map.of(
                        "message", "授权撤销成功",
                        "cardNo", cardNo != null ? cardNo : "",
                        "userId", userId != null ? userId : ""
                ));
            } else {
                return CommandResult.failure("撤销授权失败: " + sdkResult.getMessage());
            }
        } catch (Exception e) {
            log.error("{} 撤销授权异常: deviceId={}, cardNo={}", LOG_PREFIX, deviceId, cardNo, e);
            return CommandResult.failure("撤销授权异常: " + e.getMessage());
        }
    }

    /**
     * 执行查询授权命令
     */
    private CommandResult executeQueryAuth(Long deviceId, DeviceCommand command) {
        // 使用 getStringParam 避免类型转换异常
        String cardNo = command.getStringParam("cardNo");
        String userId = command.getStringParam("userId");

        log.info("{} ========== 执行查询授权 ==========", LOG_PREFIX);
        log.info("{} deviceId={}, cardNo={}, userId={}", LOG_PREFIX, deviceId, cardNo, userId);

        try {
            // 获取登录句柄
            Long loginHandle = connectionManager.getLoginHandle(deviceId);
            log.info("{} 获取登录句柄: loginHandle={}", LOG_PREFIX, loginHandle);
            
            if (loginHandle == null || loginHandle <= 0) {
                log.warn("{} 设备未连接，无法查询", LOG_PREFIX);
                return CommandResult.failure("设备未连接");
            }
            
            // 查询用户列表
            log.info("{} 开始查询用户列表, userId={}", LOG_PREFIX, userId);
            List<AccessGen2UserInfo> users = adapterFor(deviceId).queryUsers(loginHandle, userId);
            log.info("{} ✅ SDK查询完成，获取到 {} 个用户", LOG_PREFIX, users.size());
            
            // 转换为 Map 列表
            List<Map<String, Object>> recordMaps = users.stream()
                    .map(this::convertUserInfoToMap)
                    .collect(Collectors.toList());
            
            log.info("{} 返回结果: totalCount={}", LOG_PREFIX, recordMaps.size());
            
            return CommandResult.success(Map.of(
                    "message", "查询成功",
                    "totalCount", users.size(),
                    "records", recordMaps
            ));
        } catch (Exception e) {
            log.error("{} 查询授权异常: deviceId={}", LOG_PREFIX, deviceId, e);
            return CommandResult.failure("查询授权异常: " + e.getMessage());
        }
    }

    /**
     * 将用户信息转换为 Map
     */
    private Map<String, Object> convertUserInfoToMap(AccessGen2UserInfo userInfo) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userInfo.getUserId());
        map.put("userName", userInfo.getUserName());
        map.put("cardNo", userInfo.getCardNo());
        map.put("cardType", userInfo.getCardType());
        map.put("cardStatus", userInfo.getCardStatus());
        map.put("userType", userInfo.getUserType());
        map.put("validStartTime", userInfo.getValidStartTime());
        map.put("validEndTime", userInfo.getValidEndTime());
        map.put("faceCount", userInfo.getFaceCount());
        map.put("fingerprintCount", userInfo.getFingerprintCount());
        map.put("doors", userInfo.getDoors());
        map.put("timeSections", userInfo.getTimeSections());
        return map;
    }

    /**
     * 从 Map 中获取字符串值，支持多个候选键
     * 
     * @param map 数据源 Map
     * @param keys 候选键名，按顺序尝试获取
     * @return 首个存在的值，如果都不存在则返回 null
     */
    private String getStringFromMap(Map<String, Object> map, String... keys) {
        if (map == null) return null;
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

    /**
     * 规范化有效期时间
     * 如果是 null、空或 1970-01-01（epoch time），返回默认值
     * 
     * @param timeStr 时间字符串
     * @param isStart 是否是开始时间（决定默认值）
     * @return 规范化后的时间字符串
     */
    private String normalizeValidTime(String timeStr, boolean isStart) {
        if (timeStr == null || timeStr.isEmpty() || timeStr.startsWith("1970-01-01")) {
            // 设置默认有效期：开始时间=当前时间，结束时间=5年后
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime defaultTime = isStart ? now : now.plusYears(5);
            String result = defaultTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            log.info("{} 有效期为空或无效，使用默认值: original={}, isStart={}, default={}", 
                    LOG_PREFIX, timeStr, isStart, result);
            return result;
        }
        return timeStr;
    }
    
    /**
     * 规范化有效期时间（单参数版本，向后兼容）
     * 仅过滤 epoch time，不设置默认值
     * 
     * @param timeStr 时间字符串
     * @return 规范化后的时间字符串，如果是 epoch time 则返回 null
     */
    private String normalizeValidTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        if (timeStr.startsWith("1970-01-01")) {
            log.debug("{} 忽略 epoch time 有效期: {}", LOG_PREFIX, timeStr);
            return null;
        }
        return timeStr;
    }

    /**
     * 执行清空所有授权命令
     */
    private CommandResult executeClearAllAuth(Long deviceId) {
        log.info("{} 执行清空所有授权: deviceId={}", LOG_PREFIX, deviceId);

        try {
            // 获取登录句柄
            Long loginHandle = connectionManager.getLoginHandle(deviceId);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接");
            }
            
            // 清空所有用户
            AccessGen2OperationResult userResult = adapterFor(deviceId).clearAllUsers(loginHandle);
            
            // 清空所有卡号
            AccessGen2OperationResult cardResult = adapterFor(deviceId).clearAllCards(loginHandle);
            
            if (userResult.isSuccess() || cardResult.isSuccess()) {
                // 发布清空授权事件
                publishAccessEvent(deviceId, EVENT_CARD_SWIPE, Map.of(
                        "action", "CLEAR_ALL_AUTH"
                ));
                
                return CommandResult.success(Map.of(
                        "message", "清空所有授权成功"
                ));
            } else {
                return CommandResult.failure("清空授权失败: " + userResult.getMessage());
            }
        } catch (Exception e) {
            log.error("{} 清空授权异常: deviceId={}", LOG_PREFIX, deviceId, e);
            return CommandResult.failure("清空授权异常: " + e.getMessage());
        }
    }


    /**
     * 执行下发人脸命令
     * 
     * <p>新方案：从 faceUrl 下载图片，在网关侧进行处理（压缩、格式转换），然后下发到设备</p>
     */
    private CommandResult executeDispatchFace(Long deviceId, DeviceCommand command) {
        if (!config.isFaceDownloadEnabled()) {
            return CommandResult.failure("人脸下发功能未启用");
        }

        // 打印所有参数以便调试
        Map<String, Object> params = command.getParams();
        log.info("{} 下发人脸命令参数: deviceId={}, params keys={}", 
                LOG_PREFIX, deviceId, params != null ? params.keySet() : "null");
        
        // 使用 getStringParam 避免类型转换异常
        String userId = command.getStringParam("userId");
        String faceUrl = command.getStringParam("faceUrl"); // 人脸图片 URL（推荐方式）

        log.info("{} 提取参数: userId={}, faceUrl={}", LOG_PREFIX, userId, faceUrl);

        if (userId == null || userId.isEmpty()) {
            log.warn("{} 缺少 userId 参数, params={}", LOG_PREFIX, params);
            return CommandResult.failure("缺少 userId 参数");
        }
        if (faceUrl == null || faceUrl.isEmpty()) {
            log.warn("{} 缺少 faceUrl 参数, 可用参数: {}", LOG_PREFIX, params != null ? params.keySet() : "null");
            return CommandResult.failure("缺少 faceUrl 参数");
        }

        log.info("{} 执行下发人脸: deviceId={}, userId={}, faceUrl={}", 
                LOG_PREFIX, deviceId, userId, faceUrl);

        try {
            // 获取登录句柄（支持按需连接）
            Long loginHandle = ensureConnected(deviceId, command);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接，无法建立连接");
            }
            
            // 检查设备是否支持人脸
            if (!connectionManager.isSupportsFace(deviceId)) {
                return CommandResult.failure("设备不支持人脸识别");
            }
            
            // 从 URL 下载并处理图片
            String faceDataBase64 = downloadAndProcessFaceImage(faceUrl);
            if (faceDataBase64 == null || faceDataBase64.isEmpty()) {
                log.error("{} 下载或处理人脸图片失败: faceUrl={}", LOG_PREFIX, faceUrl);
                return CommandResult.failure("下载或处理人脸图片失败");
            }
            
            log.info("{} 人脸图片处理完成: userId={}, base64Len={}", 
                    LOG_PREFIX, userId, faceDataBase64.length());
            
            // 构建人脸信息
            AccessGen2FaceInfo faceInfo = AccessGen2FaceInfo.builder()
                    .userId(userId)
                    .faceData(faceDataBase64)
                    .build();
            
            // 调用 SDK 下发人脸（如果失败则尝试删除后重新添加）
            AccessGen2OperationResult sdkResult = adapterFor(deviceId).addFace(loginHandle, faceInfo);
            
            if (!sdkResult.isSuccess()) {
                // 添加失败，可能是人脸已存在，尝试删除后重新添加
                log.info("{} 添加人脸失败，尝试删除后重新添加: userId={}, msg={}", LOG_PREFIX, userId, sdkResult.getMessage());
                AccessGen2OperationResult deleteResult = adapterFor(deviceId).deleteFace(loginHandle, userId);
                log.info("{} 删除人脸结果: userId={}, success={}, msg={}", 
                        LOG_PREFIX, userId, deleteResult.isSuccess(), deleteResult.getMessage());
                
                // 等待设备处理删除操作
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                sdkResult = adapterFor(deviceId).addFace(loginHandle, faceInfo);
                log.info("{} 重新添加人脸结果: userId={}, success={}, msg={}", 
                        LOG_PREFIX, userId, sdkResult.isSuccess(), sdkResult.getMessage());
            }
            
            if (sdkResult.isSuccess()) {
                // 发布人脸下发事件
                publishAccessEvent(deviceId, EVENT_FACE_RECOGNIZE, Map.of(
                        "action", "DISPATCH_FACE",
                        "userId", userId
                ));
                
                return CommandResult.success(Map.of(
                        "message", "人脸下发成功",
                        "userId", userId
                ));
            } else {
                return CommandResult.failure("下发人脸失败: " + sdkResult.getMessage());
            }
        } catch (Exception e) {
            log.error("{} 下发人脸异常: deviceId={}, userId={}", LOG_PREFIX, deviceId, userId, e);
            return CommandResult.failure("下发人脸异常: " + e.getMessage());
        }
    }
    
    /**
     * 从 URL 下载人脸图片并处理
     * 
     * @param faceUrl 图片 URL
     * @return Base64 编码的图片数据，失败返回 null
     */
    private String downloadAndProcessFaceImage(String faceUrl) {
        try {
            log.info("{} 开始处理人脸图片: url={}", LOG_PREFIX, 
                    faceUrl != null && faceUrl.length() > 100 ? faceUrl.substring(0, 100) + "..." : faceUrl);
            
            byte[] imageData;
            
            // 检查是否是 Base64 数据（以 base64:// 前缀标记）
            if (faceUrl != null && faceUrl.startsWith("base64://")) {
                // 直接解码 Base64 数据
                String base64Data = faceUrl.substring("base64://".length());
                log.info("{} 检测到 Base64 数据，开始解码: dataLen={}", LOG_PREFIX, base64Data.length());
                try {
                    imageData = java.util.Base64.getDecoder().decode(base64Data);
                    log.info("{} Base64 解码完成: size={}KB", LOG_PREFIX, imageData.length / 1024);
                } catch (IllegalArgumentException e) {
                    log.error("{} Base64 解码失败: {}", LOG_PREFIX, e.getMessage());
                    return null;
                }
            } else {
                // 从 URL 下载图片
                log.info("{} 从 URL 下载图片: url={}", LOG_PREFIX, faceUrl);
                java.net.URL url = new java.net.URL(faceUrl);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);  // 10秒连接超时
                connection.setReadTimeout(30000);     // 30秒读取超时
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "AccessGen2Plugin/1.0");
                
                int responseCode = connection.getResponseCode();
                if (responseCode != java.net.HttpURLConnection.HTTP_OK) {
                    log.error("{} 下载图片失败: url={}, responseCode={}", LOG_PREFIX, faceUrl, responseCode);
                    return null;
                }
                
                // 读取图片数据
                try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = connection.getInputStream().read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    imageData = baos.toByteArray();
                } finally {
                    connection.disconnect();
                }
                
                log.info("{} 图片下载完成: size={}KB", LOG_PREFIX, imageData.length / 1024);
            }
            
            // 处理图片：压缩到 100KB 以内，最大分辨率 720x576（大华门禁标准要求）
            byte[] processedData = processImage(imageData, 100, 720, 576);
            if (processedData == null) {
                log.error("{} 处理图片失败", LOG_PREFIX);
                return null;
            }
            
            log.info("{} 图片处理完成: originalSize={}KB, processedSize={}KB", 
                    LOG_PREFIX, imageData.length / 1024, processedData.length / 1024);
            
            // 转换为 Base64
            return java.util.Base64.getEncoder().encodeToString(processedData);
            
        } catch (java.net.SocketTimeoutException e) {
            log.error("{} 下载图片超时: url={}", LOG_PREFIX, faceUrl);
            return null;
        } catch (java.io.IOException e) {
            log.error("{} 下载图片失败: url={}, error={}", LOG_PREFIX, faceUrl, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("{} 处理图片异常: url={}", LOG_PREFIX, faceUrl, e);
            return null;
        }
    }
    
    /**
     * 处理图片：调整尺寸和压缩
     * 
     * @param imageData 原始图片数据
     * @param maxSizeKB 最大文件大小（KB）
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @return 处理后的图片数据，失败返回 null
     */
    private byte[] processImage(byte[] imageData, int maxSizeKB, int maxWidth, int maxHeight) {
        try {
            // 读取图片
            java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(
                    new java.io.ByteArrayInputStream(imageData));
            if (image == null) {
                log.warn("{} 无法解析图片数据", LOG_PREFIX);
                return null;
            }
            
            int originalWidth = image.getWidth();
            int originalHeight = image.getHeight();
            
            // 检查是否需要调整尺寸
            if (originalWidth > maxWidth || originalHeight > maxHeight) {
                double widthRatio = (double) maxWidth / originalWidth;
                double heightRatio = (double) maxHeight / originalHeight;
                double ratio = Math.min(widthRatio, heightRatio);
                
                int newWidth = (int) (originalWidth * ratio);
                int newHeight = (int) (originalHeight * ratio);
                
                java.awt.image.BufferedImage resized = new java.awt.image.BufferedImage(
                        newWidth, newHeight, java.awt.image.BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics2D g2d = resized.createGraphics();
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, 
                        java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setColor(java.awt.Color.WHITE);
                g2d.fillRect(0, 0, newWidth, newHeight);
                g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
                g2d.dispose();
                image = resized;
                
                log.info("{} 图片调整尺寸: {}x{} -> {}x{}", 
                        LOG_PREFIX, originalWidth, originalHeight, newWidth, newHeight);
            }
            
            // 确保是 RGB 格式
            if (image.getType() != java.awt.image.BufferedImage.TYPE_INT_RGB) {
                java.awt.image.BufferedImage rgbImage = new java.awt.image.BufferedImage(
                        image.getWidth(), image.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics2D g2d = rgbImage.createGraphics();
                g2d.setColor(java.awt.Color.WHITE);
                g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
                g2d.drawImage(image, 0, 0, null);
                g2d.dispose();
                image = rgbImage;
            }
            
            // 压缩为 JPEG 格式
            float quality = 0.9f;
            byte[] result = null;
            
            while (quality >= 0.3f) {
                result = compressToJpeg(image, quality);
                if (result.length <= maxSizeKB * 1024) {
                    return result;
                }
                quality -= 0.1f;
            }
            
            // 如果仍然超过限制，进一步缩小尺寸
            if (result != null && result.length > maxSizeKB * 1024) {
                int newWidth = image.getWidth() * 3 / 4;
                int newHeight = image.getHeight() * 3 / 4;
                
                if (newWidth > 100 && newHeight > 100) {
                    java.awt.image.BufferedImage smaller = new java.awt.image.BufferedImage(
                            newWidth, newHeight, java.awt.image.BufferedImage.TYPE_INT_RGB);
                    java.awt.Graphics2D g2d = smaller.createGraphics();
                    g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, 
                            java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
                    g2d.dispose();
                    result = compressToJpeg(smaller, 0.3f);
                }
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("{} 处理图片异常: {}", LOG_PREFIX, e.getMessage());
            return imageData;  // 处理失败返回原始数据
        }
    }
    
    /**
     * 压缩图片为 JPEG 格式
     */
    private byte[] compressToJpeg(java.awt.image.BufferedImage image, float quality) throws java.io.IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        
        javax.imageio.ImageWriter writer = javax.imageio.ImageIO.getImageWritersByFormatName("jpeg").next();
        javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);
        
        try (javax.imageio.stream.ImageOutputStream ios = javax.imageio.ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
        
        return baos.toByteArray();
    }

    /**
     * 执行删除人脸命令
     */
    private CommandResult executeDeleteFace(Long deviceId, DeviceCommand command) {
        // 使用 getStringParam 避免类型转换异常
        String userId = command.getStringParam("userId");

        if (userId == null || userId.isEmpty()) {
            return CommandResult.failure("缺少 userId 参数");
        }

        log.info("{} 执行删除人脸: deviceId={}, userId={}", LOG_PREFIX, deviceId, userId);

        try {
            // 获取登录句柄（支持按需连接）
            Long loginHandle = ensureConnected(deviceId, command);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接，无法建立连接");
            }
            
            // 调用 SDK 删除人脸
            AccessGen2OperationResult sdkResult = adapterFor(deviceId).deleteFace(loginHandle, userId);
            
            if (sdkResult.isSuccess()) {
                // 发布人脸删除事件
                publishAccessEvent(deviceId, EVENT_FACE_RECOGNIZE, Map.of(
                        "action", "DELETE_FACE",
                        "userId", userId
                ));
                
                return CommandResult.success(Map.of(
                        "message", "人脸删除成功",
                        "userId", userId
                ));
            } else {
                return CommandResult.failure("删除人脸失败: " + sdkResult.getMessage());
            }
        } catch (Exception e) {
            log.error("{} 删除人脸异常: deviceId={}, userId={}", LOG_PREFIX, deviceId, userId, e);
            return CommandResult.failure("删除人脸异常: " + e.getMessage());
        }
    }

    /**
     * 执行下发指纹命令
     */
    private CommandResult executeDispatchFingerprint(Long deviceId, DeviceCommand command) {
        if (!config.isFingerprintDownloadEnabled()) {
            return CommandResult.failure("指纹下发功能未启用");
        }

        // 使用安全的类型转换方法避免类型转换异常
        String userId = command.getStringParam("userId");
        String fingerprintData = command.getStringParam("fingerprintData"); // Base64 编码的指纹数据
        Integer fingerIndex = command.getIntParam("fingerIndex"); // 手指索引

        if (userId == null || userId.isEmpty()) {
            return CommandResult.failure("缺少 userId 参数");
        }
        if (fingerprintData == null || fingerprintData.isEmpty()) {
            return CommandResult.failure("缺少 fingerprintData 参数");
        }

        log.info("{} 执行下发指纹: deviceId={}, userId={}, fingerIndex={}", 
                LOG_PREFIX, deviceId, userId, fingerIndex);

        try {
            // 获取登录句柄（支持按需连接）
            Long loginHandle = ensureConnected(deviceId, command);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接，无法建立连接");
            }
            
            // 检查设备是否支持指纹
            if (!connectionManager.isSupportsFingerprint(deviceId)) {
                return CommandResult.failure("设备不支持指纹识别");
            }
            
            // 构建指纹信息
            AccessGen2FingerprintInfo fingerprintInfo = AccessGen2FingerprintInfo.builder()
                    .userId(userId)
                    .fingerprintData(fingerprintData)
                    .fingerIndex(fingerIndex != null ? fingerIndex : 0)
                    .build();
            
            // 调用 SDK 下发指纹
            AccessGen2OperationResult sdkResult = adapterFor(deviceId).addFingerprint(loginHandle, fingerprintInfo);
            
            if (sdkResult.isSuccess()) {
                // 发布指纹下发事件
                publishAccessEvent(deviceId, EVENT_FINGERPRINT_RECOGNIZE, Map.of(
                        "action", "DISPATCH_FINGERPRINT",
                        "userId", userId,
                        "fingerIndex", fingerIndex != null ? fingerIndex : 0
                ));
                
                return CommandResult.success(Map.of(
                        "message", "指纹下发成功",
                        "userId", userId
                ));
            } else {
                return CommandResult.failure("下发指纹失败: " + sdkResult.getMessage());
            }
        } catch (Exception e) {
            log.error("{} 下发指纹异常: deviceId={}, userId={}", LOG_PREFIX, deviceId, userId, e);
            return CommandResult.failure("下发指纹异常: " + e.getMessage());
        }
    }

    /**
     * 执行删除指纹命令
     */
    private CommandResult executeDeleteFingerprint(Long deviceId, DeviceCommand command) {
        // 使用安全的类型转换方法避免类型转换异常
        String userId = command.getStringParam("userId");
        Integer fingerIndex = command.getIntParam("fingerIndex"); // 手指索引，null 表示删除所有

        if (userId == null || userId.isEmpty()) {
            return CommandResult.failure("缺少 userId 参数");
        }

        log.info("{} 执行删除指纹: deviceId={}, userId={}, fingerIndex={}", 
                LOG_PREFIX, deviceId, userId, fingerIndex);

        try {
            // 获取登录句柄（支持按需连接）
            Long loginHandle = ensureConnected(deviceId, command);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接，无法建立连接");
            }
            
            // 调用 SDK 删除指纹
            AccessGen2OperationResult sdkResult = adapterFor(deviceId).deleteFingerprint(loginHandle, userId, fingerIndex);
            
            if (sdkResult.isSuccess()) {
                // 发布指纹删除事件
                publishAccessEvent(deviceId, EVENT_FINGERPRINT_RECOGNIZE, Map.of(
                        "action", "DELETE_FINGERPRINT",
                        "userId", userId,
                        "fingerIndex", fingerIndex != null ? fingerIndex : -1
                ));
                
                return CommandResult.success(Map.of(
                        "message", "指纹删除成功",
                        "userId", userId
                ));
            } else {
                return CommandResult.failure("删除指纹失败: " + sdkResult.getMessage());
            }
        } catch (Exception e) {
            log.error("{} 删除指纹异常: deviceId={}, userId={}", LOG_PREFIX, deviceId, userId, e);
            return CommandResult.failure("删除指纹异常: " + e.getMessage());
        }
    }

    /**
     * 执行查询通道命令
     * <p>
     * 从设备获取已配置的门通道信息，包含生物识别能力。
     * </p>
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
     */
    private CommandResult executeQueryChannels(Long deviceId, DeviceCommand command) {
        log.info("{} 执行查询通道: deviceId={}", LOG_PREFIX, deviceId);

        try {
            // 获取登录句柄（支持按需连接）
            Long loginHandle = ensureConnected(deviceId, command);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接，无法查询通道");
            }
            
            // 获取设备能力
            boolean supportsFace = connectionManager.isSupportsFace(deviceId);
            boolean supportsFingerprint = connectionManager.isSupportsFingerprint(deviceId);
            
            // 调用 SDK 查询门通道
            AccessGen2OperationResult sdkResult = adapterFor(deviceId).queryDoorChannels(loginHandle, supportsFace, supportsFingerprint);
            
            if (!sdkResult.isSuccess()) {
                return CommandResult.failure("查询通道失败: " + sdkResult.getMessage());
            }
            
            // 构建通道列表
            List<Map<String, Object>> channelList = new ArrayList<>();
            @SuppressWarnings("unchecked")
            List<AccessGen2DoorInfo> doors = sdkResult.getData() != null ? 
                    (List<AccessGen2DoorInfo>) sdkResult.getData().get("doorList") : null;
            
            if (doors != null) {
                for (AccessGen2DoorInfo door : doors) {
                    Map<String, Object> channelInfo = new HashMap<>();
                    channelInfo.put("channelNo", door.getDoorNo());
                    channelInfo.put("channelName", door.getDoorName());
                    channelInfo.put("channelType", "ACCESS");
                    channelInfo.put("status", door.getDoorStatus()); // 0-关闭, 1-打开, 2-未知
                    channelInfo.put("capabilities", Map.of(
                        "hasCard", Boolean.TRUE.equals(door.getCardSupported()),
                        "hasFace", Boolean.TRUE.equals(door.getFaceSupported()),
                        "hasFingerprint", Boolean.TRUE.equals(door.getFingerprintSupported())
                    ));
                    channelList.add(channelInfo);
                }
            }
            
            log.info("{} ✅ 查询通道成功: deviceId={}, channelCount={}", 
                    LOG_PREFIX, deviceId, channelList.size());
            
            return CommandResult.success(Map.of(
                "deviceId", deviceId,
                "channelList", channelList,
                "queryTime", java.time.LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            log.error("{} 查询通道异常: deviceId={}", LOG_PREFIX, deviceId, e);
            return CommandResult.failure("查询通道异常: " + e.getMessage());
        }
    }


    // ==================== 事件处理方法 ====================

    /**
     * 处理门禁事件
     * <p>
     * 当收到设备的门禁事件回调时调用此方法。
     * </p>
     *
     * @param deviceId  设备ID
     * @param eventType 事件类型
     * @param eventData 事件数据
     */
    public void handleAccessEvent(Long deviceId, String eventType, Map<String, Object> eventData) {
        log.info("{} 收到门禁事件: deviceId={}, eventType={}", LOG_PREFIX, deviceId, eventType);

        try {
            int eventTypeInt = Integer.parseInt(eventType);
            publishAccessEvent(deviceId, eventTypeInt, eventData);
            log.info("{} 门禁事件已发布: deviceId={}, eventType={}", LOG_PREFIX, deviceId, eventType);
        } catch (Exception e) {
            log.error("{} 处理门禁事件失败: deviceId={}, eventType={}", LOG_PREFIX, deviceId, eventType, e);
        }
    }

    /**
     * 发布门禁事件到消息总线
     *
     * @param deviceId  设备ID
     * @param eventType 事件类型（使用 AccessControlEventMessage.EventType 中的整数常量）
     * @param eventData 事件数据
     */
    private void publishAccessEvent(Long deviceId, int eventType, Map<String, Object> eventData) {
        // 构建 AccessControlEventMessage 对象（与 Biz 端期望的消息格式一致）
        AccessControlEventMessage.AccessControlEventMessageBuilder builder = AccessControlEventMessage.builder()
                .deviceId(deviceId)
                .eventType(eventType)
                .timestamp(System.currentTimeMillis())
                .eventTime(java.time.LocalDateTime.now());
        
        // 从 eventData 中提取常用字段
        if (eventData != null) {
            if (eventData.containsKey("channelNo")) {
                Object channelNo = eventData.get("channelNo");
                if (channelNo instanceof Number) {
                    builder.channelNo(((Number) channelNo).intValue());
                }
            }
            if (eventData.containsKey("cardNo")) {
                Object cardNo = eventData.get("cardNo");
                builder.cardNo(cardNo != null ? cardNo.toString() : null);
            }
            if (eventData.containsKey("userId")) {
                Object userId = eventData.get("userId");
                builder.personId(userId != null ? userId.toString() : null);
            }
            if (eventData.containsKey("userName")) {
                Object userName = eventData.get("userName");
                builder.personName(userName != null ? userName.toString() : null);
            }
            if (eventData.containsKey("verifyMode")) {
                Object verifyMode = eventData.get("verifyMode");
                if (verifyMode instanceof Number) {
                    builder.verifyMode(((Number) verifyMode).intValue());
                }
            }
            if (eventData.containsKey("verifyResult")) {
                Object verifyResult = eventData.get("verifyResult");
                if (verifyResult instanceof Number) {
                    builder.verifyResult(((Number) verifyResult).intValue());
                }
            }
            // 将其他数据放入 extData
            Map<String, Object> extData = new HashMap<>(eventData);
            extData.put("deviceType", PluginConstants.DEVICE_TYPE_ACCESS_GEN2);
            extData.put("pluginId", PluginConstants.PLUGIN_ID_ACCESS_GEN2);
            builder.extData(extData);
        } else {
            // 即使没有 eventData，也要设置 extData 以包含设备类型信息
            Map<String, Object> extData = new HashMap<>();
            extData.put("deviceType", PluginConstants.DEVICE_TYPE_ACCESS_GEN2);
            extData.put("pluginId", PluginConstants.PLUGIN_ID_ACCESS_GEN2);
            builder.extData(extData);
        }

        // 顶层 deviceType 用于统一事件通道路由（DEVICE_EVENT_REPORTED）
        builder.deviceType(PluginConstants.DEVICE_TYPE_ACCESS_GEN2);

        // 统一发布到 DEVICE_EVENT_REPORTED：Biz 侧由 DeviceEventConsumer 统一消费/路由
        messagePublisher.publishEvent(IotMessageTopics.DEVICE_EVENT_REPORTED, builder.build());
    }

    /**
     * 处理刷卡事件回调
     *
     * <p>【重要】二代门禁的远程开门操作会触发刷卡事件（设备将远程开门视为一种"刷卡开门"）。
     * 因此，当收到刷卡成功事件（result=0）时，需要尝试确认开门命令。</p>
     *
     * @param deviceId   设备ID
     * @param cardNo     卡号
     * @param userId     用户ID
     * @param channelNo  通道号
     * @param accessTime 刷卡时间
     * @param result     刷卡结果（0-成功，其他-失败）
     */
    public void handleCardSwipeEvent(Long deviceId, String cardNo, String userId, 
                                      int channelNo, long accessTime, int result) {
        // 事件去重：防止SDK短时间内触发多次相同事件
        String dedupeKey = deviceId + ":" + channelNo + ":" + EVENT_CARD_SWIPE + ":" + result + ":" + cardNo;
        Long lastEventTime = eventDedupeCache.get(dedupeKey);
        long now = System.currentTimeMillis();
        
        if (lastEventTime != null && (now - lastEventTime) < EVENT_DEDUPE_WINDOW_MS) {
            log.debug("{} 忽略重复刷卡事件（{}ms内）: deviceId={}, channel={}, cardNo={}", 
                    LOG_PREFIX, EVENT_DEDUPE_WINDOW_MS, deviceId, channelNo, cardNo);
            return;
        }
        
        // 更新去重缓存
        eventDedupeCache.put(dedupeKey, now);
        
        // 清理过期的缓存条目（避免内存泄漏）
        cleanupExpiredDedupeEntries();
        
        log.info("{} 收到刷卡事件: deviceId={}, cardNo={}, userId={}, channel={}, result={}", 
                LOG_PREFIX, deviceId, cardNo, userId, channelNo, result);

        // 【关键】二代门禁的远程开门会触发刷卡成功事件（result=0）
        // 当收到刷卡成功且是空卡号（远程开门特征），尝试确认开门命令
        if (result == 0) {
            // 使用虚拟的 alarmType=12673（远程开门事件）来确认命令
            // 这样可以复用一代门禁的确认机制
            var matchResult = doorCommandConfirmationService.confirmCommand(
                    deviceId, channelNo, 
                    cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1.DoorCommandConfirmationService.ALARM_REMOTE_OPEN_DOOR);
            
            if (matchResult.isMatched()) {
                log.info("{} ✅ 二代门禁开门命令已通过刷卡成功事件确认: deviceId={}, channelNo={}, matchedChannelNo={}", 
                        LOG_PREFIX, deviceId, channelNo, matchResult.getChannelNo());
            }
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("cardNo", cardNo);
        eventData.put("userId", userId);
        eventData.put("channelNo", channelNo);
        eventData.put("accessTime", accessTime);
        eventData.put("result", result);
        eventData.put("resultDesc", result == 0 ? "成功" : "失败");

        publishAccessEvent(deviceId, EVENT_CARD_SWIPE, eventData);
    }
    
    /**
     * 清理过期的去重缓存条目
     */
    private void cleanupExpiredDedupeEntries() {
        long expireThreshold = System.currentTimeMillis() - (EVENT_DEDUPE_WINDOW_MS * 10);
        eventDedupeCache.entrySet().removeIf(entry -> entry.getValue() < expireThreshold);
    }

    /**
     * 门禁事件回调（智能报警带图）
     * <p>
     * 按《智能楼宇分册》：dwAlarmType=EVENT_IVS_ACCESS_CTL，pAlarmInfo=DEV_EVENT_ACCESS_CTL_INFO
     * </p>
     */
    private static class AccessCtlAnalyzerDataCallBack implements NetSDKLib.fAnalyzerDataCallBack {
        private final AccessGen2Plugin plugin;

        private AccessCtlAnalyzerDataCallBack(AccessGen2Plugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public int invoke(NetSDKLib.LLong lAnalyzerHandle, int dwAlarmType, Pointer pAlarmInfo,
                          Pointer pBuffer, int dwBufSize, Pointer dwUser, int nSequence, Pointer reserved) {
            try {
                if (pAlarmInfo == null) {
                    return 0;
                }
                if (dwAlarmType != NetSDKLib.EVENT_IVS_ACCESS_CTL) {
                    return 0;
                }
                long deviceId = dwUser != null ? Pointer.nativeValue(dwUser) : 0L;
                if (deviceId <= 0) {
                    return 0;
                }
                NetSDKLib.DEV_EVENT_ACCESS_CTL_INFO msg = new NetSDKLib.DEV_EVENT_ACCESS_CTL_INFO();
                ToolKits.GetPointerData(pAlarmInfo, msg);

                String cardNo = new String(msg.szCardNo).trim();
                String userId = new String(msg.szUserID).trim();
                int channelNo = msg.nChannelID + 1;
                // result：0 成功，其它失败（优先用 nErrorCode，其次用 bStatus 兜底）
                int result = (msg.nErrorCode == 0 && msg.bStatus == 1) ? 0 : (msg.nErrorCode != 0 ? msg.nErrorCode : 1);

                plugin.handleCardSwipeEvent(deviceId, cardNo, userId, channelNo, System.currentTimeMillis(), result);
                return 0;
            } catch (Exception e) {
                // 回调线程内禁止抛出异常
                log.warn("[AccessGen2Plugin] 门禁事件回调处理异常: {}", e.getMessage(), e);
                return 0;
            }
        }
    }

    /**
     * 处理人脸识别事件回调
     *
     * @param deviceId   设备ID
     * @param userId     用户ID
     * @param channelNo  通道号
     * @param accessTime 识别时间
     * @param result     识别结果（0-成功，其他-失败）
     * @param similarity 相似度（0-100）
     */
    public void handleFaceRecognizeEvent(Long deviceId, String userId, int channelNo, 
                                          long accessTime, int result, int similarity) {
        log.info("{} 收到人脸识别事件: deviceId={}, userId={}, channel={}, result={}, similarity={}", 
                LOG_PREFIX, deviceId, userId, channelNo, result, similarity);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("userId", userId);
        eventData.put("channelNo", channelNo);
        eventData.put("accessTime", accessTime);
        eventData.put("result", result);
        eventData.put("resultDesc", result == 0 ? "成功" : "失败");
        eventData.put("similarity", similarity);

        publishAccessEvent(deviceId, EVENT_FACE_RECOGNIZE, eventData);
    }

    /**
     * 处理指纹识别事件回调
     *
     * @param deviceId    设备ID
     * @param userId      用户ID
     * @param channelNo   通道号
     * @param accessTime  识别时间
     * @param result      识别结果（0-成功，其他-失败）
     * @param fingerIndex 手指索引
     */
    public void handleFingerprintRecognizeEvent(Long deviceId, String userId, int channelNo, 
                                                 long accessTime, int result, int fingerIndex) {
        log.info("{} 收到指纹识别事件: deviceId={}, userId={}, channel={}, result={}, fingerIndex={}", 
                LOG_PREFIX, deviceId, userId, channelNo, result, fingerIndex);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("userId", userId);
        eventData.put("channelNo", channelNo);
        eventData.put("accessTime", accessTime);
        eventData.put("result", result);
        eventData.put("resultDesc", result == 0 ? "成功" : "失败");
        eventData.put("fingerIndex", fingerIndex);

        publishAccessEvent(deviceId, EVENT_FINGERPRINT_RECOGNIZE, eventData);
    }

    /**
     * 处理报警事件回调
     *
     * @param deviceId  设备ID
     * @param alarmType 报警类型
     * @param channelNo 通道号
     * @param alarmTime 报警时间
     */
    public void handleAlarmEvent(Long deviceId, int alarmType, int channelNo, long alarmTime) {
        log.info("{} 收到报警事件: deviceId={}, alarmType={}, channel={}", 
                LOG_PREFIX, deviceId, alarmType, channelNo);

        // 【关键】尝试确认待确认的命令（基于设备事件回调的可靠确认机制）
        var matchResult = doorCommandConfirmationService.confirmCommand(deviceId, channelNo, alarmType);
        
        // 【关键】使用匹配到的 channelNo（用户实际操作的门），而不是设备上报的 channelNo（可能是 0）
        int effectiveChannelNo = channelNo;
        if (matchResult.isMatched() && matchResult.getChannelNo() != null) {
            effectiveChannelNo = matchResult.getChannelNo();
            log.info("{} 命令已通过设备事件确认: deviceId={}, alarmType={}, eventChannelNo={}, effectiveChannelNo={}", 
                    LOG_PREFIX, deviceId, alarmType, channelNo, effectiveChannelNo);
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("alarmType", alarmType);
        eventData.put("channelNo", effectiveChannelNo);  // 使用有效的 channelNo
        eventData.put("alarmTime", alarmTime);
        eventData.put("alarmTypeDesc", getAlarmTypeDescription(alarmType));

        publishAccessEvent(deviceId, EVENT_ALARM, eventData);
    }

    /**
     * 获取报警类型描述
     */
    private String getAlarmTypeDescription(int alarmType) {
        switch (alarmType) {
            case 0: return "门磁报警";
            case 1: return "强制开门报警";
            case 2: return "门未关报警";
            case 3: return "胁迫报警";
            case 4: return "防拆报警";
            case 5: return "非法卡报警";
            case 6: return "人脸识别失败报警";
            case 7: return "指纹识别失败报警";
            default: return "未知报警(" + alarmType + ")";
        }
    }

    // ==================== 新增命令实现 ====================

    /**
     * 执行检查设备在线状态命令
     */
    private CommandResult executeCheckDeviceOnline(Long deviceId, DeviceCommand command) {
        log.info("{} 执行检查设备在线状态: deviceId={}", LOG_PREFIX, deviceId);
        
        boolean isOnline = connectionManager.isOnline(deviceId);
        return CommandResult.success(Map.of(
            "isOnline", isOnline,
            "deviceId", deviceId
        ));
    }

    /**
     * 执行激活设备命令（登录设备）
     */
    private CommandResult executeActivateDevice(Long deviceId, DeviceCommand command) {
        // 使用安全的类型转换方法避免类型转换异常
        String ipAddress = command.getStringParam("ipAddress");
        if (ipAddress == null) {
            ipAddress = command.getStringParam("ip");
        }
        Integer port = command.getIntParam("port");
        String username = command.getStringParam("username");
        String password = command.getStringParam("password");

        log.info("{} 执行激活设备: deviceId={}, ip={}", LOG_PREFIX, deviceId, ipAddress);

        if (ipAddress == null || ipAddress.isEmpty()) {
            return CommandResult.failure("缺少 ipAddress 参数");
        }

        DeviceConnectionInfo connectionInfo = DeviceConnectionInfo.builder()
            .deviceId(deviceId)
            .ipAddress(ipAddress)
            .port(port != null ? port : config.getDefaultPort())
            .username(username != null ? username : "admin")
            .password(password != null ? password : "")
            .deviceType(PluginConstants.DEVICE_TYPE_ACCESS_GEN2)
            .build();

        LoginResult result = login(connectionInfo);
        if (result.isSuccess()) {
            return CommandResult.success(Map.of(
                "loginHandle", result.getLoginHandle(),
                "deviceInfo", result.getDeviceInfo() != null ? result.getDeviceInfo() : Map.of()
            ));
        } else {
            return CommandResult.failure(result.getErrorMessage());
        }
    }

    /**
     * 执行获取登录句柄命令
     */
    private CommandResult executeGetLoginHandle(Long deviceId, DeviceCommand command) {
        log.info("{} 执行获取登录句柄: deviceId={}", LOG_PREFIX, deviceId);

        Long loginHandle = connectionManager.getLoginHandle(deviceId);
        if (loginHandle != null && loginHandle > 0) {
            return CommandResult.success(Map.of(
                "loginHandle", loginHandle,
                "deviceId", deviceId
            ));
        } else {
            return CommandResult.failure("设备未连接");
        }
    }

    /**
     * 执行查询设备能力命令
     */
    private CommandResult executeQueryDeviceCapability(Long deviceId, DeviceCommand command) {
        log.info("{} 执行查询设备能力: deviceId={}", LOG_PREFIX, deviceId);

        // 获取设备能力
        Integer channelCount = connectionManager.getChannelCount(deviceId);
        boolean supportsFace = connectionManager.isSupportsFace(deviceId);
        boolean supportsFingerprint = connectionManager.isSupportsFingerprint(deviceId);
        
        return CommandResult.success(Map.of(
            "deviceId", deviceId,
            "deviceType", PluginConstants.DEVICE_TYPE_ACCESS_GEN2,
            "capabilities", Map.of(
                "hasCard", true,
                "hasFace", supportsFace,
                "hasFingerprint", supportsFingerprint,
                "maxCardCount", 50000,
                "maxDoorCount", channelCount != null ? channelCount : 4,
                "maxFaceCount", supportsFace ? 10000 : 0,
                "maxFingerprintCount", supportsFingerprint ? 5000 : 0
            )
        ));
    }

    /**
     * 执行常开门命令
     */
    private CommandResult executeAlwaysOpen(Long deviceId, DeviceCommand command) {
        Integer channelNo = command.getParam("channelNo");
        if (channelNo == null) {
            channelNo = 1;
        }
        int channelIndex = channelNo - 1;
        if (channelIndex < 0) {
            channelIndex = 0;
        }

        log.info("{} 执行常开门: deviceId={}, channelNo={}", LOG_PREFIX, deviceId, channelNo);

        try {
            Long loginHandle = connectionManager.getLoginHandle(deviceId);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接");
            }

            AccessGen2OperationResult sdkResult = adapterFor(deviceId).setDoorAlwaysOpen(loginHandle, channelIndex);
            
            if (sdkResult.isSuccess()) {
                return CommandResult.success(Map.of(
                    "message", "常开门命令已发送",
                    "channelNo", channelNo
                ));
            } else {
                return CommandResult.failure("常开门失败: " + sdkResult.getMessage());
            }
        } catch (Exception e) {
            log.error("{} 常开门异常: deviceId={}, channelNo={}", LOG_PREFIX, deviceId, channelNo, e);
            return CommandResult.failure("常开门异常: " + e.getMessage());
        }
    }

    /**
     * 执行常闭门命令
     */
    private CommandResult executeAlwaysClosed(Long deviceId, DeviceCommand command) {
        Integer channelNo = command.getParam("channelNo");
        if (channelNo == null) {
            channelNo = 1;
        }
        int channelIndex = channelNo - 1;
        if (channelIndex < 0) {
            channelIndex = 0;
        }

        log.info("{} 执行常闭门: deviceId={}, channelNo={}", LOG_PREFIX, deviceId, channelNo);

        try {
            Long loginHandle = connectionManager.getLoginHandle(deviceId);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接");
            }

            AccessGen2OperationResult sdkResult = adapterFor(deviceId).setDoorAlwaysClosed(loginHandle, channelIndex);
            
            if (sdkResult.isSuccess()) {
                return CommandResult.success(Map.of(
                    "message", "常闭门命令已发送",
                    "channelNo", channelNo
                ));
            } else {
                return CommandResult.failure("常闭门失败: " + sdkResult.getMessage());
            }
        } catch (Exception e) {
            log.error("{} 常闭门异常: deviceId={}, channelNo={}", LOG_PREFIX, deviceId, channelNo, e);
            return CommandResult.failure("常闭门异常: " + e.getMessage());
        }
    }

    /**
     * 执行取消常开/常闭命令
     */
    private CommandResult executeCancelAlways(Long deviceId, DeviceCommand command) {
        Integer channelNo = command.getParam("channelNo");
        if (channelNo == null) {
            channelNo = 1;
        }
        int channelIndex = channelNo - 1;
        if (channelIndex < 0) {
            channelIndex = 0;
        }

        log.info("{} 执行取消常开/常闭: deviceId={}, channelNo={}", LOG_PREFIX, deviceId, channelNo);

        try {
            Long loginHandle = connectionManager.getLoginHandle(deviceId);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接");
            }

            AccessGen2OperationResult sdkResult = adapterFor(deviceId).cancelDoorAlways(loginHandle, channelIndex);
            
            if (sdkResult.isSuccess()) {
                return CommandResult.success(Map.of(
                    "message", "取消常开/常闭命令已发送",
                    "channelNo", channelNo
                ));
            } else {
                return CommandResult.failure("取消常开/常闭失败: " + sdkResult.getMessage());
            }
        } catch (Exception e) {
            log.error("{} 取消常开/常闭异常: deviceId={}, channelNo={}", LOG_PREFIX, deviceId, channelNo, e);
            return CommandResult.failure("取消常开/常闭异常: " + e.getMessage());
        }
    }
}
