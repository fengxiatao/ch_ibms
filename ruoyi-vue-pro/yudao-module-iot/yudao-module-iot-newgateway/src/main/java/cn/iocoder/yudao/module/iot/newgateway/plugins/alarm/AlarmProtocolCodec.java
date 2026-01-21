package cn.iocoder.yudao.module.iot.newgateway.plugins.alarm;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 报警主机协议编解码器
 * 
 * <p>实现 PS600 OPC 协议的编解码功能。</p>
 * 
 * <h2>协议格式</h2>
 * <p>PS600 OPC 协议采用 ASCII 文本格式，字段以逗号分隔：</p>
 * <pre>
 * account,CMD[,PARAM1,PARAM2,...]
 * </pre>
 * 
 * <h3>字段说明</h3>
 * <ul>
 *     <li>account: 设备账号（4-16位数字或字母）</li>
 *     <li>CMD: 命令类型（如 HB, ARM_ALL, DISARM 等）</li>
 *     <li>PARAM1, PARAM2, ...: 可选参数</li>
 * </ul>
 * 
 * <h3>消息示例</h3>
 * <ul>
 *     <li>心跳: {@code 1234,HB}</li>
 *     <li>全局布防: {@code 1234,ARM_ALL}</li>
 *     <li>撤防: {@code 1234,DISARM,1234} (带密码)</li>
 *     <li>分区布防: {@code 1234,ARM_PARTITION,1} (分区号)</li>
 *     <li>旁路防区: {@code 1234,BYPASS_ZONE,5,1234} (防区号,密码)</li>
 *     <li>报警事件: {@code 1234,ALARM,ZONE,5,INTRUSION} (防区号,报警类型)</li>
 *     <li>状态上报: {@code 1234,STATUS,ARMED,1,2,3} (状态,分区列表)</li>
 * </ul>
 * 
 * <h3>响应格式</h3>
 * <ul>
 *     <li>成功: {@code account,ACK[,CMD]}</li>
 *     <li>失败: {@code account,NAK[,CMD,ERROR_CODE]}</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 * @see AlarmPlugin
 * @see AlarmMessageType
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "alarm", havingValue = "true", matchIfMissing = true)
public class AlarmProtocolCodec {

    /**
     * 日志前缀
     */
    private static final String LOG_PREFIX = "[AlarmProtocolCodec]";

    // ==================== 协议常量 ====================

    /**
     * 字段分隔符
     */
    public static final char FIELD_SEPARATOR = ',';

    /**
     * 消息结束符（可选，用于TCP流分割）
     */
    public static final String MESSAGE_TERMINATOR = "\r\n";

    /**
     * 账号最小长度
     */
    public static final int MIN_ACCOUNT_LENGTH = 4;

    /**
     * 账号最大长度
     */
    public static final int MAX_ACCOUNT_LENGTH = 16;

    /**
     * 密码最小长度
     */
    public static final int MIN_PASSWORD_LENGTH = 4;

    /**
     * 密码最大长度
     */
    public static final int MAX_PASSWORD_LENGTH = 8;

    /**
     * 最小消息长度（账号 + 分隔符 + 命令）
     */
    public static final int MIN_MESSAGE_LENGTH = MIN_ACCOUNT_LENGTH + 1 + 2;

    // ==================== 解析方法 ====================


