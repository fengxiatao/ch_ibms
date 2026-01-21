package cn.iocoder.yudao.module.iot.newgateway.plugins.template;

import cn.iocoder.yudao.module.iot.core.enums.ConnectionMode;
import cn.iocoder.yudao.module.iot.core.gateway.dto.DeviceInfo;
import cn.iocoder.yudao.module.iot.newgateway.core.annotation.DevicePlugin;
import cn.iocoder.yudao.module.iot.newgateway.core.handler.PassiveDeviceHandler;
import cn.iocoder.yudao.module.iot.newgateway.core.lifecycle.DeviceLifecycleManager;
import cn.iocoder.yudao.module.iot.newgateway.core.message.GatewayMessagePublisher;
import cn.iocoder.yudao.module.iot.newgateway.core.model.CommandResult;
import cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceCommand;
import cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceConnectionInfo;
import cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceStatus;
import cn.iocoder.yudao.module.iot.newgateway.core.model.HeartbeatData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.Map;

/**
 * 模板插件 - 设备插件参考实现
 * 
 * <p>这是一个完整的设备插件模板，展示了如何实现 {@link PassiveDeviceHandler} 接口。
 * 开发新的被动连接设备插件时，可以复制此模板并修改。</p>
 * 
 * <h2>开发步骤</h2>
 * <ol>
 *   <li>复制 template 目录到 plugins/{your_device_type}/</li>
 *   <li>重命名所有类，将 Template 替换为你的设备类型名称</li>
 *   <li>修改 @DevicePlugin 注解中的属性（id, name, deviceType, vendor, description）</li>
 *   <li>修改 TemplateConfig 中的配置前缀和属性</li>
 *   <li>实现 {@link #parseDeviceIdentifier(ByteBuf)} 方法，解析设备标识</li>
 *   <li>实现 {@link #executeCommand(Long, DeviceCommand)} 方法，处理设备命令</li>
 *   <li>根据需要实现协议编解码器（可选）</li>
 *   <li>在 application.yaml 中配置插件参数</li>
 * </ol>
 * 
 * <h2>注意事项</h2>
 * <ul>
 *   <li>此模板默认禁用（enabledByDefault = false），需要在配置中显式启用</li>
 *   <li>每个插件应使用不同的端口，避免冲突</li>
 *   <li>日志应使用统一的前缀格式：[{PluginName}]</li>
 *   <li>所有方法应处理异常，避免影响其他插件</li>
 * </ul>
 * 
 * <h2>配置示例</h2>
 * <pre>
 * iot:
 *   newgateway:
 *     plugins:
 *       template:
 *         enabled: true
 *         port: 9800
 *         heartbeat-timeout: 60000
 * </pre>
 * 
 * @author IoT Gateway Team
 * @see PassiveDeviceHandler
 * @see TemplateConnectionManager
 * @see TemplateConfig
 */
@DevicePlugin(
    id = "template",
    name = "模板插件",
    deviceType = "TEMPLATE",
    vendor = "Generic",
    description = "设备插件参考实现模板，开发新插件时可复制此模板",
    enabledByDefault = false  // 模板默认禁用
)
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "template", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
public class TemplatePlugin implements PassiveDeviceHandler {

    /**
     * 日志前缀
     */
    private static final String LOG_PREFIX = "[TemplatePlugin]";

    /**
     * 设备类型常量
     */
    private static final String DEVICE_TYPE = "TEMPLATE";

    /**
     * 厂商常量
     */
    private static final String VENDOR = "Generic";

    // ==================== 依赖注入 ====================

    /**
     * 连接管理器
     */
    private final TemplateConnectionManager connectionManager;

    /**
     * 生命周期管理器
     */
    private final DeviceLifecycleManager lifecycleManager;

    /**
     * 消息发布器
     */
    private final GatewayMessagePublisher messagePublisher;

    /**
     * 插件配置
     */
    private final TemplateConfig config;

    // ==================== DeviceHandler 接口实现 ====================

    @Override
    public String getDeviceType() {
        return DEVICE_TYPE;
    }

    @Override
    public String getVendor() {
        return VENDOR;
    }

