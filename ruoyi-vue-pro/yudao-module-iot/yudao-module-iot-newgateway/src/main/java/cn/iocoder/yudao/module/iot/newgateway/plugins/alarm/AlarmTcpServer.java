package cn.iocoder.yudao.module.iot.newgateway.plugins.alarm;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.newgateway.plugins.changhui.VertxNetSocketChannelAdapter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 报警主机TCP服务器
 * 
 * <p>基于 Vert.x 实现的 TCP 服务器，用于接收报警主机设备的连接和数据。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *     <li>监听指定端口，接受设备连接</li>
 *     <li>解析 PS600 OPC 协议消息</li>
 *     <li>分发数据到 AlarmPlugin 处理</li>
 *     <li>管理连接生命周期</li>
 * </ul>
 * 
 * <p>PS600 OPC 协议格式：</p>
 * <pre>
 * account,CMD[,PARAM1,PARAM2,...]\r\n
 * </pre>
 * 
 * @author IoT Gateway Team
 * @see AlarmPlugin
 * @see AlarmProtocolCodec
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "alarm", havingValue = "true", matchIfMissing = true)
public class AlarmTcpServer {

    /**
     * 日志前缀
     */
    private static final String LOG_PREFIX = "[AlarmTcpServer]";

    /**
     * 消息结束符
     */
    private static final String MESSAGE_TERMINATOR = "\r\n";

    /**
     * 单行消息最大长度
     */
    private static final int MAX_LINE_LENGTH = 1024;

    // ==================== 依赖注入 ====================

    /**
     * 插件配置
     */
    private final AlarmConfig config;

    /**
     * 协议编解码器
     */
    private final AlarmProtocolCodec protocolCodec;

    /**
     * 连接管理器
     */
    private final AlarmConnectionManager connectionManager;

    /**
     * 报警主机插件（用于事件分发）
     */
    private final AlarmPlugin plugin;

    /**
     * 设备 API（用于查询设备信息）
     */
    @Resource(name = "iotDeviceCommonApiImpl")
    private IotDeviceCommonApi deviceApi;

    /**
     * 构造函数
     */
    public AlarmTcpServer(AlarmConfig config, AlarmProtocolCodec protocolCodec,
                          AlarmConnectionManager connectionManager, AlarmPlugin plugin) {
        this.config = config;
        this.protocolCodec = protocolCodec;
        this.connectionManager = connectionManager;
        this.plugin = plugin;
    }

    // ==================== 服务器状态 ====================

    /**
     * Vert.x 实例
     */
    private Vertx vertx;

    /**
     * TCP 服务器
     */
    private NetServer server;

    /**
     * 服务器是否运行中
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Socket -> 设备ID 映射
     */
    private final Map<String, Long> socketDeviceMap = new ConcurrentHashMap<>();

    /**
     * Socket -> 数据缓冲区 映射（用于处理粘包/拆包）
     */
    private final Map<String, StringBuilder> socketBufferMap = new ConcurrentHashMap<>();

    /**
     * Socket ID 生成器
     */
    private final AtomicLong socketIdGenerator = new AtomicLong(0);

    /**
     * NetSocket -> Socket ID 映射（用于获取稳定的 socketId）
     */
    private final Map<NetSocket, String> socketIdMap = new ConcurrentHashMap<>();

    // ==================== 生命周期方法 ====================

    /**
     * 启动服务器
     */
    @PostConstruct
    public void start() {
        if (!config.isEnabled()) {
            log.info("{} 插件已禁用，跳过启动", LOG_PREFIX);
            return;
        }

        if (running.get()) {
            log.warn("{} 服务器已在运行中", LOG_PREFIX);
            return;
        }

        try {
            // 创建 Vert.x 实例
            vertx = Vertx.vertx();

            // 配置服务器选项
            NetServerOptions options = new NetServerOptions()
                    .setPort(config.getPort())
                    .setHost("0.0.0.0")
                    .setIdleTimeout((int) (config.getConnectionTimeout() / 1000))
                    .setTcpKeepAlive(true)
                    .setTcpNoDelay(true);

            // 创建服务器
            server = vertx.createNetServer(options);

            // 设置连接处理器
            server.connectHandler(this::handleConnection);

            // 启动服务器
            server.listen(ar -> {
                if (ar.succeeded()) {
                    running.set(true);
                    log.info("{} 服务器启动成功: port={}", LOG_PREFIX, config.getPort());
                } else {
                    log.error("{} 服务器启动失败: port={}", LOG_PREFIX, config.getPort(), ar.cause());
                }
            });
        } catch (Exception e) {
            log.error("{} 服务器启动异常", LOG_PREFIX, e);
        }
    }