    /**
     * 验证账号格式是否有效
     *
     * @param account 账号
     * @return 是否有效
     */
    public boolean validateAccount(String account) {
        if (account == null || account.isEmpty()) {
            return false;
        }
        
        int length = account.length();
        if (length < MIN_ACCOUNT_LENGTH || length > MAX_ACCOUNT_LENGTH) {
            return false;
        }
        
        // 账号只能包含数字和字母
        for (char c : account.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * 验证密码格式是否有效
     *
     * @param password 密码
     * @return 是否有效
     */
    public boolean validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        
        int length = password.length();
        if (length < MIN_PASSWORD_LENGTH || length > MAX_PASSWORD_LENGTH) {
            return false;
        }
        
        // 密码只能包含数字
        for (char c : password.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * 解析账号
     * <p>
     * 从消息中提取账号字段（第一个逗号前的内容）。
     * </p>
     *
     * @param message 消息内容
     * @return 账号，解析失败返回 null
     */
    public String parseAccount(String message) {
        if (message == null || message.length() < MIN_MESSAGE_LENGTH) {
            log.warn("{} 消息长度不足，无法解析账号: length={}", 
                    LOG_PREFIX, message != null ? message.length() : 0);
            return null;
        }

        try {
            // 去除可能的结束符
            String trimmedMessage = message.trim();
            if (trimmedMessage.endsWith("\r\n")) {
                trimmedMessage = trimmedMessage.substring(0, trimmedMessage.length() - 2);
            } else if (trimmedMessage.endsWith("\n") || trimmedMessage.endsWith("\r")) {
                trimmedMessage = trimmedMessage.substring(0, trimmedMessage.length() - 1);
            }

            // 查找第一个分隔符
            int separatorIndex = trimmedMessage.indexOf(FIELD_SEPARATOR);
            if (separatorIndex <= 0) {
                log.warn("{} 未找到字段分隔符", LOG_PREFIX);
                return null;
            }

            String account = trimmedMessage.substring(0, separatorIndex).trim();
            
            // 验证账号格式
            if (!validateAccount(account)) {
                log.warn("{} 无效的账号格式: {}", LOG_PREFIX, account);
                return null;
            }

            log.debug("{} 解析账号: {}", LOG_PREFIX, account);
            return account;
        } catch (Exception e) {
            log.error("{} 解析账号失败", LOG_PREFIX, e);
            return null;
        }
    }

    /**
     * 解析账号（ByteBuf 版本）
     *
     * @param data 数据
     * @return 账号，解析失败返回 null
     */
    public String parseAccount(ByteBuf data) {
        if (data == null || data.readableBytes() < MIN_MESSAGE_LENGTH) {
            return null;
        }

        try {
            byte[] bytes = new byte[data.readableBytes()];
            data.getBytes(data.readerIndex(), bytes);
            String message = new String(bytes, StandardCharsets.US_ASCII);
            return parseAccount(message);
        } catch (Exception e) {
            log.error("{} 解析账号失败", LOG_PREFIX, e);
            return null;
        }
    }

    /**
     * 解析消息类型
     * <p>
     * 从消息中提取命令类型字段（第一个逗号后的内容）。
     * </p>
     *
     * @param message 消息内容
     * @return 消息类型，解析失败返回 null
     */
    public AlarmMessageType parseMessageType(String message) {
        if (message == null || message.length() < MIN_MESSAGE_LENGTH) {
            log.warn("{} 消息长度不足，无法解析消息类型: length={}", 
                    LOG_PREFIX, message != null ? message.length() : 0);
            return null;
        }

        try {
            // 去除可能的结束符
            String trimmedMessage = message.trim();
            if (trimmedMessage.endsWith("\r\n")) {
                trimmedMessage = trimmedMessage.substring(0, trimmedMessage.length() - 2);
            } else if (trimmedMessage.endsWith("\n") || trimmedMessage.endsWith("\r")) {
                trimmedMessage = trimmedMessage.substring(0, trimmedMessage.length() - 1);
            }

            // 分割字段
            String[] parts = trimmedMessage.split(String.valueOf(FIELD_SEPARATOR));
            if (parts.length < 2) {
                log.warn("{} 消息字段不足", LOG_PREFIX);
                return null;
            }

            String cmdCode = parts[1].trim();
            AlarmMessageType messageType = AlarmMessageType.fromCode(cmdCode);

            log.debug("{} 解析消息类型: code={}, type={}", LOG_PREFIX, cmdCode, messageType);
            return messageType;
        } catch (Exception e) {
            log.error("{} 解析消息类型失败", LOG_PREFIX, e);
            return null;
        }
    }

    /**
     * 解析消息类型（ByteBuf 版本）
     *
     * @param data 数据
     * @return 消息类型，解析失败返回 null
     */
    public AlarmMessageType parseMessageType(ByteBuf data) {
        if (data == null || data.readableBytes() < MIN_MESSAGE_LENGTH) {
            return null;
        }

        try {
            byte[] bytes = new byte[data.readableBytes()];
            data.getBytes(data.readerIndex(), bytes);
            String message = new String(bytes, StandardCharsets.US_ASCII);
            return parseMessageType(message);
        } catch (Exception e) {
            log.error("{} 解析消息类型失败", LOG_PREFIX, e);
            return null;
        }
    }

    /**
     * 解析消息参数
     * <p>
     * 从消息中提取所有参数字段（第二个逗号后的内容）。
     * </p>
     *
     * @param message 消息内容
     * @return 参数数组，解析失败返回空数组
     */
    public String[] parseParams(String message) {
        if (message == null || message.length() < MIN_MESSAGE_LENGTH) {
            return new String[0];
        }

        try {
            // 去除可能的结束符
            String trimmedMessage = message.trim();
            if (trimmedMessage.endsWith("\r\n")) {
                trimmedMessage = trimmedMessage.substring(0, trimmedMessage.length() - 2);
            } else if (trimmedMessage.endsWith("\n") || trimmedMessage.endsWith("\r")) {
                trimmedMessage = trimmedMessage.substring(0, trimmedMessage.length() - 1);
            }

            // 分割字段
            String[] parts = trimmedMessage.split(String.valueOf(FIELD_SEPARATOR));
            if (parts.length <= 2) {
                return new String[0];
            }

            // 提取参数（从第三个字段开始）
            String[] params = new String[parts.length - 2];
            for (int i = 2; i < parts.length; i++) {
                params[i - 2] = parts[i].trim();
            }

            return params;
        } catch (Exception e) {
            log.error("{} 解析参数失败", LOG_PREFIX, e);
            return new String[0];
        }
    }


    /**
     * 完整解析消息
     *
     * @param message 消息内容
     * @return 解析结果
     */
    public ParseResult parseMessage(String message) {
        if (message == null || message.length() < MIN_MESSAGE_LENGTH) {
            return ParseResult.failure("消息长度不足");
        }

        try {
            // 解析账号
            String account = parseAccount(message);
            if (account == null) {
                return ParseResult.failure("解析账号失败");
            }

            // 解析消息类型
            AlarmMessageType messageType = parseMessageType(message);
            if (messageType == null) {
                return ParseResult.failure("解析消息类型失败");
            }

            // 解析参数
            String[] params = parseParams(message);

            return ParseResult.success(account, messageType, params);
        } catch (Exception e) {
            log.error("{} 解析消息失败", LOG_PREFIX, e);
            return ParseResult.failure("解析异常: " + e.getMessage());
        }
    }

    /**
     * 完整解析消息（ByteBuf 版本）
     *
     * @param data 数据
     * @return 解析结果
     */
    public ParseResult parseMessage(ByteBuf data) {
        if (data == null || data.readableBytes() < MIN_MESSAGE_LENGTH) {
            return ParseResult.failure("数据长度不足");
        }

        try {
            byte[] bytes = new byte[data.readableBytes()];
            data.getBytes(data.readerIndex(), bytes);
            String message = new String(bytes, StandardCharsets.US_ASCII);
            return parseMessage(message);
        } catch (Exception e) {
            log.error("{} 解析消息失败", LOG_PREFIX, e);
            return ParseResult.failure("解析异常: " + e.getMessage());
        }
    }

    // ==================== 构建方法 ====================

    /**
     * 构建消息
     * <p>
     * 通用的消息构建方法，根据账号、命令类型和参数构建完整的协议消息。
     * </p>
     *
     * @param account     账号
     * @param messageType 消息类型
     * @param params      参数（可为 null）
     * @return 完整的协议消息，构建失败返回 null
     */
    public String buildMessage(String account, AlarmMessageType messageType, String... params) {
        if (!validateAccount(account)) {
            log.warn("{} 无效的账号: {}", LOG_PREFIX, account);
            return null;
        }

        if (messageType == null || messageType == AlarmMessageType.UNKNOWN) {
            log.warn("{} 无效的消息类型: {}", LOG_PREFIX, messageType);
            return null;
        }

        try {
            StringBuilder sb = new StringBuilder();
            sb.append(account);
            sb.append(FIELD_SEPARATOR);
            sb.append(messageType.getCode());

            // 添加参数
            if (params != null && params.length > 0) {
                for (String param : params) {
                    if (param != null) {
                        sb.append(FIELD_SEPARATOR);
                        sb.append(param);
                    }
                }
            }

            String message = sb.toString();
            log.debug("{} 构建消息: {}", LOG_PREFIX, message);
            return message;
        } catch (Exception e) {
            log.error("{} 构建消息失败", LOG_PREFIX, e);
            return null;
        }
    }

    /**
     * 构建消息（带结束符）
     *
     * @param account     账号
     * @param messageType 消息类型
     * @param params      参数（可为 null）
     * @return 完整的协议消息（带结束符），构建失败返回 null
     */
    public String buildMessageWithTerminator(String account, AlarmMessageType messageType, String... params) {
        String message = buildMessage(account, messageType, params);
        if (message == null) {
            return null;
        }
        return message + MESSAGE_TERMINATOR;
    }

    /**
     * 构建消息字节数组
     *
     * @param account     账号
     * @param messageType 消息类型
     * @param params      参数（可为 null）
     * @return 消息字节数组，构建失败返回 null
     */
    public byte[] buildMessageBytes(String account, AlarmMessageType messageType, String... params) {
        String message = buildMessageWithTerminator(account, messageType, params);
        if (message == null) {
            return null;
        }
        return message.getBytes(StandardCharsets.US_ASCII);
    }

    // ==================== 命令构建方法 ====================

    /**
     * 构建心跳消息
     *
     * @param account 账号
     * @return 心跳消息
     */
    public String buildHeartbeatMessage(String account) {
        return buildMessage(account, AlarmMessageType.HEARTBEAT);
    }

    /**
     * 构建全局布防命令
     *
     * @param account 账号
     * @return 布防命令
     */
    public String buildArmAllCommand(String account) {
        return buildMessage(account, AlarmMessageType.ARM_ALL);
    }

    /**
     * 构建留守布防命令
     *
     * @param account 账号
     * @return 留守布防命令
     */
    public String buildArmStayCommand(String account) {
        return buildMessage(account, AlarmMessageType.ARM_STAY);
    }

    /**
     * 构建撤防命令
     *
     * @param account  账号
     * @param password 密码
     * @return 撤防命令
     */
    public String buildDisarmCommand(String account, String password) {
        if (!validatePassword(password)) {
            log.warn("{} 无效的密码格式", LOG_PREFIX);
            return null;
        }
        return buildMessage(account, AlarmMessageType.DISARM, password);
    }

    /**
     * 构建分区布防命令
     *
     * @param account     账号
     * @param partitionNo 分区号
     * @return 分区布防命令
     */
    public String buildArmPartitionCommand(String account, int partitionNo) {
        if (partitionNo < 1) {
            log.warn("{} 无效的分区号: {}", LOG_PREFIX, partitionNo);
            return null;
        }
        return buildMessage(account, AlarmMessageType.ARM_PARTITION, String.valueOf(partitionNo));
    }

    /**
     * 构建分区撤防命令
     *
     * @param account     账号
     * @param partitionNo 分区号
     * @param password    密码
     * @return 分区撤防命令
     */
    public String buildDisarmPartitionCommand(String account, int partitionNo, String password) {
        if (partitionNo < 1) {
            log.warn("{} 无效的分区号: {}", LOG_PREFIX, partitionNo);
            return null;
        }
        if (!validatePassword(password)) {
            log.warn("{} 无效的密码格式", LOG_PREFIX);
            return null;
        }
        return buildMessage(account, AlarmMessageType.DISARM_PARTITION, 
                String.valueOf(partitionNo), password);
    }

    /**
     * 构建旁路防区命令
     *
     * @param account  账号
     * @param zoneNo   防区号
     * @param password 密码
     * @return 旁路防区命令
     */
    public String buildBypassZoneCommand(String account, int zoneNo, String password) {
        if (zoneNo < 1) {
            log.warn("{} 无效的防区号: {}", LOG_PREFIX, zoneNo);
            return null;
        }
        if (!validatePassword(password)) {
            log.warn("{} 无效的密码格式", LOG_PREFIX);
            return null;
        }
        return buildMessage(account, AlarmMessageType.BYPASS_ZONE, 
                String.valueOf(zoneNo), password);
    }

    /**
     * 构建取消旁路命令
     *
     * @param account  账号
     * @param zoneNo   防区号
     * @param password 密码
     * @return 取消旁路命令
     */
    public String buildUnbypassZoneCommand(String account, int zoneNo, String password) {
        if (zoneNo < 1) {
            log.warn("{} 无效的防区号: {}", LOG_PREFIX, zoneNo);
            return null;
        }
        if (!validatePassword(password)) {
            log.warn("{} 无效的密码格式", LOG_PREFIX);
            return null;
        }
        return buildMessage(account, AlarmMessageType.UNBYPASS_ZONE, 
                String.valueOf(zoneNo), password);
    }

    /**
     * 构建状态查询命令
     *
     * @param account 账号
     * @return 状态查询命令
     */
    public String buildQueryStatusCommand(String account) {
        return buildMessage(account, AlarmMessageType.QUERY_STATUS);
    }

    /**
     * 构建确认响应
     *
     * @param account 账号
     * @param cmd     原始命令（可选）
     * @return 确认响应
     */
    public String buildAckResponse(String account, String cmd) {
        if (cmd != null && !cmd.isEmpty()) {
            return buildMessage(account, AlarmMessageType.ACK, cmd);
        }
        return buildMessage(account, AlarmMessageType.ACK);
    }

    /**
     * 构建否定响应
     *
     * @param account   账号
     * @param cmd       原始命令（可选）
     * @param errorCode 错误码（可选）
     * @return 否定响应
     */
    public String buildNakResponse(String account, String cmd, String errorCode) {
        if (cmd != null && !cmd.isEmpty()) {
            if (errorCode != null && !errorCode.isEmpty()) {
                return buildMessage(account, AlarmMessageType.NAK, cmd, errorCode);
            }
            return buildMessage(account, AlarmMessageType.NAK, cmd);
        }
        return buildMessage(account, AlarmMessageType.NAK);
    }


    // ==================== 内部数据类 ====================

    /**
     * 解析结果
     */
    @Data
    public static class ParseResult {
        /**
         * 是否成功
         */
        private boolean success;

        /**
         * 账号
         */
        private String account;

        /**
         * 消息类型
         */
        private AlarmMessageType messageType;

        /**
         * 参数数组
         */
        private String[] params;

        /**
         * 错误信息
         */
        private String errorMessage;

        /**
         * 创建成功结果
         *
         * @param account     账号
         * @param messageType 消息类型
         * @param params      参数
         * @return 成功结果
         */
        public static ParseResult success(String account, AlarmMessageType messageType, String[] params) {
            ParseResult result = new ParseResult();
            result.setSuccess(true);
            result.setAccount(account);
            result.setMessageType(messageType);
            result.setParams(params != null ? params : new String[0]);
            return result;
        }

        /**
         * 创建失败结果
         *
         * @param errorMessage 错误信息
         * @return 失败结果
         */
        public static ParseResult failure(String errorMessage) {
            ParseResult result = new ParseResult();
            result.setSuccess(false);
            result.setErrorMessage(errorMessage);
            return result;
        }

        /**
         * 获取指定索引的参数
         *
         * @param index 索引
         * @return 参数值，如果索引越界则返回 null
         */
        public String getParam(int index) {
            if (params == null || index < 0 || index >= params.length) {
                return null;
            }
            return params[index];
        }

        /**
         * 获取参数数量
         *
         * @return 参数数量
         */
        public int getParamCount() {
            return params != null ? params.length : 0;
        }

        /**
         * 获取指定索引的参数（整数）
         *
         * @param index        索引
         * @param defaultValue 默认值
         * @return 参数值，如果解析失败则返回默认值
         */
        public int getParamAsInt(int index, int defaultValue) {
            String param = getParam(index);
            if (param == null || param.isEmpty()) {
                return defaultValue;
            }
            try {
                return Integer.parseInt(param);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
    }

    /**
     * 报警事件信息
     */
    @Data
    public static class AlarmEventInfo {
        /**
         * 账号
         */
        private String account;

        /**
         * 事件类型（如 ZONE, PARTITION, SYSTEM）
         */
        private String eventType;

        /**
         * 事件源编号（防区号或分区号）
         */
        private Integer sourceNo;

        /**
         * 报警类型（如 INTRUSION, FIRE, PANIC）
         */
        private String alarmType;

        /**
         * 时间戳
         */
        private long timestamp;

        /**
         * 原始消息
         */
        private String rawMessage;
    }

    /**
     * 状态信息
     */
    @Data
    public static class StatusInfo {
        /**
         * 账号
         */
        private String account;

        /**
         * 布防状态（ARMED, DISARMED, STAY_ARMED）
         */
        private String armStatus;

        /**
         * 已布防的分区列表
         */
        private int[] armedPartitions;

        /**
         * 已旁路的防区列表
         */
        private int[] bypassedZones;

        /**
         * 报警中的防区列表
         */
        private int[] alarmingZones;

        /**
         * 时间戳
         */
        private long timestamp;
    }

    // ==================== IP9500 OPC 协议支持 ====================

    /**
     * IP9500 OPC 协议解析结果
     * <p>
     * 支持以下消息类型：
     * <ul>
     *     <li>E - 事件报告（设备→中心）</li>
     *     <li>e - 事件应答（中心→设备）</li>
     *     <li>C - 控制命令（中心→设备）</li>
     *     <li>c - 控制应答（设备→中心）</li>
     * </ul>
     * </p>
     */
    @Data
    public static class OpcParseResult {
        /**
         * 是否解析成功
         */
        private boolean success;

        /**
         * 消息类型前缀：'E'=事件报告, 'e'=事件应答, 'C'=控制命令, 'c'=控制应答
         */
        private char messageType;

        /**
         * 账号（不含前缀，如 "1234"）
         */
        private String account;

        /**
         * CID 事件码（4位），仅事件报告有效
         * 千位为1表示新事件，千位为3表示事件恢复
         */
        private String eventCode;

        /**
         * 分区号（2位），仅事件报告有效
         * 无分区的系统为 "00"
         */
        private String area;

        /**
         * 防区号/用户号/模块号（3位），仅事件报告有效
         */
        private String point;

        /**
         * 序列号（0-9999）
         */
        private int sequence;

        /**
         * 是否为心跳消息
         * 心跳消息的事件码为 "0000"
         */
        private boolean heartbeat;

        /**
         * 控制指令码，仅控制命令有效
         */
        private Integer controlCode;

        /**
         * 控制参数，仅控制命令有效
         */
        private String controlParam;

        /**
         * 控制密码，仅控制命令有效
         */
        private String controlPassword;

        /**
         * 执行结果（0=成功，1=失败），仅控制应答有效
         */
        private Integer result;

        /**
         * 错误信息
         */
        private String errorMessage;

        /**
         * 原始消息
         */
        private String rawMessage;

        /**
         * 创建成功的解析结果
         */
        public static OpcParseResult success() {
            OpcParseResult result = new OpcParseResult();
            result.setSuccess(true);
            return result;
        }

        /**
         * 创建失败的解析结果
         *
         * @param errorMessage 错误信息
         * @return 失败结果
         */
        public static OpcParseResult failure(String errorMessage) {
            OpcParseResult result = new OpcParseResult();
            result.setSuccess(false);
            result.setErrorMessage(errorMessage);
            return result;
        }

        /**
         * 判断是否为事件报告消息
         */
        public boolean isEventReport() {
            return messageType == 'E';
        }

        /**
         * 判断是否为事件应答消息
         */
        public boolean isEventAck() {
            return messageType == 'e';
        }

        /**
         * 判断是否为控制命令消息
         */
        public boolean isControlCommand() {
            return messageType == 'C';
        }

        /**
         * 判断是否为控制应答消息
         */
        public boolean isControlAck() {
            return messageType == 'c';
        }
    }

    /**
     * IP9500 OPC 协议控制指令码枚举
     */
    public enum OpcControlCode {
        QUERY_STATUS(0, "查询设备状态"),
        DISARM(1, "撤防"),
        ARM_AWAY(2, "外出布防"),
        ARM_STAY(3, "居家布防"),
        BYPASS_ZONE(4, "防区旁路"),
        UNBYPASS_ZONE(5, "撤销防区旁路"),
        OUTPUT_ON(6, "打开输出"),
        OUTPUT_OFF(7, "关闭输出"),
        ARM_ZONE(8, "单防区布防"),
        DISARM_ZONE(9, "单防区撤防"),
        QUERY_PARTITION_STATUS(10, "查询分区和防区状态"),
        ALARM_RESET(11, "报警复位");

        private final int code;
        private final String description;

        OpcControlCode(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        /**
         * 根据代码获取枚举值
         *
         * @param code 控制指令码
         * @return 枚举值，未找到返回 null
         */
        public static OpcControlCode fromCode(int code) {
            for (OpcControlCode value : values()) {
                if (value.code == code) {
                    return value;
                }
            }
            return null;
        }
    }

    // ==================== IP9500 OPC 协议解析方法 ====================

    /**
     * 心跳事件码
     */
    public static final String OPC_HEARTBEAT_EVENT_CODE = "0000";

    /**
     * 解析 IP9500 OPC 协议消息
     * <p>
     * 支持以下消息格式：
     * <ul>
     *     <li>事件报告：E{account},{event}{area}{point}{sequence}</li>
     *     <li>事件应答：e{account},{sequence}</li>
     *     <li>控制命令：C{account},{control},{param},{password},{sequence}</li>
     *     <li>控制应答：c{account},{result},{sequence}</li>
     * </ul>
     * </p>
     *
     * @param message 原始消息
     * @return 解析结果
     */
    public OpcParseResult parseOpcMessage(String message) {
        if (message == null || message.isEmpty()) {
            return OpcParseResult.failure("消息为空");
        }

        // 去除可能的结束符
        String trimmedMessage = message.trim();
        if (trimmedMessage.endsWith("\r\n")) {
            trimmedMessage = trimmedMessage.substring(0, trimmedMessage.length() - 2);
        } else if (trimmedMessage.endsWith("\n") || trimmedMessage.endsWith("\r")) {
            trimmedMessage = trimmedMessage.substring(0, trimmedMessage.length() - 1);
        }

        if (trimmedMessage.isEmpty()) {
            return OpcParseResult.failure("消息为空");
        }

        // 获取消息类型前缀
        char prefix = trimmedMessage.charAt(0);

        switch (prefix) {
            case 'E':
                return parseOpcEventReport(trimmedMessage);
            case 'e':
                return parseOpcEventAck(trimmedMessage);
            case 'C':
                return parseOpcControlCommand(trimmedMessage);
            case 'c':
                return parseOpcControlAck(trimmedMessage);
            default:
                return OpcParseResult.failure("未知的消息类型前缀: " + prefix);
        }
    }

    /**
     * 解析 OPC 事件报告消息
     * <p>
     * 格式：E{account},{event}{area}{point}{sequence}
     * 示例：E1234,1130001790123
     * </p>
     *
     * @param message 消息（已去除结束符）
     * @return 解析结果
     */
    private OpcParseResult parseOpcEventReport(String message) {
        OpcParseResult result = OpcParseResult.success();
        result.setMessageType('E');
        result.setRawMessage(message);

        try {
            // 查找逗号分隔符
            int commaIndex = message.indexOf(',');
            if (commaIndex <= 1) {
                return OpcParseResult.failure("事件报告格式错误：缺少逗号分隔符");
            }

            // 提取账号（去掉 E 前缀）
            String account = message.substring(1, commaIndex);
            if (!validateAccount(account)) {
                return OpcParseResult.failure("无效的账号格式: " + account);
            }
            result.setAccount(account);

            // 提取事件数据部分
            String eventData = message.substring(commaIndex + 1);
            
            // 事件数据格式：{event:4}{area:2}{point:3}{sequence:3-4}
            // 最小长度：4+2+3+1 = 10，最大长度：4+2+3+4 = 13
            if (eventData.length() < 10) {
                return OpcParseResult.failure("事件数据长度不足: " + eventData.length());
            }

            // 提取事件码（4位）
            String eventCode = eventData.substring(0, 4);
            result.setEventCode(eventCode);

            // 提取分区号（2位）
            String area = eventData.substring(4, 6);
            result.setArea(area);

            // 提取防区号（3位）
            String point = eventData.substring(6, 9);
            result.setPoint(point);

            // 提取序列号（剩余部分）
            String sequenceStr = eventData.substring(9);
            try {
                int sequence = Integer.parseInt(sequenceStr);
                result.setSequence(sequence);
            } catch (NumberFormatException e) {
                return OpcParseResult.failure("无效的序列号: " + sequenceStr);
            }

            // 判断是否为心跳消息
            result.setHeartbeat(isHeartbeatEvent(eventCode));

            log.debug("{} 解析 OPC 事件报告: account={}, eventCode={}, area={}, point={}, seq={}, heartbeat={}",
                    LOG_PREFIX, account, eventCode, area, point, result.getSequence(), result.isHeartbeat());

            return result;
        } catch (Exception e) {
            log.error("{} 解析 OPC 事件报告失败: {}", LOG_PREFIX, message, e);
            return OpcParseResult.failure("解析异常: " + e.getMessage());
        }
    }

    /**
     * 解析 OPC 事件应答消息
     * <p>
     * 格式：e{account},{sequence}
     * 示例：e1234,125
     * </p>
     *
     * @param message 消息（已去除结束符）
     * @return 解析结果
     */
    private OpcParseResult parseOpcEventAck(String message) {
        OpcParseResult result = OpcParseResult.success();
        result.setMessageType('e');
        result.setRawMessage(message);

        try {
            // 查找逗号分隔符
            int commaIndex = message.indexOf(',');
            if (commaIndex <= 1) {
                return OpcParseResult.failure("事件应答格式错误：缺少逗号分隔符");
            }

            // 提取账号（去掉 e 前缀）
            String account = message.substring(1, commaIndex);
            if (!validateAccount(account)) {
                return OpcParseResult.failure("无效的账号格式: " + account);
            }
            result.setAccount(account);

            // 提取序列号
            String sequenceStr = message.substring(commaIndex + 1);
            try {
                int sequence = Integer.parseInt(sequenceStr);
                result.setSequence(sequence);
            } catch (NumberFormatException e) {
                return OpcParseResult.failure("无效的序列号: " + sequenceStr);
            }

            log.debug("{} 解析 OPC 事件应答: account={}, seq={}",
                    LOG_PREFIX, account, result.getSequence());

            return result;
        } catch (Exception e) {
            log.error("{} 解析 OPC 事件应答失败: {}", LOG_PREFIX, message, e);
            return OpcParseResult.failure("解析异常: " + e.getMessage());
        }
    }

    /**
     * 解析 OPC 控制命令消息
     * <p>
     * 格式：C{account},{control},{param},{password},{sequence}
     * 示例：C1234,2,0,9876,126
     * </p>
     *
     * @param message 消息（已去除结束符）
     * @return 解析结果
     */
    private OpcParseResult parseOpcControlCommand(String message) {
        OpcParseResult result = OpcParseResult.success();
        result.setMessageType('C');
        result.setRawMessage(message);

        try {
            // 分割字段
            String[] parts = message.split(",");
            if (parts.length < 5) {
                return OpcParseResult.failure("控制命令格式错误：字段不足，需要5个字段");
            }

            // 提取账号（去掉 C 前缀）
            String account = parts[0].substring(1);
            if (!validateAccount(account)) {
                return OpcParseResult.failure("无效的账号格式: " + account);
            }
            result.setAccount(account);

            // 提取控制指令码
            try {
                int controlCode = Integer.parseInt(parts[1]);
                result.setControlCode(controlCode);
            } catch (NumberFormatException e) {
                return OpcParseResult.failure("无效的控制指令码: " + parts[1]);
            }

            // 提取控制参数
            result.setControlParam(parts[2]);

            // 提取密码
            result.setControlPassword(parts[3]);

            // 提取序列号
            try {
                int sequence = Integer.parseInt(parts[4]);
                result.setSequence(sequence);
            } catch (NumberFormatException e) {
                return OpcParseResult.failure("无效的序列号: " + parts[4]);
            }

            log.debug("{} 解析 OPC 控制命令: account={}, control={}, param={}, seq={}",
                    LOG_PREFIX, account, result.getControlCode(), result.getControlParam(), result.getSequence());

            return result;
        } catch (Exception e) {
            log.error("{} 解析 OPC 控制命令失败: {}", LOG_PREFIX, message, e);
            return OpcParseResult.failure("解析异常: " + e.getMessage());
        }
    }

    /**
     * 解析 OPC 控制应答消息
     * <p>
     * 格式：c{account},{result},{sequence}
     * 示例：c1234,0,126
     * </p>
     *
     * @param message 消息（已去除结束符）
     * @return 解析结果
     */
    private OpcParseResult parseOpcControlAck(String message) {
        OpcParseResult result = OpcParseResult.success();
        result.setMessageType('c');
        result.setRawMessage(message);

        try {
            // 分割字段
            String[] parts = message.split(",");
            if (parts.length < 3) {
                return OpcParseResult.failure("控制应答格式错误：字段不足，需要3个字段");
            }

            // 提取账号（去掉 c 前缀）
            String account = parts[0].substring(1);
            if (!validateAccount(account)) {
                return OpcParseResult.failure("无效的账号格式: " + account);
            }
            result.setAccount(account);

            // 提取执行结果
            try {
                int execResult = Integer.parseInt(parts[1]);
                result.setResult(execResult);
            } catch (NumberFormatException e) {
                return OpcParseResult.failure("无效的执行结果: " + parts[1]);
            }

            // 提取序列号
            try {
                int sequence = Integer.parseInt(parts[2]);
                result.setSequence(sequence);
            } catch (NumberFormatException e) {
                return OpcParseResult.failure("无效的序列号: " + parts[2]);
            }

            log.debug("{} 解析 OPC 控制应答: account={}, result={}, seq={}",
                    LOG_PREFIX, account, result.getResult(), result.getSequence());

            return result;
        } catch (Exception e) {
            log.error("{} 解析 OPC 控制应答失败: {}", LOG_PREFIX, message, e);
            return OpcParseResult.failure("解析异常: " + e.getMessage());
        }
    }

    /**
     * 判断事件码是否为心跳
     * <p>
     * 心跳消息的事件码为 "0000"
     * </p>
     *
     * @param eventCode 事件码
     * @return 是否为心跳
     */
    public boolean isHeartbeatEvent(String eventCode) {
        return OPC_HEARTBEAT_EVENT_CODE.equals(eventCode);
    }

    // ==================== IP9500 OPC 协议构建方法 ====================

    /**
     * 构建 OPC 事件应答消息
     * <p>
     * 格式：e{account},{sequence}
     * 示例：e1234,125
     * </p>
     *
     * @param account  账号（不含前缀）
     * @param sequence 序列号
     * @return 应答消息
     */
    public String buildOpcEventAck(String account, int sequence) {
        if (account == null || account.isEmpty()) {
            log.warn("{} 构建 OPC 事件应答失败：账号为空", LOG_PREFIX);
            return null;
        }
        String message = "e" + account + "," + sequence;
        log.debug("{} 构建 OPC 事件应答: {}", LOG_PREFIX, message);
        return message;
    }

    /**
     * 构建 OPC 控制命令消息
     * <p>
     * 格式：C{account},{control},{param},{password},{sequence}
     * 示例：C1234,2,0,9876,126
     * </p>
     *
     * @param account     账号（不含前缀）
     * @param controlCode 控制指令码
     * @param param       参数
     * @param password    密码
     * @param sequence    序列号
     * @return 控制命令消息
     */
    public String buildOpcControlCommand(String account, int controlCode, String param, String password, int sequence) {
        if (account == null || account.isEmpty()) {
            log.warn("{} 构建 OPC 控制命令失败：账号为空", LOG_PREFIX);
            return null;
        }
        if (param == null) {
            param = "0";
        }
        if (password == null) {
            password = "";
        }
        String message = "C" + account + "," + controlCode + "," + param + "," + password + "," + sequence;
        log.debug("{} 构建 OPC 控制命令: {}", LOG_PREFIX, message);
        return message;
    }

    /**
     * 构建 OPC 控制命令消息（使用枚举）
     *
     * @param account     账号（不含前缀）
     * @param controlCode 控制指令码枚举
     * @param param       参数
     * @param password    密码
     * @param sequence    序列号
     * @return 控制命令消息
     */
    public String buildOpcControlCommand(String account, OpcControlCode controlCode, String param, String password, int sequence) {
        if (controlCode == null) {
            log.warn("{} 构建 OPC 控制命令失败：控制指令码为空", LOG_PREFIX);
            return null;
        }
        return buildOpcControlCommand(account, controlCode.getCode(), param, password, sequence);
    }

    // ==================== 事件解析方法 ====================

    /**
     * 解析报警事件
     * <p>
     * 报警事件格式：account,ALARM,EVENT_TYPE,SOURCE_NO,ALARM_TYPE
     * </p>
     *
     * @param message 消息内容
     * @return 报警事件信息，解析失败返回 null
     */
    public AlarmEventInfo parseAlarmEvent(String message) {
        ParseResult result = parseMessage(message);
        if (!result.isSuccess() || result.getMessageType() != AlarmMessageType.ALARM) {
            return null;
        }

        try {
            AlarmEventInfo info = new AlarmEventInfo();
            info.setAccount(result.getAccount());
            info.setTimestamp(System.currentTimeMillis());
            info.setRawMessage(message);

            // 解析事件类型
            if (result.getParamCount() >= 1) {
                info.setEventType(result.getParam(0));
            }

            // 解析事件源编号
            if (result.getParamCount() >= 2) {
                info.setSourceNo(result.getParamAsInt(1, -1));
            }

            // 解析报警类型
            if (result.getParamCount() >= 3) {
                info.setAlarmType(result.getParam(2));
            }

            return info;
        } catch (Exception e) {
            log.error("{} 解析报警事件失败", LOG_PREFIX, e);
            return null;
        }
    }

    /**
     * 解析状态上报
     * <p>
     * 状态上报格式：account,STATUS,ARM_STATUS,PARTITION1,PARTITION2,...
     * </p>
     *
     * @param message 消息内容
     * @return 状态信息，解析失败返回 null
     */
    public StatusInfo parseStatusReport(String message) {
        ParseResult result = parseMessage(message);
        if (!result.isSuccess() || result.getMessageType() != AlarmMessageType.STATUS) {
            return null;
        }

        try {
            StatusInfo info = new StatusInfo();
            info.setAccount(result.getAccount());
            info.setTimestamp(System.currentTimeMillis());

            // 解析布防状态
            if (result.getParamCount() >= 1) {
                info.setArmStatus(result.getParam(0));
            }

            // 解析已布防的分区列表
            if (result.getParamCount() > 1) {
                int[] partitions = new int[result.getParamCount() - 1];
                for (int i = 1; i < result.getParamCount(); i++) {
                    partitions[i - 1] = result.getParamAsInt(i, 0);
                }
                info.setArmedPartitions(partitions);
            }

            return info;
        } catch (Exception e) {
            log.error("{} 解析状态上报失败", LOG_PREFIX, e);
            return null;
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 将消息转换为 Map 格式
     *
     * @param message 消息内容
     * @return Map 格式的消息，包含 account, cmd, params
     */
    public Map<String, Object> parseToMap(String message) {
        Map<String, Object> map = new HashMap<>();
        
        ParseResult result = parseMessage(message);
        if (!result.isSuccess()) {
            map.put("error", result.getErrorMessage());
            return map;
        }

        map.put("account", result.getAccount());
        map.put("cmd", result.getMessageType().getCode());
        map.put("messageType", result.getMessageType());
        map.put("params", result.getParams());
        
        return map;
    }

    /**
     * 检查消息是否为心跳
     *
     * @param message 消息内容
     * @return 是否为心跳消息
     */
    public boolean isHeartbeat(String message) {
        AlarmMessageType type = parseMessageType(message);
        return type == AlarmMessageType.HEARTBEAT;
    }

    /**
     * 检查消息是否为报警事件
     *
     * @param message 消息内容
     * @return 是否为报警事件
     */
    public boolean isAlarmEvent(String message) {
        AlarmMessageType type = parseMessageType(message);
        return type == AlarmMessageType.ALARM;
    }

    /**
     * 检查消息是否为控制命令
     *
     * @param message 消息内容
     * @return 是否为控制命令
     */
    public boolean isControlCommand(String message) {
        AlarmMessageType type = parseMessageType(message);
        return type != null && type.isControlCommand();
    }

    /**
     * 检查消息是否为响应消息
     *
     * @param message 消息内容
     * @return 是否为响应消息
     */
    public boolean isResponse(String message) {
        AlarmMessageType type = parseMessageType(message);
        return type != null && type.isResponseMessage();
    }
}
