package cn.iocoder.yudao.module.iot.newgateway.plugins.changhui;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.ConnectionMode;
import cn.iocoder.yudao.module.iot.core.gateway.dto.DeviceInfo;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.core.mq.message.changhui.ChanghuiAlarmMessage;
import cn.iocoder.yudao.module.iot.core.mq.message.changhui.ChanghuiControlResultMessage;
import cn.iocoder.yudao.module.iot.core.mq.message.changhui.ChanghuiDataReportMessage;
import cn.iocoder.yudao.module.iot.core.mq.message.changhui.ChanghuiUpgradeStatusMessage;
import cn.iocoder.yudao.module.iot.newgateway.core.annotation.DevicePlugin;
import cn.iocoder.yudao.module.iot.newgateway.core.handler.PassiveDeviceHandler;
import cn.iocoder.yudao.module.iot.newgateway.core.lifecycle.DeviceLifecycleManager;
import cn.iocoder.yudao.module.iot.newgateway.core.message.GatewayMessagePublisher;
import cn.iocoder.yudao.module.iot.newgateway.core.model.CommandResult;
import cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceCommand;
import cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceConnectionInfo;
import cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceStatus;
import cn.iocoder.yudao.module.iot.newgateway.core.model.HeartbeatData;
import cn.iocoder.yudao.module.iot.newgateway.plugins.PluginConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 长辉TCP模拟设备插件
 * 
 * <p>实现长辉自研TCP协议设备的接入，支持：</p>
 * <ul>
 *     <li>心跳保活（AFN=0x3C）</li>
 *     <li>远程升级触发（AFN=0x02）</li>
 *     <li>升级URL下发（AFN=0x10）</li>
 *     <li>升级状态上报（AFN=0x15/0x66/0x67/0x68）</li>
 * </ul>
 * 
 * <h2>协议格式</h2>
 * <ul>
 *     <li>帧头: EF7EEF (3字节)</li>
 *     <li>长度: 4字节</li>
 *     <li>测站编码: 10字节（用于设备标识）</li>
 *     <li>起始字符: 68 (1字节)</li>
 *     <li>L: 1字节</li>
 *     <li>起始字符: 68 (1字节)</li>
 *     <li>控制域C: 1字节</li>
 *     <li>应用功能码AFN: 1字节</li>
 *     <li>数据域: 可变</li>
 *     <li>密码: 2字节</li>
 *     <li>时间标签: 5字节</li>
 *     <li>CS: 1字节</li>
 *     <li>结束字符: 16 (1字节)</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 * @see PassiveDeviceHandler
 * @see ChanghuiConnectionManager
 * @see ChanghuiConfig
 */
@DevicePlugin(
    id = PluginConstants.PLUGIN_ID_CHANGHUI,
    name = "长辉TCP模拟设备",
    deviceType = PluginConstants.DEVICE_TYPE_CHANGHUI,
    vendor = "Changhui",
    description = "长辉自研TCP协议设备，支持心跳、远程升级等功能",
    enabledByDefault = true
)
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "changhui", havingValue = "true", matchIfMissing = true)
@Slf4j
@RequiredArgsConstructor
public class ChanghuiPlugin implements PassiveDeviceHandler {

    /**
     * 日志前缀
     */
    private static final String LOG_PREFIX = "[ChanghuiPlugin]";

    // ==================== 协议常量 ====================

    /**
     * 帧头: EF7EEF
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
     * 测站编码长度
     */
    private static final int STATION_CODE_LENGTH = 10;

    /**
     * 最小帧长度（帧头 + 长度 + 测站编码）
     */
    private static final int MIN_FRAME_LENGTH = FRAME_HEADER_LENGTH + LENGTH_FIELD_LENGTH + STATION_CODE_LENGTH;

    // ==================== 命令类型常量 ====================

    /**
     * 升级触发命令
     */
    public static final String CMD_UPGRADE_TRIGGER = "UPGRADE_TRIGGER";

    /**
     * 升级URL下发命令
     */
    public static final String CMD_UPGRADE_URL = "UPGRADE_URL";

    /**
     * 查询状态命令
     */
    public static final String CMD_QUERY_STATUS = "QUERY_STATUS";

    /**
     * 模式切换命令
     */
    public static final String CMD_SWITCH_MODE = "SWITCH_MODE";

    /**
     * 手动控制命令（升/降/停）
     */
    public static final String CMD_MANUAL_CONTROL = "MANUAL_CONTROL";

    /**
     * 自动控制命令（流量/开度/水位/水量控制）
     */
    public static final String CMD_AUTO_CONTROL = "AUTO_CONTROL";

    /**
     * 查询数据命令
     */
    public static final String CMD_QUERY_DATA = "QUERY_DATA";

    /**
     * 查询多指标数据命令
     */
    public static final String CMD_QUERY_MULTIPLE_DATA = "QUERY_MULTIPLE_DATA";

    // ==================== 升级状态常量 ====================

    /**
     * 升级状态：开始
     */
    public static final String UPGRADE_STATUS_START = "START";

    /**
     * 升级状态：进行中
     */
    public static final String UPGRADE_STATUS_PROGRESS = "PROGRESS";

    /**
     * 升级状态：完成
     */
    public static final String UPGRADE_STATUS_COMPLETE = "COMPLETE";

    /**
     * 升级状态：失败
     */
    public static final String UPGRADE_STATUS_FAILED = "FAILED";

    // ==================== 依赖注入 ====================

    /**
     * 连接管理器
     */
    private final ChanghuiConnectionManager connectionManager;

    /**
     * 协议编解码器
     */
    private final ChanghuiProtocolCodec protocolCodec;

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
    private final ChanghuiConfig config;

    /**
     * 设备 API（用于从数据库查询设备）
     */
    @Setter(onMethod_ = @Autowired(required = false))
    private IotDeviceCommonApi deviceApi;

    /**
     * 设备升级状态缓存
     * Key: deviceId
     * Value: 升级状态信息
     */
    private final Map<Long, UpgradeState> upgradeStates = new ConcurrentHashMap<>();

    /**
     * 升级保护设备集合
     * <p>
     * 记录正在升级的设备，这些设备在升级期间不进行心跳超时检测
     * Key: deviceId
     * Value: 升级保护开始时间（毫秒时间戳）
     * </p>
     */
    private final Map<Long, Long> upgradeProtectedDevices = new ConcurrentHashMap<>();

    // ==================== DeviceHandler 接口实现 ====================

    @Override
    public String getDeviceType() {
        return PluginConstants.DEVICE_TYPE_CHANGHUI;
    }

    @Override
    public String getVendor() {
        return "Changhui";
    }