    /**
     * 停止服务器
     */
    @PreDestroy
    public void stop() {
        if (!running.get()) {
            return;
        }

        log.info("{} 正在停止服务器...", LOG_PREFIX);

        try {
            // 关闭所有连接
            connectionManager.closeAll();
            socketDeviceMap.clear();
            socketBufferMap.clear();

            // 关闭服务器
            if (server != null) {
                server.close(ar -> {
                    if (ar.succeeded()) {
                        log.info("{} 服务器已关闭", LOG_PREFIX);
                    } else {
                        log.error("{} 服务器关闭失败", LOG_PREFIX, ar.cause());
                    }
                });
            }

            // 关闭 Vert.x
            if (vertx != null) {
                vertx.close();
            }

            running.set(false);
        } catch (Exception e) {
            log.error("{} 服务器停止异常", LOG_PREFIX, e);
        }
    }

    /**
     * 检查服务器是否运行中
     *
     * @return 是否运行中
     */
    public boolean isRunning() {
        return running.get();
    }

    // ==================== 连接处理 ====================

    /**
     * 处理新连接
     *
     * @param socket 连接
     */
    private void handleConnection(NetSocket socket) {
        // 生成稳定的 socketId（不依赖 writeHandlerID，因为它可能为 null）
        String socketId = generateSocketId(socket);
        String remoteAddress = socket.remoteAddress() != null ? socket.remoteAddress().toString() : "unknown";

        log.info("{} 新连接: socketId={}, remoteAddress={}", LOG_PREFIX, socketId, remoteAddress);

        // 检查最大连接数
        if (config.getMaxConnections() > 0 && connectionManager.getOnlineCount() >= config.getMaxConnections()) {
            log.warn("{} 连接数已达上限，拒绝连接: remoteAddress={}", LOG_PREFIX, remoteAddress);
            socket.close();
            return;
        }

        // 保存 socketId 映射
        socketIdMap.put(socket, socketId);

        // 初始化缓冲区
        socketBufferMap.put(socketId, new StringBuilder());

        // 设置数据处理器
        socket.handler(buffer -> handleData(socket, buffer));

        // 设置关闭处理器
        socket.closeHandler(v -> handleClose(socket));

        // 设置异常处理器
        socket.exceptionHandler(e -> handleException(socket, e));
    }

    /**
     * 生成稳定的 Socket ID
     *
     * @param socket 连接
     * @return Socket ID
     */
    private String generateSocketId(NetSocket socket) {
        // 优先使用 writeHandlerID
        String handlerId = socket.writeHandlerID();
        if (handlerId != null && !handlerId.isEmpty()) {
            return handlerId;
        }
        
        // 如果 writeHandlerID 为 null，使用自增 ID + 远程地址
        String remoteAddr = socket.remoteAddress() != null ? socket.remoteAddress().toString() : "unknown";
        return "alarm-" + socketIdGenerator.incrementAndGet() + "-" + remoteAddr;
    }

    /**
     * 获取 Socket 的 ID
     *
     * @param socket 连接
     * @return Socket ID
     */
    private String getSocketId(NetSocket socket) {
        String socketId = socketIdMap.get(socket);
        if (socketId == null) {
            // 如果没有找到，生成一个新的（理论上不应该发生）
            socketId = generateSocketId(socket);
            socketIdMap.put(socket, socketId);
        }
        return socketId;
    }


