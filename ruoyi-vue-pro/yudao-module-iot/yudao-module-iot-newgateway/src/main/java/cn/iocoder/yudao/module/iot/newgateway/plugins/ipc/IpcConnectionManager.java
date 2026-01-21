package cn.iocoder.yudao.module.iot.newgateway.plugins.ipc;

import cn.iocoder.yudao.module.iot.newgateway.core.connection.ConnectionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * IPC 设备连接管理器
 * 
 * <p>管理 IPC 摄像头的连接状态、登录句柄等信息。</p>
 * 
 * @author IoT Gateway Team
 * @see IpcPlugin
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "ipc", havingValue = "true", matchIfMissing = false)
public class IpcConnectionManager implements ConnectionManager<Long> {

    private static final String LOG_PREFIX = "[IpcConnectionManager]";

    private final Map<Long, IpcConnectionInfo> connections = new ConcurrentHashMap<>();
    private final Map<String, Long> identifierDeviceMap = new ConcurrentHashMap<>();

    /**
     * IPC 连接信息
     */
    public static class IpcConnectionInfo {
        Long deviceId;
        String deviceIdentifier;
        Long loginHandle;
        String ipAddress;
        Integer port;
        String deviceModel;
        String firmwareVersion;
        boolean ptzSupported;
        boolean audioSupported;
        boolean aiSupported;
        long lastHeartbeat;
        boolean online;
        boolean reconnecting;

        IpcConnectionInfo(Long deviceId, String deviceIdentifier, Long loginHandle) {
            this.deviceId = deviceId;
            this.deviceIdentifier = deviceIdentifier;
            this.loginHandle = loginHandle;
            this.lastHeartbeat = System.currentTimeMillis();
            this.online = true;
            this.reconnecting = false;
        }
        
        public Long getDeviceId() { return deviceId; }
        public String getDeviceIdentifier() { return deviceIdentifier; }
        public Long getLoginHandle() { return loginHandle; }
        public String getIpAddress() { return ipAddress; }
        public Integer getPort() { return port; }
        public String getDeviceModel() { return deviceModel; }
        public String getFirmwareVersion() { return firmwareVersion; }
        public boolean isPtzSupported() { return ptzSupported; }
        public boolean isAudioSupported() { return audioSupported; }
        public boolean isAiSupported() { return aiSupported; }
        public long getLastHeartbeat() { return lastHeartbeat; }
        public boolean isOnline() { return online; }
        public boolean isReconnecting() { return reconnecting; }
    }

    @Override
    public void register(Long deviceId, String deviceIdentifier, Long connection) {
        log.info("{} 注册连接: deviceId={}, identifier={}, handle={}", 
                LOG_PREFIX, deviceId, deviceIdentifier, connection);
        
        IpcConnectionInfo info = new IpcConnectionInfo(deviceId, deviceIdentifier, connection);
        connections.put(deviceId, info);
        if (deviceIdentifier != null) {
            identifierDeviceMap.put(deviceIdentifier, deviceId);
        }
    }

    @Override
    public void unregister(Long deviceId) {
        log.info("{} 注销连接: deviceId={}", LOG_PREFIX, deviceId);
        IpcConnectionInfo info = connections.remove(deviceId);
        if (info != null && info.deviceIdentifier != null) {
            identifierDeviceMap.remove(info.deviceIdentifier);
        }
    }

    @Override
    public Long getConnection(Long deviceId) {
        IpcConnectionInfo info = connections.get(deviceId);
        return info != null ? info.loginHandle : null;
    }

    @Override
    public Long getDeviceIdByIdentifier(String identifier) {
        return identifierDeviceMap.get(identifier);
    }

    @Override
    public boolean isOnline(Long deviceId) {
        IpcConnectionInfo info = connections.get(deviceId);
        return info != null && info.online;
    }

    @Override
    public void updateHeartbeat(Long deviceId) {
        IpcConnectionInfo info = connections.get(deviceId);
        if (info != null) {
            info.lastHeartbeat = System.currentTimeMillis();
            log.debug("{} 更新心跳: deviceId={}", LOG_PREFIX, deviceId);
        }
    }

    @Override
    public Long getLastHeartbeatTime(Long deviceId) {
        IpcConnectionInfo info = connections.get(deviceId);
        return info != null ? info.lastHeartbeat : null;
    }

    @Override
    public int getOnlineCount() {
        return (int) connections.values().stream()
                .filter(info -> info.online)
                .count();
    }

