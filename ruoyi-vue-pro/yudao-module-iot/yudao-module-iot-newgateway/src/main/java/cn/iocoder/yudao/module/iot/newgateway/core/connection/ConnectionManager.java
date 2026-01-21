package cn.iocoder.yudao.module.iot.newgateway.core.connection;

import java.util.Collection;

/**
 * 连接管理器接口
 * <p>
 * 每个插件独立实现，管理设备连接状态。
 * 泛型参数 T 表示连接类型，不同插件使用不同的连接类型：
 * <ul>
 *     <li>被动连接插件：NetSocket、Channel 等</li>
 *     <li>主动连接插件：Long（登录句柄）、SDK 连接对象等</li>
 * </ul>
 * </p>
 *
 * <p>设计原则：</p>
 * <ul>
 *     <li>每个插件独立实现，不共享连接管理器</li>
 *     <li>支持设备ID和设备标识符的双向映射</li>
 *     <li>线程安全，支持并发访问</li>
 * </ul>
 *
 * <p>实现示例：</p>
 * <pre>
 * {@code
 * @Component
 * public class AlarmConnectionManager implements ConnectionManager<Channel> {
 *
 *     private final Map<Long, Channel> deviceConnections = new ConcurrentHashMap<>();
 *     private final Map<String, Long> identifierDeviceMap = new ConcurrentHashMap<>();
 *     private final Map<Long, Long> lastHeartbeatMap = new ConcurrentHashMap<>();
 *
 *     @Override
 *     public void register(Long deviceId, String identifier, Channel connection) {
 *         deviceConnections.put(deviceId, connection);
 *         identifierDeviceMap.put(identifier, deviceId);
 *         lastHeartbeatMap.put(deviceId, System.currentTimeMillis());
 *     }
 *
 *     @Override
 *     public void unregister(Long deviceId) {
 *         Channel channel = deviceConnections.remove(deviceId);
 *         if (channel != null) {
 *             channel.close();
 *         }
 *         // 清理标识符映射...
 *     }
 *
 *     // ... 其他方法实现
 * }
 * }
 * </pre>
 *
 * @param <T> 连接类型
 * @author IoT Gateway Team
 */
public interface ConnectionManager<T> {

    /**
     * 注册连接
     * <p>
     * 将设备连接注册到管理器中。如果设备已有连接，应先关闭旧连接。
     * </p>
     *
     * @param deviceId   设备ID
     * @param identifier 设备标识符（如账号、测站编码等）
     * @param connection 连接对象
     */
    void register(Long deviceId, String identifier, T connection);

    /**
     * 注销连接
     * <p>
     * 从管理器中移除设备连接，并关闭连接、清理相关资源。
     * </p>
     *
     * @param deviceId 设备ID
     */
    void unregister(Long deviceId);

    /**
     * 获取连接
     * <p>
     * 根据设备ID获取连接对象。
     * </p>
     *
     * @param deviceId 设备ID
     * @return 连接对象，如果设备未连接则返回 null
     */
    T getConnection(Long deviceId);

    /**
     * 根据标识符获取设备ID
     * <p>
     * 根据设备标识符（如账号、测站编码）查找设备ID。
     * </p>
     *
     * @param identifier 设备标识符
     * @return 设备ID，如果未找到则返回 null
     */
    Long getDeviceIdByIdentifier(String identifier);

    /**
     * 检查设备是否在线
     * <p>
     * 判断设备是否有活跃的连接。
     * </p>
     *
     * @param deviceId 设备ID
     * @return 如果设备在线返回 true，否则返回 false
     */
    boolean isOnline(Long deviceId);

    /**
     * 更新心跳时间
     * <p>
     * 更新设备的最后心跳时间，用于心跳超时检测。
     * </p>
     *
     * @param deviceId 设备ID
     */
    void updateHeartbeat(Long deviceId);

    /**
     * 获取最后心跳时间
     * <p>
     * 获取设备的最后心跳时间戳。
     * </p>
     *
     * @param deviceId 设备ID
     * @return 最后心跳时间（毫秒时间戳），如果设备未连接则返回 null
     */
    Long getLastHeartbeatTime(Long deviceId);

    /**
     * 获取在线设备数量
     *
     * @return 在线设备数量
     */
    int getOnlineCount();

    /**
     * 获取所有在线设备ID
     *
     * @return 在线设备ID集合
     */
    Collection<Long> getOnlineDeviceIds();

    /**
     * 关闭所有连接
     * <p>
     * 关闭所有设备连接并清理资源。通常在插件停止时调用。
     * </p>
     */
    void closeAll();

    /**
     * 检查连接是否有效
     * <p>
     * 检查指定设备的连接是否仍然有效（未断开、未超时）。
     * 默认实现检查设备是否在线。
     * </p>
     *
     * @param deviceId 设备ID
     * @return 如果连接有效返回 true，否则返回 false
     */
    default boolean isConnectionValid(Long deviceId) {
        return isOnline(deviceId);
    }

    /**
     * 检查心跳是否超时
     * <p>
     * 检查设备的心跳是否已超时。
     * </p>
     *
     * @param deviceId 设备ID
     * @param timeout  超时时间（毫秒）
     * @return 如果心跳超时返回 true，否则返回 false
     */
    default boolean isHeartbeatTimeout(Long deviceId, long timeout) {
        Long lastHeartbeat = getLastHeartbeatTime(deviceId);
        if (lastHeartbeat == null) {
            return true;
        }
        return System.currentTimeMillis() - lastHeartbeat > timeout;
    }
}