    /**
     * 处理接收到的数据
     *
     * @param socket 连接
     * @param buffer 数据
     */
    private void handleData(NetSocket socket, Buffer buffer) {
        String socketId = getSocketId(socket);

        try {
            // 获取或创建缓冲区
            StringBuilder existingBuffer = socketBufferMap.computeIfAbsent(socketId, k -> new StringBuilder());
            
            // 追加新数据
            String data = buffer.toString(StandardCharsets.US_ASCII);
            existingBuffer.append(data);

            // 检查缓冲区是否过大
            if (existingBuffer.length() > MAX_LINE_LENGTH * 10) {
                log.warn("{} 缓冲区过大，清空: socketId={}, length={}", LOG_PREFIX, socketId, existingBuffer.length());
                existingBuffer.setLength(0);
                return;
            }

            // 处理完整的消息行
            processMessages(socket, socketId, existingBuffer);
        } catch (Exception e) {
            log.error("{} 处理数据异常: socketId={}", LOG_PREFIX, socketId, e);
        }
    }

    /**
     * 处理完整的消息
     *
     * @param socket   连接
     * @param socketId Socket ID
     * @param buffer   缓冲区
     */
    private void processMessages(NetSocket socket, String socketId, StringBuilder buffer) {
        int terminatorIndex;
        
        // 循环处理所有完整的消息
        while ((terminatorIndex = findTerminator(buffer)) >= 0) {
            // 提取完整的消息
            String message = buffer.substring(0, terminatorIndex).trim();
            
            // 移除已处理的数据（包括结束符）
            int deleteEnd = terminatorIndex + MESSAGE_TERMINATOR.length();
            if (deleteEnd > buffer.length()) {
                deleteEnd = buffer.length();
            }
            buffer.delete(0, deleteEnd);

            // 跳过空消息
            if (message.isEmpty()) {
                continue;
            }

            // 检查消息长度
            if (message.length() > MAX_LINE_LENGTH) {
                log.warn("{} 消息过长，丢弃: socketId={}, length={}", LOG_PREFIX, socketId, message.length());
                continue;
            }

            // 处理消息
            processMessage(socket, socketId, message);
        }
    }

    /**
     * 查找消息结束符位置
     *
     * @param buffer 缓冲区
     * @return 结束符位置，如果未找到返回 -1
     */
    private int findTerminator(StringBuilder buffer) {
        // 查找 \r\n
        int crlfIndex = buffer.indexOf(MESSAGE_TERMINATOR);
        if (crlfIndex >= 0) {
            return crlfIndex;
        }
        
        // 也支持单独的 \n 作为结束符
        int lfIndex = buffer.indexOf("\n");
        if (lfIndex >= 0) {
            return lfIndex;
        }
        
        // 也支持单独的 \r 作为结束符
        int crIndex = buffer.indexOf("\r");
        if (crIndex >= 0) {
            return crIndex;
        }
        
        return -1;
    }

    /**
     * 处理单条消息
     *
     * @param socket   连接
     * @param socketId Socket ID
     * @param message  消息内容
     */
    private void processMessage(NetSocket socket, String socketId, String message) {
        try {
            log.debug("{} 收到消息: socketId={}, message={}", LOG_PREFIX, socketId, message);

            // 使用 OPC 协议解析消息
            AlarmProtocolCodec.OpcParseResult opcResult = protocolCodec.parseOpcMessage(message);
            
            String account;
            if (opcResult.isSuccess()) {
                // OPC 协议消息（如 E1234,0000000000125）
                account = opcResult.getAccount(); // 已去掉 E 前缀，返回 "1234"
                log.debug("{} OPC 协议解析成功: account={}, heartbeat={}", 
                        LOG_PREFIX, account, opcResult.isHeartbeat());
            } else {
                // 尝试使用旧协议解析（如 1234,HB）
                account = protocolCodec.parseAccount(message);
                if (account == null) {
                    log.warn("{} 解析账号失败: socketId={}, message={}", LOG_PREFIX, socketId, message);
                    return;
                }
            }

            // 获取或查找设备ID
            Long deviceId = socketDeviceMap.get(socketId);
            if (deviceId == null) {
                // 首次收到数据，尝试查找设备ID
                deviceId = connectionManager.getDeviceIdByIdentifier(account);
                
                if (deviceId == null) {
                    // 设备未在连接管理器中注册，尝试从数据库查询
                    deviceId = tryRegisterDevice(socket, socketId, account);
                    
                    if (deviceId == null) {
                        // 设备不存在，记录警告
                        log.warn("{} 未知设备: socketId={}, account={}", LOG_PREFIX, socketId, account);
                        
                        // 发送 NAK 响应
                        if (opcResult.isSuccess()) {
                            sendOpcNakResponse(socket, account, opcResult.getSequence());
                        } else {
                            sendNakResponse(socket, account, "UNKNOWN_DEVICE");
                        }
                        return;
                    }
                } else {
                    // 设备已在连接管理器中，记录映射关系
                    socketDeviceMap.put(socketId, deviceId);
                    log.info("{} 设备已注册: socketId={}, deviceId={}, account={}", 
                            LOG_PREFIX, socketId, deviceId, account);
                }
            }

            // 发送 ACK 应答（仅对 OPC 协议消息）
            if (opcResult.isSuccess()) {
                sendOpcAckResponse(socket, account, opcResult.getSequence());
            }

            // 转换为 ByteBuf 并分发到插件处理
            ByteBuf byteBuf = Unpooled.wrappedBuffer(message.getBytes(StandardCharsets.US_ASCII));
            plugin.onDataReceived(deviceId, byteBuf);

        } catch (Exception e) {
            log.error("{} 处理消息异常: socketId={}", LOG_PREFIX, socketId, e);
        }
    }

