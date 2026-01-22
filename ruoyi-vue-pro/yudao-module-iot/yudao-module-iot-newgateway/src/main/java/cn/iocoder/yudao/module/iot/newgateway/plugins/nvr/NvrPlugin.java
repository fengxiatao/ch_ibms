package cn.iocoder.yudao.module.iot.newgateway.plugins.nvr;

import cn.iocoder.yudao.module.iot.core.enums.ConnectionMode;
import cn.iocoder.yudao.module.iot.core.gateway.dto.DeviceInfo;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.core.mq.message.nvr.NvrEventMessage;
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
import cn.iocoder.yudao.module.iot.newgateway.plugins.nvr.dto.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * NVR 插件
 * 
 * <p>实现大华 NVR 设备的接入，支持以下功能：</p>
 * <ul>
 *     <li>录像回放控制</li>
 *     <li>PTZ 云台控制</li>
 *     <li>通道查询</li>
 *     <li>硬盘状态查询</li>
 *     <li>实时预览</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 * @see ActiveDeviceHandler
 * @see NvrConfig
 */
@DevicePlugin(
    id = PluginConstants.PLUGIN_ID_NVR,
    name = "NVR录像机",
    deviceType = PluginConstants.DEVICE_TYPE_NVR,
    vendor = "Dahua",
    description = "大华NVR录像机设备，支持录像回放、PTZ控制、通道查询等功能",
    capabilityRefreshEnabled = true,
    enabledByDefault = true
)
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "nvr", havingValue = "true", matchIfMissing = true)
@Slf4j
@RequiredArgsConstructor
public class NvrPlugin implements ActiveDeviceHandler {

    private static final String LOG_PREFIX = "[NvrPlugin]";


    // ==================== 命令类型常量 ====================

    /** PTZ 控制命令 */
    public static final String CMD_PTZ_CONTROL = "PTZ_CONTROL";
    
    /** 开始录像回放 */
    public static final String CMD_START_PLAYBACK = "START_PLAYBACK";
    
    /** 停止录像回放 */
    public static final String CMD_STOP_PLAYBACK = "STOP_PLAYBACK";
    
    /** 查询录像文件 */
    public static final String CMD_QUERY_RECORD = "QUERY_RECORD";
    
    /** 下载录像文件 */
    public static final String CMD_DOWNLOAD_RECORD = "DOWNLOAD_RECORD";
    
    /** 查询通道信息 */
    public static final String CMD_QUERY_CHANNELS = "QUERY_CHANNELS";
    
    /** 查询硬盘状态 */
    public static final String CMD_QUERY_DISK_STATUS = "QUERY_DISK_STATUS";
    
    /** 开始实时预览 */
    public static final String CMD_START_PREVIEW = "START_PREVIEW";
    
    /** 停止实时预览 */
    public static final String CMD_STOP_PREVIEW = "STOP_PREVIEW";
    
    /** 抓图 */
    public static final String CMD_CAPTURE_PICTURE = "CAPTURE_PICTURE";

    // ==================== 命令类型映射 ====================

    /**
     * 命令类型映射表
     * 将 biz 层的命令类型映射到插件内部命令类型
     */
    private static final Map<String, String> COMMAND_TYPE_MAPPING = Map.ofEntries(
        // PTZ 控制命令映射
        Map.entry("PTZ", CMD_PTZ_CONTROL),
        Map.entry("PTZ_MOVE", CMD_PTZ_CONTROL),
        Map.entry("PTZ_STOP", CMD_PTZ_CONTROL),
        Map.entry("PTZ_PRESET", CMD_PTZ_CONTROL),
        Map.entry("PTZ_CRUISE", CMD_PTZ_CONTROL),
        // 录像回放命令映射
        Map.entry("PLAYBACK", CMD_START_PLAYBACK),
        Map.entry("PLAY_RECORD", CMD_START_PLAYBACK),
        Map.entry("STOP_RECORD_PLAY", CMD_STOP_PLAYBACK),
        // 录像查询命令映射
        Map.entry("SEARCH_RECORD", CMD_QUERY_RECORD),
        Map.entry("SEARCH_RECORDS", CMD_QUERY_RECORD),
        Map.entry("FIND_RECORD", CMD_QUERY_RECORD),
        // 通道查询命令映射
        Map.entry("SCAN_CHANNELS", CMD_QUERY_CHANNELS),
        Map.entry("DISCOVER_CHANNELS", CMD_QUERY_CHANNELS),
        Map.entry("LIST_CHANNELS", CMD_QUERY_CHANNELS),
        Map.entry("GET_CHANNELS", CMD_QUERY_CHANNELS),
        Map.entry("QUERY_CHANNELS", CMD_QUERY_CHANNELS),
        // 抓图命令映射
        Map.entry("SNAPSHOT", CMD_CAPTURE_PICTURE),
        Map.entry("CAPTURE_SNAPSHOT", CMD_CAPTURE_PICTURE),
        Map.entry("CAPTURE", CMD_CAPTURE_PICTURE),
        Map.entry("TAKE_PICTURE", CMD_CAPTURE_PICTURE),
        // 硬盘状态查询映射
        Map.entry("DISK_STATUS", CMD_QUERY_DISK_STATUS),
        Map.entry("GET_DISK_INFO", CMD_QUERY_DISK_STATUS)
    );

