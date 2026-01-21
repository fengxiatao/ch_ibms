package cn.iocoder.yudao.module.iot.newgateway.plugins.changhui;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 长辉TCP服务器
 * 
 * <p>基于 Vert.x 实现的 TCP 服务器，用于接收长辉设备的连接和数据。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *     <li>监听指定端口，接受设备连接</li>
 *     <li>解析协议帧，提取设备标识</li>
 *     <li>分发数据到 ChanghuiPlugin 处理</li>
 *     <li>管理连接生命周期</li>
 * </ul>
 * 
 * <p>协议帧格式：</p>
 * <ul>
 *     <li>帧头: EF7EEF (3字节)</li>
 *     <li>长度: 4字节（小端序）</li>
 *     <li>测站编码: 10字节</li>
 *     <li>...</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 * @see ChanghuiPlugin
 * @see ChanghuiProtocolCodec
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "changhui", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class ChanghuiTcpServer {

    /**
     * 日志前缀
     */
    private static final String LOG_PREFIX = "[ChanghuiTcpServer]";

    /**
     * 帧头
     */
    private static final byte[] FRAME_HEADER = {(byte) 0xEF, 0x7E, (byte) 0xEF};

    /**
     * 帧头长度
     */
    private static final int FRAME_HEADER_LENGTH = 3;

    /**
     * 长度字段长度
     */
    private static final int LENGTH_FIELD_LENGTH = 4;

    /**
     * 固定头部长度（帧头 + 长度）
     */
    private static final int FIXED_HEADER_LENGTH = FRAME_HEADER_LENGTH + LENGTH_FIELD_LENGTH;

    /**
     * 最小帧长度
     */
    private static final int MIN_FRAME_LENGTH = ChanghuiProtocolCodec.MIN_FRAME_LENGTH;

    // ==================== 依赖注入 ====================

    /**
     * 插件配置
     */
    private final ChanghuiConfig config;

    /**
     * 协议编解码器
     */
    private final ChanghuiProtocolCodec protocolCodec;

    /**
     * 连接管理器
     */
    private final ChanghuiConnectionManager connectionManager;

    /**
     * 长辉插件（用于事件分发）
     */
    private final ChanghuiPlugin plugin;

    /**
     * 设备 API（用于查询设备信息）
     *
     * <p>说明：长辉TCP为被动连接设备，首包进来时需要通过 stationCode 查到 deviceId。
     * 统一约定：biz侧 iot_device.deviceKey = stationCode，Gateway 通过 deviceKey 查询。</p>
     */
    @Resource(name = "iotDeviceCommonApiImpl")
    private IotDeviceCommonApi deviceApi;

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
    private final Map<String, Buffer> socketBufferMap = new ConcurrentHashMap<>();

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
        socketBufferMap.put(socketId, Buffer.buffer());

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
        return "changhui-" + socketIdGenerator.incrementAndGet() + "-" + remoteAddr;
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
            // 打印接收到的原始数据（十六进制格式，便于调试）
            if (log.isDebugEnabled()) {
                StringBuilder hexBuilder = new StringBuilder();
                byte[] bytes = buffer.getBytes();
                for (byte b : bytes) {
                    hexBuilder.append(String.format("%02X", b & 0xFF));
                }
                log.debug("{} 接收原始数据: socketId={}, length={}, hex={}", 
                        LOG_PREFIX, socketId, buffer.length(), hexBuilder.toString());
            }
            
            // 获取或创建缓冲区
            Buffer existingBuffer = socketBufferMap.computeIfAbsent(socketId, k -> Buffer.buffer());
            
            // 追加新数据
            existingBuffer.appendBuffer(buffer);
            socketBufferMap.put(socketId, existingBuffer);

            // 处理完整的帧
            processFrames(socket, socketId);
        } catch (Exception e) {
            log.error("{} 处理数据异常: socketId={}", LOG_PREFIX, socketId, e);
        }
    }

    /**
     * 处理完整的协议帧
     *
     * @param socket   连接
     * @param socketId Socket ID
     */
    private void processFrames(NetSocket socket, String socketId) {
        Buffer buffer = socketBufferMap.get(socketId);
        if (buffer == null || buffer.length() < MIN_FRAME_LENGTH) {
            return;
        }

        while (buffer.length() >= FIXED_HEADER_LENGTH) {
            // 查找帧头
            int headerIndex = findFrameHeader(buffer);
            if (headerIndex < 0) {
                // 没有找到帧头，清空缓冲区
                log.debug("{} 未找到帧头，清空缓冲区: socketId={}", LOG_PREFIX, socketId);
                socketBufferMap.put(socketId, Buffer.buffer());
                return;
            }

            // 如果帧头不在开头，丢弃前面的数据
            if (headerIndex > 0) {
                log.debug("{} 丢弃无效数据: socketId={}, bytes={}", LOG_PREFIX, socketId, headerIndex);
                buffer = buffer.getBuffer(headerIndex, buffer.length());
                socketBufferMap.put(socketId, buffer);
            }

            // 检查是否有足够的数据读取长度字段
            if (buffer.length() < FIXED_HEADER_LENGTH) {
                return;
            }

            // 读取帧长度（大端序，根据协议文档长度字段为大端序格式）
            int contentLength = buffer.getInt(FRAME_HEADER_LENGTH);
            int frameLength = FIXED_HEADER_LENGTH + contentLength;

            // 检查帧长度是否合理
            if (contentLength < 0 || frameLength > 65535) {
                // 打印原始报文的十六进制，便于调试
                int previewLength = Math.min(buffer.length(), 64);
                StringBuilder hexBuilder = new StringBuilder();
                for (int i = 0; i < previewLength; i++) {
                    hexBuilder.append(String.format("%02X", buffer.getByte(i) & 0xFF));
                }
                log.warn("{} 无效的帧长度: socketId={}, contentLength={}, rawHex={}{}",
                    LOG_PREFIX, socketId, contentLength, hexBuilder.toString(),
                    buffer.length() > previewLength ? "..." : "");
                // 跳过帧头，继续查找
                buffer = buffer.getBuffer(FRAME_HEADER_LENGTH, buffer.length());
                socketBufferMap.put(socketId, buffer);
                continue;
            }

            // 检查是否有完整的帧
            if (buffer.length() < frameLength) {
                // 数据不完整，等待更多数据
                return;
            }

            // 提取完整的帧
            Buffer frame = buffer.getBuffer(0, frameLength);
            buffer = buffer.getBuffer(frameLength, buffer.length());
            socketBufferMap.put(socketId, buffer);

            // 处理帧
            processFrame(socket, socketId, frame);
        }
    }

    /**
     * 查找帧头位置
     *
     * @param buffer 缓冲区
     * @return 帧头位置，如果未找到返回 -1
     */
    private int findFrameHeader(Buffer buffer) {
        for (int i = 0; i <= buffer.length() - FRAME_HEADER_LENGTH; i++) {
            if (buffer.getByte(i) == FRAME_HEADER[0] &&
                buffer.getByte(i + 1) == FRAME_HEADER[1] &&
                buffer.getByte(i + 2) == FRAME_HEADER[2]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 处理单个协议帧
     *
     * @param socket   连接
     * @param socketId Socket ID
     * @param frame    帧数据
     */
    private void processFrame(NetSocket socket, String socketId, Buffer frame) {
        try {
            // 打印收到的原始帧（十六进制格式，便于调试）
            if (log.isDebugEnabled()) {
                StringBuilder hexBuilder = new StringBuilder();
                byte[] frameBytes = frame.getBytes();
                for (byte b : frameBytes) {
                    hexBuilder.append(String.format("%02X", b & 0xFF));
                }
                log.debug("{} 收到原始帧: socketId={}, length={}, hex={}", 
                        LOG_PREFIX, socketId, frame.length(), hexBuilder.toString());
            }
            
            // 转换为 ByteBuf
            ByteBuf byteBuf = Unpooled.wrappedBuffer(frame.getBytes());

            // 解析测站编码
            String stationCode = protocolCodec.parseStationCode(byteBuf);
            if (stationCode == null) {
                log.warn("{} 解析测站编码失败: socketId={}", LOG_PREFIX, socketId);
                return;
            }

            // 解析消息类型用于日志
            ChanghuiMessageType messageType = protocolCodec.parseMessageType(byteBuf);
            log.debug("{} 解析帧: socketId={}, stationCode={}, messageType={}", 
                    LOG_PREFIX, socketId, stationCode, messageType);

            // 获取或查找设备ID
            Long deviceId = socketDeviceMap.get(socketId);
            if (deviceId == null) {
                // 首次收到数据：通过 deviceKey=stationCode 查询设备并注册连接
                log.info("{} 首次收到数据，尝试注册设备: socketId={}, stationCode={}", 
                        LOG_PREFIX, socketId, stationCode);
                deviceId = tryRegisterDevice(socket, socketId, stationCode);
                if (deviceId == null) {
                    log.warn("{} 未知设备（未在平台创建设备或 deviceKey 未绑定 stationCode）: socketId={}, stationCode={}",
                            LOG_PREFIX, socketId, stationCode);
                    // 保持连接不断开：允许设备继续发包，便于排查；业务上可按需改为 socket.close()
                    return;
                }

                // 记录映射关系
                socketDeviceMap.put(socketId, deviceId);
                log.info("{} 设备映射已创建: socketId={} -> deviceId={}", LOG_PREFIX, socketId, deviceId);

                // 注册连接到连接管理器
                // 需要将 NetSocket 转换为 Netty Channel，这里使用适配器模式
                io.netty.channel.Channel nettyChannel = createNettyChannelAdapter(socket);
                connectionManager.register(deviceId, stationCode, nettyChannel);
            }

            // 重置 ByteBuf 读取位置
            byteBuf.resetReaderIndex();

            // 分发数据到插件处理
            plugin.onDataReceived(deviceId, byteBuf);

        } catch (Exception e) {
            log.error("{} 处理帧异常: socketId={}", LOG_PREFIX, socketId, e);
        }
    }

    /**
     * 尝试从数据库查询设备并注册连接
     *
     * @param socket     连接
     * @param socketId   Socket ID
     * @param stationCode 测站编码（20字符十六进制字符串，对应10字节）
     * @return 设备ID，如果设备不存在则返回 null
     */
    private Long tryRegisterDevice(NetSocket socket, String socketId, String stationCode) {
        if (deviceApi == null) {
            log.warn("{} deviceApi 未注入，无法查询数据库", LOG_PREFIX);
            return null;
        }
        try {
            IotDeviceGetReqDTO reqDTO = new IotDeviceGetReqDTO();
            // 统一约定：deviceKey = stationCode（20字符十六进制字符串）
            reqDTO.setDeviceKey(stationCode);
            log.debug("{} 查询设备: deviceKey={}", LOG_PREFIX, stationCode);
            
            CommonResult<IotDeviceRespDTO> result = deviceApi.getDevice(reqDTO);
            if (result == null || !result.isSuccess() || result.getData() == null) {
                log.warn("{} 数据库中未找到设备: stationCode(deviceKey)={}, result={}",
                        LOG_PREFIX, stationCode, result != null ? result.getMsg() : "null");
                log.warn("{} 提示：请确保设备的 deviceKey 字段与测站编码一致（20字符十六进制）", LOG_PREFIX);
                return null;
            }
            Long deviceId = result.getData().getId();
            String deviceName = result.getData().getDeviceName();
            log.info("{} 设备查询成功: deviceId={}, deviceName={}, stationCode={}",
                    LOG_PREFIX, deviceId, deviceName, stationCode);
            
            return deviceId;
        } catch (Exception e) {
            log.error("{} 查询设备失败: stationCode={}", LOG_PREFIX, stationCode, e);
            return null;
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
}
