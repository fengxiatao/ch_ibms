package cn.iocoder.yudao.module.iot.newgateway.plugins.changhui;

import cn.iocoder.yudao.module.iot.newgateway.core.connection.ConnectionManager;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 长辉设备连接管理器
 * 
 * <p>管理长辉TCP设备的连接状态，实现设备ID与测站编码的双向映射。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *     <li>设备连接的注册和注销</li>
 *     <li>设备ID与测站编码的双向映射</li>
 *     <li>心跳时间跟踪</li>
 *     <li>在线状态查询</li>
 * </ul>
 * 
 * <p>设计原则：</p>
 * <ul>
 *     <li>单一职责：只负责连接管理，不负责生命周期状态管理</li>
 *     <li>生命周期状态由 Plugin 层调用 DeviceLifecycleManager 的原子方法处理</li>
 * </ul>
 * 
 * <p>线程安全：所有 Map 使用 ConcurrentHashMap，支持并发访问。</p>
 * 
 * @author IoT Gateway Team
 * @see ConnectionManager
 * @see ChanghuiPlugin
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "changhui", havingValue = "true", matchIfMissing = true)
public class ChanghuiConnectionManager implements ConnectionManager<Channel> {

    /**
     * 日志前缀
     */
    private static final String LOG_PREFIX = "[ChanghuiConnectionManager]";

    /**
     * 设备ID -> 连接对象
     */
    private final Map<Long, Channel> deviceConnections = new ConcurrentHashMap<>();

    /**
     * 测站编码 -> 设备ID
     * <p>
     * 测站编码是长辉设备的唯一标识，10字节十六进制字符串。
     * </p>
     */
    private final Map<String, Long> stationCodeDeviceMap = new ConcurrentHashMap<>();

    /**
     * 设备ID -> 连接信息
     */
    private final Map<Long, ChanghuiConnectionInfo> connectionInfoMap = new ConcurrentHashMap<>();

    @Override
    public void register(Long deviceId, String stationCode, Channel connection) {
        if (deviceId == null || stationCode == null || connection == null) {
            log.warn("{} 注册连接失败: 参数不能为空, deviceId={}, stationCode={}, connection={}",
                    LOG_PREFIX, deviceId, stationCode, connection);
            return;
        }

        // 移除旧连接（如果存在）
        Channel oldConnection = deviceConnections.get(deviceId);
        if (oldConnection != null && !oldConnection.equals(connection)) {
            try {
                oldConnection.close();
                log.info("{} 关闭旧连接: deviceId={}", LOG_PREFIX, deviceId);
            } catch (Exception e) {
                log.error("{} 关闭旧连接失败: deviceId={}", LOG_PREFIX, deviceId, e);
            }
        }

        // 注册新连接
        deviceConnections.put(deviceId, connection);
        stationCodeDeviceMap.put(stationCode, deviceId);

        // 创建连接信息
        ChanghuiConnectionInfo info = new ChanghuiConnectionInfo();
        info.setDeviceId(deviceId);
        info.setStationCode(stationCode);
        info.setRemoteAddress(connection.remoteAddress() != null ? connection.remoteAddress().toString() : null);
        info.setConnectTime(System.currentTimeMillis());
        info.setLastHeartbeatTime(System.currentTimeMillis());
        connectionInfoMap.put(deviceId, info);

        log.info("{} 连接注册成功: deviceId={}, stationCode={}, remoteAddress={}",
                LOG_PREFIX, deviceId, stationCode, info.getRemoteAddress());
        
        // 诊断日志：确认注册状态
        log.info("{} 注册后状态: deviceId={}, connectionInMap={}, isActive={}, onlineCount={}", 
                LOG_PREFIX, deviceId, 
                deviceConnections.containsKey(deviceId),
                deviceConnections.get(deviceId) != null ? deviceConnections.get(deviceId).isActive() : "N/A",
                getOnlineCount());
        
        // 注意：生命周期状态由 ChanghuiPlugin 调用 DeviceLifecycleManager.onDeviceConnected() 原子方法处理
    }