    /**
     * 尝试从数据库查询设备并注册连接
     *
     * @param socket   连接
     * @param socketId Socket ID
     * @param account  设备账号（如 "1234"，不是 "E1234"）
     * @return 设备ID，如果设备不存在则返回 null
     */
    private Long tryRegisterDevice(NetSocket socket, String socketId, String account) {
        if (deviceApi == null) {
            log.warn("{} deviceApi 未注入，无法查询数据库", LOG_PREFIX);
            return null;
        }

        try {
            // 使用 account 作为 deviceKey 查询设备
            IotDeviceGetReqDTO reqDTO = new IotDeviceGetReqDTO();
            reqDTO.setDeviceKey(account);
            
            CommonResult<IotDeviceRespDTO> result = deviceApi.getDevice(reqDTO);
            if (result == null || !result.isSuccess() || result.getData() == null) {
                log.warn("{} 数据库中未找到设备: account={}, result={}", 
                        LOG_PREFIX, account, result != null ? result.getMsg() : "null");
                return null;
            }

            Long deviceId = result.getData().getId();
            
            // 记录映射关系
            socketDeviceMap.put(socketId, deviceId);

            // 注册连接到连接管理器
            io.netty.channel.Channel nettyChannel = createNettyChannelAdapter(socket);
            connectionManager.register(deviceId, account, nettyChannel);

            // 注意：不调用 plugin.onConnect()，因为：
            // 1. AlarmTcpServer 使用 Vert.x NetSocket，而 onConnect 需要 Netty ChannelHandlerContext
            // 2. 连接注册已在上面完成，设备状态更新由 lifecycleManager 在其他地方处理
            // 3. 传入 null 会导致 NPE（ctx.channel().remoteAddress()）
            
            log.info("{} 设备注册成功: socketId={}, deviceId={}, account={}", 
                    LOG_PREFIX, socketId, deviceId, account);
            return deviceId;
        } catch (Exception e) {
            log.error("{} 查询设备失败: account={}", LOG_PREFIX, account, e);
            return null;
        }
    }

    /**
     * 发送 OPC 协议 ACK 响应
     *
     * @param socket   连接
     * @param account  账号（不含前缀）
     * @param sequence 序列号
     */
    private void sendOpcAckResponse(NetSocket socket, String account, int sequence) {
        try {
            // 格式：e{account},{sequence}
            String response = "e" + account + "," + sequence + MESSAGE_TERMINATOR;
            socket.write(Buffer.buffer(response));
            log.debug("{} 发送 OPC ACK: {}", LOG_PREFIX, response.trim());
        } catch (Exception e) {
            log.error("{} 发送 OPC ACK 响应失败", LOG_PREFIX, e);
        }
    }