    @Override
    public boolean supports(DeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            return false;
        }
        // 根据设备类型判断是否支持
        // 可以根据需要添加更多判断条件，如厂商、协议版本等
        return DEVICE_TYPE.equalsIgnoreCase(deviceInfo.getDeviceType());
    }

    @Override
    public CommandResult executeCommand(Long deviceId, DeviceCommand command) {
        if (deviceId == null || command == null) {
            return CommandResult.failure("参数不能为空");
        }

        String commandType = command.getCommandType();
        log.info("{} 执行命令: deviceId={}, commandType={}", LOG_PREFIX, deviceId, commandType);

        try {
            // 检查设备是否在线
            if (!connectionManager.isOnline(deviceId)) {
                return CommandResult.failure("设备未连接");
            }

            // 根据命令类型分发处理
            // TODO: 根据实际设备协议实现命令处理
            switch (commandType) {
                case "QUERY_STATUS":
                    return executeQueryStatus(deviceId);
                case "EXAMPLE_COMMAND":
                    return executeExampleCommand(deviceId, command);
                default:
                    return CommandResult.failure("不支持的命令类型: " + commandType);
            }
        } catch (Exception e) {
            log.error("{} 执行命令失败: deviceId={}, commandType={}", LOG_PREFIX, deviceId, commandType, e);
            return CommandResult.failure("命令执行异常: " + e.getMessage());
        }
    }

    @Override
    public DeviceStatus queryStatus(Long deviceId) {
        if (deviceId == null) {
            return DeviceStatus.offline(null);
        }

        boolean online = connectionManager.isOnline(deviceId);
        if (online) {
            Long lastHeartbeat = connectionManager.getLastHeartbeatTime(deviceId);
            return DeviceStatus.builder()
                    .deviceId(deviceId)
                    .online(true)
                    .lastHeartbeatTime(lastHeartbeat)
                    .lastActiveTime(lastHeartbeat)
                    .build();
        } else {
            return DeviceStatus.offline(deviceId);
        }
    }

    // ==================== PassiveDeviceHandler 接口实现 ====================

    @Override
    public int getListenPort() {
        return config.getPort();
    }

    @Override
    public long getHeartbeatTimeout() {
        return config.getHeartbeatTimeout();
    }

    @Override
    public long getConnectionTimeout() {
        return config.getConnectionTimeout();
    }

    @Override
    public void onConnect(ChannelHandlerContext ctx, String deviceIdentifier) {
        log.info("{} 设备连接: identifier={}, remoteAddress={}",
                LOG_PREFIX, deviceIdentifier, ctx.channel().remoteAddress());

        try {
            // TODO: 根据设备标识符查找设备ID
            // 实际实现中，应该调用设备服务查询设备ID
            Long deviceId = lookupDeviceId(deviceIdentifier);
            
            if (deviceId == null) {
                log.warn("{} 未找到设备: identifier={}", LOG_PREFIX, deviceIdentifier);
                // 可以选择关闭连接或等待后续消息
                return;
            }

            // 1. 注册连接（ConnectionManager 只负责连接管理）
            connectionManager.register(deviceId, deviceIdentifier, ctx.channel());
            
            // 2. 构建设备连接信息
            DeviceConnectionInfo connectionInfo = DeviceConnectionInfo.builder()
                    .deviceId(deviceId)
                    .deviceType("template")  // TODO: 替换为实际设备类型
                    .vendor("Generic")       // TODO: 替换为实际厂商
                    .connectionMode(ConnectionMode.PASSIVE)
                    .build();
            
            // 3. 原子地更新设备生命周期状态（注册 -> 激活 -> 上线）
            lifecycleManager.onDeviceConnected(deviceId, connectionInfo);

            log.info("{} 设备连接成功: deviceId={}, identifier={}", LOG_PREFIX, deviceId, deviceIdentifier);
        } catch (Exception e) {
            log.error("{} 处理设备连接失败: identifier={}", LOG_PREFIX, deviceIdentifier, e);
        }
    }

    @Override
    public void onHeartbeat(Long deviceId, HeartbeatData data) {
        if (deviceId == null) {
            return;
        }

        log.debug("{} 收到心跳: deviceId={}", LOG_PREFIX, deviceId);

        try {
            // 更新心跳时间
            connectionManager.updateHeartbeat(deviceId);
            lifecycleManager.updateLastSeen(deviceId);

            // 如果设备之前不在线，使用原子方法重新上线
            if (!lifecycleManager.isOnline(deviceId)) {
                DeviceConnectionInfo connectionInfo = DeviceConnectionInfo.builder()
                        .deviceId(deviceId)
                        .deviceType("template")  // TODO: 替换为实际设备类型
                        .connectionMode(ConnectionMode.PASSIVE)
                        .build();
                lifecycleManager.onDeviceConnected(deviceId, connectionInfo);
                log.info("{} 设备心跳触发重新上线: deviceId={}", LOG_PREFIX, deviceId);
            }
        } catch (Exception e) {
            log.error("{} 处理心跳失败: deviceId={}", LOG_PREFIX, deviceId, e);
        }
    }

    @Override
    public void onDisconnect(Long deviceId) {
        if (deviceId == null) {
            return;
        }

        log.info("{} 设备断开: deviceId={}", LOG_PREFIX, deviceId);

        try {
            // 1. 原子地更新设备生命周期状态为离线
            lifecycleManager.onDeviceDisconnected(deviceId, "连接断开");
            
            // 2. 注销连接（ConnectionManager 只负责连接管理）
            connectionManager.unregister(deviceId);
        } catch (Exception e) {
            log.error("{} 处理设备断开失败: deviceId={}", LOG_PREFIX, deviceId, e);
        }
    }

    @Override
    public String parseDeviceIdentifier(ByteBuf data) {
        if (data == null || data.readableBytes() == 0) {
            return null;
        }

        try {
            // TODO: 根据实际协议解析设备标识符
            // 示例：假设设备标识符在数据的前 10 个字节
            // 实际实现需要根据设备协议进行解析
            
            if (data.readableBytes() < 10) {
                return null;
            }

            byte[] identifierBytes = new byte[10];
            data.getBytes(data.readerIndex(), identifierBytes);
            
            // 转换为字符串（根据实际协议可能需要不同的转换方式）
            StringBuilder sb = new StringBuilder();
            for (byte b : identifierBytes) {
                sb.append(String.format("%02X", b));
            }
            
            return sb.toString();
        } catch (Exception e) {
            log.error("{} 解析设备标识符失败", LOG_PREFIX, e);
            return null;
        }
    }

    @Override
    public void onDataReceived(Long deviceId, ByteBuf data) {
        if (deviceId == null || data == null) {
            return;
        }

        log.debug("{} 收到数据: deviceId={}, length={}", LOG_PREFIX, deviceId, data.readableBytes());

        try {
            // TODO: 根据实际协议处理接收到的数据
            // 示例：解析消息类型并分发处理
            
            // 更新最后活跃时间
            connectionManager.updateHeartbeat(deviceId);
            lifecycleManager.updateLastSeen(deviceId);
            
            // 解析并处理数据...
        } catch (Exception e) {
            log.error("{} 处理数据失败: deviceId={}", LOG_PREFIX, deviceId, e);
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 根据设备标识符查找设备ID
     * <p>
     * TODO: 实际实现中应该调用设备服务查询
     * </p>
     *
     * @param identifier 设备标识符
     * @return 设备ID，如果未找到则返回 null
     */
    private Long lookupDeviceId(String identifier) {
        // TODO: 调用设备服务查询设备ID
        // 示例：return deviceService.getDeviceIdByIdentifier(identifier);
        
        // 临时实现：从连接管理器查找
        return connectionManager.getDeviceIdByIdentifier(identifier);
    }

    /**
     * 执行状态查询命令
     *
     * @param deviceId 设备ID
     * @return 命令结果
     */
    private CommandResult executeQueryStatus(Long deviceId) {
        DeviceStatus status = queryStatus(deviceId);
        return CommandResult.success(Map.of(
                "online", status.isOnline(),
                "lastHeartbeatTime", status.getLastHeartbeatTime() != null ? status.getLastHeartbeatTime() : 0
        ));
    }

    /**
     * 执行示例命令
     * <p>
     * TODO: 根据实际设备协议实现
     * </p>
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
     */
    private CommandResult executeExampleCommand(Long deviceId, DeviceCommand command) {
        // 获取连接
        Channel channel = connectionManager.getConnection(deviceId);
        if (channel == null || !channel.isActive()) {
            return CommandResult.failure("设备连接不可用");
        }

        // TODO: 构建命令帧并发送
        // 示例：
        // byte[] frame = buildCommandFrame(command);
        // channel.writeAndFlush(Unpooled.wrappedBuffer(frame));

        return CommandResult.success("命令已发送");
    }

    // ==================== 生命周期方法（可选） ====================

    /**
     * 插件启动时调用
     * <p>
     * 可以在此方法中进行初始化操作，如启动 TCP 服务器。
     * </p>
     */
    public void onStart() {
        log.info("{} 插件启动: port={}", LOG_PREFIX, config.getPort());
        // TODO: 启动 TCP 服务器
    }

    /**
     * 插件停止时调用
     * <p>
     * 可以在此方法中进行清理操作，如关闭所有连接。
     * </p>
     */
    public void onStop() {
        log.info("{} 插件停止", LOG_PREFIX);
        connectionManager.closeAll();
    }

    /**
     * 健康检查
     *
     * @return 是否健康
     */
    public boolean onHealthCheck() {
        // 检查插件是否正常运行
        return config.isEnabled();
    }
}