    @Override
    public void unregister(Long deviceId) {
        if (deviceId == null) {
            return;
        }

        // 移除连接
        Channel connection = deviceConnections.remove(deviceId);
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                log.error("{} 关闭连接失败: deviceId={}", LOG_PREFIX, deviceId, e);
            }
        }

        // 移除测站编码映射
        ChanghuiConnectionInfo info = connectionInfoMap.remove(deviceId);
        if (info != null && info.getStationCode() != null) {
            stationCodeDeviceMap.remove(info.getStationCode());
        }

        log.info("{} 连接注销成功: deviceId={}", LOG_PREFIX, deviceId);
        
        // 注意：生命周期状态由 ChanghuiPlugin 调用 DeviceLifecycleManager.onDeviceDisconnected() 原子方法处理
    }

    @Override
    public Channel getConnection(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        return deviceConnections.get(deviceId);
    }

    @Override
    public Long getDeviceIdByIdentifier(String stationCode) {
        if (stationCode == null || stationCode.isEmpty()) {
            return null;
        }
        return stationCodeDeviceMap.get(stationCode);
    }

    @Override
    public boolean isOnline(Long deviceId) {
        if (deviceId == null) {
            return false;
        }
        Channel connection = deviceConnections.get(deviceId);
        return connection != null && connection.isActive();
    }

    @Override
    public void updateHeartbeat(Long deviceId) {
        if (deviceId == null) {
            return;
        }
        ChanghuiConnectionInfo info = connectionInfoMap.get(deviceId);
        if (info != null) {
            info.setLastHeartbeatTime(System.currentTimeMillis());
            log.trace("{} 更新心跳时间: deviceId={}", LOG_PREFIX, deviceId);
        }
    }

    @Override
    public Long getLastHeartbeatTime(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        ChanghuiConnectionInfo info = connectionInfoMap.get(deviceId);
        return info != null ? info.getLastHeartbeatTime() : null;
    }

    @Override
    public int getOnlineCount() {
        return (int) deviceConnections.values().stream()
                .filter(Channel::isActive)
                .count();
    }

    @Override
    public Collection<Long> getOnlineDeviceIds() {
        return deviceConnections.keySet();
    }

    @Override
    public void closeAll() {
        log.info("{} 关闭所有连接, 当前连接数: {}", LOG_PREFIX, deviceConnections.size());
        
        // 注意：生命周期状态由 ChanghuiPlugin 在调用 closeAll() 前/后处理
        
        deviceConnections.values().forEach(connection -> {
            try {
                connection.close();
            } catch (Exception e) {
                log.error("{} 关闭连接失败", LOG_PREFIX, e);
            }
        });
        
        deviceConnections.clear();
        stationCodeDeviceMap.clear();
        connectionInfoMap.clear();
        
        log.info("{} 所有连接已关闭", LOG_PREFIX);
    }

    @Override
    public boolean isConnectionValid(Long deviceId) {
        if (deviceId == null) {
            return false;
        }
        Channel connection = deviceConnections.get(deviceId);
        return connection != null && connection.isActive() && connection.isWritable();
    }

    // ==================== 扩展方法 ====================

    /**
     * 获取连接信息
     *
     * @param deviceId 设备ID
     * @return 连接信息，如果不存在则返回 null
     */
    public ChanghuiConnectionInfo getConnectionInfo(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        return connectionInfoMap.get(deviceId);
    }

    /**
     * 获取测站编码
     *
     * @param deviceId 设备ID
     * @return 测站编码，如果不存在则返回 null
     */
    public String getStationCode(Long deviceId) {
        ChanghuiConnectionInfo info = connectionInfoMap.get(deviceId);
        return info != null ? info.getStationCode() : null;
    }

    /**
     * 检查测站编码是否已注册
     *
     * @param stationCode 测站编码
     * @return 是否已注册
     */
    public boolean isStationCodeRegistered(String stationCode) {
        return stationCode != null && stationCodeDeviceMap.containsKey(stationCode);
    }

    /**
     * 根据测站编码获取连接
     *
     * @param stationCode 测站编码
     * @return 连接对象，如果不存在则返回 null
     */
    public Channel getConnectionByStationCode(String stationCode) {
        Long deviceId = getDeviceIdByIdentifier(stationCode);
        return deviceId != null ? getConnection(deviceId) : null;
    }

    /**
     * 更新协议版本
     *
     * @param deviceId        设备ID
     * @param protocolVersion 协议版本
     */
    public void updateProtocolVersion(Long deviceId, String protocolVersion) {
        ChanghuiConnectionInfo info = connectionInfoMap.get(deviceId);
        if (info != null) {
            info.setProtocolVersion(protocolVersion);
        }
    }

    // ==================== 内部类 ====================

    /**
     * 长辉设备连接信息
     * <p>
     * 存储长辉设备连接的详细信息。
     * </p>
     */
    @Data
    public static class ChanghuiConnectionInfo {
        /**
         * 设备ID
         */
        private Long deviceId;

        /**
         * 测站编码（10字节十六进制字符串）
         */
        private String stationCode;

        /**
         * 远程地址
         */
        private String remoteAddress;

        /**
         * 远程端口
         */
        private int remotePort;

        /**
         * 连接建立时间（毫秒时间戳）
         */
        private Long connectTime;

        /**
         * 最后心跳时间（毫秒时间戳）
         */
        private Long lastHeartbeatTime;

        /**
         * 协议版本
         */
        private String protocolVersion;

        /**
         * 设备固件版本
         */
        private String firmwareVersion;

        /**
         * 升级状态
         */
        private String upgradeStatus;

        /**
         * 升级进度（0-100）
         */
        private Integer upgradeProgress;
    }
}