    /**
     * 发送 OPC 协议 NAK 响应
     *
     * @param socket   连接
     * @param account  账号（不含前缀）
     * @param sequence 序列号
     */
    private void sendOpcNakResponse(NetSocket socket, String account, int sequence) {
        try {
            // 对于未知设备，不发送 NAK，只记录日志
            // 因为 OPC 协议没有定义 NAK 格式
            log.debug("{} 未知设备，不发送 NAK: account={}, sequence={}", LOG_PREFIX, account, sequence);
        } catch (Exception e) {
            log.error("{} 发送 OPC NAK 响应失败", LOG_PREFIX, e);
        }
    }

    /**
     * 发送 NAK 响应
     *
     * @param socket    连接
     * @param account   账号
     * @param errorCode 错误码
     */
    private void sendNakResponse(NetSocket socket, String account, String errorCode) {
        try {
            String response = protocolCodec.buildNakResponse(account, null, errorCode);
            if (response != null) {
                socket.write(Buffer.buffer(response + MESSAGE_TERMINATOR));
            }
        } catch (Exception e) {
            log.error("{} 发送 NAK 响应失败", LOG_PREFIX, e);
        }
    }

    /**
     * 处理连接关闭
     *
     * @param socket 连接
     */
    private void handleClose(NetSocket socket) {
        String socketId = getSocketId(socket);
        String remoteAddress = socket.remoteAddress() != null ? socket.remoteAddress().toString() : "unknown";

        log.info("{} 连接关闭: socketId={}, remoteAddress={}", LOG_PREFIX, socketId, remoteAddress);

        // 获取设备ID
        Long deviceId = socketDeviceMap.remove(socketId);
        
        // 清理缓冲区
        socketBufferMap.remove(socketId);
        
        // 清理 socketId 映射
        socketIdMap.remove(socket);

        // 触发断开事件
        if (deviceId != null) {
            plugin.onDisconnect(deviceId);
        }
    }

    /**
     * 处理连接异常
     *
     * @param socket 连接
     * @param e      异常
     */
    private void handleException(NetSocket socket, Throwable e) {
        String socketId = getSocketId(socket);
        log.error("{} 连接异常: socketId={}", LOG_PREFIX, socketId, e);

        // 关闭连接
        socket.close();
    }

    /**
     * 创建 Netty Channel 适配器
     * <p>
     * 将 Vert.x NetSocket 适配为 Netty Channel，用于与 ConnectionManager 兼容。
     * </p>
     *
     * @param socket Vert.x NetSocket
     * @return Netty Channel 适配器
     */
    private io.netty.channel.Channel createNettyChannelAdapter(NetSocket socket) {
        return new VertxNetSocketChannelAdapter(socket);
    }

    // ==================== 公共方法 ====================

    /**
     * 获取当前连接数
     *
     * @return 连接数
     */
    public int getConnectionCount() {
        return socketDeviceMap.size();
    }

    /**
     * 获取服务器端口
     *
     * @return 端口
     */
    public int getPort() {
        return config.getPort();
    }

    /**
     * 向指定设备发送数据
     *
     * @param deviceId 设备ID
     * @param data     数据
     * @return 是否发送成功
     */
    public boolean sendToDevice(Long deviceId, byte[] data) {
        io.netty.channel.Channel channel = connectionManager.getConnection(deviceId);
        if (channel == null || !channel.isActive()) {
            log.warn("{} 无法发送数据，设备未连接: deviceId={}", LOG_PREFIX, deviceId);
            return false;
        }

        try {
            channel.writeAndFlush(Unpooled.wrappedBuffer(data));
            return true;
        } catch (Exception e) {
            log.error("{} 发送数据失败: deviceId={}", LOG_PREFIX, deviceId, e);
            return false;
        }
    }

    /**
     * 向指定设备发送消息
     *
     * @param deviceId 设备ID
     * @param message  消息内容
     * @return 是否发送成功
     */
    public boolean sendMessage(Long deviceId, String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }
        
        String fullMessage = message;
        if (!message.endsWith(MESSAGE_TERMINATOR)) {
            fullMessage = message + MESSAGE_TERMINATOR;
        }
        
        return sendToDevice(deviceId, fullMessage.getBytes(StandardCharsets.US_ASCII));
    }
}
