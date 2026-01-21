package cn.iocoder.yudao.module.iot.newgateway.plugins.ipc;

import cn.iocoder.yudao.module.iot.core.enums.ConnectionMode;
import cn.iocoder.yudao.module.iot.core.gateway.dto.DeviceInfo;
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
import cn.iocoder.yudao.module.iot.newgateway.plugins.ipc.adapter.IpcVendorAdapter;
import cn.iocoder.yudao.module.iot.newgateway.plugins.ipc.dto.*;
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
 * IPC 摄像头插件（多品牌支持）
 * 
 * <p>实现独立 IPC 摄像头设备的接入，支持多个品牌：</p>
 * <ul>
 *     <li>大华（Dahua）- 使用 NetSDK</li>
 *     <li>海康威视（Hikvision）- 使用 HCNetSDK</li>
 *     <li>ONVIF - 通用协议，支持大多数品牌</li>
 * </ul>
 * 
 * <p>功能支持：</p>
 * <ul>
 *     <li>设备登录/登出</li>
 *     <li>实时预览（返回 RTSP 地址）</li>
 *     <li>PTZ 云台控制</li>
 *     <li>抓图</li>
 *     <li>AI 事件（人脸检测、车牌识别等）</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 * @see ActiveDeviceHandler
 * @see IpcConfig
 */
@DevicePlugin(
    id = PluginConstants.PLUGIN_ID_IPC,
    name = "IPC摄像头",
    deviceType = PluginConstants.DEVICE_TYPE_IPC,
    vendor = "Multi-Vendor",
    description = "多品牌IPC摄像头设备，支持大华、海康、ONVIF等，提供实时预览、PTZ控制、抓图、AI事件等功能",
    capabilityRefreshEnabled = true,
    enabledByDefault = false
)
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "ipc", havingValue = "true", matchIfMissing = false)
@Slf4j
@RequiredArgsConstructor
public class IpcPlugin implements ActiveDeviceHandler {

    private static final String LOG_PREFIX = "[IpcPlugin]";

    // ==================== 命令类型常量 ====================

    public static final String CMD_PTZ_CONTROL = "PTZ_CONTROL";
    public static final String CMD_START_PREVIEW = "START_PREVIEW";
    public static final String CMD_STOP_PREVIEW = "STOP_PREVIEW";
    public static final String CMD_GET_RTSP_URL = "GET_RTSP_URL";
    public static final String CMD_CAPTURE_PICTURE = "CAPTURE_PICTURE";
    public static final String CMD_QUERY_DEVICE_INFO = "QUERY_DEVICE_INFO";
    public static final String CMD_QUERY_CAPABILITIES = "QUERY_CAPABILITIES";
    public static final String CMD_SET_OSD = "SET_OSD";
    public static final String CMD_SET_IMAGE_CONFIG = "SET_IMAGE_CONFIG";

    private static final Map<String, String> COMMAND_TYPE_MAPPING = Map.ofEntries(
        Map.entry("PTZ", CMD_PTZ_CONTROL),
        Map.entry("PTZ_MOVE", CMD_PTZ_CONTROL),
        Map.entry("PTZ_STOP", CMD_PTZ_CONTROL),
        Map.entry("PTZ_PRESET", CMD_PTZ_CONTROL),
        Map.entry("PREVIEW", CMD_START_PREVIEW),
        Map.entry("LIVE", CMD_START_PREVIEW),
        Map.entry("REALTIME", CMD_START_PREVIEW),
        Map.entry("RTSP", CMD_GET_RTSP_URL),
        Map.entry("GET_STREAM_URL", CMD_GET_RTSP_URL),
        Map.entry("SNAPSHOT", CMD_CAPTURE_PICTURE),
        Map.entry("CAPTURE", CMD_CAPTURE_PICTURE),
        Map.entry("TAKE_PICTURE", CMD_CAPTURE_PICTURE),
        Map.entry("DEVICE_INFO", CMD_QUERY_DEVICE_INFO),
        Map.entry("GET_INFO", CMD_QUERY_DEVICE_INFO)
    );

    // ==================== 事件类型常量 ====================

    public static final String EVENT_MOTION_DETECT = "MOTION_DETECT";
    public static final String EVENT_VIDEO_BLIND = "VIDEO_BLIND";
    public static final String EVENT_VIDEO_LOSS = "VIDEO_LOSS";
    public static final String EVENT_FACE_DETECT = "FACE_DETECT";
    public static final String EVENT_PLATE_RECOGNIZE = "PLATE_RECOGNIZE";
    public static final String EVENT_CROSS_LINE = "CROSS_LINE";
    public static final String EVENT_REGION_INTRUSION = "REGION_INTRUSION";

