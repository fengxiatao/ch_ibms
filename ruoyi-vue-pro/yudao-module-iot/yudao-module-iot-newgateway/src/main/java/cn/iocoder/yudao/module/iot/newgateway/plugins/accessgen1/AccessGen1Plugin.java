package cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1;

import cn.iocoder.yudao.module.iot.core.enums.ConnectionMode;
import cn.iocoder.yudao.module.iot.core.gateway.dto.AccessControlEventMessage;
import cn.iocoder.yudao.module.iot.core.gateway.dto.DeviceInfo;
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
import cn.iocoder.yudao.module.iot.newgateway.plugins.PluginConstants;
import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1.dto.AccessGen1CardRecord;
import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1.dto.AccessGen1DoorInfo;
import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1.dto.AccessGen1LoginResult;
import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1.dto.AccessGen1OperationResult;
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

/**
 * 门禁一代插件
 * 
 * <p>实现大华门禁一代设备的接入，使用 Recordset 操作进行用户/卡管理：</p>
 * <ul>
 *     <li>远程开门（OPEN_DOOR）</li>
 *     <li>远程关门（CLOSE_DOOR）</li>
 *     <li>下发授权（DISPATCH_AUTH）- 通过 Recordset 操作</li>
 *     <li>撤销授权（REVOKE_AUTH）- 通过 Recordset 操作</li>
 * </ul>
 * 
 * <h2>设备特点</h2>
 * <p>门禁一代设备使用大华 NetSDK 进行通信，通过 Recordset 操作管理卡号和用户。
 * 与门禁二代不同，一代设备使用 recNo（记录号）来标识卡号/用户记录。</p>
 * 
 * <h2>连接模式</h2>
 * <p>门禁一代设备采用主动连接模式（ACTIVE），由平台主动登录设备。
 * 登录成功后需要定期执行保活检测，确保连接有效。</p>
 * 
 * @author IoT Gateway Team
 * @see ActiveDeviceHandler
 * @see AccessGen1Config
 */
@DevicePlugin(
    id = PluginConstants.PLUGIN_ID_ACCESS_GEN1,
    name = "门禁一代",
    deviceType = PluginConstants.DEVICE_TYPE_ACCESS_GEN1,
    vendor = "Dahua",
    description = "大华门禁一代设备，使用 Recordset 操作进行用户/卡管理",
    capabilityRefreshEnabled = true,
    enabledByDefault = true
)
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "access-gen1", havingValue = "true", matchIfMissing = true)
@Slf4j
@RequiredArgsConstructor
public class AccessGen1Plugin implements ActiveDeviceHandler {

    /**
     * 日志前缀
     */
    private static final String LOG_PREFIX = "[AccessGen1Plugin]";

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
     * 下发授权命令（通过 Recordset）
     */
    public static final String CMD_DISPATCH_AUTH = "DISPATCH_AUTH";

    /**
     * 撤销授权命令（通过 Recordset）
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
        Map.entry("DISPATCH_FACE", CMD_DISPATCH_AUTH),
        Map.entry("DISPATCH_FINGERPRINT", CMD_DISPATCH_AUTH),
        Map.entry("ADD_CARD", CMD_DISPATCH_AUTH),
        Map.entry("UPDATE_CARD", CMD_DISPATCH_AUTH),
        // 撤销命令映射
        Map.entry("REVOKE_USER", CMD_REVOKE_AUTH),
        Map.entry("REVOKE_CARD", CMD_REVOKE_AUTH),
        Map.entry("REVOKE_FACE", CMD_REVOKE_AUTH),
        Map.entry("REVOKE_FINGERPRINT", CMD_REVOKE_AUTH),
        Map.entry("DELETE_CARD", CMD_REVOKE_AUTH),
        // 查询命令映射
        Map.entry("DISCOVER_CHANNELS", CMD_QUERY_CHANNELS),
        Map.entry("QUERY_AUTH", CMD_QUERY_AUTH),
        Map.entry("LIST_CARDS", CMD_QUERY_AUTH),
        // 清空命令映射
        Map.entry("CLEAR_ALL_AUTH", CMD_CLEAR_ALL_AUTH)
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
     * 门禁一代事件主题
     * @deprecated 已迁移到统一事件主题 {@link IotMessageTopics#DEVICE_EVENT_REPORTED}
     */
    @Deprecated
    public static final String TOPIC_ACCESS_GEN1_EVENT = IotMessageTopics.DEVICE_EVENT_REPORTED;

    // ==================== 依赖注入 ====================

    /**
     * 插件配置
     */
    private final AccessGen1Config config;

    /**
     * 连接管理器
     */
    private final AccessGen1ConnectionManager connectionManager;

    /**
     * 生命周期管理器
     */
    private final DeviceLifecycleManager lifecycleManager;

    /**
     * 消息发布器
     */
    private final GatewayMessagePublisher messagePublisher;

    /**
     * SDK 封装
     */
    private final AccessGen1SdkWrapper sdkWrapper;

    /**
     * 门禁事件回调（EVENT_IVS_ACCESS_CTL）
     * <p>
     * 必须保持强引用，避免被 GC 导致回调失效
     * </p>
     */
    private final NetSDKLib.fAnalyzerDataCallBack accessCtlCallback = new AccessCtlAnalyzerDataCallBack(this);

