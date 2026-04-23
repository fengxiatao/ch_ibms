package cn.iocoder.yudao.module.iot.newgateway.plugins.template;

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
 * 模板插件连接管理器
 * 
 * <p>管理设备连接状态，每个插件独立实现。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>设备连接的注册和注销</li>
 *   <li>设备ID与标识符的双向映射</li>
 *   <li>心跳时间跟踪</li>
 *   <li>在线状态查询</li>
 * </ul>
 * 
 * <p>开发新插件时，请修改：</p>
 * <ul>
 *   <li>类名：将 TemplateConnectionManager 改为 {YourDevice}ConnectionManager</li>
 *   <li>泛型参数：根据连接类型修改（如 Channel、NetSocket、Long 等）</li>
 *   <li>ConnectionInfo：根据需要添加设备特定的连接信息字段</li>
 * </ul>
 * 
 * <p>线程安全：所有 Map 使用 ConcurrentHashMap，支持并发访问。</p>
 * 
 * @author IoT Gateway Team
 * @see ConnectionManager
 * @see TemplatePlugin
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "template", havingValue = "true")
public class TemplateConnectionManager implements ConnectionManager<Channel> {

    /**
     * 日志前缀
     */
    private static final String LOG_PREFIX = "[TemplateConnectionManager]";

    /**
     * 设备ID -> 连接对象
     */
    private final Map<Long, Channel> deviceConnections = new ConcurrentHashMap<>();

    /**
     * 设备标识符 -> 设备ID
     * <p>
     * 标识符可以是账号、测站编码、序列号等，根据设备协议确定。
     * </p>
     */
    private final Map<String, Long> identifierDeviceMap = new ConcurrentHashMap<>();

    /**
     * 设备ID -> 连接信息
     */
    private final Map<Long, ConnectionInfo> connectionInfoMap = new ConcurrentHashMap<>();

    @Override
    public void register(Long deviceId, String identifier, Channel connection) {
        if (deviceId == null || identifier == null || connection == null) {
            log.warn("{} 注册连接失败: 参数不能为空, deviceId={}, identifier={}, connection={}",
                    LOG_PREFIX, deviceId, identifier, connection);
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
        identifierDeviceMap.put(identifier, deviceId);

        // 创建连接信息
        ConnectionInfo info = new ConnectionInfo();
        info.setDeviceId(deviceId);
        info.setIdentifier(identifier);
        info.setConnectTime(System.currentTimeMillis());
        info.setLastHeartbeatTime(System.currentTimeMillis());
        connectionInfoMap.put(deviceId, info);

        log.info("{} 连接注册成功: deviceId={}, identifier={}", LOG_PREFIX, deviceId, identifier);
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

        // 移除标识符映射
        ConnectionInfo info = connectionInfoMap.remove(deviceId);
        if (info != null && info.getIdentifier() != null) {
            identifierDeviceMap.remove(info.getIdentifier());
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
        Channel connection = deviceConnections.get(deviceId);
        return connection != null && connection.isActive();
    }

    @Override
    public void updateHeartbeat(Long deviceId) {
        if (deviceId == null) {
            return;
        }
        ConnectionInfo info = connectionInfoMap.get(deviceId);
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
        ConnectionInfo info = connectionInfoMap.get(deviceId);
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
        identifierDeviceMap.clear();
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
    public ConnectionInfo getConnectionInfo(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        return connectionInfoMap.get(deviceId);
    }

    /**
     * 获取设备标识符
     *
     * @param deviceId 设备ID
     * @return 设备标识符，如果不存在则返回 null
     */
    public String getIdentifier(Long deviceId) {
        ConnectionInfo info = connectionInfoMap.get(deviceId);
        return info != null ? info.getIdentifier() : null;
    }

    /**
     * 检查标识符是否已注册
     *
     * @param identifier 设备标识符
     * @return 是否已注册
     */
    public boolean isIdentifierRegistered(String identifier) {
        return identifier != null && identifierDeviceMap.containsKey(identifier);
    }

    // ==================== 内部类 ====================

    /**
     * 连接信息
     * <p>
     * 存储设备连接的详细信息，可根据需要扩展字段。
     * </p>
     */
    @Data
    public static class ConnectionInfo {
        /**
         * 设备ID
         */
        private Long deviceId;

        /**
         * 设备标识符（如账号、测站编码等）
         */
        private String identifier;

        /**
         * 连接建立时间（毫秒时间戳）
         */
        private Long connectTime;

        /**
         * 最后心跳时间（毫秒时间戳）
         */
        private Long lastHeartbeatTime;

        /**
         * 远程地址
         */
        private String remoteAddress;

        /**
         * 协议版本
         */
        private String protocolVersion;

        // 可根据需要添加更多字段，如：
        // - 设备固件版本
        // - 信号强度
        // - 电池电量
        // - 自定义属性 Map
    }
}