    // ==================== 依赖注入 ====================

    private final IpcConfig config;
    private final IpcConnectionManager connectionManager;
    private final DeviceLifecycleManager lifecycleManager;
    private final GatewayMessagePublisher messagePublisher;
    private final IpcSdkWrapper sdkWrapper;

    private final ScheduledExecutorService reconnectScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Ipc-Reconnect");
        t.setDaemon(true);
        return t;
    });

    // ==================== 初始化 ====================

    @PostConstruct
    public void init() {
        log.info("{} 初始化多品牌 IPC 插件...", LOG_PREFIX);
        
        // 注册断线监听器
        sdkWrapper.addDisconnectListener((loginHandle, ip, port) -> {
            handleDeviceDisconnect(loginHandle, ip, port);
        });
        
        log.info("{} ✅ IPC 插件初始化完成，支持厂商: {}", LOG_PREFIX, sdkWrapper.getSupportedVendors());
    }

    private void handleDeviceDisconnect(long loginHandle, String ip, int port) {
        Long deviceId = connectionManager.getDeviceIdByLoginHandle(loginHandle);
        
        if (deviceId == null) {
            log.warn("{} 无法找到断线设备的 deviceId: loginHandle={}, ip={}", LOG_PREFIX, loginHandle, ip);
            return;
        }
        
        log.warn("{} 设备断线: deviceId={}, ip={}, port={}", LOG_PREFIX, deviceId, ip, port);
        
        IpcConnectionManager.IpcConnectionInfo connInfo = connectionManager.getConnectionInfo(deviceId);
        
        try {
            lifecycleManager.onDeviceDisconnected(deviceId, "SDK检测到设备断线");
        } catch (Exception e) {
            log.error("{} 更新设备离线状态失败: deviceId={}", LOG_PREFIX, deviceId, e);
        }
        
        connectionManager.unregister(deviceId);
        
        if (connInfo != null && connInfo.getIpAddress() != null) {
            scheduleReconnect(deviceId, connInfo);
        }
    }

    private void scheduleReconnect(Long deviceId, IpcConnectionManager.IpcConnectionInfo connInfo) {
        if (connectionManager.isReconnecting(deviceId)) {
            return;
        }
        
        connectionManager.setReconnecting(deviceId, true);
        
        long reconnectInterval = config.getReconnectInterval();
        log.info("{} 调度自动重连: deviceId={}, ip={}, 延迟={}ms", 
                LOG_PREFIX, deviceId, connInfo.getIpAddress(), reconnectInterval);
        
        reconnectScheduler.schedule(() -> {
            attemptReconnect(deviceId, connInfo);
        }, reconnectInterval, TimeUnit.MILLISECONDS);
    }

    private void attemptReconnect(Long deviceId, IpcConnectionManager.IpcConnectionInfo connInfo) {
        log.info("{} 尝试重连设备: deviceId={}, ip={}", LOG_PREFIX, deviceId, connInfo.getIpAddress());
        
        try {
            if (connectionManager.isOnline(deviceId)) {
                connectionManager.setReconnecting(deviceId, false);
                return;
            }
            
            DeviceConnectionInfo connectionInfo = DeviceConnectionInfo.builder()
                    .deviceId(deviceId)
                    .ipAddress(connInfo.getIpAddress())
                    .port(connInfo.getPort() != null ? connInfo.getPort() : config.getDefaultPort())
                    .username(config.getDefaultUsername())
                    .password(config.getDefaultPassword())
                    .deviceType(PluginConstants.DEVICE_TYPE_IPC)
                    .connectionMode(ConnectionMode.ACTIVE)
                    .build();
            
            LoginResult result = login(connectionInfo);
            
            if (result.isSuccess()) {
                log.info("{} ✅ 设备重连成功: deviceId={}", LOG_PREFIX, deviceId);
                connectionManager.setReconnecting(deviceId, false);
            } else {
                log.warn("{} 设备重连失败，将继续重试: deviceId={}", LOG_PREFIX, deviceId);
                scheduleReconnect(deviceId, connInfo);
            }
        } catch (Exception e) {
            log.error("{} 重连异常: deviceId={}", LOG_PREFIX, deviceId, e);
            scheduleReconnect(deviceId, connInfo);
        }
    }

    // ==================== DeviceHandler 接口实现 ====================

    @Override
    public String getDeviceType() {
        return PluginConstants.DEVICE_TYPE_IPC;
    }

    @Override
    public String getVendor() {
        return "Multi-Vendor";
    }

    @Override
    public boolean supports(DeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            return false;
        }
        return PluginConstants.DEVICE_TYPE_IPC.equalsIgnoreCase(deviceInfo.getDeviceType());
    }

    @Override
    public CommandResult executeCommand(Long deviceId, DeviceCommand command) {
        if (deviceId == null || command == null) {
            return CommandResult.failure("参数不能为空");
        }

        String commandType = command.getCommandType();
        String mappedCommandType = mapCommandType(commandType);
        
        log.info("{} 执行命令: deviceId={}, commandType={}, mappedType={}", 
                LOG_PREFIX, deviceId, commandType, mappedCommandType);

        try {
            switch (mappedCommandType) {
                case CMD_PTZ_CONTROL:
                    return executePtzControl(deviceId, command);
                case CMD_START_PREVIEW:
                case CMD_GET_RTSP_URL:
                    return executeGetRtspUrl(deviceId, command);
                case CMD_CAPTURE_PICTURE:
                    return executeCapturePicture(deviceId, command);
                case CMD_QUERY_DEVICE_INFO:
                    return executeQueryDeviceInfo(deviceId);
                case CMD_QUERY_CAPABILITIES:
                    return executeQueryCapabilities(deviceId);
                default:
                    return CommandResult.failure("不支持的命令类型: " + commandType);
            }
        } catch (Exception e) {
            log.error("{} 执行命令失败: deviceId={}, commandType={}", LOG_PREFIX, deviceId, commandType, e);
            return CommandResult.failure("命令执行异常: " + e.getMessage());
        }
    }

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
            username = config.getDefaultUsername();
        }
        if (password == null || password.isEmpty()) {
            password = config.getDefaultPassword();
        }

        if (!sdkWrapper.isInitialized()) {
            log.error("{} SDK 未初始化，无法登录设备: deviceId={}", LOG_PREFIX, deviceId);
            return LoginResult.failure("SDK 未初始化");
        }

        // 检查是否已有连接
        if (connectionManager.isOnline(deviceId)) {
            Long existingHandle = connectionManager.getLoginHandle(deviceId);
            log.info("{} 设备已在线，复用现有连接: deviceId={}, handle={}", LOG_PREFIX, deviceId, existingHandle);
            
            IpcConnectionManager.IpcConnectionInfo connInfo = connectionManager.getConnectionInfo(deviceId);
            
            Map<String, Object> deviceInfo = new HashMap<>();
            deviceInfo.put("deviceType", PluginConstants.DEVICE_TYPE_IPC);
            deviceInfo.put("alreadyOnline", true);
            if (connInfo != null) {
                deviceInfo.put("serialNumber", connInfo.getDeviceIdentifier());
                deviceInfo.put("ptzSupported", connInfo.isPtzSupported());
                deviceInfo.put("aiSupported", connInfo.isAiSupported());
            }
            
            return LoginResult.success(existingHandle, deviceInfo);
        }

        try {
            // 构建设备信息（用于选择适配器）
            Map<String, Object> deviceInfoMap = new HashMap<>();
            deviceInfoMap.put("deviceId", deviceId);
            deviceInfoMap.put("vendor", connectionInfo.getVendor());
            if (connectionInfo.getConfig() != null) {
                deviceInfoMap.putAll(connectionInfo.getConfig());
            }
            
            // 使用多品牌 SDK 包装器登录
            IpcLoginResult sdkResult = sdkWrapper.login(deviceId, deviceInfoMap, 
                    ipAddress, port, username, password);
            
            if (!sdkResult.isSuccess()) {
                log.error("{} 设备登录失败: deviceId={}, error={}", 
                        LOG_PREFIX, deviceId, sdkResult.getErrorMessage());
                return LoginResult.failure(sdkResult.getErrorMessage());
            }
            
            long loginHandle = sdkResult.getLoginHandle();
            String serialNumber = sdkResult.getSerialNumber();
            boolean ptzSupported = sdkResult.isPtzSupported();
            boolean audioSupported = sdkResult.isAudioSupported();
            boolean aiSupported = sdkResult.isAiSupported();
            
            // 获取使用的适配器信息
            IpcVendorAdapter adapter = sdkWrapper.getDeviceAdapter(deviceId);
            String vendorCode = adapter != null ? adapter.getVendorCode() : "UNKNOWN";
            
            Map<String, Object> deviceInfo = new HashMap<>();
            deviceInfo.put("serialNumber", serialNumber);
            deviceInfo.put("deviceType", PluginConstants.DEVICE_TYPE_IPC);
            deviceInfo.put("deviceModel", sdkResult.getDeviceModel());
            deviceInfo.put("firmwareVersion", sdkResult.getFirmwareVersion());
            deviceInfo.put("channelCount", sdkResult.getChannelCount());
            deviceInfo.put("ptzSupported", ptzSupported);
            deviceInfo.put("audioSupported", audioSupported);
            deviceInfo.put("aiSupported", aiSupported);
            deviceInfo.put("vendor", vendorCode);
            
            // 注册连接
            connectionManager.register(deviceId, serialNumber, loginHandle);
            connectionManager.updateIpAddress(deviceId, ipAddress);
            connectionManager.updatePort(deviceId, port);
            connectionManager.updateCapabilities(deviceId, ptzSupported, audioSupported, aiSupported);
            
            // 更新生命周期状态
            DeviceConnectionInfo loginConnectionInfo = DeviceConnectionInfo.builder()
                    .deviceId(deviceId)
                    .deviceType(PluginConstants.DEVICE_TYPE_IPC)
                    .vendor(vendorCode)
                    .connectionMode(ConnectionMode.ACTIVE)
                    .build();
            lifecycleManager.onDeviceLogin(deviceId, loginConnectionInfo);
            
            // 发布登录事件
            publishIpcEvent(deviceId, "LOGIN", Map.of(
                    "action", "LOGIN",
                    "serialNumber", serialNumber != null ? serialNumber : "",
                    "vendor", vendorCode,
                    "ptzSupported", ptzSupported,
                    "aiSupported", aiSupported
            ));
            
            log.info("{} ✅ 设备登录成功: deviceId={}, handle={}, vendor={}, sn={}", 
                    LOG_PREFIX, deviceId, loginHandle, vendorCode, serialNumber);
            
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
            boolean logoutSuccess = sdkWrapper.logoutByDeviceId(deviceId);
            if (!logoutSuccess) {
                log.warn("{} SDK 登出失败，但仍将清理本地状态: deviceId={}", LOG_PREFIX, deviceId);
            }
            
            lifecycleManager.onDeviceLogout(deviceId, "SDK登出");
            connectionManager.unregister(deviceId);
            
            publishIpcEvent(deviceId, "LOGOUT", Map.of("action", "LOGOUT"));
            
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

        try {
            if (!connectionManager.isOnline(deviceId)) {
                return false;
            }
            
            connectionManager.updateHeartbeat(deviceId);
            lifecycleManager.updateLastSeen(deviceId);
            
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

    private CommandResult executePtzControl(Long deviceId, DeviceCommand command) {
        if (!connectionManager.isPtzSupported(deviceId)) {
            return CommandResult.failure("设备不支持 PTZ 控制");
        }
        
        Integer channelNo = command.getParam("channelNo");
        String ptzCommand = command.getParam("ptzCommand");
        Integer speed = command.getParam("speed");

        if (channelNo == null) channelNo = 0;
        if (ptzCommand == null || ptzCommand.isEmpty()) {
            return CommandResult.failure("缺少 ptzCommand 参数");
        }
        if (speed == null) speed = 4;

        log.info("{} 执行PTZ控制: deviceId={}, channel={}, cmd={}, speed={}", 
                LOG_PREFIX, deviceId, channelNo, ptzCommand, speed);

        try {
            IpcOperationResult sdkResult = sdkWrapper.ptzControlByDeviceId(deviceId, channelNo, ptzCommand, speed);
            
            if (sdkResult.isSuccess()) {
                return CommandResult.success(Map.of(
                        "message", "PTZ控制成功",
                        "channelNo", channelNo,
                        "ptzCommand", ptzCommand
                ));
            } else {
                return CommandResult.failure("PTZ控制失败: " + sdkResult.getMessage());
            }
        } catch (Exception e) {
            log.error("{} PTZ控制异常: deviceId={}", LOG_PREFIX, deviceId, e);
            return CommandResult.failure("PTZ控制异常: " + e.getMessage());
        }
    }

    private CommandResult executeGetRtspUrl(Long deviceId, DeviceCommand command) {
        Integer channelNo = command.getParam("channelNo");
        Integer subtype = command.getParam("subtype");
        
        if (channelNo == null) channelNo = 1;
        if (subtype == null) subtype = 0;

        try {
            IpcConnectionManager.IpcConnectionInfo connInfo = connectionManager.getConnectionInfo(deviceId);
            if (connInfo == null) {
                return CommandResult.failure("设备未连接");
            }
            
            String ip = connInfo.getIpAddress();
            Integer port = connInfo.getPort();
            
            String rtspUrl = sdkWrapper.buildRtspUrl(deviceId, ip, config.getRtspPort(), 
                    config.getDefaultUsername(), config.getDefaultPassword(), channelNo, subtype);
            
            String streamType = subtype == 0 ? "主码流" : "子码流";
            
            return CommandResult.success(Map.of(
                    "rtspUrl", rtspUrl,
                    "channelNo", channelNo,
                    "subtype", subtype,
                    "streamType", streamType,
                    "ip", ip,
                    "port", port
            ));
        } catch (Exception e) {
            log.error("{} 获取RTSP地址异常: deviceId={}", LOG_PREFIX, deviceId, e);
            return CommandResult.failure("获取RTSP地址异常: " + e.getMessage());
        }
    }

    private CommandResult executeCapturePicture(Long deviceId, DeviceCommand command) {
        Integer channelNo = command.getParam("channelNo");
        if (channelNo == null) channelNo = 0;

        try {
            IpcOperationResult sdkResult = sdkWrapper.capturePictureByDeviceId(deviceId, channelNo);
            
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

    private CommandResult executeQueryDeviceInfo(Long deviceId) {
        try {
            IpcConnectionManager.IpcConnectionInfo connInfo = connectionManager.getConnectionInfo(deviceId);
            if (connInfo == null) {
                return CommandResult.failure("设备未连接");
            }
            
            IpcVendorAdapter adapter = sdkWrapper.getDeviceAdapter(deviceId);
            
            Map<String, Object> info = new HashMap<>();
            info.put("deviceId", deviceId);
            info.put("serialNumber", connInfo.getDeviceIdentifier());
            info.put("ipAddress", connInfo.getIpAddress());
            info.put("port", connInfo.getPort());
            info.put("deviceModel", connInfo.getDeviceModel());
            info.put("firmwareVersion", connInfo.getFirmwareVersion());
            info.put("online", connInfo.isOnline());
            info.put("ptzSupported", connInfo.isPtzSupported());
            info.put("audioSupported", connInfo.isAudioSupported());
            info.put("aiSupported", connInfo.isAiSupported());
            info.put("vendor", adapter != null ? adapter.getVendorCode() : "UNKNOWN");
            
            return CommandResult.success(info);
        } catch (Exception e) {
            log.error("{} 查询设备信息异常: deviceId={}", LOG_PREFIX, deviceId, e);
            return CommandResult.failure("查询设备信息异常: " + e.getMessage());
        }
    }

    private CommandResult executeQueryCapabilities(Long deviceId) {
        try {
            IpcConnectionManager.IpcConnectionInfo connInfo = connectionManager.getConnectionInfo(deviceId);
            if (connInfo == null) {
                return CommandResult.failure("设备未连接");
            }
            
            Map<String, Object> capabilities = new HashMap<>();
            capabilities.put("ptz", connInfo.isPtzSupported());
            capabilities.put("audio", connInfo.isAudioSupported());
            capabilities.put("ai", connInfo.isAiSupported());
            capabilities.put("preview", config.isPreviewEnabled());
            capabilities.put("capture", config.isCaptureEnabled());
            capabilities.put("supportedVendors", sdkWrapper.getSupportedVendors());
            
            return CommandResult.success(capabilities);
        } catch (Exception e) {
            log.error("{} 查询设备能力异常: deviceId={}", LOG_PREFIX, deviceId, e);
            return CommandResult.failure("查询设备能力异常: " + e.getMessage());
        }
    }

    // ==================== 事件发布方法 ====================

    private void publishIpcEvent(Long deviceId, String eventTypeName, Map<String, Object> eventData) {
        Map<String, Object> event = new HashMap<>();
        event.put("deviceId", deviceId);
        event.put("deviceType", PluginConstants.DEVICE_TYPE_IPC);
        event.put("pluginId", PluginConstants.PLUGIN_ID_IPC);
        event.put("eventType", eventTypeName);
        event.put("timestamp", System.currentTimeMillis());
        event.put("data", eventData);

        messagePublisher.publishEvent(IotMessageTopics.DEVICE_EVENT_REPORTED, event);
    }
}