    /**
     * 重连调度器
     */
    private final ScheduledExecutorService reconnectScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "AccessGen1-Reconnect");
        t.setDaemon(true);
        return t;
    });

    // ==================== 初始化 ====================

    /**
     * 初始化插件，注册断线监听器
     */
    @PostConstruct
    public void init() {
        log.info("{} 初始化插件，注册断线监听器和报警监听器...", LOG_PREFIX);
        
        // 注册断线监听器
        sdkWrapper.addDisconnectListener((loginHandle, ip, port) -> {
            handleDeviceDisconnect(loginHandle, ip, port);
        });
        
        // 注册报警事件监听器
        sdkWrapper.addAlarmListener((deviceId, alarmType, channelNo, alarmTime, alarmData) -> {
            handleAlarmEvent(deviceId, alarmType, channelNo, alarmTime);
        });
        
        log.info("{} ✅ 断线监听器和报警监听器注册完成", LOG_PREFIX);
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
        AccessGen1ConnectionManager.AccessGen1ConnectionInfo connInfo = connectionManager.getConnectionInfo(deviceId);
        
        // 2. 更新设备生命周期状态为离线
        try {
            lifecycleManager.onDeviceDisconnected(deviceId, "SDK检测到设备断线");
            log.info("{} 设备状态已更新为离线: deviceId={}", LOG_PREFIX, deviceId);
        } catch (Exception e) {
            log.error("{} 更新设备离线状态失败: deviceId={}, error={}", LOG_PREFIX, deviceId, e.getMessage(), e);
        }
        
        // 3. 清理连接管理器中的连接信息
        connectionManager.unregister(deviceId);
        
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
    private void scheduleReconnect(Long deviceId, AccessGen1ConnectionManager.AccessGen1ConnectionInfo connInfo) {
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
    private void attemptReconnect(Long deviceId, AccessGen1ConnectionManager.AccessGen1ConnectionInfo connInfo) {
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
                    .deviceType(PluginConstants.DEVICE_TYPE_ACCESS_GEN1)
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
        return PluginConstants.DEVICE_TYPE_ACCESS_GEN1;
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
        return PluginConstants.DEVICE_TYPE_ACCESS_GEN1.equalsIgnoreCase(deviceInfo.getDeviceType());
    }

    @Override
    public CommandResult executeCommand(Long deviceId, DeviceCommand command) {
        if (deviceId == null || command == null) {
            return CommandResult.failure("参数不能为空");
        }

        String commandType = command.getCommandType();
        // 检查命令类型是否为空
        if (commandType == null || commandType.isEmpty()) {
            return CommandResult.failure("不支持的命令类型: null");
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
                    return executeDispatchAuth(deviceId, mappedCommand);
                case CMD_REVOKE_AUTH:
                    return executeRevokeAuth(deviceId, mappedCommand);
                case CMD_QUERY_AUTH:
                    return executeQueryAuth(deviceId, mappedCommand);
                case CMD_CLEAR_ALL_AUTH:
                    return executeClearAllAuth(deviceId, mappedCommand);
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

        // 检查 SDK 是否已初始化
        if (!sdkWrapper.isInitialized()) {
            log.error("{} SDK 未初始化，无法登录设备: deviceId={}", LOG_PREFIX, deviceId);
            return LoginResult.failure("SDK 未初始化");
        }

        // 检查是否已有连接 - 如果已在线，直接返回成功，避免重复登录导致设备被踢
        if (connectionManager.isOnline(deviceId)) {
            Long existingHandle = connectionManager.getLoginHandle(deviceId);
            log.info("{} 设备已在线，复用现有连接: deviceId={}, handle={}", LOG_PREFIX, deviceId, existingHandle);
            
            // 从连接信息中获取设备信息
            AccessGen1ConnectionManager.AccessGen1ConnectionInfo connInfo = connectionManager.getConnectionInfo(deviceId);
            
            // 构建设备信息返回
            Map<String, Object> deviceInfo = new HashMap<>();
            deviceInfo.put("deviceType", PluginConstants.DEVICE_TYPE_ACCESS_GEN1);
            deviceInfo.put("alreadyOnline", true);
            if (connInfo != null) {
                deviceInfo.put("serialNumber", connInfo.getSerialNumber());
                deviceInfo.put("channelCount", connInfo.getChannelCount());
            }
            
            return LoginResult.success(existingHandle, deviceInfo);
        }

        try {
            // 调用 SDK 登录设备
            AccessGen1LoginResult sdkResult = sdkWrapper.login(ipAddress, port, username, password);
            
            if (!sdkResult.isSuccess()) {
                log.error("{} 设备登录失败: deviceId={}, error={}", 
                        LOG_PREFIX, deviceId, sdkResult.getErrorMessage());
                return LoginResult.failure(sdkResult.getErrorMessage());
            }
            
            long loginHandle = sdkResult.getLoginHandle();
            String serialNumber = sdkResult.getSerialNumber();
            int channelCount = sdkResult.getChannelCount();
            
            // 构建设备信息
            Map<String, Object> deviceInfo = new HashMap<>();
            deviceInfo.put("serialNumber", serialNumber);
            deviceInfo.put("deviceType", PluginConstants.DEVICE_TYPE_ACCESS_GEN1);
            deviceInfo.put("channelCount", channelCount);
            deviceInfo.put("sdkDeviceType", sdkResult.getDeviceType());
            
            // 注册连接到连接管理器
            connectionManager.register(deviceId, serialNumber, loginHandle);
            connectionManager.updateIpAddress(deviceId, ipAddress);
            connectionManager.updatePort(deviceId, port);
            connectionManager.updateSerialNumber(deviceId, serialNumber);
            connectionManager.updateChannelCount(deviceId, channelCount);
            
            // 构建设备连接信息并原子地更新状态（注册 → 直接上线）
            DeviceConnectionInfo loginConnectionInfo = DeviceConnectionInfo.builder()
                    .deviceId(deviceId)
                    .deviceType(PluginConstants.DEVICE_TYPE_ACCESS_GEN1)
                    .vendor("Dahua")
                    .connectionMode(ConnectionMode.ACTIVE)
                    .build();
            lifecycleManager.onDeviceLogin(deviceId, loginConnectionInfo);

            // 订阅门禁事件（按文档：EVENT_IVS_ACCESS_CTL / DEV_EVENT_ACCESS_CTL_INFO）
            Long analyzerHandle = sdkWrapper.subscribeAccessCtlEvent(loginHandle, deviceId, accessCtlCallback);
            if (analyzerHandle != null && analyzerHandle > 0) {
                connectionManager.updateAnalyzerHandle(deviceId, analyzerHandle);
            } else {
                log.warn("{} 门禁事件订阅失败（不影响登录，但事件记录将无法实时回推）: deviceId={}", LOG_PREFIX, deviceId);
            }
            
            // 订阅报警事件（门磁报警、蓄电池掉电等）
            boolean alarmSubscribed = sdkWrapper.subscribeAlarmEvent(loginHandle, deviceId);
            if (!alarmSubscribed) {
                log.warn("{} 报警事件订阅失败（不影响登录，但报警事件将无法实时回推）: deviceId={}", LOG_PREFIX, deviceId);
            }
            
            // 发布设备上线事件
            publishAccessEvent(deviceId, EVENT_DOOR_OPEN, Map.of(
                    "action", "LOGIN",
                    "serialNumber", serialNumber,
                    "channelCount", channelCount
            ));
            
            log.info("{} ✅ 设备登录成功: deviceId={}, handle={}, sn={}, channels={}", 
                    LOG_PREFIX, deviceId, loginHandle, serialNumber, channelCount);
            
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
            // 获取登录句柄
            Long loginHandle = connectionManager.getLoginHandle(deviceId);
            Long analyzerHandle = connectionManager.getAnalyzerHandle(deviceId);
            if (analyzerHandle != null && analyzerHandle > 0) {
                sdkWrapper.unsubscribeAccessEvent(analyzerHandle);
            }
            
            // 取消报警事件订阅
            if (loginHandle != null && loginHandle > 0) {
                sdkWrapper.unsubscribeAlarmEvent(loginHandle);
            }
            
            if (loginHandle != null && loginHandle > 0) {
                // 调用 SDK 登出
                boolean logoutSuccess = sdkWrapper.logout(loginHandle);
                if (!logoutSuccess) {
                    log.warn("{} SDK 登出失败，但仍将清理本地状态: deviceId={}", LOG_PREFIX, deviceId);
                }
            }
            
            // 原子地更新设备状态为离线
            lifecycleManager.onDeviceLogout(deviceId, "SDK登出");
            
            // 从连接管理器注销连接
            connectionManager.unregister(deviceId);
            
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
            
            // 通过查询设备状态来验证连接是否有效
            // 这里使用查询卡片数量作为保活检测方式
            int cardCount = sdkWrapper.getCardCount(loginHandle);
            
            // 如果查询成功（返回值 >= 0），说明连接有效
            if (cardCount >= 0) {
                // 更新心跳时间
                connectionManager.updateHeartbeat(deviceId);
                lifecycleManager.updateLastSeen(deviceId);
                
                log.debug("{} 保活检测成功: deviceId={}, cardCount={}", LOG_PREFIX, deviceId, cardCount);
                return true;
            } else {
                log.warn("{} 保活检测失败: 查询卡片数量失败, deviceId={}", LOG_PREFIX, deviceId);
                return false;
            }
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
     * 执行远程开门命令
     * 
     * <p>包含自动重连机制：当检测到"数据发送失败"等网络错误时，
     * 会自动清除旧连接并重新建立连接，然后重试开门操作。</p>
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
     */
    private CommandResult executeOpenDoor(Long deviceId, DeviceCommand command) {
        // 获取通道号，默认为1（门禁通道从1开始）
        Integer channelNo = command.getParam("channelNo");
        if (channelNo == null) {
            channelNo = 1; // 默认通道1
        }
        // 直接传递 channelNo，SDK Wrapper 层会处理 -1 转换（SDK 内部从0开始）

        log.info("{} 执行远程开门: deviceId={}, channelNo={}", LOG_PREFIX, deviceId, channelNo);

        try {
            // 获取登录句柄（支持按需连接）
            Long loginHandle = ensureConnected(deviceId, command);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接，无法建立连接");
            }
            
            // 调用 SDK 开门（SDK Wrapper 内部会处理通道号转换）
            AccessGen1OperationResult sdkResult = sdkWrapper.openDoor(loginHandle, channelNo);
            
            if (sdkResult.isSuccess()) {
                // 发布开门事件
                publishAccessEvent(deviceId, EVENT_DOOR_OPEN, Map.of(
                        "channelNo", channelNo,
                        "action", "REMOTE_OPEN"
                ));
                
                return CommandResult.success(Map.of(
                        "message", "开门命令已发送",
                        "channelNo", channelNo
                ));
            } else {
                // 检查是否是网络/连接相关错误（错误码 516 = 数据发送失败）
                String errorMsg = sdkResult.getMessage();
                if (isConnectionError(errorMsg)) {
                    log.warn("{} 开门失败(连接错误)，尝试重连: deviceId={}, error={}", LOG_PREFIX, deviceId, errorMsg);
                    
                    // 清除旧连接，强制重新连接
                    connectionManager.unregister(deviceId);
                    
                    // 尝试重新登录
                    Long newLoginHandle = ensureConnected(deviceId, command);
                    if (newLoginHandle == null || newLoginHandle <= 0) {
                        return CommandResult.failure("重连失败，设备无法建立连接");
                    }
                    
                    // 使用新连接重试开门
                    log.info("{} 重连成功，重试开门: deviceId={}, newHandle={}", LOG_PREFIX, deviceId, newLoginHandle);
                    AccessGen1OperationResult retryResult = sdkWrapper.openDoor(newLoginHandle, channelNo);
                    
                    if (retryResult.isSuccess()) {
                        publishAccessEvent(deviceId, EVENT_DOOR_OPEN, Map.of(
                                "channelNo", channelNo,
                                "action", "REMOTE_OPEN"
                        ));
                        return CommandResult.success(Map.of(
                                "message", "开门命令已发送（重连后成功）",
                                "channelNo", channelNo
                        ));
                    } else {
                        return CommandResult.failure("开门失败(重试): " + retryResult.getMessage());
                    }
                }
                return CommandResult.failure("开门失败: " + sdkResult.getMessage());
            }
        } catch (Exception e) {
            log.error("{} 远程开门异常: deviceId={}, channelNo={}", LOG_PREFIX, deviceId, channelNo, e);
            return CommandResult.failure("开门异常: " + e.getMessage());
        }
    }
    
    /**
     * 判断是否为连接/网络相关错误
     * 
     * <p>常见的网络错误包括：</p>
     * <ul>
     *     <li>错误码 516 - 数据发送失败</li>
     *     <li>网络连接断开</li>
     *     <li>连接超时</li>
     * </ul>
     *
     * @param errorMsg 错误信息
     * @return true=连接错误，需要重连；false=其他错误
     */
    private boolean isConnectionError(String errorMsg) {
        if (errorMsg == null) {
            return false;
        }
        // 错误码 516 对应 "数据发送失败"
        return errorMsg.contains("516") 
                || errorMsg.contains("数据发送失败") 
                || errorMsg.contains("发送失败")
                || errorMsg.contains("网络")
                || errorMsg.contains("连接断开")
                || errorMsg.contains("超时");
    }

    /**
     * 执行远程关门命令
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
     */
    private CommandResult executeCloseDoor(Long deviceId, DeviceCommand command) {
        // 获取通道号，默认为1（门禁通道从1开始）
        Integer channelNo = command.getParam("channelNo");
        if (channelNo == null) {
            channelNo = 1; // 默认通道1
        }
        // 直接传递 channelNo，SDK Wrapper 层会处理 -1 转换

        log.info("{} 执行远程关门: deviceId={}, channelNo={}", LOG_PREFIX, deviceId, channelNo);

        try {
            // 获取登录句柄（支持按需连接）
            Long loginHandle = ensureConnected(deviceId, command);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接，无法建立连接");
            }
            
            // 调用 SDK 关门（SDK Wrapper 内部会处理通道号转换）
            AccessGen1OperationResult sdkResult = sdkWrapper.closeDoor(loginHandle, channelNo);
            
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
     * 执行下发授权命令（通过 Recordset）
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
     */
    private CommandResult executeDispatchAuth(Long deviceId, DeviceCommand command) {
        // 支持三种参数格式：
        // 1. 直接参数: cardNo, userId, userName, ...
        // 2. userInfo 对象: { cardNo, userId, userName, ... }
        // 3. cardInfo 对象: { cardNo, userId, ... } (DISPATCH_CARD 命令)
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
            validStartTime = normalizeValidTime(userInfo.getValidStartTime());
            validEndTime = normalizeValidTime(userInfo.getValidEndTime());
            password = userInfo.getPassword();
            doors = userInfo.getDoors();
        } else if (userInfoObj instanceof Map) {
            // 从 userInfo Map 提取参数
            Map<String, Object> userInfo = (Map<String, Object>) userInfoObj;
            cardNo = getStringFromMap(userInfo, "cardNo");
            userId = getStringFromMap(userInfo, "userId");
            userName = getStringFromMap(userInfo, "userName");
            validStartTime = normalizeValidTime(getStringFromMap(userInfo, "validStartTime"));
            validEndTime = normalizeValidTime(getStringFromMap(userInfo, "validEndTime"));
            password = getStringFromMap(userInfo, "password");
            
            // 获取门权限
            Object doorsObj = userInfo.get("doors");
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
            cardNo = command.getStringParam("cardNo");
            userId = command.getStringParam("userId");
            userName = command.getStringParam("userName");
            validStartTime = normalizeValidTime(command.getStringParam("validStartTime"));
            validEndTime = normalizeValidTime(command.getStringParam("validEndTime"));
            password = command.getStringParam("password");
            
            Object doorsParam = command.getParam("doors");
            if (doorsParam instanceof int[]) {
                doors = (int[]) doorsParam;
            }
        }

        // 一代门禁设备必须有卡号，如果没有卡号但有用户ID，使用用户ID作为卡号
        if ((cardNo == null || cardNo.isEmpty()) && (userId != null && !userId.isEmpty())) {
            cardNo = userId;
            log.info("{} 无卡号，使用userId作为cardNo: {}", LOG_PREFIX, cardNo);
        }
        
        if (cardNo == null || cardNo.isEmpty()) {
            return CommandResult.failure("缺少 cardNo 参数（一代门禁需要卡号或用户ID）");
        }

        log.info("{} 执行下发授权: deviceId={}, cardNo={}, userId={}, userName={}", 
                LOG_PREFIX, deviceId, cardNo, userId, userName);

        try {
            // 获取登录句柄（支持按需连接）
            Long loginHandle = ensureConnected(deviceId, command);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接，无法建立连接");
            }
            
            // 构建卡片记录
            AccessGen1CardRecord cardRecord;
            if (validStartTime != null && validEndTime != null) {
                cardRecord = AccessGen1CardRecord.createWithValidity(
                        cardNo, userId, userName, validStartTime, validEndTime);
            } else {
                cardRecord = AccessGen1CardRecord.create(cardNo, userId, userName);
            }
            
            // 设置密码（如果有）
            if (password != null && !password.isEmpty()) {
                cardRecord.setPassword(password);
            }
            
            // 设置门权限（如果有）
            if (doors != null && doors.length > 0) {
                cardRecord.setDoors(doors);
            }
            
            // 调用 SDK 插入卡片记录
            AccessGen1OperationResult sdkResult = sdkWrapper.insertCard(loginHandle, cardRecord);
            
            // 如果插入失败，检查是否是"用户已存在"错误（错误码 146）
            if (!sdkResult.isSuccess() && sdkResult.getErrorCode() != null && sdkResult.getErrorCode() == 146) {
                log.info("{} 用户已存在，尝试更新: cardNo={}", LOG_PREFIX, cardNo);
                
                // 查询现有卡片记录获取 recNo
                AccessGen1CardRecord existingCard = sdkWrapper.queryCardByCardNo(loginHandle, cardNo);
                if (existingCard != null && existingCard.getRecNo() != null) {
                    // 设置 recNo 后进行更新
                    cardRecord.setRecNo(existingCard.getRecNo());
                    sdkResult = sdkWrapper.updateCard(loginHandle, cardRecord);
                    
                    if (sdkResult.isSuccess()) {
                        log.info("{} ✅ 更新用户成功: cardNo={}, recNo={}", LOG_PREFIX, cardNo, existingCard.getRecNo());
                    }
                } else {
                    log.warn("{} 无法查询到现有卡片记录: cardNo={}", LOG_PREFIX, cardNo);
                }
            }
            
            if (sdkResult.isSuccess()) {
                Integer recNo = sdkResult.getIntData("recNo");
                // 如果是更新操作，recNo 可能在 cardRecord 中
                if (recNo == null && cardRecord.getRecNo() != null) {
                    recNo = cardRecord.getRecNo();
                }
                
                // 发布授权下发事件
                publishAccessEvent(deviceId, EVENT_CARD_SWIPE, Map.of(
                        "action", "DISPATCH_AUTH",
                        "cardNo", cardNo,
                        "userId", userId != null ? userId : "",
                        "recNo", recNo != null ? recNo : 0
                ));
                
                return CommandResult.success(Map.of(
                        "message", "授权下发成功",
                        "cardNo", cardNo,
                        "recNo", recNo != null ? recNo : 0
                ));
            } else {
                return CommandResult.failure("下发授权失败: " + sdkResult.getMessage());
            }
        } catch (Exception e) {
            log.error("{} 下发授权异常: deviceId={}, cardNo={}", LOG_PREFIX, deviceId, cardNo, e);
            return CommandResult.failure("下发授权异常: " + e.getMessage());
        }
    }

    /**
     * 执行撤销授权命令（通过 Recordset）
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
     */
    private CommandResult executeRevokeAuth(Long deviceId, DeviceCommand command) {
        // 使用安全的类型转换方法避免类型转换异常
        String cardNo = command.getStringParam("cardNo");
        Integer recNo = command.getIntParam("recNo");
        String userId = command.getStringParam("userId");

        // 与下发保持一致：如果没有卡号但有 userId，使用 userId 作为 cardNo
        // 这是因为下发时如果没有卡号，会用 userId 作为 cardNo 存入设备
        if ((cardNo == null || cardNo.isEmpty()) && (userId != null && !userId.isEmpty())) {
            cardNo = userId;
            log.info("{} 撤销时无卡号，使用userId作为cardNo: {}", LOG_PREFIX, cardNo);
        }

        if ((cardNo == null || cardNo.isEmpty()) && recNo == null) {
            return CommandResult.failure("缺少 cardNo 或 recNo 参数");
        }

        log.info("{} 执行撤销授权: deviceId={}, cardNo={}, recNo={}, userId={}", 
                LOG_PREFIX, deviceId, cardNo, recNo, userId);

        try {
            // 获取登录句柄（支持按需连接）
            Long loginHandle = ensureConnected(deviceId, command);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接，无法建立连接");
            }
            
            AccessGen1OperationResult sdkResult;
            
            // 优先使用 recNo 删除，否则使用 cardNo 删除
            if (recNo != null && recNo > 0) {
                sdkResult = sdkWrapper.deleteCardByRecNo(loginHandle, recNo);
            } else {
                sdkResult = sdkWrapper.deleteCardByCardNo(loginHandle, cardNo);
            }
            
            if (sdkResult.isSuccess()) {
                // 发布授权撤销事件
                publishAccessEvent(deviceId, EVENT_CARD_SWIPE, Map.of(
                        "action", "REVOKE_AUTH",
                        "cardNo", cardNo != null ? cardNo : "",
                        "recNo", recNo != null ? recNo : 0
                ));
                
                return CommandResult.success(Map.of(
                        "message", "授权撤销成功",
                        "cardNo", cardNo != null ? cardNo : "",
                        "recNo", recNo != null ? recNo : 0
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
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
     */
    private CommandResult executeQueryAuth(Long deviceId, DeviceCommand command) {
        // 使用安全的类型转换方法避免类型转换异常
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
            
            List<AccessGen1CardRecord> records;
            
            // 根据参数选择查询方式
            if (cardNo != null && !cardNo.isEmpty()) {
                log.info("{} 按卡号查询: cardNo={}", LOG_PREFIX, cardNo);
                AccessGen1CardRecord record = sdkWrapper.queryCardByCardNo(loginHandle, cardNo);
                records = record != null ? List.of(record) : List.of();
            } else if (userId != null && !userId.isEmpty()) {
                log.info("{} 按用户ID查询: userId={}", LOG_PREFIX, userId);
                records = sdkWrapper.queryCardsByUserId(loginHandle, userId);
            } else {
                log.info("{} 查询所有用户", LOG_PREFIX);
                records = sdkWrapper.queryAllCards(loginHandle);
            }
            
            log.info("{} ✅ SDK查询完成，获取到 {} 条记录", LOG_PREFIX, records.size());
            
            // 转换为 Map 列表
            List<Map<String, Object>> recordMaps = records.stream()
                    .map(this::convertCardRecordToMap)
                    .toList();
            
            log.info("{} 返回结果: totalCount={}", LOG_PREFIX, recordMaps.size());
            
            return CommandResult.success(Map.of(
                    "message", "查询成功",
                    "totalCount", records.size(),
                    "records", recordMaps
            ));
        } catch (Exception e) {
            log.error("{} 查询授权异常: deviceId={}", LOG_PREFIX, deviceId, e);
            return CommandResult.failure("查询授权异常: " + e.getMessage());
        }
    }

    /**
     * 执行清空所有授权命令
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
     */
    private CommandResult executeClearAllAuth(Long deviceId, DeviceCommand command) {
        log.info("{} 执行清空所有授权: deviceId={}", LOG_PREFIX, deviceId);

        try {
            // 获取登录句柄（支持按需连接）
            Long loginHandle = ensureConnected(deviceId, command);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接，无法建立连接");
            }
            
            // 调用 SDK 清空所有卡片
            AccessGen1OperationResult sdkResult = sdkWrapper.clearAllCards(loginHandle);
            
            if (sdkResult.isSuccess()) {
                // 发布清空授权事件
                publishAccessEvent(deviceId, EVENT_CARD_SWIPE, Map.of(
                        "action", "CLEAR_ALL_AUTH"
                ));
                
                return CommandResult.success(Map.of(
                        "message", "清空所有授权成功"
                ));
            } else {
                return CommandResult.failure("清空授权失败: " + sdkResult.getMessage());
            }
        } catch (Exception e) {
            log.error("{} 清空授权异常: deviceId={}", LOG_PREFIX, deviceId, e);
            return CommandResult.failure("清空授权异常: " + e.getMessage());
        }
    }

    /**
     * 执行查询通道命令
     * <p>
     * 从设备获取已配置的门通道信息。
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
            
            // 调用 SDK 查询门通道
            AccessGen1OperationResult sdkResult = sdkWrapper.queryDoorChannels(loginHandle);
            
            if (!sdkResult.isSuccess()) {
                return CommandResult.failure("查询通道失败: " + sdkResult.getMessage());
            }
            
            // 构建通道列表
            List<Map<String, Object>> channelList = new ArrayList<>();
            @SuppressWarnings("unchecked")
            List<AccessGen1DoorInfo> doors = (List<AccessGen1DoorInfo>) sdkResult.getData("doorList");
            
            if (doors != null) {
                for (AccessGen1DoorInfo door : doors) {
                    Map<String, Object> channelInfo = new HashMap<>();
                    channelInfo.put("channelNo", door.getDoorNo());
                    channelInfo.put("channelName", door.getDoorName());
                    channelInfo.put("channelType", "ACCESS");
                    channelInfo.put("status", door.getDoorStatus()); // 0-关闭, 1-打开, 2-未知
                    channelInfo.put("capabilities", Map.of(
                        "hasCard", Boolean.TRUE.equals(door.getCardSupported()),
                        "hasFace", false,
                        "hasFingerprint", false
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

    /**
     * 确保设备已连接（支持按需连接）
     * <p>
     * 如果设备未连接，尝试使用命令中的连接参数进行按需连接。
     * </p>
     *
     * @param deviceId 设备ID
     * @param command  命令（可能包含连接参数）
     * @return 登录句柄，如果连接失败则返回 null
     */
    private Long ensureConnected(Long deviceId, DeviceCommand command) {
        // 1. 检查是否已连接
        Long loginHandle = connectionManager.getLoginHandle(deviceId);
        if (loginHandle != null && loginHandle > 0) {
            return loginHandle;
        }
        
        // 2. 从命令参数中提取连接信息
        // 使用安全的类型转换方法避免类型转换异常
        String ipAddress = command.getStringParam("ipAddress");
        if (ipAddress == null) {
            ipAddress = command.getStringParam("ip");
        }
        Integer port = command.getIntParam("port");
        String username = command.getStringParam("username");
        String password = command.getStringParam("password");
        
        // 3. 如果没有连接参数，无法按需连接
        if (ipAddress == null || ipAddress.isEmpty()) {
            log.warn("{} 设备未连接且命令中无连接参数: deviceId={}", LOG_PREFIX, deviceId);
            return null;
        }
        
        log.info("{} 设备未连接，尝试按需连接: deviceId={}, ip={}", LOG_PREFIX, deviceId, ipAddress);
        
        // 4. 构建连接信息并登录
        DeviceConnectionInfo connectionInfo = DeviceConnectionInfo.builder()
                .deviceId(deviceId)
                .ipAddress(ipAddress)
                .port(port != null ? port : config.getDefaultPort())
                .username(username != null ? username : "admin")
                .password(password != null ? password : "")
                .deviceType(PluginConstants.DEVICE_TYPE_ACCESS_GEN1)
                .build();
        
        LoginResult loginResult = login(connectionInfo);
        if (loginResult.isSuccess()) {
            log.info("{} ✅ 按需连接成功: deviceId={}", LOG_PREFIX, deviceId);
            return connectionManager.getLoginHandle(deviceId);
        } else {
            log.error("{} 按需连接失败: deviceId={}, error={}", 
                    LOG_PREFIX, deviceId, loginResult.getErrorMessage());
            return null;
        }
    }

    /**
     * 从 Map 中获取字符串值，支持多个可能的 key
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
     * 将 epoch time (1970-01-01) 视为 null，因为这表示"未设置有效期"
     * 
     * @param timeStr 时间字符串
     * @return 规范化后的时间字符串，如果是 epoch time 则返回 null
     */
    private String normalizeValidTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        // 过滤 epoch time (1970-01-01)，这表示未设置有效期
        if (timeStr.startsWith("1970-01-01")) {
            log.debug("{} 忽略 epoch time 有效期: {}", LOG_PREFIX, timeStr);
            return null;
        }
        return timeStr;
    }

    /**
     * 将卡片记录转换为 Map
     */
    private Map<String, Object> convertCardRecordToMap(AccessGen1CardRecord record) {
        Map<String, Object> map = new HashMap<>();
        map.put("recNo", record.getRecNo());
        map.put("cardNo", record.getCardNo());
        map.put("userId", record.getUserId());
        map.put("cardName", record.getCardName());
        map.put("cardStatus", record.getCardStatus());
        map.put("cardType", record.getCardType());
        map.put("isValid", record.getIsValid());
        map.put("validStartTime", record.getValidStartTime());
        map.put("validEndTime", record.getValidEndTime());
        map.put("doorNum", record.getDoorNum());
        map.put("doors", record.getDoors());
        return map;
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
            extData.put("deviceType", PluginConstants.DEVICE_TYPE_ACCESS_GEN1);
            extData.put("pluginId", PluginConstants.PLUGIN_ID_ACCESS_GEN1);
            builder.extData(extData);
        } else {
            // 即使没有 eventData，也要设置 extData 以包含设备类型信息
            Map<String, Object> extData = new HashMap<>();
            extData.put("deviceType", PluginConstants.DEVICE_TYPE_ACCESS_GEN1);
            extData.put("pluginId", PluginConstants.PLUGIN_ID_ACCESS_GEN1);
            builder.extData(extData);
        }

        // 顶层 deviceType 用于统一事件通道路由（DEVICE_EVENT_REPORTED）
        builder.deviceType(PluginConstants.DEVICE_TYPE_ACCESS_GEN1);

        // 统一发布到 DEVICE_EVENT_REPORTED：Biz 侧由 DeviceEventConsumer 统一消费/路由
        messagePublisher.publishEvent(IotMessageTopics.DEVICE_EVENT_REPORTED, builder.build());
    }

    /**
     * 处理刷卡事件回调
     * <p>
     * 当设备上报刷卡事件时调用此方法。
     * </p>
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
        log.info("{} 收到刷卡事件: deviceId={}, cardNo={}, userId={}, channel={}, result={}", 
                LOG_PREFIX, deviceId, cardNo, userId, channelNo, result);

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
     * 门禁事件回调（智能报警带图）
     * <p>
     * 按《智能楼宇分册》：dwAlarmType=EVENT_IVS_ACCESS_CTL，pAlarmInfo=DEV_EVENT_ACCESS_CTL_INFO
     * </p>
     */
    private static class AccessCtlAnalyzerDataCallBack implements NetSDKLib.fAnalyzerDataCallBack {
        private final AccessGen1Plugin plugin;

        private AccessCtlAnalyzerDataCallBack(AccessGen1Plugin plugin) {
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
                int result = (msg.nErrorCode == 0 && msg.bStatus == 1) ? 0 : (msg.nErrorCode != 0 ? msg.nErrorCode : 1);

                plugin.handleCardSwipeEvent(deviceId, cardNo, userId, channelNo, System.currentTimeMillis(), result);
                return 0;
            } catch (Exception e) {
                log.warn("[AccessGen1Plugin] 门禁事件回调处理异常: {}", e.getMessage(), e);
                return 0;
            }
        }
    }

    /**
     * 处理报警事件回调
     * <p>
     * 当设备上报报警事件时调用此方法。
     * </p>
     *
     * @param deviceId  设备ID
     * @param alarmType 报警类型
     * @param channelNo 通道号
     * @param alarmTime 报警时间
     */
    public void handleAlarmEvent(Long deviceId, int alarmType, int channelNo, long alarmTime) {
        log.info("{} 收到报警事件: deviceId={}, alarmType={}, channel={}", 
                LOG_PREFIX, deviceId, alarmType, channelNo);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("alarmType", alarmType);
        eventData.put("channelNo", channelNo);
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
            default: return "未知报警(" + alarmType + ")";
        }
    }

    // ==================== 新增命令实现 ====================

    /**
     * 执行检查设备在线状态命令
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
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
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
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
            .deviceType(PluginConstants.DEVICE_TYPE_ACCESS_GEN1)
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
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
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
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
     */
    private CommandResult executeQueryDeviceCapability(Long deviceId, DeviceCommand command) {
        log.info("{} 执行查询设备能力: deviceId={}", LOG_PREFIX, deviceId);

        // 获取通道数量
        Integer channelCount = connectionManager.getChannelCount(deviceId);
        
        return CommandResult.success(Map.of(
            "deviceId", deviceId,
            "deviceType", PluginConstants.DEVICE_TYPE_ACCESS_GEN1,
            "capabilities", Map.of(
                "hasCard", true,
                "hasFace", false,
                "hasFingerprint", false,
                "maxCardCount", 10000,
                "maxDoorCount", channelCount != null ? channelCount : 4
            )
        ));
    }

    /**
     * 执行常开门命令
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
     */
    private CommandResult executeAlwaysOpen(Long deviceId, DeviceCommand command) {
        Integer channelNo = command.getParam("channelNo");
        if (channelNo == null) {
            channelNo = 1;
        }

        log.info("{} 执行常开门: deviceId={}, channelNo={}", LOG_PREFIX, deviceId, channelNo);

        try {
            Long loginHandle = connectionManager.getLoginHandle(deviceId);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接");
            }

            AccessGen1OperationResult sdkResult = sdkWrapper.setDoorAlwaysOpen(loginHandle, channelNo);
            
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
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
     */
    private CommandResult executeAlwaysClosed(Long deviceId, DeviceCommand command) {
        Integer channelNo = command.getParam("channelNo");
        if (channelNo == null) {
            channelNo = 1;
        }

        log.info("{} 执行常闭门: deviceId={}, channelNo={}", LOG_PREFIX, deviceId, channelNo);

        try {
            Long loginHandle = connectionManager.getLoginHandle(deviceId);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接");
            }

            AccessGen1OperationResult sdkResult = sdkWrapper.setDoorAlwaysClosed(loginHandle, channelNo);
            
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
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
     */
    private CommandResult executeCancelAlways(Long deviceId, DeviceCommand command) {
        Integer channelNo = command.getParam("channelNo");
        if (channelNo == null) {
            channelNo = 1;
        }

        log.info("{} 执行取消常开/常闭: deviceId={}, channelNo={}", LOG_PREFIX, deviceId, channelNo);

        try {
            Long loginHandle = connectionManager.getLoginHandle(deviceId);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接");
            }

            AccessGen1OperationResult sdkResult = sdkWrapper.cancelDoorAlways(loginHandle, channelNo);
            
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
