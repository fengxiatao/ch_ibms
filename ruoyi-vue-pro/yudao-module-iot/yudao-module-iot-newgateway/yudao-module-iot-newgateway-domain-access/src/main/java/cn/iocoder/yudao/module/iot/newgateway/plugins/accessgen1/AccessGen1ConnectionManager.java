package cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1;

import cn.iocoder.yudao.module.iot.newgateway.core.connection.ConnectionManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 门禁一代连接管理器
 * 
 * <p>管理门禁一代设备的连接状态，实现设备ID与登录句柄的映射。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *     <li>设备连接的注册和注销</li>
 *     <li>设备ID与登录句柄的映射</li>
 *     <li>设备ID与设备标识符（如序列号）的双向映射</li>
 *     <li>心跳时间跟踪</li>
 *     <li>在线状态查询</li>
 * </ul>
 * 
 * <p>连接类型说明：</p>
 * <p>门禁一代设备使用大华 NetSDK 进行通信，登录成功后返回一个 Long 类型的登录句柄（loginHandle）。
 * 后续所有操作（开门、授权下发等）都需要使用这个句柄。</p>
 * 
 * <p>线程安全：所有 Map 使用 ConcurrentHashMap，支持并发访问。</p>
 * 
 * @author IoT Gateway Team
 * @see ConnectionManager
 * @see AccessGen1Plugin
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "access-gen1", havingValue = "true", matchIfMissing = true)
public class AccessGen1ConnectionManager implements ConnectionManager<Long> {

    /**
     * 日志前缀
     */
    private static final String LOG_PREFIX = "[AccessGen1ConnectionManager]";

    /**
     * 设备ID -> 登录句柄
     * <p>
     * 登录句柄是大华 NetSDK 登录设备后返回的唯一标识，用于后续所有设备操作。
     * </p>
     */
    private final Map<Long, Long> deviceLoginHandles = new ConcurrentHashMap<>();

    /**
     * 设备标识符（序列号） -> 设备ID
     * <p>
     * 设备标识符通常是设备的序列号，用于在设备回调时查找对应的设备ID。
     * </p>
     */
    private final Map<String, Long> identifierDeviceMap = new ConcurrentHashMap<>();

    /**
     * 设备ID -> 连接信息
     */
    private final Map<Long, AccessGen1ConnectionInfo> connectionInfoMap = new ConcurrentHashMap<>();

    @Override
    public void register(Long deviceId, String identifier, Long loginHandle) {
        if (deviceId == null || loginHandle == null) {
            log.warn("{} 注册连接失败: 参数不能为空, deviceId={}, identifier={}, loginHandle={}",
                    LOG_PREFIX, deviceId, identifier, loginHandle);
            return;
        }

        // 检查是否已有旧连接（如果有，需要先登出）
        Long oldHandle = deviceLoginHandles.get(deviceId);
        if (oldHandle != null && !oldHandle.equals(loginHandle)) {
            log.info("{} 设备已有旧连接，将被替换: deviceId={}, oldHandle={}, newHandle={}",
                    LOG_PREFIX, deviceId, oldHandle, loginHandle);
            // 注意：实际的 SDK 登出操作应该在调用方处理
        }

        // 注册新连接
        deviceLoginHandles.put(deviceId, loginHandle);
        if (identifier != null && !identifier.isEmpty()) {
            identifierDeviceMap.put(identifier, deviceId);
        }

        // 创建连接信息
        AccessGen1ConnectionInfo info = new AccessGen1ConnectionInfo();
        info.setDeviceId(deviceId);
        info.setLoginHandle(loginHandle);
        info.setIdentifier(identifier);
        info.setConnectTime(System.currentTimeMillis());
        info.setLastHeartbeatTime(System.currentTimeMillis());
        connectionInfoMap.put(deviceId, info);

        log.info("{} 连接注册成功: deviceId={}, identifier={}, loginHandle={}",
                LOG_PREFIX, deviceId, identifier, loginHandle);
    }

    @Override
    public void unregister(Long deviceId) {
        if (deviceId == null) {
            return;
        }

        // 移除登录句柄
        Long loginHandle = deviceLoginHandles.remove(deviceId);

        // 移除标识符映射
        AccessGen1ConnectionInfo info = connectionInfoMap.remove(deviceId);
        if (info != null && info.getIdentifier() != null) {
            identifierDeviceMap.remove(info.getIdentifier());
        }

        log.info("{} 连接注销成功: deviceId={}, loginHandle={}", LOG_PREFIX, deviceId, loginHandle);
    }

    @Override
    public Long getConnection(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        return deviceLoginHandles.get(deviceId);
    }