    // ==================== 事件类型常量 ====================

    /** 移动侦测事件 */
    public static final String EVENT_MOTION_DETECT = "MOTION_DETECT";
    
    /** 视频丢失事件 */
    public static final String EVENT_VIDEO_LOSS = "VIDEO_LOSS";
    
    /** 硬盘故障事件 */
    public static final String EVENT_DISK_ERROR = "DISK_ERROR";
    
    /** 录像状态变更事件 */
    public static final String EVENT_RECORD_STATUS = "RECORD_STATUS";

    // ==================== 消息主题常量 ====================

    /**
     * NVR 事件主题
     * @deprecated 已迁移到统一事件主题 {@link IotMessageTopics#DEVICE_EVENT_REPORTED}
     */
    @Deprecated
    public static final String TOPIC_NVR_EVENT = IotMessageTopics.DEVICE_EVENT_REPORTED;

    // ==================== 依赖注入 ====================

    private final NvrConfig config;
    private final NvrConnectionManager connectionManager;
    private final DeviceLifecycleManager lifecycleManager;
    private final GatewayMessagePublisher messagePublisher;
    private final NvrSdkWrapper sdkWrapper;

    /**
     * 重连调度器
     */
    private final ScheduledExecutorService reconnectScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Nvr-Reconnect");
        t.setDaemon(true);
        return t;
    });

    // ==================== 初始化 ====================

    /**
     * 初始化插件，注册断线监听器
     */
    @PostConstruct
    public void init() {
        log.info("{} 初始化插件，注册断线监听器...", LOG_PREFIX);
        
        // 注册断线监听器
        sdkWrapper.addDisconnectListener((loginHandle, ip, port) -> {
            handleDeviceDisconnect(loginHandle, ip, port);
        });
        
        log.info("{} ✅ 断线监听器注册完成", LOG_PREFIX);
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
        NvrConnectionManager.NvrConnectionInfo connInfo = connectionManager.getConnectionInfo(deviceId);
        
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
    private void scheduleReconnect(Long deviceId, NvrConnectionManager.NvrConnectionInfo connInfo) {
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
    private void attemptReconnect(Long deviceId, NvrConnectionManager.NvrConnectionInfo connInfo) {
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
                    .deviceType(PluginConstants.DEVICE_TYPE_NVR)
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
        return PluginConstants.DEVICE_TYPE_NVR;
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
        return PluginConstants.DEVICE_TYPE_NVR.equalsIgnoreCase(deviceInfo.getDeviceType());
    }

    @Override
    public CommandResult executeCommand(Long deviceId, DeviceCommand command) {
        if (deviceId == null || command == null) {
            return CommandResult.failure("参数不能为空");
        }

        String commandType = command.getCommandType();
        // 映射命令类型
        String mappedCommandType = mapCommandType(commandType);
        
        log.info("{} 执行命令: deviceId={}, commandType={}, mappedType={}", 
                LOG_PREFIX, deviceId, commandType, mappedCommandType);

        try {
            switch (mappedCommandType) {
                case CMD_PTZ_CONTROL:
                    return executePtzControl(deviceId, command);
                case "TOUR_CONTROL":
                    return executeTourControl(deviceId, command);
                case CMD_START_PLAYBACK:
                    return executeStartPlayback(deviceId, command);
                case CMD_STOP_PLAYBACK:
                    return executeStopPlayback(deviceId, command);
                case CMD_QUERY_RECORD:
                    return executeQueryRecord(deviceId, command);
                case CMD_QUERY_CHANNELS:
                    return executeQueryChannels(deviceId);
                case CMD_QUERY_DISK_STATUS:
                    return executeQueryDiskStatus(deviceId);
                case CMD_CAPTURE_PICTURE:
                    return executeCapturePicture(deviceId, command);
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

    @Override
    public DeviceStatus queryStatus(Long deviceId) {
        if (deviceId == null) {
            return DeviceStatus.offline(null);
        }
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

        log.info("{} 登录设备: deviceId={}, ip={}, port={}", LOG_PREFIX, deviceId, ipAddress, port);

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

        if (!sdkWrapper.isInitialized()) {
            log.error("{} SDK 未初始化，无法登录设备: deviceId={}", LOG_PREFIX, deviceId);
            return LoginResult.failure("SDK 未初始化");
        }

        // 检查是否已有连接 - 如果已在线，直接返回成功，避免重复登录导致设备被踢
        if (connectionManager.isOnline(deviceId)) {
            Long existingHandle = connectionManager.getLoginHandle(deviceId);
            log.info("{} 设备已在线，复用现有连接: deviceId={}, handle={}", LOG_PREFIX, deviceId, existingHandle);
            
            // 从连接信息中获取设备信息
            NvrConnectionManager.NvrConnectionInfo connInfo = connectionManager.getConnectionInfo(deviceId);
            
            // 构建设备信息返回
            Map<String, Object> deviceInfo = new HashMap<>();
            deviceInfo.put("deviceType", PluginConstants.DEVICE_TYPE_NVR);
            deviceInfo.put("alreadyOnline", true);
            if (connInfo != null) {
                deviceInfo.put("serialNumber", connInfo.getDeviceIdentifier());
                deviceInfo.put("channelCount", connInfo.getChannelCount());
                deviceInfo.put("diskCount", connInfo.getDiskCount());
            }
            
            return LoginResult.success(existingHandle, deviceInfo);
        }

        try {
            NvrLoginResult sdkResult = sdkWrapper.login(ipAddress, port, username, password);
            
            if (!sdkResult.isSuccess()) {
                log.error("{} 设备登录失败: deviceId={}, error={}", 
                        LOG_PREFIX, deviceId, sdkResult.getErrorMessage());
                return LoginResult.failure(sdkResult.getErrorMessage());
            }
            
            long loginHandle = sdkResult.getLoginHandle();
            String serialNumber = sdkResult.getSerialNumber();
            int channelCount = sdkResult.getChannelCount();
            int diskCount = sdkResult.getDiskCount();
            
            Map<String, Object> deviceInfo = new HashMap<>();
            deviceInfo.put("serialNumber", serialNumber);
            deviceInfo.put("deviceType", PluginConstants.DEVICE_TYPE_NVR);
            deviceInfo.put("channelCount", channelCount);
            deviceInfo.put("diskCount", diskCount);
            deviceInfo.put("sdkDeviceType", sdkResult.getDeviceType());
            
            connectionManager.register(deviceId, serialNumber, loginHandle);
            connectionManager.updateIpAddress(deviceId, ipAddress);
            connectionManager.updatePort(deviceId, port);
            connectionManager.updateSerialNumber(deviceId, serialNumber);
            connectionManager.updateChannelCount(deviceId, channelCount);
            connectionManager.updateDiskCount(deviceId, diskCount);
            
            // 设置通道数量缓存，供后续 queryChannels 使用
            sdkWrapper.setChannelCount(loginHandle, channelCount);
            
            // 构建设备连接信息并原子地更新状态（注册 → 直接上线）
            DeviceConnectionInfo loginConnectionInfo = DeviceConnectionInfo.builder()
                    .deviceId(deviceId)
                    .deviceType(PluginConstants.DEVICE_TYPE_NVR)
                    .vendor("Dahua")
                    .connectionMode(ConnectionMode.ACTIVE)
                    .build();
            lifecycleManager.onDeviceLogin(deviceId, loginConnectionInfo);
            
            publishNvrEvent(deviceId, "LOGIN", Map.of(
                    "action", "LOGIN",
                    "serialNumber", serialNumber,
                    "channelCount", channelCount,
                    "diskCount", diskCount
            ));
            
            log.info("{} ✅ 设备登录成功: deviceId={}, handle={}, sn={}, channels={}, disks={}", 
                    LOG_PREFIX, deviceId, loginHandle, serialNumber, channelCount, diskCount);
            
            // ✅ 登录成功后，主动查询通道信息（获取详细的通道状态和 PTZ 能力）
            asyncQueryChannelsAfterLogin(deviceId, loginHandle);
            
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
            Long loginHandle = connectionManager.getLoginHandle(deviceId);
            
            if (loginHandle != null && loginHandle > 0) {
                boolean logoutSuccess = sdkWrapper.logout(loginHandle);
                if (!logoutSuccess) {
                    log.warn("{} SDK 登出失败，但仍将清理本地状态: deviceId={}", LOG_PREFIX, deviceId);
                }
            }
            
            // 原子地更新设备状态为离线
            lifecycleManager.onDeviceLogout(deviceId, "SDK登出");
            connectionManager.unregister(deviceId);
            
            publishNvrEvent(deviceId, "LOGOUT", Map.of("action", "LOGOUT"));
            
            log.info("{} ✅ 设备登出成功: deviceId={}", LOG_PREFIX, deviceId);
        } catch (Exception e) {
            log.error("{} 设备登出异常: deviceId={}", LOG_PREFIX, deviceId, e);
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
            if (!connectionManager.isOnline(deviceId)) {
                log.warn("{} 保活检测失败: 设备未连接, deviceId={}", LOG_PREFIX, deviceId);
                return false;
            }
            
            Long loginHandle = connectionManager.getLoginHandle(deviceId);
            if (loginHandle == null || loginHandle <= 0) {
                log.warn("{} 保活检测失败: 无效的登录句柄, deviceId={}", LOG_PREFIX, deviceId);
                return false;
            }
            
            // 通过查询通道信息来验证连接是否有效
            NvrOperationResult result = sdkWrapper.queryChannels(loginHandle);
            
            connectionManager.updateHeartbeat(deviceId);
            lifecycleManager.updateLastSeen(deviceId);
            
            log.debug("{} 保活检测成功: deviceId={}", LOG_PREFIX, deviceId);
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


    // ==================== 登录后自动任务 ====================

    /**
     * 登录成功后异步查询通道信息
     * 
     * <p>在设备登录成功后，主动查询一次通道信息，获取详细的通道状态和 PTZ 能力，
     * 并通过 {@code iot_device_service_result} 主题发送结果，供 biz 层 
     * {@code DeviceServiceResultConsumer} 处理。</p>
     * 
     * <p>这解决了 Gateway 启动时 SCAN_CHANNELS 命令可能在设备登录前到达导致失败的问题。</p>
     *
     * @param deviceId    设备ID
     * @param loginHandle 登录句柄
     */
    private void asyncQueryChannelsAfterLogin(Long deviceId, long loginHandle) {
        reconnectScheduler.schedule(() -> {
            try {
                log.info("{} 登录后自动查询通道信息: deviceId={}, handle={}", LOG_PREFIX, deviceId, loginHandle);
                
                NvrOperationResult result = sdkWrapper.queryChannels(loginHandle);
                
                if (result.isSuccess()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = result.getData();
                    @SuppressWarnings("unchecked")
                    java.util.List<Map<String, Object>> channels = (java.util.List<Map<String, Object>>) data.get("channels");
                    int totalCount = data.get("totalCount") != null ? (int) data.get("totalCount") : 0;
                    
                    log.info("{} ✅ 通道查询成功: deviceId={}, totalCount={}", LOG_PREFIX, deviceId, totalCount);
                    
                    // 构建 channelList 结构（与 QUERY_CHANNELS 命令结果一致）
                    java.util.List<Map<String, Object>> channelList = new java.util.ArrayList<>();
                    if (channels != null) {
                        for (Map<String, Object> ch : channels) {
                            Map<String, Object> channelInfo = new HashMap<>();
                            channelInfo.put("channelNo", ch.get("channelNo"));
                            channelInfo.put("channelName", ch.get("channelName"));
                            channelInfo.put("channelType", "VIDEO");
                            channelInfo.put("status", Boolean.TRUE.equals(ch.get("online")) ? 0 : 1);
                            
                            // 构建 capabilities
                            Map<String, Object> capabilities = new HashMap<>();
                            @SuppressWarnings("unchecked")
                            Map<String, Object> srcCaps = (Map<String, Object>) ch.get("capabilities");
                            if (srcCaps != null) {
                                capabilities.put("ptzSupport", srcCaps.get("ptzSupport"));
                                capabilities.put("audioSupport", srcCaps.get("audioSupport"));
                            }
                            channelInfo.put("capabilities", capabilities);
                            channelList.add(channelInfo);
                        }
                    }
                    
                    // 发送结果到 iot_device_service_result，供 biz 层 DeviceServiceResultConsumer 处理
                    Map<String, Object> resultData = new HashMap<>();
                    resultData.put("channelList", channelList);
                    resultData.put("deviceId", deviceId);
                    resultData.put("queryTime", java.time.LocalDateTime.now().toString());
                    
                    // params 字段用于 extractDeviceType 识别设备类型
                    Map<String, Object> params = new HashMap<>();
                    params.put("deviceType", PluginConstants.DEVICE_TYPE_NVR);
                    params.put("commandType", "QUERY_CHANNELS");
                    
                    cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage resultMessage = 
                            cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage.builder()
                                    .deviceId(deviceId)
                                    .requestId("auto-" + java.util.UUID.randomUUID().toString())
                                    .method("QUERY_CHANNELS")
                                    .params(params) // 必须设置 params.deviceType 供 biz 层识别
                                    .code(0) // 成功
                                    .data(resultData)
                                    .build();
                    
                    // 使用 publish 方法发送到 DEVICE_SERVICE_RESULT 主题
                    messagePublisher.publish(
                            IotMessageTopics.DEVICE_SERVICE_RESULT,
                            resultMessage
                    );
                    
                    log.info("{} ✅ 通道查询结果已发送到 biz 层: deviceId={}, channelCount={}", 
                            LOG_PREFIX, deviceId, channelList.size());
                    
                } else {
                    log.warn("{} 通道查询失败: deviceId={}, error={}", LOG_PREFIX, deviceId, result.getMessage());
                }
            } catch (Exception e) {
                log.error("{} 登录后查询通道异常: deviceId={}, error={}", LOG_PREFIX, deviceId, e.getMessage(), e);
            }
        }, 2, TimeUnit.SECONDS); // 延迟 2 秒执行，确保登录流程完成
    }

    // ==================== 命令执行方法 ====================

    /**
     * 执行 PTZ 控制命令
     * 
     * <p>支持两种模式：</p>
     * <ul>
     *     <li>直接控制 NVR 通道：当没有 targetIp 时</li>
     *     <li>直连 IPC 控制：当指定了 targetIp 时，直接连接该 IP 的设备</li>
     * </ul>
     */
    private CommandResult executePtzControl(Long deviceId, DeviceCommand command) {
        Integer channelNo = command.getParam("channelNo");
        String ptzCommand = command.getParam("ptzCommand");
        Integer speed = command.getParam("speed");
        String targetIp = command.getParam("targetIp");
        String targetUsername = command.getParam("targetUsername");
        String targetPassword = command.getParam("targetPassword");
        // 预设点参数
        Integer presetNo = command.getParam("presetNo");
        String presetAction = command.getParam("presetAction");
        String presetName = command.getParam("presetName");

        if (channelNo == null) {
            channelNo = 0;
        }
        if (speed == null) {
            speed = 4;
        }

        // 判断是否为预设点操作
        boolean isPresetOperation = presetAction != null && !presetAction.isEmpty() && presetNo != null;
        
        if (!isPresetOperation && (ptzCommand == null || ptzCommand.isEmpty())) {
            return CommandResult.failure("缺少 ptzCommand 或 presetAction 参数");
        }

        log.info("{} 执行PTZ控制: deviceId={}, channel={}, cmd={}, speed={}, targetIp={}, presetNo={}, presetAction={}", 
                LOG_PREFIX, deviceId, channelNo, ptzCommand, speed, targetIp, presetNo, presetAction);

        try {
            NvrOperationResult sdkResult;
            
            // 如果指定了 targetIp，则直接连接该设备进行控制
            if (targetIp != null && !targetIp.isEmpty()) {
                String username = targetUsername != null ? targetUsername : "admin";
                String password = targetPassword != null ? targetPassword : "admin123";
                
                if (isPresetOperation) {
                    // 预设点控制（直连 IPC）
                    log.info("{} 直连IPC进行预设点控制: ip={}, channel={}, presetNo={}, action={}, name={}", 
                            LOG_PREFIX, targetIp, channelNo, presetNo, presetAction, presetName);
                    sdkResult = sdkWrapper.presetControlDirect(targetIp, username, password, 
                            channelNo, presetNo, presetAction, presetName);
                } else {
                    // PTZ 方向控制（直连 IPC）
                    log.info("{} 直连IPC进行PTZ控制: ip={}, channel={}, cmd={}", 
                            LOG_PREFIX, targetIp, channelNo, ptzCommand);
                    sdkResult = sdkWrapper.ptzControlDirect(targetIp, username, password, 
                            channelNo, ptzCommand, speed);
                }
            } else {
                // 通过 NVR 控制
                Long loginHandle = connectionManager.getLoginHandle(deviceId);
                if (loginHandle == null || loginHandle <= 0) {
                    return CommandResult.failure("设备未连接");
                }
                
                if (isPresetOperation) {
                    // 预设点控制
                    sdkResult = sdkWrapper.presetControl(loginHandle, channelNo, presetNo, presetAction);
                } else {
                    // PTZ 方向控制
                    sdkResult = sdkWrapper.ptzControl(loginHandle, channelNo, ptzCommand, speed);
                }
            }
            
            if (sdkResult.isSuccess()) {
                Map<String, Object> resultData = new java.util.HashMap<>();
                resultData.put("message", isPresetOperation ? "预设点控制成功" : "PTZ控制成功");
                resultData.put("channelNo", channelNo);
                if (isPresetOperation) {
                    resultData.put("presetNo", presetNo);
                    resultData.put("presetAction", presetAction);
                } else {
                    resultData.put("ptzCommand", ptzCommand);
                }
                return CommandResult.success(resultData);
            } else {
                return CommandResult.failure((isPresetOperation ? "预设点" : "PTZ") + "控制失败: " + sdkResult.getMessage());
            }
        } catch (Exception e) {
            log.error("{} PTZ控制异常: deviceId={}", LOG_PREFIX, deviceId, e);
            return CommandResult.failure("PTZ控制异常: " + e.getMessage());
        }
    }

    /**
     * 执行巡航控制命令
     * 
     * <p>支持的操作：</p>
     * <ul>
     *     <li>SYNC - 同步巡航配置到设备</li>
     *     <li>START - 启动设备巡航</li>
     *     <li>STOP - 停止设备巡航</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    private CommandResult executeTourControl(Long deviceId, DeviceCommand command) {
        Integer channelNo = command.getParam("channelNo");
        String tourAction = command.getParam("tourAction");
        Integer tourNo = command.getParam("tourNo");
        String tourName = command.getParam("tourName");
        String targetIp = command.getParam("targetIp");
        String targetUsername = command.getParam("targetUsername");
        String targetPassword = command.getParam("targetPassword");
        
        if (channelNo == null) {
            channelNo = 1;
        }
        if (tourNo == null) {
            tourNo = 1;
        }
        if (tourAction == null || tourAction.isEmpty()) {
            return CommandResult.failure("缺少 tourAction 参数");
        }

        log.info("{} 执行巡航控制: deviceId={}, channel={}, tourNo={}, tourName={}, action={}, targetIp={}", 
                LOG_PREFIX, deviceId, channelNo, tourNo, tourName, tourAction, targetIp);

        try {
            NvrOperationResult sdkResult;
            String username = targetUsername != null ? targetUsername : "admin";
            String password = targetPassword != null ? targetPassword : "admin123";
            
            if (targetIp == null || targetIp.isEmpty()) {
                return CommandResult.failure("巡航控制需要指定 targetIp");
            }

            switch (tourAction.toUpperCase()) {
                case "SYNC":
                    // 获取预设点列表和停留时间
                    java.util.List<Integer> presetNos = command.getParam("presetNos");
                    java.util.List<Integer> dwellTimes = command.getParam("dwellTimes");
                    if (presetNos == null || presetNos.isEmpty()) {
                        return CommandResult.failure("同步巡航需要 presetNos 参数");
                    }
                    log.info("{} 同步巡航到设备: ip={}, tourNo={}, tourName={}, presetCount={}, dwellTimes={}", 
                            LOG_PREFIX, targetIp, tourNo, tourName, presetNos.size(), dwellTimes);
                    sdkResult = sdkWrapper.syncTourToDeviceDirect(targetIp, username, password, channelNo, 
                            tourNo, tourName, presetNos, dwellTimes);
                    break;
                    
                case "START":
                    log.info("{} 启动设备巡航: ip={}, tourNo={}", LOG_PREFIX, targetIp, tourNo);
                    sdkResult = sdkWrapper.startTourDirect(targetIp, username, password, channelNo, tourNo);
                    break;
                    
                case "STOP":
                    log.info("{} 停止设备巡航: ip={}, tourNo={}", LOG_PREFIX, targetIp, tourNo);
                    sdkResult = sdkWrapper.stopTourDirect(targetIp, username, password, channelNo, tourNo);
                    break;
                    
                default:
                    return CommandResult.failure("不支持的巡航操作: " + tourAction);
            }
            
            if (sdkResult.isSuccess()) {
                Map<String, Object> resultData = new java.util.HashMap<>();
                resultData.put("tourAction", tourAction);
                resultData.put("tourNo", tourNo);
                resultData.put("tourName", tourName);
                resultData.put("channelNo", channelNo);
                resultData.put("message", sdkResult.getMessage());
                return CommandResult.success(resultData);
            } else {
                return CommandResult.failure("巡航控制失败: " + sdkResult.getMessage());
            }
        } catch (Exception e) {
            log.error("{} 巡航控制异常: deviceId={}", LOG_PREFIX, deviceId, e);
            return CommandResult.failure("巡航控制异常: " + e.getMessage());
        }
    }

    /**
     * 执行开始录像回放命令
     */
    private CommandResult executeStartPlayback(Long deviceId, DeviceCommand command) {
        Integer channelNo = command.getParam("channelNo");
        String startTime = command.getParam("startTime");
        String endTime = command.getParam("endTime");

        if (channelNo == null) {
            return CommandResult.failure("缺少 channelNo 参数");
        }
        if (startTime == null || endTime == null) {
            return CommandResult.failure("缺少 startTime 或 endTime 参数");
        }

        log.info("{} 开始录像回放: deviceId={}, channel={}, start={}, end={}", 
                LOG_PREFIX, deviceId, channelNo, startTime, endTime);

        try {
            Long loginHandle = connectionManager.getLoginHandle(deviceId);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接");
            }
            
            NvrOperationResult sdkResult = sdkWrapper.startPlayback(loginHandle, channelNo, startTime, endTime);
            
            if (sdkResult.isSuccess()) {
                return CommandResult.success(Map.of(
                        "message", "开始录像回放成功",
                        "channelNo", channelNo,
                        "playbackHandle", sdkResult.getData().get("playbackHandle")
                ));
            } else {
                return CommandResult.failure("开始录像回放失败: " + sdkResult.getMessage());
            }
        } catch (Exception e) {
            log.error("{} 开始录像回放异常: deviceId={}", LOG_PREFIX, deviceId, e);
            return CommandResult.failure("开始录像回放异常: " + e.getMessage());
        }
    }

    /**
     * 执行停止录像回放命令
     */
    private CommandResult executeStopPlayback(Long deviceId, DeviceCommand command) {
        Long playbackHandle = command.getParam("playbackHandle");

        if (playbackHandle == null) {
            return CommandResult.failure("缺少 playbackHandle 参数");
        }

        log.info("{} 停止录像回放: deviceId={}, handle={}", LOG_PREFIX, deviceId, playbackHandle);

        try {
            NvrOperationResult sdkResult = sdkWrapper.stopPlayback(playbackHandle);
            
            if (sdkResult.isSuccess()) {
                return CommandResult.success(Map.of("message", "停止录像回放成功"));
            } else {
                return CommandResult.failure("停止录像回放失败: " + sdkResult.getMessage());
            }
        } catch (Exception e) {
            log.error("{} 停止录像回放异常: deviceId={}", LOG_PREFIX, deviceId, e);
            return CommandResult.failure("停止录像回放异常: " + e.getMessage());
        }
    }

    /**
     * 执行查询录像文件命令
     */
    private CommandResult executeQueryRecord(Long deviceId, DeviceCommand command) {
        Integer channelNo = command.getParam("channelNo");
        String startTime = command.getParam("startTime");
        String endTime = command.getParam("endTime");
        Integer recordType = command.getParam("recordType");

        if (channelNo == null) {
            return CommandResult.failure("缺少 channelNo 参数");
        }
        if (startTime == null || endTime == null) {
            return CommandResult.failure("缺少 startTime 或 endTime 参数");
        }

        log.info("{} 查询录像文件: deviceId={}, channel={}, start={}, end={}", 
                LOG_PREFIX, deviceId, channelNo, startTime, endTime);

        try {
            Long loginHandle = connectionManager.getLoginHandle(deviceId);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接");
            }
            
            NvrOperationResult sdkResult = sdkWrapper.queryRecordFiles(
                    loginHandle, channelNo, startTime, endTime, recordType);
            
            if (sdkResult.isSuccess()) {
                return CommandResult.success(sdkResult.getData());
            } else {
                return CommandResult.failure("查询录像文件失败: " + sdkResult.getMessage());
            }
        } catch (Exception e) {
            log.error("{} 查询录像文件异常: deviceId={}", LOG_PREFIX, deviceId, e);
            return CommandResult.failure("查询录像文件异常: " + e.getMessage());
        }
    }

    /**
     * 执行查询通道信息命令
     */
    private CommandResult executeQueryChannels(Long deviceId) {
        log.info("{} 查询通道信息: deviceId={}", LOG_PREFIX, deviceId);

        try {
            Long loginHandle = connectionManager.getLoginHandle(deviceId);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接");
            }
            
            NvrOperationResult sdkResult = sdkWrapper.queryChannels(loginHandle);
            
            if (sdkResult.isSuccess()) {
                return CommandResult.success(sdkResult.getData());
            } else {
                return CommandResult.failure("查询通道信息失败: " + sdkResult.getMessage());
            }
        } catch (Exception e) {
            log.error("{} 查询通道信息异常: deviceId={}", LOG_PREFIX, deviceId, e);
            return CommandResult.failure("查询通道信息异常: " + e.getMessage());
        }
    }

    /**
     * 执行查询硬盘状态命令
     */
    private CommandResult executeQueryDiskStatus(Long deviceId) {
        log.info("{} 查询硬盘状态: deviceId={}", LOG_PREFIX, deviceId);

        try {
            Long loginHandle = connectionManager.getLoginHandle(deviceId);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接");
            }
            
            NvrOperationResult sdkResult = sdkWrapper.queryDiskStatus(loginHandle);
            
            if (sdkResult.isSuccess()) {
                return CommandResult.success(sdkResult.getData());
            } else {
                return CommandResult.failure("查询硬盘状态失败: " + sdkResult.getMessage());
            }
        } catch (Exception e) {
            log.error("{} 查询硬盘状态异常: deviceId={}", LOG_PREFIX, deviceId, e);
            return CommandResult.failure("查询硬盘状态异常: " + e.getMessage());
        }
    }

    /**
     * 执行抓图命令
     */
    private CommandResult executeCapturePicture(Long deviceId, DeviceCommand command) {
        Integer channelNo = command.getParam("channelNo");

        if (channelNo == null) {
            channelNo = 0;
        }

        log.info("{} 抓图: deviceId={}, channel={}", LOG_PREFIX, deviceId, channelNo);

        try {
            Long loginHandle = connectionManager.getLoginHandle(deviceId);
            if (loginHandle == null || loginHandle <= 0) {
                return CommandResult.failure("设备未连接");
            }
            
            NvrOperationResult sdkResult = sdkWrapper.capturePicture(loginHandle, channelNo);
            
            if (sdkResult.isSuccess()) {
                return CommandResult.success(sdkResult.getData());
            } else {
                return CommandResult.failure("抓图失败: " + sdkResult.getMessage());
            }
        } catch (Exception e) {
            log.error("{} 抓图异常: deviceId={}", LOG_PREFIX, deviceId, e);
            return CommandResult.failure("抓图异常: " + e.getMessage());
        }
    }

    // ==================== 事件发布方法 ====================

    /**
     * 发布 NVR 事件
     * 
     * <p>使用强类型 DTO（NvrEventMessage）发布事件，符合 Requirements 4.3</p>
     *
     * @param deviceId  设备ID
     * @param eventType 事件类型（使用 NvrEventMessage.EventType 常量）
     * @param eventData 事件数据
     */
    private void publishNvrEvent(Long deviceId, int eventType, Map<String, Object> eventData) {
        NvrEventMessage event = NvrEventMessage.builder()
                .deviceId(deviceId)
                .eventType(eventType)
                .timestamp(System.currentTimeMillis())
                .deviceType(PluginConstants.DEVICE_TYPE_NVR)
                .pluginId(PluginConstants.PLUGIN_ID_NVR)
                .extData(eventData)
                .build();

        // 发布事件到统一的设备事件主题 DEVICE_EVENT_REPORTED
        messagePublisher.publishEvent(IotMessageTopics.DEVICE_EVENT_REPORTED, event);
    }
    
    /**
     * 发布 NVR 事件（兼容旧代码，使用字符串事件类型）
     * 
     * @param deviceId      设备ID
     * @param eventTypeName 事件类型名称
     * @param eventData     事件数据
     * @deprecated 请使用 {@link #publishNvrEvent(Long, int, Map)} 方法
     */
    @Deprecated
    private void publishNvrEvent(Long deviceId, String eventTypeName, Map<String, Object> eventData) {
        // 将字符串事件类型转换为整数常量
        int eventType = convertEventTypeNameToInt(eventTypeName);
        
        NvrEventMessage event = NvrEventMessage.builder()
                .deviceId(deviceId)
                .eventType(eventType)
                .eventTypeName(eventTypeName)
                .timestamp(System.currentTimeMillis())
                .deviceType(PluginConstants.DEVICE_TYPE_NVR)
                .pluginId(PluginConstants.PLUGIN_ID_NVR)
                .extData(eventData)
                .build();

        // 发布事件到统一的设备事件主题 DEVICE_EVENT_REPORTED
        messagePublisher.publishEvent(IotMessageTopics.DEVICE_EVENT_REPORTED, event);
    }
    
    /**
     * 将事件类型名称转换为整数常量
     *
     * @param eventTypeName 事件类型名称
     * @return 事件类型常量
     */
    private int convertEventTypeNameToInt(String eventTypeName) {
        if (eventTypeName == null) {
            return 0;
        }
        switch (eventTypeName.toUpperCase()) {
            case "MOTION_DETECT": return NvrEventMessage.EventType.MOTION_DETECT;
            case "VIDEO_LOSS": return NvrEventMessage.EventType.VIDEO_LOSS;
            case "VIDEO_BLIND": return NvrEventMessage.EventType.VIDEO_BLIND;
            case "DISK_ERROR": return NvrEventMessage.EventType.DISK_ERROR;
            case "DISK_FULL": return NvrEventMessage.EventType.DISK_FULL;
            case "NETWORK_DISCONNECT": return NvrEventMessage.EventType.NETWORK_DISCONNECT;
            case "ILLEGAL_ACCESS": return NvrEventMessage.EventType.ILLEGAL_ACCESS;
            case "RECORDING_START": return NvrEventMessage.EventType.RECORDING_START;
            case "RECORDING_END": return NvrEventMessage.EventType.RECORDING_END;
            case "CAPTURE_COMPLETE": return NvrEventMessage.EventType.CAPTURE_COMPLETE;
            case "PTZ_CONTROL": return NvrEventMessage.EventType.PTZ_CONTROL;
            case "CHANNEL_STATUS_CHANGE": return NvrEventMessage.EventType.CHANNEL_STATUS_CHANGE;
            default: return 0;
        }
    }
}
