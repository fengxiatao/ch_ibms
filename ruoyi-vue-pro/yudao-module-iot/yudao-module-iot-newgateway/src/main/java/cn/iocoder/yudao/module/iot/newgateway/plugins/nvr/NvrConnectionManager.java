package cn.iocoder.yudao.module.iot.newgateway.plugins.nvr;

import cn.iocoder.yudao.module.iot.newgateway.core.connection.ConnectionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * NVR 设备连接管理器
 * 
 * @author IoT Gateway Team
 * @see NvrPlugin
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "nvr", havingValue = "true", matchIfMissing = true)
public class NvrConnectionManager implements ConnectionManager<Long> {

    private static final String LOG_PREFIX = "[NvrConnectionManager]";

    private final Map<Long, NvrConnectionInfo> connections = new ConcurrentHashMap<>();
    private final Map<String, Long> identifierDeviceMap = new ConcurrentHashMap<>();

    /**
     * NVR 连接信息
     */
    public static class NvrConnectionInfo {
        Long deviceId;
        String deviceIdentifier;
        Long loginHandle;
        String ipAddress;
        Integer port;
        Integer channelCount;
        Integer diskCount;
        long lastHeartbeat;
        boolean online;
        boolean reconnecting;

        NvrConnectionInfo(Long deviceId, String deviceIdentifier, Long loginHandle) {
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
        public Integer getChannelCount() { return channelCount; }
        public Integer getDiskCount() { return diskCount; }
        public long getLastHeartbeat() { return lastHeartbeat; }
        public boolean isOnline() { return online; }
        public boolean isReconnecting() { return reconnecting; }
    }

    @Override
    public void register(Long deviceId, String deviceIdentifier, Long connection) {
        log.info("{} 注册连接: deviceId={}, identifier={}, handle={}", 
                LOG_PREFIX, deviceId, deviceIdentifier, connection);
        
        NvrConnectionInfo info = new NvrConnectionInfo(deviceId, deviceIdentifier, connection);
        connections.put(deviceId, info);
        if (deviceIdentifier != null) {
            identifierDeviceMap.put(deviceIdentifier, deviceId);
        }
    }


    @Override
    public void unregister(Long deviceId) {
        log.info("{} 注销连接: deviceId={}", LOG_PREFIX, deviceId);
        NvrConnectionInfo info = connections.remove(deviceId);
        if (info != null && info.deviceIdentifier != null) {
            identifierDeviceMap.remove(info.deviceIdentifier);
        }
    }

    @Override
    public Long getConnection(Long deviceId) {
        NvrConnectionInfo info = connections.get(deviceId);
        return info != null ? info.loginHandle : null;
    }

    @Override
    public Long getDeviceIdByIdentifier(String identifier) {
        return identifierDeviceMap.get(identifier);
    }

    @Override
    public boolean isOnline(Long deviceId) {
        NvrConnectionInfo info = connections.get(deviceId);
        return info != null && info.online;
    }

    @Override
    public void updateHeartbeat(Long deviceId) {
        NvrConnectionInfo info = connections.get(deviceId);
        if (info != null) {
            info.lastHeartbeat = System.currentTimeMillis();
            log.debug("{} 更新心跳: deviceId={}", LOG_PREFIX, deviceId);
        }
    }

    @Override
    public Long getLastHeartbeatTime(Long deviceId) {
        NvrConnectionInfo info = connections.get(deviceId);
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
        NvrConnectionInfo info = connections.get(deviceId);
        if (info != null) {
            info.ipAddress = ipAddress;
        }
    }

    public void updatePort(Long deviceId, Integer port) {
        NvrConnectionInfo info = connections.get(deviceId);
        if (info != null) {
            info.port = port;
        }
    }

    public void updateSerialNumber(Long deviceId, String serialNumber) {
        NvrConnectionInfo info = connections.get(deviceId);
        if (info != null) {
            info.deviceIdentifier = serialNumber;
        }
    }

    public void updateChannelCount(Long deviceId, Integer channelCount) {
        NvrConnectionInfo info = connections.get(deviceId);
        if (info != null) {
            info.channelCount = channelCount;
        }
    }

    public Integer getChannelCount(Long deviceId) {
        NvrConnectionInfo info = connections.get(deviceId);
        return info != null ? info.channelCount : null;
    }

    public void updateDiskCount(Long deviceId, Integer diskCount) {
        NvrConnectionInfo info = connections.get(deviceId);
        if (info != null) {
            info.diskCount = diskCount;
        }
    }

    public Integer getDiskCount(Long deviceId) {
        NvrConnectionInfo info = connections.get(deviceId);
        return info != null ? info.diskCount : null;
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
        for (NvrConnectionInfo info : connections.values()) {
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
    public NvrConnectionInfo getConnectionInfo(Long deviceId) {
        return connections.get(deviceId);
    }

    /**
     * 标记设备正在重连
     *
     * @param deviceId     设备ID
     * @param reconnecting 是否正在重连
     */
    public void setReconnecting(Long deviceId, boolean reconnecting) {
        NvrConnectionInfo info = connections.get(deviceId);
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
        NvrConnectionInfo info = connections.get(deviceId);
        return info != null && info.reconnecting;
    }

    /**
     * 获取设备IP地址
     *
     * @param deviceId 设备ID
     * @return IP地址，如果不存在则返回 null
     */
    public String getIpAddress(Long deviceId) {
        NvrConnectionInfo info = connections.get(deviceId);
        return info != null ? info.ipAddress : null;
    }

    /**
     * 获取设备端口
     *
     * @param deviceId 设备ID
     * @return 端口，如果不存在则返回 null
     */
    public Integer getPort(Long deviceId) {
        NvrConnectionInfo info = connections.get(deviceId);
        return info != null ? info.port : null;
    }
}