    @Override
    public Collection<Long> getOnlineDeviceIds() {
        return connections.values().stream()
                .filter(info -> info.online)
                .map(info -> info.deviceId)
                .collect(Collectors.toList());
    }

    @Override
    public void closeAll() {
        log.info("{} 关闭所有连接: count={}", LOG_PREFIX, connections.size());
        connections.clear();
        identifierDeviceMap.clear();
    }

    // ==================== 扩展方法 ====================

    public Long getLoginHandle(Long deviceId) {
        return getConnection(deviceId);
    }

    public void updateIpAddress(Long deviceId, String ipAddress) {
        IpcConnectionInfo info = connections.get(deviceId);
        if (info != null) {
            info.ipAddress = ipAddress;
        }
    }

    public void updatePort(Long deviceId, Integer port) {
        IpcConnectionInfo info = connections.get(deviceId);
        if (info != null) {
            info.port = port;
        }
    }

    public void updateSerialNumber(Long deviceId, String serialNumber) {
        IpcConnectionInfo info = connections.get(deviceId);
        if (info != null) {
            info.deviceIdentifier = serialNumber;
        }
    }

    public void updateDeviceModel(Long deviceId, String deviceModel) {
        IpcConnectionInfo info = connections.get(deviceId);
        if (info != null) {
            info.deviceModel = deviceModel;
        }
    }

    public void updateFirmwareVersion(Long deviceId, String firmwareVersion) {
        IpcConnectionInfo info = connections.get(deviceId);
        if (info != null) {
            info.firmwareVersion = firmwareVersion;
        }
    }

    public void updateCapabilities(Long deviceId, boolean ptzSupported, boolean audioSupported, boolean aiSupported) {
        IpcConnectionInfo info = connections.get(deviceId);
        if (info != null) {
            info.ptzSupported = ptzSupported;
            info.audioSupported = audioSupported;
            info.aiSupported = aiSupported;
        }
    }

    /**
     * 根据登录句柄获取设备ID
     *
     * @param loginHandle 登录句柄
     * @return 设备ID，如果不存在则返回 null
     */
    public Long getDeviceIdByLoginHandle(Long loginHandle) {
        if (loginHandle == null) {
            return null;
        }
        for (IpcConnectionInfo info : connections.values()) {
            if (loginHandle.equals(info.loginHandle)) {
                return info.deviceId;
            }
        }
        return null;
    }

    /**
     * 获取连接信息
     *
     * @param deviceId 设备ID
     * @return 连接信息，如果不存在则返回 null
     */
    public IpcConnectionInfo getConnectionInfo(Long deviceId) {
        return connections.get(deviceId);
    }

    /**
     * 标记设备正在重连
     *
     * @param deviceId     设备ID
     * @param reconnecting 是否正在重连
     */
    public void setReconnecting(Long deviceId, boolean reconnecting) {
        IpcConnectionInfo info = connections.get(deviceId);
        if (info != null) {
            info.reconnecting = reconnecting;
        }
    }

    /**
     * 检查设备是否正在重连
     *
     * @param deviceId 设备ID
     * @return 是否正在重连
     */
    public boolean isReconnecting(Long deviceId) {
        IpcConnectionInfo info = connections.get(deviceId);
        return info != null && info.reconnecting;
    }

    /**
     * 获取设备IP地址
     *
     * @param deviceId 设备ID
     * @return IP地址，如果不存在则返回 null
     */
    public String getIpAddress(Long deviceId) {
        IpcConnectionInfo info = connections.get(deviceId);
        return info != null ? info.ipAddress : null;
    }

    /**
     * 获取设备端口
     *
     * @param deviceId 设备ID
     * @return 端口，如果不存在则返回 null
     */
    public Integer getPort(Long deviceId) {
        IpcConnectionInfo info = connections.get(deviceId);
        return info != null ? info.port : null;
    }

    /**
     * 检查设备是否支持 PTZ
     *
     * @param deviceId 设备ID
     * @return 是否支持 PTZ
     */
    public boolean isPtzSupported(Long deviceId) {
        IpcConnectionInfo info = connections.get(deviceId);
        return info != null && info.ptzSupported;
    }

    /**
     * 检查设备是否支持 AI
     *
     * @param deviceId 设备ID
     * @return 是否支持 AI
     */
    public boolean isAiSupported(Long deviceId) {
        IpcConnectionInfo info = connections.get(deviceId);
        return info != null && info.aiSupported;
    }
}