    @Override
    public Long getDeviceIdByIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return null;
        }
        return identifierDeviceMap.get(identifier);
    }

    @Override
    public boolean isOnline(Long deviceId) {
        if (deviceId == null) {
            return false;
        }
        return deviceLoginHandles.containsKey(deviceId);
    }

    @Override
    public void updateHeartbeat(Long deviceId) {
        if (deviceId == null) {
            return;
        }
        AccessGen1ConnectionInfo info = connectionInfoMap.get(deviceId);
        if (info != null) {
            info.setLastHeartbeatTime(System.currentTimeMillis());
            log.debug("{} 更新心跳时间: deviceId={}", LOG_PREFIX, deviceId);
        }
    }

    @Override
    public Long getLastHeartbeatTime(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        AccessGen1ConnectionInfo info = connectionInfoMap.get(deviceId);
        return info != null ? info.getLastHeartbeatTime() : null;
    }

    @Override
    public int getOnlineCount() {
        return deviceLoginHandles.size();
    }

    @Override
    public Collection<Long> getOnlineDeviceIds() {
        return deviceLoginHandles.keySet();
    }

    @Override
    public void closeAll() {
        log.info("{} 关闭所有连接, 当前连接数: {}", LOG_PREFIX, deviceLoginHandles.size());
        
        // 注意：实际的 SDK 登出操作应该在调用方处理
        // 这里只清理内部状态
        deviceLoginHandles.clear();
        identifierDeviceMap.clear();
        connectionInfoMap.clear();
        
        log.info("{} 所有连接已关闭", LOG_PREFIX);
    }

    @Override
    public boolean isConnectionValid(Long deviceId) {
        if (deviceId == null) {
            return false;
        }
        // 对于主动连接设备，只要有登录句柄就认为连接有效
        // 实际的连接有效性需要通过保活检测来确认
        return deviceLoginHandles.containsKey(deviceId);
    }

    // ==================== 扩展方法 ====================

    /**
     * 获取连接信息
     *
     * @param deviceId 设备ID
     * @return 连接信息，如果不存在则返回 null
     */
    public AccessGen1ConnectionInfo getConnectionInfo(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        return connectionInfoMap.get(deviceId);
    }

    /**
     * 获取登录句柄
     *
     * @param deviceId 设备ID
     * @return 登录句柄，如果不存在则返回 null
     */
    public Long getLoginHandle(Long deviceId) {
        return getConnection(deviceId);
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
        for (Map.Entry<Long, Long> entry : deviceLoginHandles.entrySet()) {
            if (loginHandle.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 检查登录句柄是否已注册
     *
     * @param loginHandle 登录句柄
     * @return 是否已注册
     */
    public boolean isLoginHandleRegistered(Long loginHandle) {
        return loginHandle != null && deviceLoginHandles.containsValue(loginHandle);
    }

    /**
     * 获取设备标识符
     *
     * @param deviceId 设备ID
     * @return 设备标识符，如果不存在则返回 null
     */
    public String getIdentifier(Long deviceId) {
        AccessGen1ConnectionInfo info = connectionInfoMap.get(deviceId);
        return info != null ? info.getIdentifier() : null;
    }

    /**
     * 更新设备IP地址
     *
     * @param deviceId  设备ID
     * @param ipAddress IP地址
     */
    public void updateIpAddress(Long deviceId, String ipAddress) {
        AccessGen1ConnectionInfo info = connectionInfoMap.get(deviceId);
        if (info != null) {
            info.setIpAddress(ipAddress);
        }
    }

    /**
     * 获取设备IP地址
     *
     * @param deviceId 设备ID
     * @return IP地址，如果不存在则返回 null
     */
    public String getIpAddress(Long deviceId) {
        AccessGen1ConnectionInfo info = connectionInfoMap.get(deviceId);
        return info != null ? info.getIpAddress() : null;
    }

    /**
     * 更新设备端口
     *
     * @param deviceId 设备ID
     * @param port     端口
     */
    public void updatePort(Long deviceId, Integer port) {
        AccessGen1ConnectionInfo info = connectionInfoMap.get(deviceId);
        if (info != null) {
            info.setPort(port);
        }
    }

    /**
     * 获取设备端口
     *
     * @param deviceId 设备ID
     * @return 端口，如果不存在则返回 null
     */
    public Integer getPort(Long deviceId) {
        AccessGen1ConnectionInfo info = connectionInfoMap.get(deviceId);
        return info != null ? info.getPort() : null;
    }

    /**
     * 更新设备序列号
     *
     * @param deviceId     设备ID
     * @param serialNumber 序列号
     */
    public void updateSerialNumber(Long deviceId, String serialNumber) {
        AccessGen1ConnectionInfo info = connectionInfoMap.get(deviceId);
        if (info != null) {
            info.setSerialNumber(serialNumber);
            // 同时更新标识符映射
            if (serialNumber != null && !serialNumber.isEmpty()) {
                identifierDeviceMap.put(serialNumber, deviceId);
            }
        }
    }

    /**
     * 获取设备序列号
     *
     * @param deviceId 设备ID
     * @return 序列号，如果不存在则返回 null
     */
    public String getSerialNumber(Long deviceId) {
        AccessGen1ConnectionInfo info = connectionInfoMap.get(deviceId);
        return info != null ? info.getSerialNumber() : null;
    }

    /**
     * 更新通道数量
     *
     * @param deviceId     设备ID
     * @param channelCount 通道数量
     */
    public void updateChannelCount(Long deviceId, Integer channelCount) {
        AccessGen1ConnectionInfo info = connectionInfoMap.get(deviceId);
        if (info != null) {
            info.setChannelCount(channelCount);
        }
    }

    /**
     * 获取通道数量
     *
     * @param deviceId 设备ID
     * @return 通道数量，如果不存在则返回 null
     */
    public Integer getChannelCount(Long deviceId) {
        AccessGen1ConnectionInfo info = connectionInfoMap.get(deviceId);
        return info != null ? info.getChannelCount() : null;
    }

    public void updateAnalyzerHandle(Long deviceId, Long analyzerHandle) {
        AccessGen1ConnectionInfo info = connectionInfoMap.get(deviceId);
        if (info != null) {
            info.setAnalyzerHandle(analyzerHandle);
        }
    }

    public Long getAnalyzerHandle(Long deviceId) {
        AccessGen1ConnectionInfo info = connectionInfoMap.get(deviceId);
        return info != null ? info.getAnalyzerHandle() : null;
    }

    /**
     * 更新设备状态
     *
     * @param deviceId 设备ID
     * @param status   状态
     */
    public void updateDeviceStatus(Long deviceId, String status) {
        AccessGen1ConnectionInfo info = connectionInfoMap.get(deviceId);
        if (info != null) {
            info.setDeviceStatus(status);
        }
    }

    /**
     * 获取设备状态
     *
     * @param deviceId 设备ID
     * @return 设备状态，如果不存在则返回 null
     */
    public String getDeviceStatus(Long deviceId) {
        AccessGen1ConnectionInfo info = connectionInfoMap.get(deviceId);
        return info != null ? info.getDeviceStatus() : null;
    }

    /**
     * 标记设备正在重连
     *
     * @param deviceId     设备ID
     * @param reconnecting 是否正在重连
     */
    public void setReconnecting(Long deviceId, boolean reconnecting) {
        AccessGen1ConnectionInfo info = connectionInfoMap.get(deviceId);
        if (info != null) {
            info.setReconnecting(reconnecting);
        }
    }

    /**
     * 检查设备是否正在重连
     *
     * @param deviceId 设备ID
     * @return 是否正在重连
     */
    public boolean isReconnecting(Long deviceId) {
        AccessGen1ConnectionInfo info = connectionInfoMap.get(deviceId);
        return info != null && info.isReconnecting();
    }

    // ==================== 内部类 ====================

    /**
     * 门禁一代设备连接信息
     * <p>
     * 存储门禁一代设备连接的详细信息。
     * </p>
     */
    @Data
    public static class AccessGen1ConnectionInfo {
        /**
         * 设备ID
         */
        private Long deviceId;

        /**
         * 登录句柄
         * <p>
         * 大华 NetSDK 登录设备后返回的句柄，用于后续所有设备操作。
         * </p>
         */
        private Long loginHandle;

        /**
         * 设备标识符（通常是序列号）
         */
        private String identifier;

        /**
         * 设备序列号
         */
        private String serialNumber;

        /**
         * 设备IP地址
         */
        private String ipAddress;

        /**
         * 设备端口
         */
        private Integer port;

        /**
         * 通道数量
         */
        private Integer channelCount;

        /**
         * 连接建立时间（毫秒时间戳）
         */
        private Long connectTime;

        /**
         * 最后心跳时间（毫秒时间戳）
         */
        private Long lastHeartbeatTime;

        /**
         * 设备状态（ONLINE/OFFLINE 等）
         */
        private String deviceStatus;

        /**
         * 设备类型
         */
        private String deviceType;

        /**
         * 设备固件版本
         */
        private String firmwareVersion;

        /**
         * 是否正在重连
         */
        private boolean reconnecting;

        /**
         * 重连次数
         */
        private int reconnectCount;

        /**
         * 最后一次重连时间
         */
        private Long lastReconnectTime;

        /**
         * 智能事件订阅句柄（CLIENT_RealLoadPictureEx 返回值）
         */
        private Long analyzerHandle;
    }
}
