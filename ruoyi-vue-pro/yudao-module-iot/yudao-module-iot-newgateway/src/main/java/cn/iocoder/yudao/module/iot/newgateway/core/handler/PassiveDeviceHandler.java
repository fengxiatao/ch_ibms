package cn.iocoder.yudao.module.iot.newgateway.core.handler;

import cn.iocoder.yudao.module.iot.core.enums.ConnectionMode;
import cn.iocoder.yudao.module.iot.newgateway.core.model.HeartbeatData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 被动连接设备处理器接口
 * <p>
 * 用于设备主动连接平台的场景（如 TCP 心跳）。
 * 继承 {@link DeviceHandler}，增加连接事件处理和协议解析方法。
 * </p>
 *
 * <p>适用设备类型：</p>
 * <ul>
 *     <li>报警主机（PS600 OPC 协议）</li>
 *     <li>长辉 TCP 模拟设备（自定义协议）</li>
 *     <li>其他通过 TCP 心跳连接的设备</li>
 * </ul>
 *
 * <p>工作流程：</p>
 * <ol>
 *     <li>设备连接到平台指定端口</li>
 *     <li>平台调用 {@link #parseDeviceIdentifier(ByteBuf)} 解析设备标识</li>
 *     <li>平台调用 {@link #onConnect(ChannelHandlerContext, String)} 处理连接事件</li>
 *     <li>设备定期发送心跳，平台调用 {@link #onHeartbeat(Long, HeartbeatData)} 处理</li>
 *     <li>设备断开时，平台调用 {@link #onDisconnect(Long)} 处理</li>
 * </ol>
 *
 * <p>实现示例：</p>
 * <pre>
 * {@code
 * @DevicePlugin(id = "alarm", name = "报警主机", deviceType = "ALARM")
 * public class AlarmPlugin implements PassiveDeviceHandler {
 *
 *     @Override
 *     public int getListenPort() {
 *         return 9500;
 *     }
 *
 *     @Override
 *     public String parseDeviceIdentifier(ByteBuf data) {
 *         // 解析协议中的设备账号
 *         return protocolCodec.parseAccount(data);
 *     }
 *
 *     @Override
 *     public void onConnect(ChannelHandlerContext ctx, String deviceIdentifier) {
 *         Long deviceId = deviceService.getDeviceIdByAccount(deviceIdentifier);
 *         connectionManager.register(deviceId, deviceIdentifier, ctx.channel());
 *         lifecycleManager.onDeviceOnline(deviceId);
 *     }
 *
 *     @Override
 *     public void onHeartbeat(Long deviceId, HeartbeatData data) {
 *         connectionManager.updateHeartbeat(deviceId);
 *         lifecycleManager.updateLastSeen(deviceId);
 *     }
 *
 *     @Override
 *     public void onDisconnect(Long deviceId) {
 *         connectionManager.unregister(deviceId);
 *         lifecycleManager.onDeviceOffline(deviceId);
 *     }
 * }
 * }
 * </pre>
 *
 * @author IoT Gateway Team
 * @see DeviceHandler
 * @see ActiveDeviceHandler
 */
public interface PassiveDeviceHandler extends DeviceHandler {

    /**
     * 获取连接模式
     * <p>
     * 被动连接设备返回 {@link ConnectionMode#PASSIVE}。
     * </p>
     *
     * @return 连接模式（PASSIVE）
     */
    default ConnectionMode getConnectionMode() {
        return ConnectionMode.PASSIVE;
    }

    /**
     * 获取监听端口
     * <p>
     * 返回此插件监听的 TCP 端口号。
     * 每个被动连接插件应监听不同的端口。
     * </p>
     *
     * @return TCP 监听端口
     */
    int getListenPort();

    /**
     * 设备连接事件
     * <p>
     * 当设备连接到平台时调用。应在此方法中：
     * <ul>
     *     <li>根据设备标识查找设备ID</li>
     *     <li>注册连接到 ConnectionManager</li>
     *     <li>通知 LifecycleManager 设备上线</li>
     * </ul>
     * </p>
     *
     * @param ctx              Netty 通道上下文
     * @param deviceIdentifier 设备标识（如账号、测站编码等）
     */
    void onConnect(ChannelHandlerContext ctx, String deviceIdentifier);

    /**
     * 心跳事件
     * <p>
     * 当收到设备心跳消息时调用。应在此方法中：
     * <ul>
     *     <li>更新 ConnectionManager 中的心跳时间</li>
     *     <li>更新 LifecycleManager 中的最后活动时间</li>
     *     <li>如果设备之前离线，触发上线事件</li>
     * </ul>
     * </p>
     *
     * @param deviceId 设备ID
     * @param data     心跳数据
     */
    void onHeartbeat(Long deviceId, HeartbeatData data);

    /**
     * 设备断开事件
     * <p>
     * 当设备断开连接时调用。应在此方法中：
     * <ul>
     *     <li>从 ConnectionManager 注销连接</li>
     *     <li>通知 LifecycleManager 设备离线</li>
     *     <li>清理相关资源</li>
     * </ul>
     * </p>
     *
     * @param deviceId 设备ID
     */
    void onDisconnect(Long deviceId);

    /**
     * 解析设备标识
     * <p>
     * 从接收到的数据中解析设备标识符。
     * 不同协议的设备标识位置和格式不同：
     * <ul>
     *     <li>报警主机：账号字段</li>
     *     <li>长辉设备：测站编码（10字节）</li>
     * </ul>
     * </p>
     *
     * @param data 接收到的数据
     * @return 设备标识符，如果无法解析则返回 null
     */
    String parseDeviceIdentifier(ByteBuf data);

    /**
     * 获取心跳超时时间（毫秒）
     * <p>
     * 如果超过此时间未收到心跳，则认为设备离线。
     * 默认 60 秒。
     * </p>
     *
     * @return 心跳超时时间（毫秒）
     */
    default long getHeartbeatTimeout() {
        return 60000L;
    }

    /**
     * 获取连接超时时间（毫秒）
     * <p>
     * 设备连接后，如果超过此时间未发送有效数据，则断开连接。
     * 默认 30 秒。
     * </p>
     *
     * @return 连接超时时间（毫秒）
     */
    default long getConnectionTimeout() {
        return 30000L;
    }

    /**
     * 处理接收到的数据
     * <p>
     * 处理从设备接收到的原始数据。默认实现为空，
     * 子类可以重写此方法实现自定义数据处理逻辑。
     * </p>
     *
     * @param deviceId 设备ID
     * @param data     接收到的数据
     */
    default void onDataReceived(Long deviceId, ByteBuf data) {
        // 默认空实现，子类可重写
    }
}
