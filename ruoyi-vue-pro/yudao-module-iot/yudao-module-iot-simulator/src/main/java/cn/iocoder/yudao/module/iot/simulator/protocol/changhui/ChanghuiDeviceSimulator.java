package cn.iocoder.yudao.module.iot.simulator.protocol.changhui;

import cn.iocoder.yudao.module.iot.simulator.core.DeviceSimulator;
import cn.iocoder.yudao.module.iot.simulator.core.SimulatorConfig;
import cn.iocoder.yudao.module.iot.simulator.core.SimulatorMode;
import cn.iocoder.yudao.module.iot.simulator.core.SimulatorState;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 长辉设备模拟器
 * 
 * <p>模拟长辉设备的TCP通信行为，支持：</p>
 * <ul>
 *     <li>心跳保活</li>
 *     <li>升级流程（触发、下载URL、进度上报、完成/失败）</li>
 *     <li>水位等数据上报</li>
 *     <li>多种测试模式（成功、拒绝、失败等）</li>
 * </ul>
 *
 * @author IoT Simulator Team
 */
@Slf4j
public class ChanghuiDeviceSimulator implements DeviceSimulator {

    private static final String LOG_PREFIX = "[ChanghuiSimulator]";

    private final ChanghuiSimulatorConfig config;
    private final ChanghuiProtocolCodec codec;
    private final ScheduledExecutorService scheduler;
    private final Random random = new Random();

    private volatile SimulatorState state = SimulatorState.STOPPED;
    private EventLoopGroup eventLoopGroup;
    private Channel channel;
    private ScheduledFuture<?> heartbeatTask;
    private ScheduledFuture<?> upgradeTask;
    
    // 升级状态
    private volatile boolean upgrading = false;
    private volatile int upgradeProgress = 0;
    @SuppressWarnings("unused")
    private volatile String upgradeUrl;

    public ChanghuiDeviceSimulator(ChanghuiSimulatorConfig config) {
        this.config = config;
        this.codec = new ChanghuiProtocolCodec();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "changhui-simulator-" + config.getStationCode());
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public String getDeviceId() {
        return config.getStationCode();
    }

    @Override
    public String getProtocolType() {
        return ChanghuiSimulatorConfig.PROTOCOL_TYPE;
    }

