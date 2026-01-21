package cn.iocoder.yudao.module.iot.newgateway.plugins.alarm;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.ConnectionMode;
import cn.iocoder.yudao.module.iot.core.gateway.dto.DeviceInfo;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.core.mq.message.alarm.AlarmControlResponseMessage;
import cn.iocoder.yudao.module.iot.core.mq.message.alarm.AlarmHostEventMessage;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 报警主机插件
 * 
 * <p>实现报警主机设备的接入，支持 PS600 OPC 协议：</p>
 * <ul>
 *     <li>全局布防（ARM_ALL）</li>
 *     <li>留守布防（ARM_STAY）</li>
 *     <li>撤防（DISARM）</li>
 *     <li>分区布防/撤防（ARM_PARTITION/DISARM_PARTITION）</li>
 *     <li>旁路防区（BYPASS_ZONE）</li>
 *     <li>状态查询（QUERY_STATUS）</li>
 * </ul>
 * 
 * <h2>PS600 OPC 协议格式</h2>
 * <p>协议采用 ASCII 文本格式，字段以逗号分隔：</p>
 * <pre>
 * account,CMD,PARAMS...
 * </pre>
 * <ul>
 *     <li>account: 设备账号（用于设备标识）</li>
 *     <li>CMD: 命令类型</li>
 *     <li>PARAMS: 命令参数（可选）</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 * @see PassiveDeviceHandler
 * @see AlarmConnectionManager
 * @see AlarmConfig
 */
@DevicePlugin(
    id = PluginConstants.PLUGIN_ID_ALARM,
    name = "报警主机",
    deviceType = PluginConstants.DEVICE_TYPE_ALARM,
    vendor = "Generic",
    description = "报警主机设备，支持 PS600 OPC 协议，提供布防/撤防/旁路等功能",
    enabledByDefault = true
)
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "alarm", havingValue = "true", matchIfMissing = true)
@Slf4j
@RequiredArgsConstructor
public class AlarmPlugin implements PassiveDeviceHandler {

    /**
     * 日志前缀
     */
    private static final String LOG_PREFIX = "[AlarmPlugin]";

    // ==================== 命令类型常量 ====================

    /**
     * 全局布防命令
     */
    public static final String CMD_ARM_ALL = "ARM_ALL";

    /**
     * 留守布防命令
     */
    public static final String CMD_ARM_STAY = "ARM_STAY";

    /**
     * 撤防命令
     */
    public static final String CMD_DISARM = "DISARM";

    /**
     * 分区布防命令
     */
    public static final String CMD_ARM_PARTITION = "ARM_PARTITION";

    /**
     * 分区撤防命令
     */
    public static final String CMD_DISARM_PARTITION = "DISARM_PARTITION";

    /**
     * 旁路防区命令
     */
    public static final String CMD_BYPASS_ZONE = "BYPASS_ZONE";

    /**
     * 取消旁路命令
     */
    public static final String CMD_UNBYPASS_ZONE = "UNBYPASS_ZONE";

    /**
     * 查询状态命令
     */
    public static final String CMD_QUERY_STATUS = "QUERY_STATUS";

    /**
     * 消警命令
     */
    public static final String CMD_CLEAR_ALARM = "CLEAR_ALARM";

    // ==================== 命令类型映射 ====================

    /**
     * 命令类型映射表
     * 将 biz 层的命令类型映射到插件内部命令类型
     */
    private static final Map<String, String> COMMAND_TYPE_MAPPING = Map.ofEntries(
        // 布防命令映射
        Map.entry("ARM", CMD_ARM_ALL),
        Map.entry("ARM_EMERGENCY", CMD_ARM_STAY),
        Map.entry("ARM_AWAY", CMD_ARM_ALL),
        Map.entry("ARM_HOME", CMD_ARM_STAY),
        Map.entry("DISARM_ALL", CMD_DISARM),
        // 分区布防/撤防映射
        Map.entry("ARM_ZONE", CMD_ARM_PARTITION),
        Map.entry("DISARM_ZONE", CMD_DISARM_PARTITION),
        // 旁路命令映射
        Map.entry("BYPASS", CMD_BYPASS_ZONE),
        Map.entry("ZONE_BYPASS", CMD_BYPASS_ZONE),
        Map.entry("UNBYPASS", CMD_UNBYPASS_ZONE),
        Map.entry("ZONE_UNBYPASS", CMD_UNBYPASS_ZONE),
        Map.entry("CANCEL_BYPASS", CMD_UNBYPASS_ZONE),
        // 消警命令映射
        Map.entry("CLEAR", CMD_CLEAR_ALARM),
        Map.entry("RESET_ALARM", CMD_CLEAR_ALARM),
        Map.entry("SILENCE", CMD_CLEAR_ALARM),
        // 查询命令映射
        Map.entry("QUERY_ZONES", CMD_QUERY_STATUS),
        Map.entry("GET_STATUS", CMD_QUERY_STATUS),
        Map.entry("QUERY_PARTITIONS", CMD_QUERY_STATUS)
    );

    // ==================== 消息主题常量 ====================

    /**
     * 报警主机事件主题
     * @deprecated 已迁移到统一事件主题 {@link IotMessageTopics#DEVICE_EVENT_REPORTED}
     */
    @Deprecated
    public static final String TOPIC_ALARM_EVENT = IotMessageTopics.DEVICE_EVENT_REPORTED;

    /**
     * 报警主机控制响应主题
     * @deprecated 已迁移到统一结果主题 {@link IotMessageTopics#DEVICE_SERVICE_RESULT}
     */
    @Deprecated
    public static final String TOPIC_ALARM_CONTROL_RESPONSE = IotMessageTopics.DEVICE_SERVICE_RESULT;

    // ==================== 依赖注入 ====================

    /**
     * 连接管理器
     */
    private final AlarmConnectionManager connectionManager;

    /**
     * 协议编解码器
     */
    private final AlarmProtocolCodec protocolCodec;

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
    private final AlarmConfig config;

    /**
     * 设备 API（用于查询设备信息）
     */
    private final IotDeviceCommonApi deviceApi;

    /**
     * OPC 协议序列号生成器（0-9999 循环）
     */
    private final java.util.concurrent.atomic.AtomicInteger opcSequenceGenerator = 
            new java.util.concurrent.atomic.AtomicInteger(0);