    @Override
    public boolean supports(DeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            return false;
        }
        return PluginConstants.DEVICE_TYPE_CHANGHUI.equalsIgnoreCase(deviceInfo.getDeviceType());
    }


    @Override
    public CommandResult executeCommand(Long deviceId, DeviceCommand command) {
        if (deviceId == null || command == null) {
            return CommandResult.failure("参数不能为空");
        }

        String commandType = command.getCommandType();
        if (commandType == null || commandType.isEmpty()) {
            return CommandResult.failure("命令类型不能为空");
        }

        log.info("{} 执行命令: deviceId={}, commandType={}", LOG_PREFIX, deviceId, commandType);

        try {
            // 检查设备是否在线（某些命令不需要设备在线）
            boolean requiresOnline = !CMD_QUERY_STATUS.equals(commandType);
            
            // 诊断日志：检查连接状态
            Channel connection = connectionManager.getConnection(deviceId);
            String stationCode = connectionManager.getStationCode(deviceId);
            boolean isOnline = connectionManager.isOnline(deviceId);
            int totalConnections = connectionManager.getOnlineCount();
            
            log.info("{} ========== 命令执行诊断 ==========", LOG_PREFIX);
            log.info("{} deviceId={}, commandType={}", LOG_PREFIX, deviceId, commandType);
            log.info("{} stationCode={}", LOG_PREFIX, stationCode);
            log.info("{} connection存在={}", LOG_PREFIX, connection != null);
            log.info("{} connection.isActive={}", LOG_PREFIX, connection != null ? connection.isActive() : "N/A");
            log.info("{} isOnline={}", LOG_PREFIX, isOnline);
            log.info("{} 当前总连接数={}", LOG_PREFIX, totalConnections);
            log.info("{} ================================", LOG_PREFIX);
            
            if (requiresOnline && !isOnline) {
                log.error("{} ❌ 设备未连接，无法执行命令！请检查设备是否已通过TCP连接到网关端口9700", LOG_PREFIX);
                log.error("{} 提示：设备需要先发送心跳包建立连接，才能接收命令", LOG_PREFIX);
                return CommandResult.failure("设备未连接（TCP连接不存在或已断开）");
            }

            // 根据命令类型分发处理
            switch (commandType) {
                case CMD_QUERY_STATUS:
                    return executeQueryStatus(deviceId);
                case CMD_UPGRADE_TRIGGER:
                    return executeUpgradeTrigger(deviceId);
                case CMD_UPGRADE_URL:
                    return executeUpgradeUrl(deviceId, command);
                case CMD_SWITCH_MODE:
                    return executeSwitchMode(deviceId, command);
                case CMD_MANUAL_CONTROL:
                    return executeManualControl(deviceId, command);
                case CMD_AUTO_CONTROL:
                    return executeAutoControl(deviceId, command);
                case CMD_QUERY_DATA:
                    return executeQueryData(deviceId, command);
                case CMD_QUERY_MULTIPLE_DATA:
                    return executeQueryMultipleData(deviceId, command);
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
            // 根据测站编码查找设备ID
            Long deviceId = lookupDeviceId(deviceIdentifier);
            
            if (deviceId == null) {
                log.warn("{} 未找到设备: identifier={}", LOG_PREFIX, deviceIdentifier);
                // 可以选择关闭连接或等待后续消息
                return;
            }

            // 1. 注册连接（ConnectionManager 只负责连接管理）
            connectionManager.register(deviceId, deviceIdentifier, ctx.channel());
            
            // 2. 构建设备连接信息（包含完整元数据）
            DeviceConnectionInfo connectionInfo = buildConnectionInfo(deviceId, deviceIdentifier);
            
            // 3. 原子地更新设备生命周期状态（注册 -> 激活 -> 上线）
            // Requirements: 3.1, 4.1
            boolean online = lifecycleManager.onDeviceConnected(deviceId, connectionInfo);
            if (online) {
                log.info("{} 设备连接成功（原子操作）: deviceId={}, stationCode={}", LOG_PREFIX, deviceId, deviceIdentifier);
            } else {
                log.warn("{} 设备上线状态更新失败: deviceId={}, stationCode={}", LOG_PREFIX, deviceId, deviceIdentifier);
            }
        } catch (Exception e) {
            log.error("{} 处理设备连接失败: identifier={}", LOG_PREFIX, deviceIdentifier, e);
        }
    }
    
    /**
     * 构建设备连接信息（包含完整元数据）
     * <p>
     * 从数据库获取设备的 tenantId、productId、deviceName 等信息。
     * </p>
     *
     * @param deviceId    设备ID
     * @param stationCode 测站编码
     * @return 设备连接信息
     */
    private DeviceConnectionInfo buildConnectionInfo(Long deviceId, String stationCode) {
        DeviceConnectionInfo.DeviceConnectionInfoBuilder builder = DeviceConnectionInfo.builder()
                .deviceId(deviceId)
                .deviceType(PluginConstants.DEVICE_TYPE_CHANGHUI)
                .vendor("Changhui")
                .connectionMode(ConnectionMode.PASSIVE);
        
        // 从数据库获取完整的设备信息
        if (deviceApi != null) {
            try {
                IotDeviceGetReqDTO reqDTO = new IotDeviceGetReqDTO();
                reqDTO.setId(deviceId);
                
                CommonResult<IotDeviceRespDTO> result = deviceApi.getDevice(reqDTO);
                if (result != null && result.isSuccess() && result.getData() != null) {
                    IotDeviceRespDTO device = result.getData();
                    builder.deviceName(device.getDeviceName())
                           .productId(device.getProductId())
                           .tenantId(device.getTenantId());
                    log.debug("{} 获取设备元数据成功: deviceId={}, tenantId={}, productId={}", 
                            LOG_PREFIX, deviceId, device.getTenantId(), device.getProductId());
                } else {
                    log.warn("{} 获取设备元数据失败: deviceId={}, result={}", 
                            LOG_PREFIX, deviceId, result != null ? result.getMsg() : "null");
                }
            } catch (Exception e) {
                log.error("{} 查询设备信息失败: deviceId={}", LOG_PREFIX, deviceId, e);
            }
        } else {
            log.warn("{} deviceApi 未注入，无法获取设备完整元数据: deviceId={}", LOG_PREFIX, deviceId);
        }
        
        return builder.build();
    }

    @Override
    public void onHeartbeat(Long deviceId, HeartbeatData data) {
        if (deviceId == null) {
            return;
        }

        log.trace("{} 收到心跳: deviceId={}", LOG_PREFIX, deviceId);

        try {
            // 更新心跳时间
            connectionManager.updateHeartbeat(deviceId);
            lifecycleManager.updateLastSeen(deviceId);

            // 如果设备之前不在线，使用原子方法重新上线
            if (!lifecycleManager.isOnline(deviceId)) {
                String stationCode = connectionManager.getStationCode(deviceId);
                DeviceConnectionInfo connectionInfo = buildConnectionInfo(deviceId, stationCode);
                log.info("{} 尝试心跳触发上线: deviceId={}, tenantId={}, deviceType={}", 
                        LOG_PREFIX, deviceId, connectionInfo.getTenantId(), connectionInfo.getDeviceType());
                boolean online = lifecycleManager.onDeviceConnected(deviceId, connectionInfo);
                if (online) {
                    log.info("{} 设备心跳触发重新上线成功: deviceId={}", LOG_PREFIX, deviceId);
                } else {
                    log.warn("{} 设备心跳触发重新上线失败: deviceId={}", LOG_PREFIX, deviceId);
                }
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
            // Requirements: 3.3, 4.4
            boolean offline = lifecycleManager.onDeviceDisconnected(deviceId, "TCP连接断开");
            if (offline) {
                log.info("{} 设备离线状态更新成功（原子操作）: deviceId={}", LOG_PREFIX, deviceId);
            }
            
            // 2. 注销连接（ConnectionManager 只负责连接管理）
            connectionManager.unregister(deviceId);
        } catch (Exception e) {
            log.error("{} 处理设备断开失败: deviceId={}", LOG_PREFIX, deviceId, e);
        }
    }

    @Override
    public String parseDeviceIdentifier(ByteBuf data) {
        if (data == null || data.readableBytes() < MIN_FRAME_LENGTH) {
            return null;
        }

        try {
            // 验证帧头
            if (!validateFrameHeader(data)) {
                log.warn("{} 无效的帧头", LOG_PREFIX);
                return null;
            }

            // 跳过帧头(3) + 长度(4)
            int stationCodeOffset = FRAME_HEADER_LENGTH + LENGTH_FIELD_LENGTH;
            
            // 读取测站编码(10字节)
            byte[] stationCodeBytes = new byte[STATION_CODE_LENGTH];
            data.getBytes(data.readerIndex() + stationCodeOffset, stationCodeBytes);
            
            // 转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : stationCodeBytes) {
                sb.append(String.format("%02X", b));
            }
            
            String stationCode = sb.toString();
            log.debug("{} 解析测站编码: {}", LOG_PREFIX, stationCode);
            
            return stationCode;
        } catch (Exception e) {
            log.error("{} 解析测站编码失败", LOG_PREFIX, e);
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
            // 更新最后活跃时间
            connectionManager.updateHeartbeat(deviceId);
            lifecycleManager.updateLastSeen(deviceId);
            
            // 关键修复：收到任何数据时，如果设备不在线则触发上线
            // 这确保即使心跳解析失败，设备也能正确上线
            if (!lifecycleManager.isOnline(deviceId)) {
                String stationCode = connectionManager.getStationCode(deviceId);
                DeviceConnectionInfo connectionInfo = buildConnectionInfo(deviceId, stationCode);
                log.info("{} 尝试触发设备上线: deviceId={}, stationCode={}, tenantId={}, deviceType={}", 
                        LOG_PREFIX, deviceId, stationCode, connectionInfo.getTenantId(), connectionInfo.getDeviceType());
                boolean online = lifecycleManager.onDeviceConnected(deviceId, connectionInfo);
                if (online) {
                    log.info("{} 设备数据触发上线成功: deviceId={}, stationCode={}", LOG_PREFIX, deviceId, stationCode);
                } else {
                    log.warn("{} 设备数据触发上线失败: deviceId={}, stationCode={}", LOG_PREFIX, deviceId, stationCode);
                }
            }
            
            // 解析消息类型
            ChanghuiMessageType messageType = protocolCodec.parseMessageType(data);
            if (messageType == null) {
                log.warn("{} 无法解析消息类型: deviceId={}", LOG_PREFIX, deviceId);
                return;
            }

            log.debug("{} 消息类型: deviceId={}, type={}", LOG_PREFIX, deviceId, messageType);

            // 根据消息类型分发处理
            switch (messageType) {
                case HEARTBEAT:
                    handleHeartbeatMessage(deviceId, data);
                    break;
                // 升级触发响应处理
                case UPGRADE_TRIGGER:
                    handleUpgradeTriggerResponse(deviceId, data);
                    break;
                // 升级URL响应处理
                case UPGRADE_URL:
                    handleUpgradeUrlResponse(deviceId, data);
                    break;
                case UPGRADE_STATUS_START:
                    handleUpgradeStatusStart(deviceId, data);
                    break;
                case UPGRADE_STATUS_PROGRESS:
                    handleUpgradeStatusProgress(deviceId, data);
                    break;
                case UPGRADE_STATUS_COMPLETE:
                    handleUpgradeStatusComplete(deviceId, data);
                    break;
                case UPGRADE_STATUS_FAILED:
                    handleUpgradeStatusFailed(deviceId, data);
                    break;
                // 数据采集消息处理 (Requirements 11.1-11.8)
                case DATA_WATER_LEVEL:
                case DATA_INSTANT_FLOW:
                case DATA_INSTANT_VELOCITY:
                case DATA_CUMULATIVE_FLOW:
                case DATA_GATE_POSITION:
                case DATA_SEEPAGE_PRESSURE:
                case DATA_TEMPERATURE:
                case DATA_LOAD:
                case DATA_MULTI_QUERY:
                    handleDataReportMessage(deviceId, data, messageType);
                    break;
                // 报警消息处理 (Requirements 13.1)
                case ALARM:
                    handleAlarmMessage(deviceId, data);
                    break;
                // 控制响应消息处理 (Requirements 14.5)
                case CONTROL_RESPONSE:
                    handleControlResponseMessage(deviceId, data);
                    break;
                default:
                    log.debug("{} 未处理的消息类型: deviceId={}, type={}", LOG_PREFIX, deviceId, messageType);
                    break;
            }
        } catch (Exception e) {
            log.error("{} 处理数据失败: deviceId={}", LOG_PREFIX, deviceId, e);
        }
    }

    /**
     * 处理心跳消息
     *
     * @param deviceId 设备ID
     * @param data     数据
     */
    private void handleHeartbeatMessage(Long deviceId, ByteBuf data) {
        log.trace("{} 收到心跳消息: deviceId={}", LOG_PREFIX, deviceId);

        // 解析心跳信息
        ChanghuiProtocolCodec.HeartbeatInfo heartbeatInfo = protocolCodec.parseHeartbeat(data);
        if (heartbeatInfo == null) {
            log.warn("{} 解析心跳消息失败: deviceId={}", LOG_PREFIX, deviceId);
            return;
        }

        log.trace("{} 心跳信息: deviceId={}, stationCode={}", 
                LOG_PREFIX, deviceId, heartbeatInfo.getStationCode());

        // 构建心跳数据
        HeartbeatData heartbeatData = HeartbeatData.builder()
                .deviceId(deviceId)
                .timestamp(heartbeatInfo.getTimestamp())
                .build();

        // 调用心跳处理
        onHeartbeat(deviceId, heartbeatData);

        // 发送心跳响应
        sendHeartbeatResponse(deviceId);
        
        log.trace("{} 心跳处理完成: deviceId={}", LOG_PREFIX, deviceId);
    }

    /**
     * 发送心跳响应
     *
     * @param deviceId 设备ID
     */
    private void sendHeartbeatResponse(Long deviceId) {
        Channel channel = connectionManager.getConnection(deviceId);
        if (channel == null) {
            log.warn("{} 无法发送心跳响应，连接为空: deviceId={}", LOG_PREFIX, deviceId);
            return;
        }
        if (!channel.isActive()) {
            log.warn("{} 无法发送心跳响应，连接不活跃: deviceId={}", LOG_PREFIX, deviceId);
            return;
        }

        String stationCode = connectionManager.getStationCode(deviceId);
        if (stationCode == null) {
            log.warn("{} 无法发送心跳响应，测站编码为空: deviceId={}", LOG_PREFIX, deviceId);
            return;
        }

        byte[] responseFrame = protocolCodec.buildHeartbeatResponseFrame(stationCode);
        if (responseFrame != null) {
            try {
                // 打印发送的响应帧（十六进制格式，便于调试）
                if (log.isDebugEnabled()) {
                    StringBuilder hexBuilder = new StringBuilder();
                    for (byte b : responseFrame) {
                        hexBuilder.append(String.format("%02X", b & 0xFF));
                    }
                    log.debug("{} 心跳响应帧内容: deviceId={}, hex={}", 
                            LOG_PREFIX, deviceId, hexBuilder.toString());
                }
                
                channel.writeAndFlush(Unpooled.wrappedBuffer(responseFrame));
                log.trace("{} 心跳响应已发送: deviceId={}, stationCode={}, frameLength={}", 
                        LOG_PREFIX, deviceId, stationCode, responseFrame.length);
            } catch (Exception e) {
                log.error("{} 发送心跳响应失败: deviceId={}", LOG_PREFIX, deviceId, e);
            }
        } else {
            log.warn("{} 构建心跳响应帧失败: deviceId={}, stationCode={}", LOG_PREFIX, deviceId, stationCode);
        }
    }

    /**
     * 处理升级触发响应 (AFN=0x02)
     * <p>
     * 设备收到升级触发命令后的响应。
     * 状态码：00=成功，01=失败
     * </p>
     *
     * @param deviceId 设备ID
     * @param data     数据
     */
    private void handleUpgradeTriggerResponse(Long deviceId, ByteBuf data) {
        // 解析升级触发响应
        ChanghuiProtocolCodec.UpgradeTriggerResponse response = protocolCodec.parseUpgradeTriggerResponse(data);
        if (response == null) {
            log.warn("{} 解析升级触发响应失败: deviceId={}", LOG_PREFIX, deviceId);
            return;
        }

        String stationCode = response.getStationCode();
        if (stationCode == null || stationCode.isEmpty()) {
            stationCode = connectionManager.getStationCode(deviceId);
        }

        if (response.isSuccess()) {
            log.info("{} 升级触发成功: deviceId={}, stationCode={}, protocolType=0x{}", 
                    LOG_PREFIX, deviceId, stationCode, 
                    String.format("%04X", response.getProtocolType()));
            
            // 更新升级状态为已触发
            UpgradeState state = upgradeStates.computeIfAbsent(deviceId, k -> new UpgradeState());
            state.setStatus("TRIGGER_SUCCESS");
            state.setLastUpdateTime(System.currentTimeMillis());
            
            // 发布 TRIGGER_SUCCESS 事件到业务侧，业务侧收到后会发送 UPGRADE_URL
            publishUpgradeTriggerSuccessEvent(deviceId, stationCode, response.getProtocolType());
        } else {
            log.error("{} 升级触发失败: deviceId={}, stationCode={}, statusCode=0x{}", 
                    LOG_PREFIX, deviceId, stationCode, 
                    String.format("%02X", response.getStatusCode()));
            
            // 更新升级状态为触发失败
            UpgradeState state = upgradeStates.get(deviceId);
            if (state != null) {
                state.setStatus("TRIGGER_FAILED");
                state.setErrorMessage("设备拒绝升级触发");
                state.setEndTime(System.currentTimeMillis());
            }
            
            // 发布 TRIGGER_FAILED 事件
            publishUpgradeTriggerFailedEvent(deviceId, stationCode, "设备拒绝升级触发，状态码: 0x" + 
                    String.format("%02X", response.getStatusCode()));
            
            // 清理升级状态
            upgradeStates.remove(deviceId);
        }
    }
    
    /**
     * 发布升级触发成功事件
     * 
     * <p>业务侧收到此事件后应发送 UPGRADE_URL 命令</p>
     * 
     * @param deviceId 设备ID
     * @param stationCode 测站编码
     * @param protocolType 协议类型
     */
    private void publishUpgradeTriggerSuccessEvent(Long deviceId, String stationCode, int protocolType) {
        ChanghuiUpgradeStatusMessage event = ChanghuiUpgradeStatusMessage.ofTriggerSuccess(
                deviceId, PluginConstants.DEVICE_TYPE_CHANGHUI, stationCode, protocolType);
        
        // 发布事件到统一的设备事件主题
        messagePublisher.publishEvent(IotMessageTopics.DEVICE_EVENT_REPORTED, event);
        
        log.info("{} 已发布升级触发成功事件: deviceId={}, stationCode={}", LOG_PREFIX, deviceId, stationCode);
    }
    
    /**
     * 发布升级触发失败事件
     * 
     * @param deviceId 设备ID
     * @param stationCode 测站编码
     * @param errorMessage 错误信息
     */
    private void publishUpgradeTriggerFailedEvent(Long deviceId, String stationCode, String errorMessage) {
        ChanghuiUpgradeStatusMessage event = ChanghuiUpgradeStatusMessage.ofTriggerFailed(
                deviceId, PluginConstants.DEVICE_TYPE_CHANGHUI, stationCode, errorMessage);
        
        // 发布事件到统一的设备事件主题
        messagePublisher.publishEvent(IotMessageTopics.DEVICE_EVENT_REPORTED, event);
        
        log.error("{} 已发布升级触发失败事件: deviceId={}, stationCode={}, error={}", 
                LOG_PREFIX, deviceId, stationCode, errorMessage);
    }

    /**
     * 处理升级URL响应 (AFN=0x10)
     * <p>
     * 设备收到升级URL后的响应。
     * 状态码：00=接收成功，01=接收失败，02=下载中，03=下载完成
     * </p>
     *
     * @param deviceId 设备ID
     * @param data     数据
     */
    private void handleUpgradeUrlResponse(Long deviceId, ByteBuf data) {
        // 解析升级URL响应
        ChanghuiProtocolCodec.UpgradeUrlResponse response = protocolCodec.parseUpgradeUrlResponse(data);
        if (response == null) {
            log.warn("{} 解析升级URL响应失败: deviceId={}", LOG_PREFIX, deviceId);
            return;
        }

        String stationCode = response.getStationCode();
        if (stationCode == null || stationCode.isEmpty()) {
            stationCode = connectionManager.getStationCode(deviceId);
        }

        log.info("{} 升级URL响应: deviceId={}, stationCode={}, status={} (0x{}), protocolType=0x{}", 
                LOG_PREFIX, deviceId, stationCode, 
                response.getStatusDescription(), String.format("%02X", response.getStatusCode()),
                String.format("%04X", response.getProtocolType()));

        // 更新升级状态
        UpgradeState state = upgradeStates.computeIfAbsent(deviceId, k -> new UpgradeState());
        state.setLastUpdateTime(System.currentTimeMillis());

        if (response.isReceiveSuccess()) {
            // URL接收成功，等待设备开始下载
            state.setStatus("URL_RECEIVED");
            log.info("{} 设备已接收升级URL: deviceId={}", LOG_PREFIX, deviceId);
            
            // 发布 URL_RECEIVED 事件
            publishUpgradeUrlStatusEvent(deviceId, stationCode, "URL_RECEIVED", 5, null);
        } else if (response.isDownloading()) {
            // 正在下载固件
            state.setStatus("DOWNLOADING");
            log.info("{} 设备正在下载固件: deviceId={}", LOG_PREFIX, deviceId);
            
            // 发布 DOWNLOADING 事件
            publishUpgradeUrlStatusEvent(deviceId, stationCode, "DOWNLOADING", 10, null);
        } else if (response.isDownloadComplete()) {
            // 下载完成，等待开始升级
            state.setStatus("DOWNLOAD_COMPLETE");
            log.info("{} 设备固件下载完成: deviceId={}", LOG_PREFIX, deviceId);
            
            // 发布 DOWNLOAD_COMPLETE 事件
            publishUpgradeUrlStatusEvent(deviceId, stationCode, "DOWNLOAD_COMPLETE", 50, null);
        } else if (response.isReceiveFailed()) {
            // URL接收失败
            state.setStatus("URL_RECEIVE_FAILED");
            state.setErrorMessage("设备接收URL失败");
            state.setEndTime(System.currentTimeMillis());
            
            log.error("{} 设备接收URL失败: deviceId={}", LOG_PREFIX, deviceId);
            
            // 发布升级失败事件
            publishUpgradeUrlStatusEvent(deviceId, stationCode, "URL_RECEIVE_FAILED", 0, "设备接收URL失败");
            
            // 清理升级状态
            upgradeStates.remove(deviceId);
        }
    }
    
    /**
     * 发布升级URL响应状态事件
     * 
     * @param deviceId 设备ID
     * @param stationCode 测站编码
     * @param statusName 状态名称
     * @param progress 进度
     * @param errorMessage 错误信息
     */
    private void publishUpgradeUrlStatusEvent(Long deviceId, String stationCode, String statusName, 
            int progress, String errorMessage) {
        int eventType;
        int status;
        switch (statusName) {
            case "URL_RECEIVED":
                eventType = ChanghuiUpgradeStatusMessage.EventType.URL_RECEIVED;
                status = ChanghuiUpgradeStatusMessage.Status.IN_PROGRESS;
                break;
            case "DOWNLOADING":
                eventType = ChanghuiUpgradeStatusMessage.EventType.DOWNLOADING;
                status = ChanghuiUpgradeStatusMessage.Status.IN_PROGRESS;
                break;
            case "DOWNLOAD_COMPLETE":
                eventType = ChanghuiUpgradeStatusMessage.EventType.DOWNLOAD_COMPLETE;
                status = ChanghuiUpgradeStatusMessage.Status.IN_PROGRESS;
                break;
            case "URL_RECEIVE_FAILED":
                eventType = ChanghuiUpgradeStatusMessage.EventType.URL_RECEIVE_FAILED;
                status = ChanghuiUpgradeStatusMessage.Status.FAILED;
                break;
            default:
                eventType = ChanghuiUpgradeStatusMessage.EventType.UPGRADE_STATUS;
                status = ChanghuiUpgradeStatusMessage.Status.IN_PROGRESS;
        }
        
        ChanghuiUpgradeStatusMessage event = ChanghuiUpgradeStatusMessage.builder()
                .deviceId(deviceId)
                .deviceType(PluginConstants.DEVICE_TYPE_CHANGHUI)
                .stationCode(stationCode)
                .eventType(eventType)
                .eventTypeName(statusName)
                .status(status)
                .statusName(statusName)
                .progress(progress)
                .errorMessage(errorMessage)
                .timestamp(System.currentTimeMillis())
                .build();
        
        messagePublisher.publishEvent(IotMessageTopics.DEVICE_EVENT_REPORTED, event);
        
        log.info("{} 已发布升级URL状态事件: deviceId={}, stationCode={}, status={}, progress={}", 
                LOG_PREFIX, deviceId, stationCode, statusName, progress);
    }

    /**
     * 处理升级开始状态 (AFN=0x15)
     *
     * @param deviceId 设备ID
     * @param data     数据
     */
    private void handleUpgradeStatusStart(Long deviceId, ByteBuf data) {
        log.info("{} 设备开始升级: deviceId={}", LOG_PREFIX, deviceId);

        // 更新升级状态
        UpgradeState state = upgradeStates.computeIfAbsent(deviceId, k -> new UpgradeState());
        state.setStatus(UPGRADE_STATUS_START);
        state.setProgress(0);
        state.setStartTime(System.currentTimeMillis());

        // 发布升级状态事件
        publishUpgradeStatusEvent(deviceId, UPGRADE_STATUS_START, 0, null);
    }

    /**
     * 处理升级进度状态 (AFN=0x66)
     *
     * @param deviceId 设备ID
     * @param data     数据
     */
    private void handleUpgradeStatusProgress(Long deviceId, ByteBuf data) {
        // 解析升级状态
        ChanghuiProtocolCodec.UpgradeStatusInfo statusInfo = protocolCodec.parseUpgradeStatus(data);
        int progress = statusInfo != null && statusInfo.getProgress() != null ? statusInfo.getProgress() : 0;

        log.info("{} 设备升级进度: deviceId={}, progress={}%", LOG_PREFIX, deviceId, progress);

        // 更新升级状态
        UpgradeState state = upgradeStates.computeIfAbsent(deviceId, k -> new UpgradeState());
        state.setStatus(UPGRADE_STATUS_PROGRESS);
        state.setProgress(progress);
        state.setLastUpdateTime(System.currentTimeMillis());

        // 发布升级状态事件
        publishUpgradeStatusEvent(deviceId, UPGRADE_STATUS_PROGRESS, progress, null);
    }

    /**
     * 处理升级完成状态 (AFN=0x67)
     *
     * @param deviceId 设备ID
     * @param data     数据
     */
    private void handleUpgradeStatusComplete(Long deviceId, ByteBuf data) {
        log.info("{} 设备升级完成: deviceId={}", LOG_PREFIX, deviceId);

        // 更新升级状态
        UpgradeState state = upgradeStates.get(deviceId);
        if (state != null) {
            state.setStatus(UPGRADE_STATUS_COMPLETE);
            state.setProgress(100);
            state.setEndTime(System.currentTimeMillis());
        }

        // 发布升级状态事件
        publishUpgradeStatusEvent(deviceId, UPGRADE_STATUS_COMPLETE, 100, null);

        // 清理升级状态和退出升级保护
        upgradeStates.remove(deviceId);
        exitUpgradeProtection(deviceId);
    }

    /**
     * 处理升级失败状态 (AFN=0x68)
     *
     * @param deviceId 设备ID
     * @param data     数据
     */
    private void handleUpgradeStatusFailed(Long deviceId, ByteBuf data) {
        // 解析错误信息
        ChanghuiProtocolCodec.UpgradeStatusInfo statusInfo = protocolCodec.parseUpgradeStatus(data);
        String errorMessage = statusInfo != null ? statusInfo.getErrorMessage() : "未知错误";

        log.error("{} 设备升级失败: deviceId={}, error={}", LOG_PREFIX, deviceId, errorMessage);

        // 更新升级状态
        UpgradeState state = upgradeStates.get(deviceId);
        if (state != null) {
            state.setStatus(UPGRADE_STATUS_FAILED);
            state.setErrorMessage(errorMessage);
            state.setEndTime(System.currentTimeMillis());
        }

        // 发布升级状态事件
        publishUpgradeStatusEvent(deviceId, UPGRADE_STATUS_FAILED, 
                state != null ? state.getProgress() : 0, errorMessage);

        // 清理升级状态和退出升级保护
        upgradeStates.remove(deviceId);
        exitUpgradeProtection(deviceId);
    }

    /**
     * 发布升级状态事件
     * 
     * <p>使用强类型 DTO（ChanghuiUpgradeStatusMessage）发布事件，符合 Requirements 4.3</p>
     *
     * @param deviceId     设备ID
     * @param status       升级状态（字符串，将转换为整数常量）
     * @param progress     进度
     * @param errorMessage 错误信息
     */
    private void publishUpgradeStatusEvent(Long deviceId, String status, int progress, String errorMessage) {
        // 将字符串状态转换为整数常量（与 Biz 的 ChanghuiUpgradeStatusEnum 对齐）
        int statusInt = convertStatusNameToInt(status);
        // 补充测站编码，Biz 侧用 stationCode 去关联升级任务
        String stationCode = connectionManager.getStationCode(deviceId);
        // 事件类型：用于 DEVICE_EVENT_REPORTED 的 eventTypeName 路由
        String eventTypeName = determineUpgradeEventTypeName(statusInt);
        
        ChanghuiUpgradeStatusMessage event = ChanghuiUpgradeStatusMessage.builder()
                .deviceId(deviceId)
                .status(statusInt)
                .statusName(status)
                .stationCode(stationCode)
                .progress(progress)
                .timestamp(System.currentTimeMillis())
                .errorMessage(errorMessage != null ? errorMessage : "")
                .deviceType(PluginConstants.DEVICE_TYPE_CHANGHUI)
                .eventTypeName(eventTypeName)
                .eventType(determineUpgradeEventType(statusInt))
                .build();

        // 发布事件到统一的设备事件主题 DEVICE_EVENT_REPORTED
        messagePublisher.publishEvent(IotMessageTopics.DEVICE_EVENT_REPORTED, event);
    }
    
    /**
     * 将状态名称转换为整数常量
     *
     * @param statusName 状态名称
     * @return 状态常量
     */
    private int convertStatusNameToInt(String statusName) {
        if (statusName == null) {
            return 0;
        }
        switch (statusName.toUpperCase()) {
            // 网关侧协议状态 → Biz侧升级状态
            case "TRIGGERED":
            case "PENDING":
                return ChanghuiUpgradeStatusMessage.Status.PENDING;
            case "START":
            case "PROGRESS":
            case "IN_PROGRESS":
                return ChanghuiUpgradeStatusMessage.Status.IN_PROGRESS;
            case "COMPLETE":
            case "SUCCESS":
                return ChanghuiUpgradeStatusMessage.Status.SUCCESS;
            case "FAILED":
                return ChanghuiUpgradeStatusMessage.Status.FAILED;
            case "CANCELLED":
                return ChanghuiUpgradeStatusMessage.Status.CANCELLED;
            default: return 0;
        }
    }
    
    private int determineUpgradeEventType(Integer statusInt) {
        if (statusInt == null) {
            return ChanghuiUpgradeStatusMessage.EventType.UPGRADE_STATUS;
        }
        switch (statusInt) {
            case ChanghuiUpgradeStatusMessage.Status.IN_PROGRESS:
                return ChanghuiUpgradeStatusMessage.EventType.UPGRADE_PROGRESS;
            case ChanghuiUpgradeStatusMessage.Status.SUCCESS:
                return ChanghuiUpgradeStatusMessage.EventType.UPGRADE_COMPLETE;
            case ChanghuiUpgradeStatusMessage.Status.FAILED:
                return ChanghuiUpgradeStatusMessage.EventType.UPGRADE_FAILED;
            default:
                return ChanghuiUpgradeStatusMessage.EventType.UPGRADE_STATUS;
        }
    }
    
    private String determineUpgradeEventTypeName(Integer statusInt) {
        if (statusInt == null) {
            return "UPGRADE_STATUS";
        }
        switch (statusInt) {
            case ChanghuiUpgradeStatusMessage.Status.IN_PROGRESS:
                return "UPGRADE_PROGRESS";
            case ChanghuiUpgradeStatusMessage.Status.SUCCESS:
                return "UPGRADE_COMPLETE";
            case ChanghuiUpgradeStatusMessage.Status.FAILED:
                return "UPGRADE_FAILED";
            default:
                return "UPGRADE_STATUS";
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 验证帧头
     *
     * @param data 数据
     * @return 是否有效
     */
    private boolean validateFrameHeader(ByteBuf data) {
        if (data.readableBytes() < FRAME_HEADER_LENGTH) {
            return false;
        }
        
        for (int i = 0; i < FRAME_HEADER_LENGTH; i++) {
            if (data.getByte(data.readerIndex() + i) != FRAME_HEADER[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 根据测站编码查找设备ID
     * <p>
     * 首先从连接管理器查找（已连接的设备），
     * 如果未找到，则通过 RPC 调用 biz 模块查询数据库。
     * </p>
     *
     * @param stationCode 测站编码（对应 deviceKey）
     * @return 设备ID，如果未找到则返回 null
     */
    private Long lookupDeviceId(String stationCode) {
        if (stationCode == null || stationCode.isEmpty()) {
            return null;
        }
        
        // 1. 首先从连接管理器查找（已连接的设备）
        Long deviceId = connectionManager.getDeviceIdByIdentifier(stationCode);
        if (deviceId != null) {
            log.debug("{} 从连接管理器找到设备: stationCode={}, deviceId={}", LOG_PREFIX, stationCode, deviceId);
            return deviceId;
        }
        
        // 2. 通过 RPC 调用 biz 模块查询数据库
        if (deviceApi != null) {
            try {
                IotDeviceGetReqDTO reqDTO = new IotDeviceGetReqDTO();
                reqDTO.setDeviceKey(stationCode);  // 长辉设备的测站编码对应 deviceKey
                
                CommonResult<IotDeviceRespDTO> result = deviceApi.getDevice(reqDTO);
                if (result != null && result.isSuccess() && result.getData() != null) {
                    deviceId = result.getData().getId();
                    log.info("{} 从数据库找到设备: stationCode={}, deviceId={}", LOG_PREFIX, stationCode, deviceId);
                    return deviceId;
                } else {
                    log.warn("{} 数据库中未找到设备: stationCode={}, result={}", 
                            LOG_PREFIX, stationCode, result != null ? result.getMsg() : "null");
                }
            } catch (Exception e) {
                log.error("{} 查询设备失败: stationCode={}", LOG_PREFIX, stationCode, e);
            }
        } else {
            log.warn("{} deviceApi 未注入，无法查询数据库", LOG_PREFIX);
        }
        
        return null;
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

    // ==================== 4G网络优化：命令重试机制 ====================

    /**
     * 带重试的命令发送
     * <p>
     * 针对4G网络不稳定问题，命令发送失败后会进行重试。
     * 使用配置中的 retryCount 和 retryInterval 参数。
     * </p>
     *
     * @param deviceId    设备ID
     * @param frame       命令帧数据
     * @param commandType 命令类型（用于日志）
     * @return 发送结果，成功返回 true
     */
    private boolean sendWithRetry(Long deviceId, byte[] frame, String commandType) {
        int maxRetries = config.getRetryCount();
        long retryInterval = config.getRetryInterval();

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            Channel channel = connectionManager.getConnection(deviceId);
            
            if (channel == null || !channel.isActive()) {
                log.warn("{} 第{}次尝试失败，设备未连接: deviceId={}, commandType={}", 
                        LOG_PREFIX, attempt, deviceId, commandType);
                
                if (attempt < maxRetries) {
                    sleepQuietly(retryInterval);
                }
                continue;
            }

            try {
                // 同步发送并等待完成
                channel.writeAndFlush(Unpooled.wrappedBuffer(frame)).sync();
                
                if (attempt > 1) {
                    log.info("{} 命令重试成功: deviceId={}, commandType={}, attempt={}/{}", 
                            LOG_PREFIX, deviceId, commandType, attempt, maxRetries);
                }
                return true;
            } catch (Exception e) {
                log.warn("{} 第{}次发送失败: deviceId={}, commandType={}, error={}", 
                        LOG_PREFIX, attempt, deviceId, commandType, e.getMessage());
                
                if (attempt < maxRetries) {
                    sleepQuietly(retryInterval);
                }
            }
        }
        
        log.error("{} 命令发送失败，已重试{}次: deviceId={}, commandType={}", 
                LOG_PREFIX, maxRetries, deviceId, commandType);
        return false;
    }

    /**
     * 安静休眠，不抛出异常
     */
    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ==================== 升级状态保护机制 ====================

    /**
     * 进入升级保护模式
     * <p>
     * 升级期间设备可能无法正常发送心跳，调用此方法后
     * 设备在保护时间内不会被心跳超时检测判定为离线。
     * </p>
     *
     * @param deviceId 设备ID
     */
    public void enterUpgradeProtection(Long deviceId) {
        if (deviceId == null) {
            return;
        }
        upgradeProtectedDevices.put(deviceId, System.currentTimeMillis());
        log.info("{} 设备进入升级保护模式: deviceId={}, protectionTime={}ms", 
                LOG_PREFIX, deviceId, config.getUpgradeHeartbeatProtectionTime());
    }

    /**
     * 退出升级保护模式
     *
     * @param deviceId 设备ID
     */
    public void exitUpgradeProtection(Long deviceId) {
        if (deviceId == null) {
            return;
        }
        Long startTime = upgradeProtectedDevices.remove(deviceId);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            log.info("{} 设备退出升级保护模式: deviceId={}, duration={}ms", 
                    LOG_PREFIX, deviceId, duration);
        }
    }

    /**
     * 检查设备是否在升级保护期内
     * <p>
     * 供 HeartbeatTimeoutChecker 调用，保护期内的设备不进行超时检测
     * </p>
     *
     * @param deviceId 设备ID
     * @return 如果在保护期内返回 true
     */
    public boolean isUnderUpgradeProtection(Long deviceId) {
        if (deviceId == null) {
            return false;
        }
        
        Long protectionStartTime = upgradeProtectedDevices.get(deviceId);
        if (protectionStartTime == null) {
            return false;
        }
        
        // 检查是否超过保护时间
        long elapsed = System.currentTimeMillis() - protectionStartTime;
        long protectionTime = config.getUpgradeHeartbeatProtectionTime();
        
        if (elapsed > protectionTime) {
            // 保护时间已过，自动退出
            upgradeProtectedDevices.remove(deviceId);
            log.warn("{} 设备升级保护时间已过，自动退出: deviceId={}, elapsed={}ms", 
                    LOG_PREFIX, deviceId, elapsed);
            return false;
        }
        
        return true;
    }

    /**
     * 获取所有处于升级保护的设备ID集合
     * <p>
     * 供外部组件查询当前受保护的设备列表
     * </p>
     *
     * @return 受保护的设备ID集合
     */
    public java.util.Set<Long> getUpgradeProtectedDevices() {
        // 清理过期的保护设备
        long now = System.currentTimeMillis();
        long protectionTime = config.getUpgradeHeartbeatProtectionTime();
        
        upgradeProtectedDevices.entrySet().removeIf(entry -> {
            long elapsed = now - entry.getValue();
            return elapsed > protectionTime;
        });
        
        return new java.util.HashSet<>(upgradeProtectedDevices.keySet());
    }

    // ==================== 升级命令执行 ====================

    /**
     * 执行升级触发命令
     * <p>
     * 发送 AFN=0x02 升级触发帧。
     * 针对4G网络优化：使用带重试的发送机制。
     * </p>
     *
     * @param deviceId 设备ID
     * @return 命令结果
     */
    private CommandResult executeUpgradeTrigger(Long deviceId) {
        Channel channel = connectionManager.getConnection(deviceId);
        if (channel == null || !channel.isActive()) {
            return CommandResult.failure("设备连接不可用");
        }

        // 检查是否已在升级中
        if (isUpgrading(deviceId)) {
            return CommandResult.failure("设备正在升级中，请等待升级完成");
        }

        // 获取测站编码
        String stationCode = connectionManager.getStationCode(deviceId);
        if (stationCode == null) {
            return CommandResult.failure("无法获取设备测站编码");
        }

        // 构建升级触发帧
        byte[] frame = protocolCodec.buildUpgradeTriggerFrame(stationCode);
        if (frame == null) {
            return CommandResult.failure("构建升级触发帧失败");
        }

        // 打印发送的报文（十六进制格式）
        if (log.isInfoEnabled()) {
            StringBuilder hexFrame = new StringBuilder();
            for (byte b : frame) {
                hexFrame.append(String.format("%02X", b & 0xFF));
            }
            log.info("{} UPGRADE_TRIGGER 下发报文: deviceId={}, hex={}", LOG_PREFIX, deviceId, hexFrame.toString());
        }

        // 使用重试机制发送命令（4G网络优化）
        boolean sent = sendWithRetry(deviceId, frame, CMD_UPGRADE_TRIGGER);
        if (!sent) {
            return CommandResult.failure("升级触发命令发送失败，已重试" + config.getRetryCount() + "次");
        }

        // 进入升级保护模式（防止被心跳超时误判离线）
        enterUpgradeProtection(deviceId);

        // 初始化升级状态
        UpgradeState state = new UpgradeState();
        state.setStatus("TRIGGERED");
        state.setStartTime(System.currentTimeMillis());
        state.setLastUpdateTime(System.currentTimeMillis());
        upgradeStates.put(deviceId, state);

        log.info("{} 升级触发命令已发送: deviceId={}, stationCode={}", LOG_PREFIX, deviceId, stationCode);
        return CommandResult.success(Map.of("message", "升级触发命令已发送"));
    }

    /**
     * 执行升级URL下发命令
     * <p>
     * 发送 AFN=0x10 升级URL帧。
     * 针对4G网络优化：使用带重试的发送机制。
     * </p>
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
     */
    private CommandResult executeUpgradeUrl(Long deviceId, DeviceCommand command) {
        // Biz 侧下发参数为 firmwareUrl；兼容旧字段 url
        String url = command.getParam("firmwareUrl");
        if (url == null || url.isEmpty()) {
            url = command.getParam("url");
        }
        if (url == null || url.isEmpty()) {
            return CommandResult.failure("缺少 firmwareUrl/url 参数");
        }

        Channel channel = connectionManager.getConnection(deviceId);
        if (channel == null || !channel.isActive()) {
            return CommandResult.failure("设备连接不可用");
        }

        // 获取测站编码
        String stationCode = connectionManager.getStationCode(deviceId);
        if (stationCode == null) {
            return CommandResult.failure("无法获取设备测站编码");
        }

        // 构建升级URL帧
        byte[] frame = protocolCodec.buildUpgradeUrlFrame(stationCode, url);
        if (frame == null) {
            return CommandResult.failure("构建升级URL帧失败");
        }

        // 打印发送的报文（十六进制格式）
        if (log.isInfoEnabled()) {
            StringBuilder hexFrame = new StringBuilder();
            for (byte b : frame) {
                hexFrame.append(String.format("%02X", b & 0xFF));
            }
            log.info("{} UPGRADE_URL 下发报文: deviceId={}, hex={}", LOG_PREFIX, deviceId, hexFrame.toString());
        }

        // 使用重试机制发送命令（4G网络优化）
        boolean sent = sendWithRetry(deviceId, frame, CMD_UPGRADE_URL);
        if (!sent) {
            return CommandResult.failure("升级URL命令发送失败，已重试" + config.getRetryCount() + "次");
        }

        // 确保设备在升级保护模式中
        if (!isUnderUpgradeProtection(deviceId)) {
            enterUpgradeProtection(deviceId);
        }

        // 更新升级状态
        UpgradeState state = upgradeStates.computeIfAbsent(deviceId, k -> new UpgradeState());
        state.setUpgradeUrl(url);
        state.setLastUpdateTime(System.currentTimeMillis());

        log.info("{} 升级URL已下发: deviceId={}, url={}", LOG_PREFIX, deviceId, url);
        return CommandResult.success(Map.of("message", "升级URL已下发", "url", url));
    }

    /**
     * 执行模式切换命令
     * <p>
     * 发送 AFN=0x20 模式切换帧
     * </p>
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
     */
    private CommandResult executeSwitchMode(Long deviceId, DeviceCommand command) {
        // TODO: 实现模式切换命令
        log.info("{} 执行模式切换命令: deviceId={}", LOG_PREFIX, deviceId);
        return CommandResult.failure("模式切换命令暂未实现");
    }

    /**
     * 执行手动控制命令
     * <p>
     * 发送 AFN=0x21 手动控制帧（升/降/停）
     * </p>
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
     */
    private CommandResult executeManualControl(Long deviceId, DeviceCommand command) {
        // TODO: 实现手动控制命令
        log.info("{} 执行手动控制命令: deviceId={}", LOG_PREFIX, deviceId);
        return CommandResult.failure("手动控制命令暂未实现");
    }

    /**
     * 执行自动控制命令
     * <p>
     * 发送 AFN=0x22 自动控制帧
     * </p>
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
     */
    private CommandResult executeAutoControl(Long deviceId, DeviceCommand command) {
        // TODO: 实现自动控制命令
        log.info("{} 执行自动控制命令: deviceId={}", LOG_PREFIX, deviceId);
        return CommandResult.failure("自动控制命令暂未实现");
    }

    /**
     * 执行数据查询命令
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
     */
    private CommandResult executeQueryData(Long deviceId, DeviceCommand command) {
        // TODO: 实现数据查询命令
        log.info("{} 执行数据查询命令: deviceId={}", LOG_PREFIX, deviceId);
        return CommandResult.failure("数据查询命令暂未实现");
    }

    /**
     * 执行多指标数据查询命令
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
     */
    private CommandResult executeQueryMultipleData(Long deviceId, DeviceCommand command) {
        // TODO: 实现多指标数据查询命令
        log.info("{} 执行多指标数据查询命令: deviceId={}", LOG_PREFIX, deviceId);
        return CommandResult.failure("多指标数据查询命令暂未实现");
    }

    // ==================== 生命周期方法 ====================

    /**
     * 插件启动时调用
     * <p>
     * TCP 服务器由 Spring 自动启动（通过 @PostConstruct）
     * </p>
     */
    public void onStart() {
        log.info("{} 插件启动: port={}, heartbeatTimeout={}ms, upgradeTimeout={}ms", 
                LOG_PREFIX, config.getPort(), config.getHeartbeatTimeout(), config.getUpgradeTimeout());
    }

    /**
     * 插件停止时调用
     */
    public void onStop() {
        log.info("{} 插件停止", LOG_PREFIX);
        
        // 清理升级状态
        upgradeStates.clear();
        
        // 关闭所有连接
        connectionManager.closeAll();
    }

    /**
     * 健康检查
     *
     * @return 是否健康
     */
    public boolean onHealthCheck() {
        return config.isEnabled();
    }

    /**
     * 获取升级超时时间
     *
     * @return 升级超时时间（毫秒）
     */
    public long getUpgradeTimeout() {
        return config.getUpgradeTimeout();
    }

    /**
     * 获取设备升级状态
     *
     * @param deviceId 设备ID
     * @return 升级状态，如果不存在则返回 null
     */
    public UpgradeState getUpgradeState(Long deviceId) {
        return upgradeStates.get(deviceId);
    }

    /**
     * 检查设备是否正在升级
     *
     * @param deviceId 设备ID
     * @return 是否正在升级
     */
    public boolean isUpgrading(Long deviceId) {
        UpgradeState state = upgradeStates.get(deviceId);
        return state != null && 
               (UPGRADE_STATUS_START.equals(state.getStatus()) || 
                UPGRADE_STATUS_PROGRESS.equals(state.getStatus()));
    }

    // ==================== 数据上报、报警、控制结果处理方法 ====================

    /**
     * 处理数据上报消息
     * <p>
     * 使用统一 Topic（DEVICE_EVENT_REPORTED）发布数据上报事件
     * Requirements: 1.7, 11.1-11.8
     * </p>
     *
     * @param deviceId    设备ID
     * @param data        数据
     * @param messageType 消息类型
     */
    private void handleDataReportMessage(Long deviceId, ByteBuf data, ChanghuiMessageType messageType) {
        log.debug("{} 处理数据上报消息: deviceId={}, type={}", LOG_PREFIX, deviceId, messageType);

        try {
            // 获取测站编码
            String stationCode = connectionManager.getStationCode(deviceId);
            if (stationCode == null) {
                log.warn("{} 无法获取测站编码: deviceId={}", LOG_PREFIX, deviceId);
                return;
            }

            // 解析指标类型和单位
            String indicator = getIndicatorFromMessageType(messageType);
            String unit = getUnitFromMessageType(messageType);
            
            // 解析数据值（简化实现，实际需要根据协议解析数据域）
            BigDecimal value = parseDataValue(data, messageType);
            if (value == null) {
                log.warn("{} 解析数据值失败: deviceId={}, type={}", LOG_PREFIX, deviceId, messageType);
                return;
            }

            // 构建数据上报消息
            ChanghuiDataReportMessage event = ChanghuiDataReportMessage.builder()
                    .deviceId(deviceId)
                    .deviceType(PluginConstants.DEVICE_TYPE_CHANGHUI)
                    .stationCode(stationCode)
                    .indicator(indicator)
                    .value(value)
                    .unit(unit)
                    .eventType(ChanghuiDataReportMessage.EventType.DATA_REPORT)
                    .afnCode(messageType.getAfn())
                    .timestamp(System.currentTimeMillis())
                    .build();

            // 发布事件到统一的设备事件主题 DEVICE_EVENT_REPORTED
            messagePublisher.publishEvent(IotMessageTopics.DEVICE_EVENT_REPORTED, event);
            
            log.info("{} 数据上报事件已发布: deviceId={}, indicator={}, value={}{}", 
                    LOG_PREFIX, deviceId, indicator, value, unit);
        } catch (Exception e) {
            log.error("{} 处理数据上报消息失败: deviceId={}", LOG_PREFIX, deviceId, e);
        }
    }

    /**
     * 处理报警消息
     * <p>
     * 使用统一 Topic（DEVICE_EVENT_REPORTED）发布报警事件
     * Requirements: 1.7, 13.1
     * </p>
     *
     * @param deviceId 设备ID
     * @param data     数据
     */
    private void handleAlarmMessage(Long deviceId, ByteBuf data) {
        log.debug("{} 处理报警消息: deviceId={}", LOG_PREFIX, deviceId);

        try {
            // 获取测站编码
            String stationCode = connectionManager.getStationCode(deviceId);
            if (stationCode == null) {
                log.warn("{} 无法获取测站编码: deviceId={}", LOG_PREFIX, deviceId);
                return;
            }

            // 解析报警类型和值（简化实现，实际需要根据协议解析数据域）
            int alarmType = parseAlarmType(data);
            String alarmValue = parseAlarmValue(data);

            // 构建报警消息
            ChanghuiAlarmMessage event = ChanghuiAlarmMessage.builder()
                    .deviceId(deviceId)
                    .deviceType(PluginConstants.DEVICE_TYPE_CHANGHUI)
                    .stationCode(stationCode)
                    .alarmType(alarmType)
                    .alarmValue(alarmValue)
                    .eventType(ChanghuiAlarmMessage.EventType.ALARM)
                    .timestamp(System.currentTimeMillis())
                    .build();

            // 发布事件到统一的设备事件主题 DEVICE_EVENT_REPORTED
            messagePublisher.publishEvent(IotMessageTopics.DEVICE_EVENT_REPORTED, event);
            
            log.info("{} 报警事件已发布: deviceId={}, alarmType={}, alarmValue={}", 
                    LOG_PREFIX, deviceId, alarmType, alarmValue);
        } catch (Exception e) {
            log.error("{} 处理报警消息失败: deviceId={}", LOG_PREFIX, deviceId, e);
        }
    }

    /**
     * 处理控制响应消息
     * <p>
     * 使用统一 Topic（DEVICE_SERVICE_RESULT）发布控制结果
     * Requirements: 1.7, 14.5
     * </p>
     *
     * @param deviceId 设备ID
     * @param data     数据
     */
    private void handleControlResponseMessage(Long deviceId, ByteBuf data) {
        log.debug("{} 处理控制响应消息: deviceId={}", LOG_PREFIX, deviceId);

        try {
            // 获取测站编码
            String stationCode = connectionManager.getStationCode(deviceId);
            if (stationCode == null) {
                log.warn("{} 无法获取测站编码: deviceId={}", LOG_PREFIX, deviceId);
                return;
            }

            // 解析控制结果（简化实现，实际需要根据协议解析数据域）
            int controlType = parseControlType(data);
            boolean success = parseControlSuccess(data);
            String errorMessage = success ? null : parseControlErrorMessage(data);

            // 构建控制结果消息
            ChanghuiControlResultMessage event = ChanghuiControlResultMessage.builder()
                    .deviceId(deviceId)
                    .deviceType(PluginConstants.DEVICE_TYPE_CHANGHUI)
                    .stationCode(stationCode)
                    .controlType(controlType)
                    .success(success)
                    .resultType(success ? ChanghuiControlResultMessage.ResultType.SUCCESS 
                            : ChanghuiControlResultMessage.ResultType.FAILED)
                    .errorMessage(errorMessage)
                    .timestamp(System.currentTimeMillis())
                    .build();

            // 发布结果到统一的服务结果主题 DEVICE_SERVICE_RESULT
            messagePublisher.publishEvent(IotMessageTopics.DEVICE_SERVICE_RESULT, event);
            
            log.info("{} 控制结果已发布: deviceId={}, controlType={}, success={}", 
                    LOG_PREFIX, deviceId, controlType, success);
        } catch (Exception e) {
            log.error("{} 处理控制响应消息失败: deviceId={}", LOG_PREFIX, deviceId, e);
        }
    }

    // ==================== 数据解析辅助方法 ====================

    /**
     * 根据消息类型获取指标名称
     *
     * @param messageType 消息类型
     * @return 指标名称
     */
    private String getIndicatorFromMessageType(ChanghuiMessageType messageType) {
        switch (messageType) {
            case DATA_WATER_LEVEL: return "waterLevel";
            case DATA_INSTANT_FLOW: return "instantFlow";
            case DATA_INSTANT_VELOCITY: return "instantVelocity";
            case DATA_CUMULATIVE_FLOW: return "cumulativeFlow";
            case DATA_GATE_POSITION: return "gatePosition";
            case DATA_SEEPAGE_PRESSURE: return "seepagePressure";
            case DATA_TEMPERATURE: return "temperature";
            case DATA_LOAD: return "load";
            case DATA_MULTI_QUERY: return "multiQuery";
            default: return "unknown";
        }
    }

    /**
     * 根据消息类型获取单位
     *
     * @param messageType 消息类型
     * @return 单位
     */
    private String getUnitFromMessageType(ChanghuiMessageType messageType) {
        switch (messageType) {
            case DATA_WATER_LEVEL: return "m";
            case DATA_INSTANT_FLOW: return "L/s";
            case DATA_INSTANT_VELOCITY: return "m/s";
            case DATA_CUMULATIVE_FLOW: return "m³";
            case DATA_GATE_POSITION: return "mm";
            case DATA_SEEPAGE_PRESSURE: return "kPa";
            case DATA_TEMPERATURE: return "°C";
            case DATA_LOAD: return "kN";
            default: return "";
        }
    }

    /**
     * 解析数据值
     * <p>
     * 简化实现，实际需要根据协议解析数据域
     * </p>
     *
     * @param data        数据
     * @param messageType 消息类型
     * @return 数据值
     */
    private BigDecimal parseDataValue(ByteBuf data, ChanghuiMessageType messageType) {
        // TODO: 根据协议实现具体的数据解析逻辑
        // 这里提供简化实现，假设数据域从 DATA_OFFSET 开始，4字节浮点数
        try {
            if (data.readableBytes() < ChanghuiProtocolCodec.DATA_OFFSET + 4) {
                return null;
            }
            int intBits = data.getIntLE(data.readerIndex() + ChanghuiProtocolCodec.DATA_OFFSET);
            float floatValue = Float.intBitsToFloat(intBits);
            return BigDecimal.valueOf(floatValue);
        } catch (Exception e) {
            log.warn("{} 解析数据值异常: {}", LOG_PREFIX, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * 解析报警类型
     *
     * @param data 数据
     * @return 报警类型
     */
    private int parseAlarmType(ByteBuf data) {
        // TODO: 根据协议实现具体的报警类型解析逻辑
        try {
            if (data.readableBytes() < ChanghuiProtocolCodec.DATA_OFFSET + 1) {
                return ChanghuiAlarmMessage.AlarmType.OTHER;
            }
            return data.getByte(data.readerIndex() + ChanghuiProtocolCodec.DATA_OFFSET) & 0xFF;
        } catch (Exception e) {
            return ChanghuiAlarmMessage.AlarmType.OTHER;
        }
    }

    /**
     * 解析报警值
     *
     * @param data 数据
     * @return 报警值
     */
    private String parseAlarmValue(ByteBuf data) {
        // TODO: 根据协议实现具体的报警值解析逻辑
        return "";
    }

    /**
     * 解析控制类型
     *
     * @param data 数据
     * @return 控制类型
     */
    private int parseControlType(ByteBuf data) {
        // TODO: 根据协议实现具体的控制类型解析逻辑
        try {
            if (data.readableBytes() < ChanghuiProtocolCodec.DATA_OFFSET + 1) {
                return ChanghuiControlResultMessage.ControlType.OTHER;
            }
            return data.getByte(data.readerIndex() + ChanghuiProtocolCodec.DATA_OFFSET) & 0xFF;
        } catch (Exception e) {
            return ChanghuiControlResultMessage.ControlType.OTHER;
        }
    }

    /**
     * 解析控制是否成功
     *
     * @param data 数据
     * @return 是否成功
     */
    private boolean parseControlSuccess(ByteBuf data) {
        // TODO: 根据协议实现具体的控制结果解析逻辑
        try {
            if (data.readableBytes() < ChanghuiProtocolCodec.DATA_OFFSET + 2) {
                return false;
            }
            // 假设第二个字节为结果码，0表示成功
            return data.getByte(data.readerIndex() + ChanghuiProtocolCodec.DATA_OFFSET + 1) == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析控制错误信息
     *
     * @param data 数据
     * @return 错误信息
     */
    private String parseControlErrorMessage(ByteBuf data) {
        // TODO: 根据协议实现具体的错误信息解析逻辑
        return "控制命令执行失败";
    }

    // ==================== 内部类 ====================

    /**
     * 升级状态
     */
    @lombok.Data
    public static class UpgradeState {
        /**
         * 升级状态
         */
        private String status;

        /**
         * 升级进度（0-100）
         */
        private int progress;

        /**
         * 升级开始时间
         */
        private Long startTime;

        /**
         * 最后更新时间
         */
        private Long lastUpdateTime;

        /**
         * 升级结束时间
         */
        private Long endTime;

        /**
         * 错误信息
         */
        private String errorMessage;

        /**
         * 升级URL
         */
        private String upgradeUrl;
    }
}