    @Override
    public CompletableFuture<Void> start() {
        if (state == SimulatorState.RUNNING) {
            log.warn("{} 模拟器已在运行中: {}", LOG_PREFIX, config.getStationCode());
            return CompletableFuture.completedFuture(null);
        }

        state = SimulatorState.STARTING;
        log.info("{} 启动模拟器: stationCode={}, server={}:{}", 
                LOG_PREFIX, config.getStationCode(), config.getServerHost(), config.getServerPort());

        CompletableFuture<Void> future = new CompletableFuture<>();

        eventLoopGroup = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeout())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        // 心跳检测
                        pipeline.addLast(new IdleStateHandler(0, config.getHeartbeatInterval(), 0, TimeUnit.SECONDS));
                        // 帧解码器
                        pipeline.addLast(new ChanghuiFrameDecoder());
                        // 业务处理器
                        pipeline.addLast(new ChanghuiMessageHandler());
                    }
                });

        bootstrap.connect(config.getServerHost(), config.getServerPort())
                .addListener((ChannelFutureListener) f -> {
                    if (f.isSuccess()) {
                        channel = f.channel();
                        state = SimulatorState.RUNNING;
                        log.info("{} 连接成功: {}", LOG_PREFIX, config.getStationCode());
                        
                        // 启动心跳
                        startHeartbeat();
                        // 发送首次心跳
                        sendHeartbeat();
                        
                        future.complete(null);
                    } else {
                        state = SimulatorState.STOPPED;
                        log.error("{} 连接失败: {}", LOG_PREFIX, config.getStationCode(), f.cause());
                        eventLoopGroup.shutdownGracefully();
                        future.completeExceptionally(f.cause());
                    }
                });

        return future;
    }

    @Override
    public CompletableFuture<Void> stop() {
        if (state == SimulatorState.STOPPED) {
            return CompletableFuture.completedFuture(null);
        }

        state = SimulatorState.STOPPING;
        log.info("{} 停止模拟器: {}", LOG_PREFIX, config.getStationCode());

        // 停止心跳
        if (heartbeatTask != null) {
            heartbeatTask.cancel(false);
            heartbeatTask = null;
        }

        // 停止升级任务
        if (upgradeTask != null) {
            upgradeTask.cancel(false);
            upgradeTask = null;
        }

        // 关闭连接
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (channel != null && channel.isActive()) {
            channel.close().addListener(f -> {
                cleanup();
                future.complete(null);
            });
        } else {
            cleanup();
            future.complete(null);
        }

        return future;
    }

    private void cleanup() {
        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully();
            eventLoopGroup = null;
        }
        state = SimulatorState.STOPPED;
        upgrading = false;
        upgradeProgress = 0;
        log.info("{} 模拟器已停止: {}", LOG_PREFIX, config.getStationCode());
    }

    @Override
    public SimulatorState getState() {
        return state;
    }

    @Override
    public void sendMessage(Object message) {
        if (message instanceof byte[]) {
            sendBytes((byte[]) message);
        }
    }

    @Override
    public SimulatorConfig getConfig() {
        return config;
    }

    // ==================== 心跳 ====================

    private void startHeartbeat() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel(false);
        }
        heartbeatTask = scheduler.scheduleAtFixedRate(
                this::sendHeartbeat,
                config.getHeartbeatInterval(),
                config.getHeartbeatInterval(),
                TimeUnit.SECONDS
        );
    }

    private void sendHeartbeat() {
        if (state != SimulatorState.RUNNING || channel == null || !channel.isActive()) {
            return;
        }
        byte[] frame = codec.buildHeartbeatFrame(config.getStationCode());
        if (frame != null) {
            sendBytes(frame);
            log.debug("{} 发送心跳: stationCode={}, frame={}", LOG_PREFIX, config.getStationCode(), codec.bytesToHex(frame));
        }
    }

    private void sendBytes(byte[] data) {
        if (channel != null && channel.isActive()) {
            ByteBuf buf = Unpooled.wrappedBuffer(data);
            channel.writeAndFlush(buf);
        }
    }

    // ==================== 升级处理 ====================

    /**
     * 处理升级触发命令
     */
    private void handleUpgradeTrigger() {
        log.info("{} 收到升级触发命令: {}", LOG_PREFIX, config.getStationCode());

        // 检查是否拒绝升级
        if (config.isRejectUpgrade() || config.getMode() == SimulatorMode.REJECT) {
            log.warn("{} 拒绝升级: {}", LOG_PREFIX, config.getStationCode());
            // 发送升级失败
            byte[] frame = codec.buildUpgradeFailedFrame(config.getStationCode(), "设备拒绝升级");
            sendBytes(frame);
            return;
        }

        // 发送升级开始状态
        byte[] startFrame = codec.buildUpgradeStartFrame(config.getStationCode());
        sendBytes(startFrame);
        log.info("{} 发送升级开始状态: {}", LOG_PREFIX, config.getStationCode());
    }

    /**
     * 处理升级URL命令
     */
    private void handleUpgradeUrl(String url) {
        log.info("{} 收到升级URL: {} -> {}", LOG_PREFIX, config.getStationCode(), url);

        this.upgradeUrl = url;
        this.upgrading = true;
        this.upgradeProgress = 0;

        // 模拟下载和升级过程
        simulateUpgradeProcess();
    }

    /**
     * 模拟升级过程
     */
    private void simulateUpgradeProcess() {
        if (upgradeTask != null) {
            upgradeTask.cancel(false);
        }

        AtomicInteger progress = new AtomicInteger(0);
        int totalSteps = 100;
        int stepInterval = config.getProgressReportInterval();

        upgradeTask = scheduler.scheduleAtFixedRate(() -> {
            if (!upgrading || state != SimulatorState.RUNNING) {
                if (upgradeTask != null) {
                    upgradeTask.cancel(false);
                }
                return;
            }

            int currentProgress = progress.incrementAndGet();
            this.upgradeProgress = currentProgress;

            // 检查是否模拟失败
            if (shouldSimulateFailure(currentProgress)) {
                log.warn("{} 模拟升级失败: {} 进度={}", LOG_PREFIX, config.getStationCode(), currentProgress);
                byte[] failFrame = codec.buildUpgradeFailedFrame(config.getStationCode(), "模拟升级失败");
                sendBytes(failFrame);
                upgrading = false;
                upgradeTask.cancel(false);
                return;
            }

            // 发送进度
            byte[] progressFrame = codec.buildUpgradeProgressFrame(config.getStationCode(), currentProgress);
            sendBytes(progressFrame);
            log.debug("{} 发送升级进度: {} -> {}%", LOG_PREFIX, config.getStationCode(), currentProgress);

            // 检查是否完成
            if (currentProgress >= totalSteps) {
                // 模拟最终处理延迟
                scheduler.schedule(() -> {
                    byte[] completeFrame = codec.buildUpgradeCompleteFrame(config.getStationCode());
                    sendBytes(completeFrame);
                    log.info("{} 升级完成: {}", LOG_PREFIX, config.getStationCode());
                    upgrading = false;
                }, config.getDownloadDelay() / 10, TimeUnit.MILLISECONDS);

                upgradeTask.cancel(false);
            }
        }, stepInterval, stepInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * 检查是否应该模拟失败
     */
    private boolean shouldSimulateFailure(int currentProgress) {
        // FRAME_FAIL 模式
        if (config.getMode() == SimulatorMode.FRAME_FAIL && config.getFailFrameNumbers() != null) {
            for (int failFrame : config.getFailFrameNumbers()) {
                if (currentProgress == failFrame) {
                    return true;
                }
            }
        }

        // 随机失败概率
        if (config.getFailureProbability() > 0 && currentProgress > 50) {
            return random.nextInt(100) < config.getFailureProbability();
        }

        return false;
    }

    // ==================== 数据上报 ====================

    /**
     * 上报水位数据
     */
    public void reportWaterLevel(float waterLevel) {
        if (state != SimulatorState.RUNNING) {
            return;
        }
        byte[] frame = codec.buildWaterLevelFrame(config.getStationCode(), waterLevel);
        if (frame != null) {
            sendBytes(frame);
            log.info("{} 上报水位: {} -> {}m", LOG_PREFIX, config.getStationCode(), waterLevel);
        }
    }

    /**
     * 获取升级进度
     */
    public int getUpgradeProgress() {
        return upgradeProgress;
    }

    /**
     * 是否正在升级
     */
    public boolean isUpgrading() {
        return upgrading;
    }

    // ==================== 内部类 ====================

    /**
     * 帧解码器
     */
    private class ChanghuiFrameDecoder extends ByteToMessageDecoder {
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            if (in.readableBytes() < ChanghuiProtocolCodec.MIN_FRAME_LENGTH) {
                return;
            }

            in.markReaderIndex();

            // 查找帧头
            while (in.readableBytes() >= ChanghuiProtocolCodec.FRAME_HEADER_LENGTH) {
                if (in.getByte(in.readerIndex()) == (byte) 0xEF
                        && in.getByte(in.readerIndex() + 1) == 0x7E
                        && in.getByte(in.readerIndex() + 2) == (byte) 0xEF) {
                    break;
                }
                in.skipBytes(1);
            }

            if (in.readableBytes() < ChanghuiProtocolCodec.MIN_FRAME_LENGTH) {
                return;
            }

            // 读取长度（大端序，与协议文档和服务端保持一致）
            int lengthOffset = ChanghuiProtocolCodec.FRAME_HEADER_LENGTH;
            int contentLength = in.getInt(in.readerIndex() + lengthOffset);
            int frameLength = ChanghuiProtocolCodec.FRAME_HEADER_LENGTH 
                    + ChanghuiProtocolCodec.LENGTH_FIELD_LENGTH + contentLength;

            if (in.readableBytes() < frameLength) {
                in.resetReaderIndex();
                return;
            }

            // 读取完整帧
            byte[] frame = new byte[frameLength];
            in.readBytes(frame);
            out.add(frame);
        }
    }

    /**
     * 消息处理器
     */
    private class ChanghuiMessageHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (!(msg instanceof byte[])) {
                return;
            }

            byte[] data = (byte[]) msg;
            ChanghuiProtocolCodec.ParseResult result = codec.parseFrame(data);

            if (!result.isSuccess()) {
                log.warn("{} 解析帧失败: {} - {}", LOG_PREFIX, config.getStationCode(), result.getErrorMessage());
                return;
            }

            // 验证测站编码
            if (!config.getStationCode().equals(result.getStationCode())) {
                log.debug("{} 忽略非本设备消息: expected={}, actual={}",
                        LOG_PREFIX, config.getStationCode(), result.getStationCode());
                return;
            }

            handleMessage(result);
        }

        private void handleMessage(ChanghuiProtocolCodec.ParseResult result) {
            ChanghuiMessageType type = result.getMessageType();
            log.debug("{} 收到消息: {} -> {}", LOG_PREFIX, config.getStationCode(), type);

            switch (type) {
                case HEARTBEAT:
                    // 收到心跳响应，不需要特殊处理
                    log.debug("{} 收到心跳响应: {}", LOG_PREFIX, config.getStationCode());
                    break;

                case UPGRADE_TRIGGER:
                    handleUpgradeTrigger();
                    break;

                case UPGRADE_URL:
                    String url = codec.parseUrl(result.getData() != null ? 
                            buildFrameWithData(result.getData()) : new byte[0]);
                    if (url == null && result.getData() != null) {
                        url = new String(result.getData(), java.nio.charset.StandardCharsets.UTF_8);
                    }
                    handleUpgradeUrl(url);
                    break;

                default:
                    log.debug("{} 忽略未处理的消息类型: {} -> {}", LOG_PREFIX, config.getStationCode(), type);
            }
        }

        private byte[] buildFrameWithData(byte[] data) {
            // 重建完整帧以便解析URL
            byte[] frame = new byte[ChanghuiProtocolCodec.MIN_FRAME_LENGTH + data.length];
            System.arraycopy(data, 0, frame, ChanghuiProtocolCodec.DATA_OFFSET, data.length);
            return frame;
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            if (evt instanceof IdleStateEvent) {
                // 发送心跳
                sendHeartbeat();
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            log.warn("{} 连接断开: {}", LOG_PREFIX, config.getStationCode());
            if (state == SimulatorState.RUNNING) {
                // 尝试重连
                scheduleReconnect();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("{} 通道异常: {}", LOG_PREFIX, config.getStationCode(), cause);
            ctx.close();
        }
    }

    /**
     * 调度重连
     */
    private void scheduleReconnect() {
        if (state != SimulatorState.RUNNING) {
            return;
        }
        
        state = SimulatorState.RECONNECTING;

        scheduler.schedule(() -> {
            if (state == SimulatorState.STOPPED) {
                return;
            }
            
            log.info("{} 尝试重连: {}", LOG_PREFIX, config.getStationCode());
            start().exceptionally(e -> {
                log.error("{} 重连失败: {}", LOG_PREFIX, config.getStationCode(), e);
                if (state != SimulatorState.STOPPED) {
                    scheduleReconnect();
                }
                return null;
            });
        }, config.getRetryInterval(), TimeUnit.SECONDS);
    }
}

