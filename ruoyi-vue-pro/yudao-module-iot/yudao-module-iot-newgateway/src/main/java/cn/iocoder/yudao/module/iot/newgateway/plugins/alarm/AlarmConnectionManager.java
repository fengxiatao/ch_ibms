package cn.iocoder.yudao.module.iot.newgateway.plugins.alarm;

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
 * 报警主机连接管理器
 * 
 * <p>管理报警主机设备的连接状态，实现设备ID与账号的双向映射。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *     <li>设备连接的注册和注销</li>
 *     <li>设备ID与账号的双向映射</li>
 *     <li>心跳时间跟踪</li>
 *     <li>在线状态查询</li>
 *     <li>设备密码管理</li>
 * </ul>
 * 
 * <p>线程安全：所有 Map 使用 ConcurrentHashMap，支持并发访问。</p>
 * 
 * @author IoT Gateway Team
 * @see ConnectionManager
 * @see AlarmPlugin
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "alarm", havingValue = "true", matchIfMissing = true)
public class AlarmConnectionManager implements ConnectionManager<Channel> {

    /**
     * 日志前缀
     */
    private static final String LOG_PREFIX = "[AlarmConnectionManager]";

    /**
     * 设备ID -> 连接对象
     */
    private final Map<Long, Channel> deviceConnections = new ConcurrentHashMap<>();

    /**
     * 账号 -> 设备ID
     * <p>
     * 账号是报警主机的唯一标识。
     * </p>
     */
    private final Map<String, Long> accountDeviceMap = new ConcurrentHashMap<>();

    /**
     * 设备ID -> 连接信息
     */
    private final Map<Long, AlarmConnectionInfo> connectionInfoMap = new ConcurrentHashMap<>();

    @Override
    public void register(Long deviceId, String account, Channel connection) {
        if (deviceId == null || account == null || connection == null) {
            log.warn("{} 注册连接失败: 参数不能为空, deviceId={}, account={}, connection={}",
                    LOG_PREFIX, deviceId, account, connection);
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
        accountDeviceMap.put(account, deviceId);

        // 创建连接信息
        AlarmConnectionInfo info = new AlarmConnectionInfo();
        info.setDeviceId(deviceId);
        info.setAccount(account);
        info.setRemoteAddress(connection.remoteAddress() != null ? connection.remoteAddress().toString() : null);
        info.setConnectTime(System.currentTimeMillis());
        info.setLastHeartbeatTime(System.currentTimeMillis());
        connectionInfoMap.put(deviceId, info);

        log.info("{} 连接注册成功: deviceId={}, account={}, remoteAddress={}",
                LOG_PREFIX, deviceId, account, info.getRemoteAddress());
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

        // 移除账号映射
        AlarmConnectionInfo info = connectionInfoMap.remove(deviceId);
        if (info != null && info.getAccount() != null) {
            accountDeviceMap.remove(info.getAccount());
        }

        log.info("{} 连接注销成功: deviceId={}", LOG_PREFIX, deviceId);
    }

    @Override
    public Channel getConnection(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        return deviceConnections.get(deviceId);
    }

    @Override
    public Long getDeviceIdByIdentifier(String account) {
        if (account == null || account.isEmpty()) {
            return null;
        }
        return accountDeviceMap.get(account);
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
        AlarmConnectionInfo info = connectionInfoMap.get(deviceId);
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
        AlarmConnectionInfo info = connectionInfoMap.get(deviceId);
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
        
        deviceConnections.values().forEach(connection -> {
            try {
                connection.close();
            } catch (Exception e) {
                log.error("{} 关闭连接失败", LOG_PREFIX, e);
            }
        });
        
        deviceConnections.clear();
        accountDeviceMap.clear();
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
    public AlarmConnectionInfo getConnectionInfo(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        return connectionInfoMap.get(deviceId);
    }

    /**
     * 获取设备账号
     *
     * @param deviceId 设备ID
     * @return 设备账号，如果不存在则返回 null
     */
    public String getAccount(Long deviceId) {
        AlarmConnectionInfo info = connectionInfoMap.get(deviceId);
        return info != null ? info.getAccount() : null;
    }

    /**
     * 根据设备ID获取账号
     *
     * @param deviceId 设备ID
     * @return 账号，如果不存在则返回 null
     */
    public String getAccountByDeviceId(Long deviceId) {
        return getAccount(deviceId);
    }

    /**
     * 检查账号是否已注册
     *
     * @param account 账号
     * @return 是否已注册
     */
    public boolean isAccountRegistered(String account) {
        return account != null && accountDeviceMap.containsKey(account);
    }

    /**
     * 根据账号获取连接
     *
     * @param account 账号
     * @return 连接对象，如果不存在则返回 null
     */
    public Channel getConnectionByAccount(String account) {
        Long deviceId = getDeviceIdByIdentifier(account);
        return deviceId != null ? getConnection(deviceId) : null;
    }

    /**
     * 设置设备密码
     *
     * @param deviceId 设备ID
     * @param password 密码
     */
    public void setPassword(Long deviceId, String password) {
        AlarmConnectionInfo info = connectionInfoMap.get(deviceId);
        if (info != null) {
            info.setPassword(password);
        }
    }

    /**
     * 获取设备密码
     *
     * @param deviceId 设备ID
     * @return 密码，如果不存在则返回 null
     */
    public String getPassword(Long deviceId) {
        AlarmConnectionInfo info = connectionInfoMap.get(deviceId);
        return info != null ? info.getPassword() : null;
    }

    /**
     * 根据账号获取密码
     *
     * @param account 账号
     * @return 密码，如果不存在则返回 null
     */
    public String getPasswordByAccount(String account) {
        Long deviceId = getDeviceIdByIdentifier(account);
        return deviceId != null ? getPassword(deviceId) : null;
    }

    /**
     * 更新设备状态
     *
     * @param deviceId 设备ID
     * @param status   状态
     */
    public void updateDeviceStatus(Long deviceId, String status) {
        AlarmConnectionInfo info = connectionInfoMap.get(deviceId);
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
        AlarmConnectionInfo info = connectionInfoMap.get(deviceId);
        return info != null ? info.getDeviceStatus() : null;
    }

    // ==================== 内部类 ====================

    /**
     * 报警主机连接信息
     * <p>
     * 存储报警主机设备连接的详细信息。
     * </p>
     */
    @Data
    public static class AlarmConnectionInfo {
        /**
         * 设备ID
         */
        private Long deviceId;

        /**
         * 设备账号
         */
        private String account;

        /**
         * 设备密码
         */
        private String password;

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
         * 设备状态（ARMED/DISARMED/ALARM 等）
         */
        private String deviceStatus;

        /**
         * 分区数量
         */
        private Integer partitionCount;

        /**
         * 防区数量
         */
        private Integer zoneCount;

        /**
         * 协议版本
         */
        private String protocolVersion;
    }
}