    /**
     * 获取下一个 OPC 序列号
     * 
     * @return 序列号（0-9999 循环）
     */
    private int nextOpcSequence() {
        return opcSequenceGenerator.updateAndGet(seq -> (seq + 1) % 10000);
    }

    // ==================== DeviceHandler 接口实现 ====================

    @Override
    public String getDeviceType() {
        return PluginConstants.DEVICE_TYPE_ALARM;
    }

    @Override
    public String getVendor() {
        return "Generic";
    }

    @Override
    public boolean supports(DeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            return false;
        }
        return PluginConstants.DEVICE_TYPE_ALARM.equalsIgnoreCase(deviceInfo.getDeviceType());
    }

    @Override
    public CommandResult executeCommand(Long deviceId, DeviceCommand command) {
        if (deviceId == null || command == null) {
            return CommandResult.failure("参数不能为空");
        }

        String commandType = command.getCommandType();
        // 映射命令类型
        String mappedCommandType = mapCommandType(commandType);
        
        log.info("{} 执行命令: deviceId={}, commandType={}, mappedType={}", 
                LOG_PREFIX, deviceId, commandType, mappedCommandType);

        try {
            // 检查设备是否在线
            if (!connectionManager.isOnline(deviceId)) {
                return CommandResult.failure("设备未连接");
            }

            // 根据命令类型分发处理
            switch (mappedCommandType) {
                case CMD_ARM_ALL:
                    return executeArmAll(deviceId);
                case CMD_ARM_STAY:
                    return executeArmStay(deviceId);
                case CMD_DISARM:
                    return executeDisarm(deviceId, command);
                case CMD_ARM_PARTITION:
                    return executeArmPartition(deviceId, command);
                case CMD_DISARM_PARTITION:
                    return executeDisarmPartition(deviceId, command);
                case CMD_BYPASS_ZONE:
                    return executeBypassZone(deviceId, command);
                case CMD_UNBYPASS_ZONE:
                    return executeUnbypassZone(deviceId, command);
                case CMD_QUERY_STATUS:
                    return executeQueryStatus(deviceId);
                case CMD_CLEAR_ALARM:
                    return executeClearAlarm(deviceId, command);
                default:
                    return CommandResult.failure("不支持的命令类型: " + commandType);
            }
        } catch (Exception e) {
            log.error("{} 执行命令失败: deviceId={}, commandType={}", LOG_PREFIX, deviceId, commandType, e);
            return CommandResult.failure("命令执行异常: " + e.getMessage());
        }
    }

    /**
     * 映射命令类型
     * 将 biz 层的命令类型映射到插件内部命令类型
     *
     * @param commandType 原始命令类型
     * @return 映射后的命令类型
     */
    private String mapCommandType(String commandType) {
        if (commandType == null) {
            return null;
        }
        return COMMAND_TYPE_MAPPING.getOrDefault(commandType, commandType);
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
            // 根据账号查找设备ID
            Long deviceId = lookupDeviceId(deviceIdentifier);
            
            if (deviceId == null) {
                log.warn("{} 未找到设备: identifier={}", LOG_PREFIX, deviceIdentifier);
                return;
            }

            // 1. 注册连接（ConnectionManager 只负责连接管理）
            connectionManager.register(deviceId, deviceIdentifier, ctx.channel());
            
            // 2. 构建设备连接信息（包含完整元数据）
            DeviceConnectionInfo connectionInfo = buildConnectionInfo(deviceId, deviceIdentifier);
            
            // 3. 原子地更新设备生命周期状态（注册 -> 激活 -> 上线）
            boolean online = lifecycleManager.onDeviceConnected(deviceId, connectionInfo);
            if (online) {
                log.info("{} 设备连接成功（原子操作）: deviceId={}, account={}", LOG_PREFIX, deviceId, deviceIdentifier);
            } else {
                log.warn("{} 设备上线状态更新失败: deviceId={}, account={}", LOG_PREFIX, deviceId, deviceIdentifier);
            }
        } catch (Exception e) {
            log.error("{} 处理设备连接失败: identifier={}", LOG_PREFIX, deviceIdentifier, e);
        }
    }
    
    /**
     * 构建设备连接信息（包含完整元数据）
     *
     * @param deviceId 设备ID
     * @param account  账号
     * @return 设备连接信息
     */
    private DeviceConnectionInfo buildConnectionInfo(Long deviceId, String account) {
        DeviceConnectionInfo.DeviceConnectionInfoBuilder builder = DeviceConnectionInfo.builder()
                .deviceId(deviceId)
                .deviceType(PluginConstants.DEVICE_TYPE_ALARM)
                .vendor("Generic")
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
                }
            } catch (Exception e) {
                log.error("{} 查询设备信息失败: deviceId={}", LOG_PREFIX, deviceId, e);
            }
        }
        
        return builder.build();
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
                String account = connectionManager.getAccount(deviceId);
                DeviceConnectionInfo connectionInfo = buildConnectionInfo(deviceId, account);
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
        if (data == null || data.readableBytes() < 4) {
            return null;
        }

        try {
            // PS600 OPC 协议格式：account,CMD,PARAMS...
            // 读取数据并解析 account
            byte[] bytes = new byte[data.readableBytes()];
            data.getBytes(data.readerIndex(), bytes);
            String message = new String(bytes, StandardCharsets.US_ASCII);
            
            // 解析 account（第一个逗号前的内容）
            int commaIndex = message.indexOf(',');
            if (commaIndex > 0) {
                String account = message.substring(0, commaIndex).trim();
                log.debug("{} 解析设备账号: {}", LOG_PREFIX, account);
                return account;
            }
            
            return null;
        } catch (Exception e) {
            log.error("{} 解析设备标识失败", LOG_PREFIX, e);
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
            
            // 解析消息内容
            byte[] bytes = new byte[data.readableBytes()];
            data.getBytes(data.readerIndex(), bytes);
            String message = new String(bytes, StandardCharsets.US_ASCII);
            
            // 处理消息
            handleMessage(deviceId, message);
        } catch (Exception e) {
            log.error("{} 处理数据失败: deviceId={}", LOG_PREFIX, deviceId, e);
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 处理接收到的消息
     *
     * @param deviceId 设备ID
     * @param message  消息内容
     */
    private void handleMessage(Long deviceId, String message) {
        log.debug("{} 处理消息: deviceId={}, message={}", LOG_PREFIX, deviceId, message);
        
        // 首先尝试使用 OPC 协议解析（IP9500 格式）
        AlarmProtocolCodec.OpcParseResult opcResult = protocolCodec.parseOpcMessage(message);
        if (opcResult.isSuccess()) {
            // OPC 协议事件报告
            if (opcResult.isEventReport()) {
                handleOpcEventReport(deviceId, opcResult);
                return;
            }
            // OPC 协议控制应答
            if (opcResult.isControlAck()) {
                handleOpcControlAck(deviceId, opcResult);
                return;
            }
        }
        
        // 使用旧协议编解码器解析消息（格式：1234,HB）
        AlarmProtocolCodec.ParseResult parseResult = protocolCodec.parseMessage(message);
        if (!parseResult.isSuccess()) {
            log.warn("{} 消息解析失败: deviceId={}, error={}", LOG_PREFIX, deviceId, parseResult.getErrorMessage());
            return;
        }
        
        AlarmMessageType messageType = parseResult.getMessageType();
        String[] params = parseResult.getParams();
        
        // 根据消息类型处理
        switch (messageType) {
            case HEARTBEAT:
                // 心跳消息
                HeartbeatData heartbeatData = HeartbeatData.builder()
                        .deviceId(deviceId)
                        .timestamp(System.currentTimeMillis())
                        .build();
                onHeartbeat(deviceId, heartbeatData);
                break;
                
            case ALARM:
                // 报警事件
                String[] parts = message.split(",");
                handleAlarmEvent(deviceId, parts);
                break;
                
            case STATUS:
                // 状态上报
                String[] statusParts = message.split(",");
                handleStatusReport(deviceId, statusParts);
                break;
                
            case ACK:
                // 确认响应
                String[] ackParts = message.split(",");
                handleResponse(deviceId, ackParts, true);
                break;
                
            case NAK:
                // 否定响应
                String[] nakParts = message.split(",");
                handleResponse(deviceId, nakParts, false);
                break;
                
            default:
                log.debug("{} 未处理的消息类型: deviceId={}, type={}", LOG_PREFIX, deviceId, messageType);
                break;
        }
    }

    /**
     * 处理 OPC 协议控制应答
     * <p>
     * 格式：c{account},{result},{sequence}
     * 示例：c1234,0,126
     * </p>
     * <p>
     * 流程：收到ACK成功后，自动发送状态查询命令获取设备实际状态
     * </p>
     *
     * @param deviceId  设备ID
     * @param opcResult OPC 解析结果
     */
    private void handleOpcControlAck(Long deviceId, AlarmProtocolCodec.OpcParseResult opcResult) {
        Integer result = opcResult.getResult();
        int sequence = opcResult.getSequence();
        String account = opcResult.getAccount();
        
        boolean success = (result != null && result == 0);
        
        log.info("{} 收到 OPC 控制应答: deviceId={}, account={}, result={}, seq={}, success={}",
                LOG_PREFIX, deviceId, account, result, sequence, success);
        
        // 构建响应事件（使用强类型 DTO）
        AlarmControlResponseMessage responseEvent = AlarmControlResponseMessage.builder()
                .deviceId(deviceId)
                .deviceType(PluginConstants.DEVICE_TYPE_ALARM)
                .account(account)
                .responseType(AlarmControlResponseMessage.ResponseType.OPC_CONTROL_ACK)
                .responseTypeName("OPC_CONTROL_ACK")
                .success(success)
                .result(result)
                .sequence(String.valueOf(sequence))
                .timestamp(System.currentTimeMillis())
                .rawMessage(opcResult.getRawMessage())
                .build();
        
        // 发布响应事件到统一结果主题（DEVICE_SERVICE_RESULT）
        // 这样 DeviceServiceResultConsumer 可以接收并路由到 AlarmDeviceResultHandler
        cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage resultMessage = 
                cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage.builder()
                        .deviceId(deviceId)
                        .method("OPC_CONTROL_ACK")
                        .code(success ? 0 : -1)
                        .msg(success ? "命令执行成功" : "命令执行失败")
                        .reportTime(java.time.LocalDateTime.now())
                        .build();
        
        // 设置参数
        Map<String, Object> params = new HashMap<>();
        params.put("deviceType", PluginConstants.DEVICE_TYPE_ALARM);
        params.put("account", account);
        params.put("result", result);
        params.put("sequence", sequence);
        params.put("rawMessage", opcResult.getRawMessage());
        resultMessage.setParams(params);
        
        messagePublisher.publishEvent(IotMessageTopics.DEVICE_SERVICE_RESULT, resultMessage);
        
        // 同时发布到旧主题（兼容性）
        messagePublisher.publishEvent(TOPIC_ALARM_CONTROL_RESPONSE, responseEvent);
        
        if (success) {
            // ACK成功后，自动发送状态查询命令获取设备实际状态
            log.info("{} ACK成功，自动发送状态查询: deviceId={}", LOG_PREFIX, deviceId);
            triggerStatusQuery(deviceId);
        } else {
            log.warn("{} OPC 控制命令执行失败: deviceId={}, result={}, seq={}", 
                    LOG_PREFIX, deviceId, result, sequence);
        }
    }
    
    /**
     * 触发状态查询
     * <p>
     * 在控制命令执行成功后，自动查询设备状态以获取实际状态
     * </p>
     *
     * @param deviceId 设备ID
     */
    private void triggerStatusQuery(Long deviceId) {
        try {
            String account = connectionManager.getAccount(deviceId);
            if (account == null) {
                log.warn("{} 无法触发状态查询，未找到设备账号: deviceId={}", LOG_PREFIX, deviceId);
                return;
            }
            
            // 使用 OPC 协议构建查询分区和防区状态命令（控制码 10）
            int sequence = nextOpcSequence();
            String password = getDevicePassword(deviceId);
            String commandStr = protocolCodec.buildOpcControlCommand(
                    account, 
                    AlarmProtocolCodec.OpcControlCode.QUERY_PARTITION_STATUS.getCode(), 
                    "0",  // 参数：查询所有分区
                    password, 
                    sequence);
            
            if (commandStr == null) {
                log.warn("{} 构建状态查询命令失败: deviceId={}", LOG_PREFIX, deviceId);
                return;
            }
            
            byte[] commandBytes = (commandStr + AlarmProtocolCodec.MESSAGE_TERMINATOR).getBytes(StandardCharsets.US_ASCII);
            
            // 发送命令
            boolean sent = sendCommand(deviceId, commandBytes);
            if (sent) {
                log.info("{} 状态查询命令已发送: deviceId={}, account={}, seq={}", 
                        LOG_PREFIX, deviceId, account, sequence);
            } else {
                log.warn("{} 状态查询命令发送失败: deviceId={}", LOG_PREFIX, deviceId);
            }
        } catch (Exception e) {
            log.error("{} 触发状态查询异常: deviceId={}", LOG_PREFIX, deviceId, e);
        }
    }

    /**
     * 处理 OPC 协议事件报告
     * <p>
     * 格式：E{account},{event}{area}{point}{sequence}
     * 示例：E1234,1130001790123
     * </p>
     *
     * @param deviceId  设备ID
     * @param opcResult OPC 解析结果
     */
    private void handleOpcEventReport(Long deviceId, AlarmProtocolCodec.OpcParseResult opcResult) {
        String eventCode = opcResult.getEventCode();
        String area = opcResult.getArea();
        String point = opcResult.getPoint();
        int sequence = opcResult.getSequence();
        
        // 心跳消息（事件码为 0000）
        if (opcResult.isHeartbeat()) {
            log.trace("{} 收到 OPC 心跳: deviceId={}, seq={}", LOG_PREFIX, deviceId, sequence);
            HeartbeatData heartbeatData = HeartbeatData.builder()
                    .deviceId(deviceId)
                    .timestamp(System.currentTimeMillis())
                    .build();
            onHeartbeat(deviceId, heartbeatData);
            return;
        }
        
        log.info("{} 收到 OPC 事件: deviceId={}, eventCode={}, area={}, point={}, seq={}",
                LOG_PREFIX, deviceId, eventCode, area, point, sequence);
        
        // 获取事件描述
        String eventName = cn.iocoder.yudao.module.iot.core.alarm.AlarmCidEventCodes.getEventDescription(eventCode);
        String eventLevel = determineEventLevel(eventCode);
        
        // 构建事件消息，发布到 DEVICE_EVENT_REPORTED 主题
        // 这样 DeviceEventConsumer 可以接收并路由到 AlarmDeviceEventHandler
        cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage eventMessage = 
                cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage.builder()
                        .deviceId(deviceId)
                        .method("alarm_event")
                        .reportTime(java.time.LocalDateTime.now())
                        .build();
        
        // 设置事件参数
        Map<String, Object> params = new HashMap<>();
        params.put("deviceType", PluginConstants.DEVICE_TYPE_ALARM);
        params.put("eventCode", eventCode);
        params.put("eventName", eventName);
        params.put("eventLevel", eventLevel);
        params.put("area", area);
        params.put("point", point);
        params.put("sequence", String.valueOf(sequence));
        params.put("rawMessage", opcResult.getRawMessage());
        params.put("timestamp", System.currentTimeMillis());
        
        // 获取设备账号
        String account = connectionManager.getAccount(deviceId);
        if (account != null) {
            params.put("account", account);
        }
        
        eventMessage.setParams(params);
        
        // 发布到 DEVICE_EVENT_REPORTED 主题，让 DeviceEventConsumer 处理
        messagePublisher.publishEvent(
                cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics.DEVICE_EVENT_REPORTED, 
                eventMessage);
        
        log.info("{} OPC 事件已发布: deviceId={}, eventCode={}, eventName={}",
                LOG_PREFIX, deviceId, eventCode, eventName);
    }

    /**
     * 确定事件级别
     *
     * @param eventCode 事件码
     * @return 事件级别
     */
    private String determineEventLevel(String eventCode) {
        // 报警事件为 ERROR 级别
        if (cn.iocoder.yudao.module.iot.core.alarm.AlarmCidEventCodes.isZoneAlarmEvent(eventCode)) {
            return "ERROR";
        }
        // 挟持报警为 CRITICAL 级别
        if (cn.iocoder.yudao.module.iot.core.alarm.AlarmCidEventCodes.DURESS_ALARM.equals(eventCode)) {
            return "CRITICAL";
        }
        // 恢复事件为 INFO 级别
        if (cn.iocoder.yudao.module.iot.core.alarm.AlarmCidEventCodes.isRestoreEvent(eventCode)) {
            return "INFO";
        }
        // 布防/撤防为 INFO 级别
        if (cn.iocoder.yudao.module.iot.core.alarm.AlarmCidEventCodes.isArmDisarmEvent(eventCode)) {
            return "INFO";
        }
        // 默认为 WARNING 级别
        return "WARNING";
    }

    /**
     * 处理报警事件
     * 
     * <p>使用强类型 DTO（AlarmHostEventMessage）发布事件，符合 Requirements 4.3</p>
     *
     * @param deviceId 设备ID
     * @param parts    消息部分
     */
    private void handleAlarmEvent(Long deviceId, String[] parts) {
        log.info("{} 收到报警事件: deviceId={}", LOG_PREFIX, deviceId);
        
        // 解析报警事件详情
        String eventTypeName = parts.length > 2 ? parts[2].trim() : "UNKNOWN";
        Integer sourceNo = null;
        String alarmType = null;
        
        if (parts.length > 3) {
            try {
                sourceNo = Integer.parseInt(parts[3].trim());
            } catch (NumberFormatException e) {
                log.warn("{} 解析事件源编号失败: {}", LOG_PREFIX, parts[3]);
            }
        }
        
        if (parts.length > 4) {
            alarmType = parts[4].trim();
        }
        
        // 获取设备账号
        String account = connectionManager.getAccount(deviceId);
        
        // 构建报警事件（使用强类型 DTO）
        AlarmHostEventMessage event = AlarmHostEventMessage.builder()
                .hostId(deviceId)
                .deviceId(deviceId)
                .deviceType(PluginConstants.DEVICE_TYPE_ALARM)
                .eventType(convertEventTypeNameToInt(eventTypeName))
                .eventTypeName(eventTypeName)
                .eventLevel(AlarmHostEventMessage.EventLevel.ERROR)
                .eventLevelName("ERROR")
                .zoneNo(sourceNo)
                .eventDesc(alarmType)
                .rawData(String.join(",", parts))
                .timestamp(System.currentTimeMillis())
                .eventTime(LocalDateTime.now())
                .build();
        
        // 发布报警事件到统一的设备事件主题 DEVICE_EVENT_REPORTED
        messagePublisher.publishEvent(IotMessageTopics.DEVICE_EVENT_REPORTED, event);
        
        log.info("{} 报警事件已发布: deviceId={}, eventType={}, sourceNo={}, alarmType={}", 
                LOG_PREFIX, deviceId, eventTypeName, sourceNo, alarmType);
    }
    
    /**
     * 将事件类型名称转换为整数常量
     *
     * @param eventTypeName 事件类型名称
     * @return 事件类型常量
     */
    private int convertEventTypeNameToInt(String eventTypeName) {
        if (eventTypeName == null) {
            return 0;
        }
        switch (eventTypeName.toUpperCase()) {
            case "ALARM": return AlarmHostEventMessage.EventType.ALARM;
            case "ARM": return AlarmHostEventMessage.EventType.ARM;
            case "DISARM": return AlarmHostEventMessage.EventType.DISARM;
            case "FAULT": return AlarmHostEventMessage.EventType.FAULT;
            case "BYPASS": return AlarmHostEventMessage.EventType.BYPASS;
            case "RESTORE": return AlarmHostEventMessage.EventType.RESTORE;
            case "STATUS_UPDATE": return AlarmHostEventMessage.EventType.STATUS_UPDATE;
            case "HEARTBEAT": return AlarmHostEventMessage.EventType.HEARTBEAT;
            default: return AlarmHostEventMessage.EventType.ALARM;
        }
    }
    
    /**
     * 将事件级别名称转换为整数常量
     *
     * @param eventLevelName 事件级别名称
     * @return 事件级别常量
     */
    private int convertEventLevelNameToInt(String eventLevelName) {
        if (eventLevelName == null) {
            return AlarmHostEventMessage.EventLevel.WARNING;
        }
        switch (eventLevelName.toUpperCase()) {
            case "INFO": return AlarmHostEventMessage.EventLevel.INFO;
            case "WARNING": return AlarmHostEventMessage.EventLevel.WARNING;
            case "ERROR": return AlarmHostEventMessage.EventLevel.ERROR;
            case "CRITICAL": return AlarmHostEventMessage.EventLevel.CRITICAL;
            default: return AlarmHostEventMessage.EventLevel.WARNING;
        }
    }

    /**
     * 处理状态上报
     * 
     * <p>解析设备状态并发布到 DEVICE_SERVICE_RESULT 主题，
     * 让业务层 AlarmDeviceResultHandler 可以接收并更新数据库</p>
     *
     * @param deviceId 设备ID
     * @param parts    消息部分
     */
    private void handleStatusReport(Long deviceId, String[] parts) {
        log.info("{} 收到状态上报: deviceId={}, rawData={}", LOG_PREFIX, deviceId, String.join(",", parts));
        
        // 解析状态信息
        String armStatus = parts.length > 2 ? parts[2].trim() : null;
        
        // 更新设备状态
        if (armStatus != null) {
            connectionManager.updateDeviceStatus(deviceId, armStatus);
            log.info("{} 设备状态更新: deviceId={}, armStatus={}", LOG_PREFIX, deviceId, armStatus);
        }
        
        // 解析已布防的分区列表
        java.util.List<String> armedPartitions = new java.util.ArrayList<>();
        if (parts.length > 3) {
            for (int i = 3; i < parts.length; i++) {
                armedPartitions.add(parts[i].trim());
            }
            log.info("{} 已布防分区: deviceId={}, partitions={}", LOG_PREFIX, deviceId, armedPartitions);
        }
        
        // 获取设备账号
        String account = connectionManager.getAccount(deviceId);
        
        // 【重要】发布状态到 DEVICE_SERVICE_RESULT 主题
        // 这样 DeviceServiceResultConsumer → AlarmDeviceResultHandler 可以处理状态更新
        cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage resultMessage = 
                cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage.builder()
                        .deviceId(deviceId)
                        .method("STATUS_UPDATE")  // 方法类型：状态更新
                        .code(0)  // 成功
                        .msg("设备状态上报")
                        .reportTime(java.time.LocalDateTime.now())
                        .build();
        
        // 设置状态参数
        Map<String, Object> params = new HashMap<>();
        params.put("deviceType", PluginConstants.DEVICE_TYPE_ALARM);
        params.put("account", account);
        params.put("armStatus", armStatus);  // 布防状态：ARMED, DISARMED, STAY_ARMED 等
        params.put("armedPartitions", armedPartitions);  // 已布防的分区列表
        params.put("rawData", String.join(",", parts));
        params.put("timestamp", System.currentTimeMillis());
        resultMessage.setParams(params);
        
        // 发布到统一结果主题
        messagePublisher.publishEvent(IotMessageTopics.DEVICE_SERVICE_RESULT, resultMessage);
        log.info("{} 状态已发布到 DEVICE_SERVICE_RESULT: deviceId={}, armStatus={}", 
                LOG_PREFIX, deviceId, armStatus);
        
        // 构建状态事件（使用强类型 DTO）- 发布到事件主题（兼容性）
        AlarmHostEventMessage statusEvent = AlarmHostEventMessage.builder()
                .hostId(deviceId)
                .deviceId(deviceId)
                .deviceType(PluginConstants.DEVICE_TYPE_ALARM)
                .eventType(AlarmHostEventMessage.EventType.STATUS_UPDATE)
                .eventTypeName("STATUS_UPDATE")
                .eventLevel(AlarmHostEventMessage.EventLevel.INFO)
                .eventLevelName("INFO")
                .eventDesc(armStatus)
                .rawData(String.join(",", parts))
                .timestamp(System.currentTimeMillis())
                .eventTime(LocalDateTime.now())
                .build();
        
        // 发布状态事件到统一的设备事件主题 DEVICE_EVENT_REPORTED
        messagePublisher.publishEvent(IotMessageTopics.DEVICE_EVENT_REPORTED, statusEvent);
    }

    /**
     * 处理响应消息
     * 
     * <p>使用强类型 DTO（AlarmControlResponseMessage）发布事件，符合 Requirements 4.3</p>
     *
     * @param deviceId 设备ID
     * @param parts    消息部分
     * @param isAck    是否为确认响应
     */
    private void handleResponse(Long deviceId, String[] parts, boolean isAck) {
        String responseTypeName = isAck ? "ACK" : "NAK";
        log.debug("{} 收到响应: deviceId={}, type={}", LOG_PREFIX, deviceId, responseTypeName);
        
        // 解析原始命令
        String originalCmd = parts.length > 2 ? parts[2].trim() : null;
        String errorCode = parts.length > 3 ? parts[3].trim() : null;
        
        // 获取设备账号
        String account = connectionManager.getAccount(deviceId);
        
        // 构建响应事件（使用强类型 DTO）
        AlarmControlResponseMessage responseEvent = AlarmControlResponseMessage.builder()
                .deviceId(deviceId)
                .deviceType(PluginConstants.DEVICE_TYPE_ALARM)
                .account(account)
                .responseType(isAck ? AlarmControlResponseMessage.ResponseType.ACK : AlarmControlResponseMessage.ResponseType.NAK)
                .responseTypeName(responseTypeName)
                .success(isAck)
                .originalCommand(originalCmd)
                .errorCode(!isAck ? errorCode : null)
                .timestamp(System.currentTimeMillis())
                .build();
        
        // 发布响应事件
        messagePublisher.publishEvent(TOPIC_ALARM_CONTROL_RESPONSE, responseEvent);
        
        if (!isAck) {
            log.warn("{} 命令执行失败: deviceId={}, cmd={}, errorCode={}", 
                    LOG_PREFIX, deviceId, originalCmd, errorCode);
        }
    }

    /**
     * 根据账号查找设备ID
     * <p>
     * 首先从连接管理器查找（已连接的设备），
     * 如果未找到，则通过 RPC 调用 biz 模块查询数据库。
     * </p>
     *
     * @param account 设备账号（对应 deviceKey）
     * @return 设备ID，如果未找到则返回 null
     */
    private Long lookupDeviceId(String account) {
        if (account == null || account.isEmpty()) {
            return null;
        }
        
        // 1. 首先从连接管理器查找（已连接的设备）
        Long deviceId = connectionManager.getDeviceIdByIdentifier(account);
        if (deviceId != null) {
            log.debug("{} 从连接管理器找到设备: account={}, deviceId={}", LOG_PREFIX, account, deviceId);
            return deviceId;
        }
        
        // 2. 通过 RPC 调用 biz 模块查询数据库
        if (deviceApi != null) {
            try {
                IotDeviceGetReqDTO reqDTO = new IotDeviceGetReqDTO();
                reqDTO.setDeviceKey(account);  // 报警主机的账号对应 deviceKey
                
                CommonResult<IotDeviceRespDTO> result = deviceApi.getDevice(reqDTO);
                if (result != null && result.isSuccess() && result.getData() != null) {
                    deviceId = result.getData().getId();
                    log.info("{} 从数据库找到设备: account={}, deviceId={}", LOG_PREFIX, account, deviceId);
                    return deviceId;
                } else {
                    log.warn("{} 数据库中未找到设备: account={}, result={}", 
                            LOG_PREFIX, account, result != null ? result.getMsg() : "null");
                }
            } catch (Exception e) {
                log.error("{} 查询设备失败: account={}", LOG_PREFIX, account, e);
            }
        } else {
            log.warn("{} deviceApi 未注入，无法查询数据库", LOG_PREFIX);
        }
        
        return null;
    }

    /**
     * 获取设备密码
     *
     * @param deviceId 设备ID
     * @return 设备密码
     */
    private String getDevicePassword(Long deviceId) {
        String password = connectionManager.getPassword(deviceId);
        return password != null ? password : config.getDefaultPassword();
    }

    // ==================== 命令执行方法 ====================

    /**
     * 执行全局布防命令
     *
     * @param deviceId 设备ID
     * @return 命令结果
     */
    private CommandResult executeArmAll(Long deviceId) {
        log.info("{} 执行全局布防: deviceId={}", LOG_PREFIX, deviceId);
        
        // 获取设备账号
        String account = connectionManager.getAccount(deviceId);
        if (account == null) {
            return CommandResult.failure("无法获取设备账号");
        }
        
        // 获取设备密码
        String password = getDevicePassword(deviceId);
        
        // 使用 OPC 协议构建外出布防命令（控制码 2）
        int sequence = nextOpcSequence();
        String commandStr = protocolCodec.buildOpcControlCommand(
                account, 
                AlarmProtocolCodec.OpcControlCode.ARM_AWAY.getCode(), 
                "0",  // 参数：0 表示全部分区
                password, 
                sequence);
        
        if (commandStr == null) {
            return CommandResult.failure("构建命令失败");
        }
        
        byte[] commandBytes = (commandStr + AlarmProtocolCodec.MESSAGE_TERMINATOR).getBytes(StandardCharsets.US_ASCII);
        
        // 发送命令
        boolean sent = sendCommand(deviceId, commandBytes);
        if (!sent) {
            return CommandResult.failure("发送命令失败");
        }
        
        return CommandResult.success(Map.of("message", "全局布防命令已发送", "sequence", sequence));
    }

    /**
     * 执行留守布防命令
     *
     * @param deviceId 设备ID
     * @return 命令结果
     */
    private CommandResult executeArmStay(Long deviceId) {
        log.info("{} 执行留守布防: deviceId={}", LOG_PREFIX, deviceId);
        
        // 获取设备账号
        String account = connectionManager.getAccount(deviceId);
        if (account == null) {
            return CommandResult.failure("无法获取设备账号");
        }
        
        // 获取设备密码
        String password = getDevicePassword(deviceId);
        
        // 使用 OPC 协议构建居家布防命令（控制码 3）
        int sequence = nextOpcSequence();
        String commandStr = protocolCodec.buildOpcControlCommand(
                account, 
                AlarmProtocolCodec.OpcControlCode.ARM_STAY.getCode(), 
                "0",  // 参数：0 表示全部分区
                password, 
                sequence);
        
        if (commandStr == null) {
            return CommandResult.failure("构建命令失败");
        }
        
        byte[] commandBytes = (commandStr + AlarmProtocolCodec.MESSAGE_TERMINATOR).getBytes(StandardCharsets.US_ASCII);
        
        // 发送命令
        boolean sent = sendCommand(deviceId, commandBytes);
        if (!sent) {
            return CommandResult.failure("发送命令失败");
        }
        
        return CommandResult.success(Map.of("message", "留守布防命令已发送", "sequence", sequence));
    }

    /**
     * 执行撤防命令
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
     */
    private CommandResult executeDisarm(Long deviceId, DeviceCommand command) {
        String password = command.getParam("password");
        if (password == null || password.isEmpty()) {
            password = getDevicePassword(deviceId);
        }
        
        log.info("{} 执行撤防: deviceId={}", LOG_PREFIX, deviceId);
        
        // 获取设备账号
        String account = connectionManager.getAccount(deviceId);
        if (account == null) {
            return CommandResult.failure("无法获取设备账号");
        }
        
        // 使用 OPC 协议构建撤防命令（控制码 1）
        int sequence = nextOpcSequence();
        String commandStr = protocolCodec.buildOpcControlCommand(
                account, 
                AlarmProtocolCodec.OpcControlCode.DISARM.getCode(), 
                "0",  // 参数：0 表示全部分区
                password, 
                sequence);
        
        if (commandStr == null) {
            return CommandResult.failure("构建命令失败，请检查密码格式");
        }
        
        byte[] commandBytes = (commandStr + AlarmProtocolCodec.MESSAGE_TERMINATOR).getBytes(StandardCharsets.US_ASCII);
        
        // 发送命令
        boolean sent = sendCommand(deviceId, commandBytes);
        if (!sent) {
            return CommandResult.failure("发送命令失败");
        }
        
        return CommandResult.success(Map.of("message", "撤防命令已发送", "sequence", sequence));
    }

    /**
     * 执行分区布防命令
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
     */
    private CommandResult executeArmPartition(Long deviceId, DeviceCommand command) {
        Integer partitionNo = command.getParam("partitionNo");
        if (partitionNo == null) {
            return CommandResult.failure("缺少 partitionNo 参数");
        }
        
        log.info("{} 执行分区布防: deviceId={}, partitionNo={}", LOG_PREFIX, deviceId, partitionNo);
        
        // 获取设备账号
        String account = connectionManager.getAccount(deviceId);
        if (account == null) {
            return CommandResult.failure("无法获取设备账号");
        }
        
        // 构建分区布防命令
        String commandStr = protocolCodec.buildArmPartitionCommand(account, partitionNo);
        if (commandStr == null) {
            return CommandResult.failure("构建命令失败，请检查分区号");
        }
        
        byte[] commandBytes = (commandStr + AlarmProtocolCodec.MESSAGE_TERMINATOR).getBytes(StandardCharsets.US_ASCII);
        
        // 发送命令
        boolean sent = sendCommand(deviceId, commandBytes);
        if (!sent) {
            return CommandResult.failure("发送命令失败");
        }
        
        return CommandResult.success(Map.of("message", "分区布防命令已发送", "partitionNo", partitionNo));
    }

    /**
     * 执行分区撤防命令
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
     */
    private CommandResult executeDisarmPartition(Long deviceId, DeviceCommand command) {
        Integer partitionNo = command.getParam("partitionNo");
        String password = command.getParam("password");
        
        if (partitionNo == null) {
            return CommandResult.failure("缺少 partitionNo 参数");
        }
        if (password == null || password.isEmpty()) {
            password = getDevicePassword(deviceId);
        }
        
        log.info("{} 执行分区撤防: deviceId={}, partitionNo={}", LOG_PREFIX, deviceId, partitionNo);
        
        // 获取设备账号
        String account = connectionManager.getAccount(deviceId);
        if (account == null) {
            return CommandResult.failure("无法获取设备账号");
        }
        
        // 构建分区撤防命令
        String commandStr = protocolCodec.buildDisarmPartitionCommand(account, partitionNo, password);
        if (commandStr == null) {
            return CommandResult.failure("构建命令失败，请检查参数");
        }
        
        byte[] commandBytes = (commandStr + AlarmProtocolCodec.MESSAGE_TERMINATOR).getBytes(StandardCharsets.US_ASCII);
        
        // 发送命令
        boolean sent = sendCommand(deviceId, commandBytes);
        if (!sent) {
            return CommandResult.failure("发送命令失败");
        }
        
        return CommandResult.success(Map.of("message", "分区撤防命令已发送", "partitionNo", partitionNo));
    }

    /**
     * 执行旁路防区命令
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
     */
    private CommandResult executeBypassZone(Long deviceId, DeviceCommand command) {
        Integer zoneNo = command.getParam("zoneNo");
        String password = command.getParam("password");
        
        if (zoneNo == null) {
            return CommandResult.failure("缺少 zoneNo 参数");
        }
        if (password == null || password.isEmpty()) {
            password = getDevicePassword(deviceId);
        }
        
        log.info("{} 执行旁路防区: deviceId={}, zoneNo={}", LOG_PREFIX, deviceId, zoneNo);
        
        // 获取设备账号
        String account = connectionManager.getAccount(deviceId);
        if (account == null) {
            return CommandResult.failure("无法获取设备账号");
        }
        
        // 使用 OPC 协议构建防区旁路命令（控制码 4）
        int sequence = nextOpcSequence();
        String commandStr = protocolCodec.buildOpcControlCommand(
                account, 
                AlarmProtocolCodec.OpcControlCode.BYPASS_ZONE.getCode(), 
                String.valueOf(zoneNo),  // 参数：防区号
                password, 
                sequence);
        
        if (commandStr == null) {
            return CommandResult.failure("构建命令失败，请检查参数");
        }
        
        byte[] commandBytes = (commandStr + AlarmProtocolCodec.MESSAGE_TERMINATOR).getBytes(StandardCharsets.US_ASCII);
        
        // 发送命令
        boolean sent = sendCommand(deviceId, commandBytes);
        if (!sent) {
            return CommandResult.failure("发送命令失败");
        }
        
        return CommandResult.success(Map.of("message", "旁路防区命令已发送", "zoneNo", zoneNo, "sequence", sequence));
    }

    /**
     * 执行取消旁路命令
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
     */
    private CommandResult executeUnbypassZone(Long deviceId, DeviceCommand command) {
        Integer zoneNo = command.getParam("zoneNo");
        String password = command.getParam("password");
        
        if (zoneNo == null) {
            return CommandResult.failure("缺少 zoneNo 参数");
        }
        if (password == null || password.isEmpty()) {
            password = getDevicePassword(deviceId);
        }
        
        log.info("{} 执行取消旁路: deviceId={}, zoneNo={}", LOG_PREFIX, deviceId, zoneNo);
        
        // 获取设备账号
        String account = connectionManager.getAccount(deviceId);
        if (account == null) {
            return CommandResult.failure("无法获取设备账号");
        }
        
        // 使用 OPC 协议构建撤销防区旁路命令（控制码 5）
        int sequence = nextOpcSequence();
        String commandStr = protocolCodec.buildOpcControlCommand(
                account, 
                AlarmProtocolCodec.OpcControlCode.UNBYPASS_ZONE.getCode(), 
                String.valueOf(zoneNo),  // 参数：防区号
                password, 
                sequence);
        
        if (commandStr == null) {
            return CommandResult.failure("构建命令失败，请检查参数");
        }
        
        byte[] commandBytes = (commandStr + AlarmProtocolCodec.MESSAGE_TERMINATOR).getBytes(StandardCharsets.US_ASCII);
        
        // 发送命令
        boolean sent = sendCommand(deviceId, commandBytes);
        if (!sent) {
            return CommandResult.failure("发送命令失败");
        }
        
        return CommandResult.success(Map.of("message", "取消旁路命令已发送", "zoneNo", zoneNo, "sequence", sequence));
    }

    /**
     * 执行状态查询命令
     *
     * @param deviceId 设备ID
     * @return 命令结果
     */
    private CommandResult executeQueryStatus(Long deviceId) {
        log.info("{} 执行状态查询: deviceId={}", LOG_PREFIX, deviceId);
        
        // 获取设备账号
        String account = connectionManager.getAccount(deviceId);
        if (account == null) {
            // 如果无法获取账号，返回基本状态
            DeviceStatus status = queryStatus(deviceId);
            return CommandResult.success(Map.of(
                    "online", status.isOnline(),
                    "lastHeartbeatTime", status.getLastHeartbeatTime() != null ? status.getLastHeartbeatTime() : 0
            ));
        }
        
        // 使用 OPC 协议构建查询状态命令（控制码 0）
        int sequence = nextOpcSequence();
        String password = getDevicePassword(deviceId);
        String commandStr = protocolCodec.buildOpcControlCommand(
                account, 
                AlarmProtocolCodec.OpcControlCode.QUERY_STATUS.getCode(), 
                "0",  // 参数
                password, 
                sequence);
        
        if (commandStr == null) {
            return CommandResult.failure("构建命令失败");
        }
        
        byte[] commandBytes = (commandStr + AlarmProtocolCodec.MESSAGE_TERMINATOR).getBytes(StandardCharsets.US_ASCII);
        
        // 发送命令
        boolean sent = sendCommand(deviceId, commandBytes);
        if (!sent) {
            return CommandResult.failure("发送命令失败");
        }
        
        // 返回当前已知状态
        DeviceStatus status = queryStatus(deviceId);
        String deviceStatus = connectionManager.getDeviceStatus(deviceId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("online", status.isOnline());
        result.put("lastHeartbeatTime", status.getLastHeartbeatTime() != null ? status.getLastHeartbeatTime() : 0);
        result.put("message", "状态查询命令已发送");
        result.put("sequence", sequence);
        if (deviceStatus != null) {
            result.put("armStatus", deviceStatus);
        }
        
        return CommandResult.success(result);
    }

    /**
     * 执行消警命令
     *
     * @param deviceId 设备ID
     * @param command  命令
     * @return 命令结果
     */
    private CommandResult executeClearAlarm(Long deviceId, DeviceCommand command) {
        String password = command.getParam("password");
        if (password == null || password.isEmpty()) {
            password = getDevicePassword(deviceId);
        }
        
        log.info("{} 执行消警: deviceId={}", LOG_PREFIX, deviceId);
        
        // 获取设备账号
        String account = connectionManager.getAccount(deviceId);
        if (account == null) {
            return CommandResult.failure("无法获取设备账号");
        }
        
        // 使用 OPC 协议构建报警复位命令（控制码 11）
        int sequence = nextOpcSequence();
        String commandStr = protocolCodec.buildOpcControlCommand(
                account, 
                AlarmProtocolCodec.OpcControlCode.ALARM_RESET.getCode(), 
                "0",  // 参数：0 表示全部分区
                password, 
                sequence);
        
        if (commandStr == null) {
            return CommandResult.failure("构建命令失败，请检查密码格式");
        }
        
        byte[] commandBytes = (commandStr + AlarmProtocolCodec.MESSAGE_TERMINATOR).getBytes(StandardCharsets.US_ASCII);
        
        // 发送命令
        boolean sent = sendCommand(deviceId, commandBytes);
        if (!sent) {
            return CommandResult.failure("发送命令失败");
        }
        
        return CommandResult.success(Map.of("message", "消警命令已发送", "sequence", sequence));
    }

    /**
     * 发送命令到设备
     *
     * @param deviceId     设备ID
     * @param commandBytes 命令字节数组
     * @return 是否发送成功
     */
    private boolean sendCommand(Long deviceId, byte[] commandBytes) {
        Channel channel = connectionManager.getConnection(deviceId);
        if (channel == null || !channel.isActive()) {
            log.warn("{} 无法发送命令，设备未连接: deviceId={}", LOG_PREFIX, deviceId);
            return false;
        }
        
        try {
            channel.writeAndFlush(Unpooled.wrappedBuffer(commandBytes));
            log.debug("{} 命令已发送: deviceId={}, length={}", LOG_PREFIX, deviceId, commandBytes.length);
            return true;
        } catch (Exception e) {
            log.error("{} 发送命令失败: deviceId={}", LOG_PREFIX, deviceId, e);
            return false;
        }
    }

    // ==================== 生命周期方法 ====================

    /**
     * 插件启动时调用
     */
    public void onStart() {
        log.info("{} 插件启动: port={}, heartbeatTimeout={}ms", 
                LOG_PREFIX, config.getPort(), config.getHeartbeatTimeout());
    }

    /**
     * 插件停止时调用
     */
    public void onStop() {
        log.info("{} 插件停止", LOG_PREFIX);
        
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
}
