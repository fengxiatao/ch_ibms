package cn.iocoder.yudao.module.iot.newgateway.plugins.changhui;

import cn.iocoder.yudao.module.iot.newgateway.plugins.changhui.TeaCrypto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * 长辉协议编解码器
 * 
 * <p>实现长辉自研TCP协议的编解码功能。</p>
 * 
 * <h2>协议格式</h2>
 * <pre>
 * +--------+--------+------------+-------+---+-------+---+-----+------+------+------+----+------+
 * | 帧头   | 长度   | 测站编码   | 起始  | L | 起始  | C | AFN | 数据 | 密码 | 时间 | CS | 结束 |
 * | 3字节  | 4字节  | 10字节     | 1字节 | 1 | 1字节 | 1 | 1   | 可变 | 2字节| 5字节| 1  | 1字节|
 * | EF7EEF |        |            | 68    |   | 68    |   |     |      |      |      |    | 16   |
 * +--------+--------+------------+-------+---+-------+---+-----+------+------+------+----+------+
 * </pre>
 * 
 * <h2>AFN 应用功能码</h2>
 * <ul>
 *     <li>0x3C - 心跳消息</li>
 *     <li>0x02 - 升级触发命令</li>
 *     <li>0x10 - 升级URL下发</li>
 *     <li>0x15 - 升级开始状态</li>
 *     <li>0x66 - 升级进度</li>
 *     <li>0x67 - 升级完成</li>
 *     <li>0x68 - 升级失败</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 * @see ChanghuiPlugin
 * @see ChanghuiMessageType
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "changhui", havingValue = "true", matchIfMissing = true)
public class ChanghuiProtocolCodec {

    /**
     * 日志前缀
     */
    private static final String LOG_PREFIX = "[ChanghuiProtocolCodec]";

    // ==================== 协议常量 ====================

    /**
     * 帧头: EF7EEF
     */
    public static final byte[] FRAME_HEADER = {(byte) 0xEF, 0x7E, (byte) 0xEF};

    /**
     * 帧头长度
     */
    public static final int FRAME_HEADER_LENGTH = 3;

    /**
     * 长度字段长度
     */
    public static final int LENGTH_FIELD_LENGTH = 4;

    /**
     * 测站编码长度
     */
    public static final int STATION_CODE_LENGTH = 10;

    /**
     * 起始字符
     */
    public static final byte START_CHAR = 0x68;

    /**
     * 结束字符
     */
    public static final byte END_CHAR = 0x16;

    /**
     * 密码字段长度
     */
    public static final int PASSWORD_LENGTH = 2;

    /**
     * 时间标签长度
     */
    public static final int TIME_TAG_LENGTH = 5;

    /**
     * 校验和长度
     */
    public static final int CHECKSUM_LENGTH = 1;

    /**
     * 最小帧长度（帧头 + 长度 + 测站编码 + 起始 + L + 起始 + C + AFN + 密码 + 时间 + CS + 结束）
     * = 3 + 4 + 10 + 1 + 1 + 1 + 1 + 1 + 2 + 5 + 1 + 1 = 31
     */
    public static final int MIN_FRAME_LENGTH = 31;

    /**
     * 固定头部长度（帧头 + 长度 + 测站编码）
     */
    public static final int FIXED_HEADER_LENGTH = FRAME_HEADER_LENGTH + LENGTH_FIELD_LENGTH + STATION_CODE_LENGTH;

    /**
     * AFN 字段偏移量（从帧头开始）
     * = 帧头(3) + 长度(4) + 测站编码(10) + 起始(1) + L(1) + 起始(1) + C(1) = 21
     */
    public static final int AFN_OFFSET = 21;

    /**
     * 数据域偏移量（从帧头开始）
     * = AFN_OFFSET + AFN(1) = 22
     */
    public static final int DATA_OFFSET = 22;

    // ==================== 控制域C常量 ====================

    /**
     * 控制域C - 上行心跳
     */
    public static final byte CONTROL_UPSTREAM_HEARTBEAT = (byte) 0x8E;

    /**
     * 控制域C - 下行心跳响应（服务器→设备）
     * 根据协议文档，心跳响应的控制域与上行心跳一致，都是 0x8E
     */
    public static final byte CONTROL_DOWNSTREAM_HEARTBEAT_RESP = (byte) 0x8E;

    /**
     * 控制域C - 下行命令（升级触发、升级URL）
     */
    public static final byte CONTROL_DOWNSTREAM_CMD = (byte) 0x0E;

    /**
     * 控制域C - 上行升级触发响应
     */
    public static final byte CONTROL_UPSTREAM_UPGRADE_TRIGGER_RESP = (byte) 0x0E;

    /**
     * 控制域C - 下行升级URL
     */
    public static final byte CONTROL_DOWNSTREAM_URL = (byte) 0x02;

    /**
     * 控制域C - 上行升级URL响应
     */
    public static final byte CONTROL_UPSTREAM_URL_RESP = (byte) 0x02;

    /**
     * 控制域C - 上行升级状态
     */
    public static final byte CONTROL_UPSTREAM_STATUS = (byte) 0xC0;

    /**
     * 控制域C - 下行升级状态响应
     */
    public static final byte CONTROL_DOWNSTREAM_STATUS_RESP = (byte) 0x40;

    // ==================== 协议标识常量 ====================

    /**
     * 协议标识 - 德通协议2010
     */
    public static final short PROTOCOL_DETONG_2010 = 0x07DA;

    /**
     * 协议标识 - 宁夏协议
     */
    public static final short PROTOCOL_NINGXIA = 0x4010;

    /**
     * 协议标识 - 651协议
     */
    public static final short PROTOCOL_651 = 0x0651;

    /**
     * 协议标识 - 427协议
     */
    public static final short PROTOCOL_427 = 0x0427;

    // ==================== 解析方法 ====================

    /**
     * 验证帧头是否有效
     *
     * @param data 数据
     * @return 是否有效
     */
    public boolean validateFrameHeader(ByteBuf data) {
        if (data == null || data.readableBytes() < FRAME_HEADER_LENGTH) {
            return false;
        }

        int readerIndex = data.readerIndex();
        for (int i = 0; i < FRAME_HEADER_LENGTH; i++) {
            if (data.getByte(readerIndex + i) != FRAME_HEADER[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 验证帧头是否有效（字节数组版本）
     *
     * @param data 数据
     * @return 是否有效
     */
    public boolean validateFrameHeader(byte[] data) {
        if (data == null || data.length < FRAME_HEADER_LENGTH) {
            return false;
        }

        for (int i = 0; i < FRAME_HEADER_LENGTH; i++) {
            if (data[i] != FRAME_HEADER[i]) {
                return false;
            }
        }
        return true;
    }


    /**
     * 解析测站编码
     * <p>
     * 测站编码位于帧头(3字节) + 长度(4字节) 之后，共10字节。
     * 返回20字符的十六进制字符串。
     * </p>
     *
     * @param data 数据
     * @return 测站编码（20字符十六进制字符串），解析失败返回 null
     */
    public String parseStationCode(ByteBuf data) {
        if (data == null || data.readableBytes() < FIXED_HEADER_LENGTH) {
            log.warn("{} 数据长度不足，无法解析测站编码: length={}", 
                    LOG_PREFIX, data != null ? data.readableBytes() : 0);
            return null;
        }

        try {
            // 验证帧头
            if (!validateFrameHeader(data)) {
                log.warn("{} 无效的帧头", LOG_PREFIX);
                return null;
            }

            // 读取测站编码（偏移量 = 帧头 + 长度 = 7）
            int stationCodeOffset = data.readerIndex() + FRAME_HEADER_LENGTH + LENGTH_FIELD_LENGTH;
            byte[] stationCodeBytes = new byte[STATION_CODE_LENGTH];
            data.getBytes(stationCodeOffset, stationCodeBytes);

            // 转换为十六进制字符串
            String stationCode = bytesToHex(stationCodeBytes);
            log.debug("{} 解析测站编码: {}", LOG_PREFIX, stationCode);

            return stationCode;
        } catch (Exception e) {
            log.error("{} 解析测站编码失败", LOG_PREFIX, e);
            return null;
        }
    }

    /**
     * 解析测站编码（字节数组版本）
     *
     * @param data 数据
     * @return 测站编码（20字符十六进制字符串），解析失败返回 null
     */
    public String parseStationCode(byte[] data) {
        if (data == null || data.length < FIXED_HEADER_LENGTH) {
            log.warn("{} 数据长度不足，无法解析测站编码: length={}", 
                    LOG_PREFIX, data != null ? data.length : 0);
            return null;
        }

        try {
            // 验证帧头
            if (!validateFrameHeader(data)) {
                log.warn("{} 无效的帧头", LOG_PREFIX);
                return null;
            }

            // 读取测站编码（偏移量 = 帧头 + 长度 = 7）
            int stationCodeOffset = FRAME_HEADER_LENGTH + LENGTH_FIELD_LENGTH;
            byte[] stationCodeBytes = new byte[STATION_CODE_LENGTH];
            System.arraycopy(data, stationCodeOffset, stationCodeBytes, 0, STATION_CODE_LENGTH);

            // 转换为十六进制字符串
            return bytesToHex(stationCodeBytes);
        } catch (Exception e) {
            log.error("{} 解析测站编码失败", LOG_PREFIX, e);
            return null;
        }
    }

    /**
     * 解析消息类型
     * <p>
     * AFN 字段位于固定位置，偏移量为 21 字节。
     * </p>
     *
     * @param data 数据
     * @return 消息类型，解析失败返回 null
     */
    public ChanghuiMessageType parseMessageType(ByteBuf data) {
        if (data == null || data.readableBytes() < AFN_OFFSET + 1) {
            log.warn("{} 数据长度不足，无法解析消息类型: length={}", 
                    LOG_PREFIX, data != null ? data.readableBytes() : 0);
            return null;
        }

        try {
            // 验证帧头
            if (!validateFrameHeader(data)) {
                log.warn("{} 无效的帧头", LOG_PREFIX);
                return null;
            }

            // 读取 AFN 字段
            byte afn = data.getByte(data.readerIndex() + AFN_OFFSET);
            ChanghuiMessageType messageType = ChanghuiMessageType.fromAfn(afn);

            log.debug("{} 解析消息类型: AFN=0x{}, type={}", 
                    LOG_PREFIX, String.format("%02X", afn), messageType);

            return messageType;
        } catch (Exception e) {
            log.error("{} 解析消息类型失败", LOG_PREFIX, e);
            return null;
        }
    }

    /**
     * 解析消息类型（字节数组版本）
     *
     * @param data 数据
     * @return 消息类型，解析失败返回 null
     */
    public ChanghuiMessageType parseMessageType(byte[] data) {
        if (data == null || data.length < AFN_OFFSET + 1) {
            log.warn("{} 数据长度不足，无法解析消息类型: length={}", 
                    LOG_PREFIX, data != null ? data.length : 0);
            return null;
        }

        try {
            // 验证帧头
            if (!validateFrameHeader(data)) {
                log.warn("{} 无效的帧头", LOG_PREFIX);
                return null;
            }

            // 读取 AFN 字段
            byte afn = data[AFN_OFFSET];
            return ChanghuiMessageType.fromAfn(afn);
        } catch (Exception e) {
            log.error("{} 解析消息类型失败", LOG_PREFIX, e);
            return null;
        }
    }

    /**
     * 解析心跳消息
     *
     * @param data 数据
     * @return 心跳数据，解析失败返回 null
     */
    public HeartbeatInfo parseHeartbeat(ByteBuf data) {
        if (data == null || data.readableBytes() < MIN_FRAME_LENGTH) {
            return null;
        }

        try {
            ChanghuiMessageType type = parseMessageType(data);
            if (type != ChanghuiMessageType.HEARTBEAT) {
                log.warn("{} 不是心跳消息: type={}", LOG_PREFIX, type);
                return null;
            }

            String stationCode = parseStationCode(data);
            if (stationCode == null) {
                return null;
            }

            HeartbeatInfo info = new HeartbeatInfo();
            info.setStationCode(stationCode);
            info.setTimestamp(System.currentTimeMillis());

            return info;
        } catch (Exception e) {
            log.error("{} 解析心跳消息失败", LOG_PREFIX, e);
            return null;
        }
    }


    /**
     * 解析升级状态消息
     *
     * @param data 数据
     * @return 升级状态信息，解析失败返回 null
     */
    public UpgradeStatusInfo parseUpgradeStatus(ByteBuf data) {
        if (data == null || data.readableBytes() < MIN_FRAME_LENGTH) {
            return null;
        }

        try {
            ChanghuiMessageType type = parseMessageType(data);
            if (!type.isUpgradeStatus()) {
                log.warn("{} 不是升级状态消息: type={}", LOG_PREFIX, type);
                return null;
            }

            String stationCode = parseStationCode(data);
            if (stationCode == null) {
                return null;
            }

            UpgradeStatusInfo info = new UpgradeStatusInfo();
            info.setStationCode(stationCode);
            info.setStatusType(type);
            info.setTimestamp(System.currentTimeMillis());

            // 解析数据域中的进度信息（如果是进度消息）
            if (type == ChanghuiMessageType.UPGRADE_STATUS_PROGRESS) {
                int dataLength = data.readableBytes() - MIN_FRAME_LENGTH;
                if (dataLength > 0) {
                    // 进度值在数据域的第一个字节
                    int progress = data.getByte(data.readerIndex() + DATA_OFFSET) & 0xFF;
                    info.setProgress(progress);
                }
            }

            return info;
        } catch (Exception e) {
            log.error("{} 解析升级状态消息失败", LOG_PREFIX, e);
            return null;
        }
    }

    /**
     * 解析升级触发响应
     * <p>
     * 根据协议文档，数据域格式为：状态码(1字节) + 协议类型(3字节)
     * 状态码：00=成功，01=失败
     * </p>
     *
     * @param data 数据
     * @return 升级触发响应信息，解析失败返回 null
     */
    public UpgradeTriggerResponse parseUpgradeTriggerResponse(ByteBuf data) {
        if (data == null || data.readableBytes() < MIN_FRAME_LENGTH) {
            return null;
        }

        try {
            ChanghuiMessageType type = parseMessageType(data);
            if (type != ChanghuiMessageType.UPGRADE_TRIGGER) {
                log.warn("{} 不是升级触发响应: type={}", LOG_PREFIX, type);
                return null;
            }

            String stationCode = parseStationCode(data);
            if (stationCode == null) {
                return null;
            }

            UpgradeTriggerResponse response = new UpgradeTriggerResponse();
            response.setStationCode(stationCode);
            response.setTimestamp(System.currentTimeMillis());

            // 解析数据域：状态码(1字节) + 协议类型(3字节)
            // 数据域偏移量 = DATA_OFFSET
            int statusCode = data.getByte(data.readerIndex() + DATA_OFFSET) & 0xFF;
            response.setStatusCode(statusCode);
            response.setSuccess(statusCode == 0x00);

            // 解析协议类型（大端序，3字节）
            int protocolType = ((data.getByte(data.readerIndex() + DATA_OFFSET + 1) & 0xFF) << 16)
                    | ((data.getByte(data.readerIndex() + DATA_OFFSET + 2) & 0xFF) << 8)
                    | (data.getByte(data.readerIndex() + DATA_OFFSET + 3) & 0xFF);
            response.setProtocolType(protocolType);

            log.debug("{} 解析升级触发响应: stationCode={}, success={}, statusCode=0x{}, protocolType=0x{}",
                    LOG_PREFIX, stationCode, response.isSuccess(), 
                    String.format("%02X", statusCode), String.format("%04X", protocolType));

            return response;
        } catch (Exception e) {
            log.error("{} 解析升级触发响应失败", LOG_PREFIX, e);
            return null;
        }
    }

    /**
     * 解析升级URL响应
     * <p>
     * 根据协议文档，数据域格式为：状态码(1字节) + 协议类型(3字节)
     * 状态码：00=接收成功，01=接收失败，02=下载中，03=下载完成
     * </p>
     *
     * @param data 数据
     * @return 升级URL响应信息，解析失败返回 null
     */
    public UpgradeUrlResponse parseUpgradeUrlResponse(ByteBuf data) {
        if (data == null || data.readableBytes() < MIN_FRAME_LENGTH) {
            return null;
        }

        try {
            ChanghuiMessageType type = parseMessageType(data);
            if (type != ChanghuiMessageType.UPGRADE_URL) {
                log.warn("{} 不是升级URL响应: type={}", LOG_PREFIX, type);
                return null;
            }

            String stationCode = parseStationCode(data);
            if (stationCode == null) {
                return null;
            }

            UpgradeUrlResponse response = new UpgradeUrlResponse();
            response.setStationCode(stationCode);
            response.setTimestamp(System.currentTimeMillis());

            // 解析数据域：状态码(1字节) + 协议类型(3字节)
            int statusCode = data.getByte(data.readerIndex() + DATA_OFFSET) & 0xFF;
            response.setStatusCode(statusCode);
            response.setStatusDescription(getUpgradeUrlStatusDescription(statusCode));

            // 解析协议类型（大端序，3字节）
            int protocolType = ((data.getByte(data.readerIndex() + DATA_OFFSET + 1) & 0xFF) << 16)
                    | ((data.getByte(data.readerIndex() + DATA_OFFSET + 2) & 0xFF) << 8)
                    | (data.getByte(data.readerIndex() + DATA_OFFSET + 3) & 0xFF);
            response.setProtocolType(protocolType);

            log.debug("{} 解析升级URL响应: stationCode={}, statusCode=0x{}, status={}, protocolType=0x{}",
                    LOG_PREFIX, stationCode, String.format("%02X", statusCode), 
                    response.getStatusDescription(), String.format("%04X", protocolType));

            return response;
        } catch (Exception e) {
            log.error("{} 解析升级URL响应失败", LOG_PREFIX, e);
            return null;
        }
    }

    /**
     * 获取升级URL状态描述
     *
     * @param statusCode 状态码
     * @return 状态描述
     */
    private String getUpgradeUrlStatusDescription(int statusCode) {
        switch (statusCode) {
            case 0x00:
                return "接收成功";
            case 0x01:
                return "接收失败";
            case 0x02:
                return "下载中";
            case 0x03:
                return "下载完成";
            default:
                return "未知状态: 0x" + String.format("%02X", statusCode);
        }
    }

    /**
     * 解析帧长度
     * <p>
     * 长度字段位于帧头之后，4字节，大端序。
     * 根据协议文档，长度字段为大端序格式（如 0000001C = 28）。
     * </p>
     *
     * @param data 数据
     * @return 帧长度，解析失败返回 -1
     */
    public int parseFrameLength(ByteBuf data) {
        if (data == null || data.readableBytes() < FRAME_HEADER_LENGTH + LENGTH_FIELD_LENGTH) {
            return -1;
        }

        try {
            // 验证帧头
            if (!validateFrameHeader(data)) {
                return -1;
            }

            // 读取长度字段（大端序）
            int lengthOffset = data.readerIndex() + FRAME_HEADER_LENGTH;
            int length = data.getInt(lengthOffset);

            return length;
        } catch (Exception e) {
            log.error("{} 解析帧长度失败", LOG_PREFIX, e);
            return -1;
        }
    }

    /**
     * 解析帧长度（字节数组版本）
     * <p>
     * 根据协议文档，长度字段为大端序格式（如 0000001C = 28）。
     * </p>
     *
     * @param data 数据
     * @return 帧长度，解析失败返回 -1
     */
    public int parseFrameLength(byte[] data) {
        if (data == null || data.length < FRAME_HEADER_LENGTH + LENGTH_FIELD_LENGTH) {
            return -1;
        }

        try {
            // 验证帧头
            if (!validateFrameHeader(data)) {
                return -1;
            }

            // 读取长度字段（大端序）
            int lengthOffset = FRAME_HEADER_LENGTH;
            return ((data[lengthOffset] & 0xFF) << 24)
                    | ((data[lengthOffset + 1] & 0xFF) << 16)
                    | ((data[lengthOffset + 2] & 0xFF) << 8)
                    | (data[lengthOffset + 3] & 0xFF);
        } catch (Exception e) {
            log.error("{} 解析帧长度失败", LOG_PREFIX, e);
            return -1;
        }
    }

    // ==================== 构建方法 ====================

    /**
     * 构建升级触发帧 (AFN=0x02)
     * <p>
     * 发送此帧后，设备将准备接收升级URL。
     * 数据域包含协议标识：07DA（2字节）
     * </p>
     * <p>
     * 注意：根据协议文档，触发升级下行帧**不包含密码和时间标签字段**。
     * 帧格式：帧头(3) + 长度(4) + 测站编码(10) + 68(1) + L(1) + 68(1) + C(1) + AFN(1) + 数据(2) + CS(1) + 16(1) = 26字节
     * L = C(1) + AFN(1) + 数据(2) = 4
     * </p>
     *
     * @param stationCode 测站编码（20字符十六进制字符串）
     * @return 完整的协议帧
     */
    public byte[] buildUpgradeTriggerFrame(String stationCode) {
        // 数据域：07DA（协议标识）
        byte[] data = new byte[2];
        data[0] = (byte) ((PROTOCOL_DETONG_2010 >> 8) & 0xFF);  // 07
        data[1] = (byte) (PROTOCOL_DETONG_2010 & 0xFF);         // DA
        // 使用简化帧格式（不含密码和时间标签）
        return buildSimpleFrame(stationCode, ChanghuiMessageType.UPGRADE_TRIGGER, data, CONTROL_DOWNSTREAM_CMD);
    }

    /**
     * 构建简化帧（不含密码和时间标签）
     * <p>
     * 用于触发升级等不需要密码和时间标签的下行命令。
     * 帧格式：帧头(3) + 长度(4) + 测站编码(10) + 68(1) + L(1) + 68(1) + C(1) + AFN(1) + 数据(n) + CS(1) + 16(1)
     * L = C(1) + AFN(1) + 数据(n) = 2 + n
     * </p>
     *
     * @param stationCode 测站编码（20字符十六进制字符串）
     * @param messageType 消息类型
     * @param data        数据域内容
     * @param controlCode 控制域C
     * @return 完整的协议帧
     */
    private byte[] buildSimpleFrame(String stationCode, ChanghuiMessageType messageType, byte[] data, byte controlCode) {
        if (stationCode == null || stationCode.length() != 20) {
            log.warn("{} 无效的测站编码: {}", LOG_PREFIX, stationCode);
            return null;
        }

        try {
            // 计算数据域长度
            int dataLength = (data != null) ? data.length : 0;

            // 计算总帧长度（简化帧，不含密码和时间标签）
            // 帧头(3) + 长度(4) + 测站编码(10) + 起始(1) + L(1) + 起始(1) + C(1) + AFN(1) + 数据(n) + CS(1) + 结束(1)
            int frameLength = 3 + 4 + 10 + 1 + 1 + 1 + 1 + 1 + dataLength + 1 + 1;

            ByteBuf buffer = Unpooled.buffer(frameLength);

            // 1. 写入帧头 EF7EEF
            buffer.writeBytes(FRAME_HEADER);

            // 2. 写入长度字段（大端序）
            // 注意：根据协议样例，UPGRADE_TRIGGER 的长度包含 CS 和结束符
            // 长度 = 测站编码(10) + 起始(1) + L(1) + 起始(1) + C(1) + AFN(1) + 数据(n) + CS(1) + 结束符(1)
            int contentLength = frameLength - FRAME_HEADER_LENGTH - LENGTH_FIELD_LENGTH; // 不减去 CS 和结束符
            buffer.writeInt(contentLength);

            // 3. 写入测站编码（10字节）
            byte[] stationCodeBytes = hexToBytes(stationCode);
            buffer.writeBytes(stationCodeBytes);

            // 4. 写入起始字符 68
            buffer.writeByte(START_CHAR);

            // 5. 写入 L 字段（简化帧：L = C + AFN + 数据）
            int lValue = 2 + dataLength;  // C(1) + AFN(1) + 数据(n)
            buffer.writeByte(lValue);

            // 6. 写入起始字符 68
            buffer.writeByte(START_CHAR);

            // 7. 写入控制域 C
            buffer.writeByte(controlCode);

            // 8. 写入 AFN
            buffer.writeByte(messageType.getAfn());

            // 9. 写入数据域
            if (data != null && data.length > 0) {
                buffer.writeBytes(data);
            }

            // 10. 计算并写入校验和 CS（从第一个 68 到数据域结束）
            int csStartIndex = FIXED_HEADER_LENGTH; // 从第一个 68 开始
            int csEndIndex = buffer.writerIndex();
            byte cs = calculateChecksum(buffer, csStartIndex, csEndIndex);
            buffer.writeByte(cs);

            // 11. 写入结束字符 16
            buffer.writeByte(END_CHAR);

            // 转换为字节数组
            byte[] result = new byte[buffer.readableBytes()];
            buffer.readBytes(result);
            buffer.release();

            log.debug("{} 构建简化帧成功: type={}, controlCode=0x{}, length={}", 
                    LOG_PREFIX, messageType, String.format("%02X", controlCode), result.length);

            return result;
        } catch (Exception e) {
            log.error("{} 构建简化帧失败: type={}", LOG_PREFIX, messageType, e);
            return null;
        }
    }

    /**
     * 构建升级URL帧 (AFN=0x10)
     * <p>
     * 下发升级文件的URL地址。
     * </p>
     *
     * @param stationCode 测站编码（20字符十六进制字符串）
     * @param url         升级URL
     * @return 完整的协议帧
     */
    public byte[] buildUpgradeUrlFrame(String stationCode, String url) {
        if (url == null || url.isEmpty()) {
            log.warn("{} 升级URL不能为空", LOG_PREFIX);
            return null;
        }

        // 硬件方要求：URL中域名/端口后面需要加反斜杠，如 http://example.com\/path/file.hex
        String formattedUrl = formatUrlForHardware(url);
        log.debug("{} 格式化固件URL: 原始={}, 格式化后={}", LOG_PREFIX, url, formattedUrl);

        byte[] urlBytes = formattedUrl.getBytes(StandardCharsets.UTF_8);
        return buildFrame(stationCode, ChanghuiMessageType.UPGRADE_URL, urlBytes, CONTROL_DOWNSTREAM_URL);
    }

    /**
     * 格式化URL以适配硬件设备要求
     * <p>
     * 硬件方要求：
     * 1. URL路径不能太长，需要将 /admin-api/infra/file/xx/get 替换为 /fireware
     * 2. URL格式：在协议+域名/端口之后、路径之前插入反斜杠
     * 例如：http://59.110.164.67:48080/admin-api/infra/file/29/get/20260113/DT2010.hex
     *   -> http://59.110.164.67:48080\/fireware/20260113/DT2010.hex
     * </p>
     *
     * @param url 原始URL
     * @return 格式化后的URL
     */
    private String formatUrlForHardware(String url) {
        // 找到协议部分结束位置 (如 http:// 或 https://)
        int protocolEnd = url.indexOf("://");
        if (protocolEnd == -1) {
            return url; // 非标准URL，原样返回
        }

        // 找到协议后的第一个斜杠（路径开始位置）
        int pathStart = url.indexOf('/', protocolEnd + 3);
        if (pathStart == -1) {
            return url; // 没有路径部分，原样返回
        }

        // 获取域名/端口部分和路径部分
        String hostPart = url.substring(0, pathStart);
        String pathPart = url.substring(pathStart);

        // 硬件方要求：将 /admin-api/infra/file/xx/get 替换为 /fireware（路径太长设备无法处理）
        // 正则匹配 /admin-api/infra/file/数字/get 并替换为 /fireware
        pathPart = pathPart.replaceFirst("/admin-api/infra/file/\\d+/get", "/fireware");

        // 在路径斜杠前插入反斜杠
        return hostPart + "\\" + pathPart;
    }

    /**
     * 构建心跳响应帧 (AFN=0x3C)
     * <p>
     * 数据域包含协议标识：0000 07DA（4字节）
     * </p>
     *
     * @param stationCode 测站编码（20字符十六进制字符串）
     * @return 完整的协议帧
     */
    public byte[] buildHeartbeatResponseFrame(String stationCode) {
        // 数据域：0000 07DA（前2字节保留，后2字节协议标识）
        byte[] data = new byte[4];
        data[0] = 0x00;
        data[1] = 0x00;
        data[2] = (byte) ((PROTOCOL_DETONG_2010 >> 8) & 0xFF);  // 07
        data[3] = (byte) (PROTOCOL_DETONG_2010 & 0xFF);         // DA
        // 使用心跳响应专用控制域 0x8E（与协议文档一致）
        return buildFrame(stationCode, ChanghuiMessageType.HEARTBEAT, data, CONTROL_DOWNSTREAM_HEARTBEAT_RESP);
    }

    /**
     * 构建升级状态响应帧
     * <p>
     * 服务器收到设备升级状态后的响应。
     * 数据域包含协议标识：0000 07DA（4字节）
     * </p>
     *
     * @param stationCode 测站编码（20字符十六进制字符串）
     * @param statusType  状态类型
     * @return 完整的协议帧
     */
    public byte[] buildUpgradeStatusResponseFrame(String stationCode, ChanghuiMessageType statusType) {
        // 数据域：0000 07DA（前2字节保留，后2字节协议标识）
        byte[] data = new byte[4];
        data[0] = 0x00;
        data[1] = 0x00;
        data[2] = (byte) ((PROTOCOL_DETONG_2010 >> 8) & 0xFF);  // 07
        data[3] = (byte) (PROTOCOL_DETONG_2010 & 0xFF);         // DA
        return buildFrame(stationCode, statusType, data, CONTROL_DOWNSTREAM_STATUS_RESP);
    }


    /**
     * 构建协议帧（使用默认控制域C）
     *
     * @param stationCode 测站编码（20字符十六进制字符串）
     * @param messageType 消息类型
     * @param data        数据域内容（可为 null）
     * @return 完整的协议帧
     */
    public byte[] buildFrame(String stationCode, ChanghuiMessageType messageType, byte[] data) {
        return buildFrame(stationCode, messageType, data, CONTROL_DOWNSTREAM_CMD);
    }

    /**
     * 构建协议帧
     * <p>
     * 通用的帧构建方法，根据消息类型和数据构建完整的协议帧。
     * </p>
     *
     * @param stationCode 测站编码（20字符十六进制字符串）
     * @param messageType 消息类型
     * @param data        数据域内容（可为 null）
     * @param controlCode 控制域C
     * @return 完整的协议帧
     */
    public byte[] buildFrame(String stationCode, ChanghuiMessageType messageType, byte[] data, byte controlCode) {
        if (stationCode == null || stationCode.length() != 20) {
            log.warn("{} 无效的测站编码: {}", LOG_PREFIX, stationCode);
            return null;
        }

        try {
            // 计算数据域长度
            int dataLength = (data != null) ? data.length : 0;

            // 计算总帧长度
            // 帧头(3) + 长度(4) + 测站编码(10) + 起始(1) + L(1) + 起始(1) + C(1) + AFN(1) + 数据(n) + 密码(2) + 时间(5) + CS(1) + 结束(1)
            int frameLength = MIN_FRAME_LENGTH + dataLength;

            ByteBuf buffer = Unpooled.buffer(frameLength);

            // 1. 写入帧头 EF7EEF
            buffer.writeBytes(FRAME_HEADER);

            // 2. 写入长度字段（大端序）
            // 注意：根据协议样例，UPGRADE_URL 等带密码/时间的帧，长度不包含 CS 和结束符
            // 长度 = 测站编码(10) + 起始(1) + L(1) + 起始(1) + L内容
            int contentLength = frameLength - FRAME_HEADER_LENGTH - LENGTH_FIELD_LENGTH - 2; // 减去 CS(1) + 结束符(1)
            buffer.writeInt(contentLength);

            // 3. 写入测站编码（10字节）
            byte[] stationCodeBytes = hexToBytes(stationCode);
            buffer.writeBytes(stationCodeBytes);

            // 4. 写入起始字符 68
            buffer.writeByte(START_CHAR);

            // 5. 写入 L 字段（数据域长度 + 固定字段长度）
            // L = C(1) + AFN(1) + 数据(n) + 密码(2) + 时间(5) = 9 + dataLength
            int lValue = 9 + dataLength;
            buffer.writeByte(lValue);

            // 6. 写入起始字符 68
            buffer.writeByte(START_CHAR);

            // 7. 写入控制域 C
            buffer.writeByte(controlCode);

            // 8. 写入 AFN
            buffer.writeByte(messageType.getAfn());

            // 9. 写入数据域
            if (data != null && data.length > 0) {
                buffer.writeBytes(data);
            }

            // 10. 写入密码（默认 0x0000）
            buffer.writeShort(0x0000);

            // 11. 写入时间标签（5字节，当前时间）
            byte[] timeTag = buildTimeTag();
            buffer.writeBytes(timeTag);

            // 12. 计算并写入校验和 CS
            // CS = 从起始字符68到时间标签的所有字节之和的低8位
            int csStartIndex = FIXED_HEADER_LENGTH; // 从第一个 68 开始
            int csEndIndex = buffer.writerIndex();
            byte cs = calculateChecksum(buffer, csStartIndex, csEndIndex);
            buffer.writeByte(cs);

            // 13. 写入结束字符 16
            buffer.writeByte(END_CHAR);

            // 转换为字节数组
            byte[] result = new byte[buffer.readableBytes()];
            buffer.readBytes(result);
            buffer.release();

            log.debug("{} 构建帧成功: type={}, controlCode=0x{}, length={}", 
                    LOG_PREFIX, messageType, String.format("%02X", controlCode), result.length);

            return result;
        } catch (Exception e) {
            log.error("{} 构建帧失败: type={}", LOG_PREFIX, messageType, e);
            return null;
        }
    }

    /**
     * 验证帧校验和
     *
     * @param data 完整的帧数据
     * @return 校验和是否正确
     */
    public boolean validateChecksum(byte[] data) {
        if (data == null || data.length < MIN_FRAME_LENGTH) {
            return false;
        }

        try {
            // CS 位于结束字符之前
            int csIndex = data.length - 2;
            byte expectedCs = data[csIndex];

            // 计算校验和（从第一个 68 到时间标签结束）
            int csStartIndex = FIXED_HEADER_LENGTH;
            int csEndIndex = csIndex;

            int sum = 0;
            for (int i = csStartIndex; i < csEndIndex; i++) {
                sum += (data[i] & 0xFF);
            }
            byte calculatedCs = (byte) (sum & 0xFF);

            return expectedCs == calculatedCs;
        } catch (Exception e) {
            log.error("{} 验证校验和失败", LOG_PREFIX, e);
            return false;
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 计算校验和
     *
     * @param buffer     数据缓冲区
     * @param startIndex 起始索引
     * @param endIndex   结束索引
     * @return 校验和
     */
    private byte calculateChecksum(ByteBuf buffer, int startIndex, int endIndex) {
        int sum = 0;
        for (int i = startIndex; i < endIndex; i++) {
            sum += (buffer.getByte(i) & 0xFF);
        }
        return (byte) (sum & 0xFF);
    }

    /**
     * 构建时间标签（5字节）
     * <p>
     * 格式：秒(1) + 分(1) + 时(1) + 日(1) + 月年(1)
     * </p>
     *
     * @return 时间标签
     */
    private byte[] buildTimeTag() {
        return encodeTimestamp(LocalDateTime.now());
    }

    // ==================== 时间戳编解码方法 ====================

    /**
     * 编码时间戳为 5 字节时间标签
     * <p>
     * 格式：秒(1) + 分(1) + 时(1) + 日(1) + 月年(1)
     * </p>
     * <p>
     * 月年字节编码规则：
     * <ul>
     *     <li>高 4 位：月份（1-12）</li>
     *     <li>低 4 位：年份后两位对 16 取模（0-15）</li>
     * </ul>
     * </p>
     *
     * @param dateTime 日期时间
     * @return 5 字节时间标签，如果输入为 null 则返回 null
     * @see #decodeTimestamp(byte[])
     */
    public byte[] encodeTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            log.warn("{} 编码时间戳失败：日期时间为空", LOG_PREFIX);
            return null;
        }

        byte[] timeTag = new byte[TIME_TAG_LENGTH];

        timeTag[0] = (byte) dateTime.getSecond();
        timeTag[1] = (byte) dateTime.getMinute();
        timeTag[2] = (byte) dateTime.getHour();
        timeTag[3] = (byte) dateTime.getDayOfMonth();
        // 月年：高4位为月，低4位为年的后两位对16取模
        timeTag[4] = (byte) ((dateTime.getMonthValue() << 4) | (dateTime.getYear() % 100 % 16));

        log.debug("{} 编码时间戳成功: {} -> {}", LOG_PREFIX, dateTime, bytesToHex(timeTag));
        return timeTag;
    }

    /**
     * 解码 5 字节时间标签为日期时间
     * <p>
     * 格式：秒(1) + 分(1) + 时(1) + 日(1) + 月年(1)
     * </p>
     * <p>
     * 注意：由于协议限制，年份只保留后两位对 16 取模的值（0-15），
     * 因此解码时会丢失完整的年份信息。解码结果的年份基于当前世纪推算。
     * </p>
     *
     * @param timeTag 5 字节时间标签
     * @return 日期时间，��果输入无效则返回 null
     * @see #encodeTimestamp(LocalDateTime)
     */
    public LocalDateTime decodeTimestamp(byte[] timeTag) {
        if (timeTag == null || timeTag.length != TIME_TAG_LENGTH) {
            log.warn("{} 解码时间戳失败：时间标签长度无效，期望 {} 字节，实际 {} 字节",
                    LOG_PREFIX, TIME_TAG_LENGTH, timeTag != null ? timeTag.length : 0);
            return null;
        }

        try {
            int second = timeTag[0] & 0xFF;
            int minute = timeTag[1] & 0xFF;
            int hour = timeTag[2] & 0xFF;
            int day = timeTag[3] & 0xFF;
            int monthYear = timeTag[4] & 0xFF;

            int month = (monthYear >> 4) & 0x0F;
            int yearMod16 = monthYear & 0x0F;

            // 验证时间字段范围
            if (second > 59 || minute > 59 || hour > 23 || day < 1 || day > 31 || month < 1 || month > 12) {
                log.warn("{} 解码时间戳失败：时间字段超出有效范围 - 秒={}, 分={}, 时={}, 日={}, 月={}",
                        LOG_PREFIX, second, minute, hour, day, month);
                return null;
            }

            // 推算年份：基于当前世纪，找到与 yearMod16 匹配的最近年份
            int currentYear = LocalDateTime.now().getYear();
            int century = (currentYear / 100) * 100;
            int baseYear = century + (yearMod16 % 100);
            
            // 如果推算的年份比当前年份大太多，可能是上个世纪
            if (baseYear > currentYear + 10) {
                baseYear -= 100;
            }

            LocalDateTime result = LocalDateTime.of(baseYear, month, day, hour, minute, second);
            log.debug("{} 解码时间戳成功: {} -> {}", LOG_PREFIX, bytesToHex(timeTag), result);
            return result;
        } catch (Exception e) {
            log.error("{} 解码时间戳异常", LOG_PREFIX, e);
            return null;
        }
    }

    /**
     * 从协议帧中提取时间标签
     * <p>
     * 时间标签位于校验和之前，共 5 字节。
     * </p>
     *
     * @param data 完整帧数据
     * @return 时间标签（5 字节），如果提取失败则返回 null
     */
    public byte[] extractTimeTag(byte[] data) {
        if (data == null || data.length < MIN_FRAME_LENGTH) {
            log.warn("{} 提取时间标签失败：数据长度不足", LOG_PREFIX);
            return null;
        }

        try {
            // 时间标签位于：结束字符(1) + 校验和(1) + 时间标签(5) = 倒数第 7 字节开始
            int timeTagOffset = data.length - 1 - CHECKSUM_LENGTH - TIME_TAG_LENGTH;
            byte[] timeTag = new byte[TIME_TAG_LENGTH];
            System.arraycopy(data, timeTagOffset, timeTag, 0, TIME_TAG_LENGTH);
            return timeTag;
        } catch (Exception e) {
            log.error("{} 提取时间标签异常", LOG_PREFIX, e);
            return null;
        }
    }

    /**
     * 从协议帧中解析时间戳
     *
     * @param data 完整帧数据
     * @return 日期时间，如果解析失败则返回 null
     */
    public LocalDateTime parseTimestamp(byte[] data) {
        byte[] timeTag = extractTimeTag(data);
        if (timeTag == null) {
            return null;
        }
        return decodeTimestamp(timeTag);
    }

    /**
     * 字节数组转十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    public String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    /**
     * 十六进制字符串转字节数组
     *
     * @param hex 十六进制字符串
     * @return 字节数组
     */
    public byte[] hexToBytes(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            return null;
        }
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    // ==================== TEA 加密解密方法 ====================

    /**
     * TEA 加密
     * <p>
     * 使用 TEA (Tiny Encryption Algorithm) 对数据进行加密。
     * TEA 是一种简单高效的分组加密算法，使用 128 位密钥对 64 位数据块进行加密。
     * </p>
     *
     * @param data 明文数据
     * @param key  TEA 密钥（4 个 32 位整数）
     * @return 密文数据，如果输入无效则返回 null
     * @see TeaCrypto#encrypt(byte[], int[])
     */
    public byte[] teaEncrypt(byte[] data, int[] key) {
        if (data == null) {
            log.warn("{} TEA 加密失败：数据为空", LOG_PREFIX);
            return null;
        }
        if (key == null || key.length != 4) {
            log.warn("{} TEA 加密失败：密钥无效，密钥必须是 4 个 32 位整数", LOG_PREFIX);
            return null;
        }

        try {
            byte[] encrypted = TeaCrypto.encrypt(data, key);
            log.debug("{} TEA 加密成功：明文长度={}, 密文长度={}", 
                    LOG_PREFIX, data.length, encrypted.length);
            return encrypted;
        } catch (Exception e) {
            log.error("{} TEA 加密异常", LOG_PREFIX, e);
            return null;
        }
    }

    /**
     * TEA 解密
     * <p>
     * 使用 TEA (Tiny Encryption Algorithm) 对数据进行解密。
     * </p>
     *
     * @param data 密文数据
     * @param key  TEA 密钥（4 个 32 位整数）
     * @return 明文数据，如果输入无效则返回 null
     * @see TeaCrypto#decrypt(byte[], int[])
     */
    public byte[] teaDecrypt(byte[] data, int[] key) {
        if (data == null) {
            log.warn("{} TEA 解密失败：数据为空", LOG_PREFIX);
            return null;
        }
        if (key == null || key.length != 4) {
            log.warn("{} TEA 解密失败：密钥无效，密钥必须是 4 个 32 位整数", LOG_PREFIX);
            return null;
        }
        if (data.length % 8 != 0) {
            log.warn("{} TEA 解密失败：密文长度必须是 8 的倍数，当前长度={}", LOG_PREFIX, data.length);
            return null;
        }

        try {
            byte[] decrypted = TeaCrypto.decrypt(data, key);
            log.debug("{} TEA 解密成功：密文长度={}, 明文长度={}", 
                    LOG_PREFIX, data.length, decrypted.length);
            return decrypted;
        } catch (Exception e) {
            log.error("{} TEA 解密异常", LOG_PREFIX, e);
            return null;
        }
    }

    /**
     * 解析 TEA 密钥字符串
     * <p>
     * 将十六进制字符串格式的密钥转换为 4 个 32 位整数数组。
     * 密钥字符串应为 32 个十六进制字符（128 位）。
     * </p>
     *
     * @param keyHex 十六进制密钥字符串（32 字符）
     * @return TEA 密钥数组（4 个 32 位整数），如果输入无效则返回 null
     */
    public int[] parseTeaKey(String keyHex) {
        if (keyHex == null || keyHex.length() != 32) {
            log.warn("{} 解析 TEA 密钥失败：密钥字符串必须是 32 个十六进制字符，当前长度={}", 
                    LOG_PREFIX, keyHex != null ? keyHex.length() : 0);
            return null;
        }

        try {
            int[] key = new int[4];
            for (int i = 0; i < 4; i++) {
                String part = keyHex.substring(i * 8, (i + 1) * 8);
                key[i] = (int) Long.parseLong(part, 16);
            }
            log.debug("{} 解析 TEA 密钥成功", LOG_PREFIX);
            return key;
        } catch (NumberFormatException e) {
            log.error("{} 解析 TEA 密钥失败：无效的十六进制字符串", LOG_PREFIX, e);
            return null;
        }
    }

    /**
     * 将 TEA 密钥数组转换为十六进制字符串
     *
     * @param key TEA 密钥数组（4 个 32 位整数）
     * @return 十六进制密钥字符串（32 字符），如果输入无效则返回 null
     */
    public String teaKeyToHex(int[] key) {
        if (key == null || key.length != 4) {
            log.warn("{} 转换 TEA 密钥失败：密钥必须是 4 个 32 位整数", LOG_PREFIX);
            return null;
        }

        StringBuilder sb = new StringBuilder(32);
        for (int k : key) {
            sb.append(String.format("%08X", k));
        }
        return sb.toString();
    }


    // ==================== 内部数据类 ====================

    /**
     * 心跳信息
     */
    @Data
    public static class HeartbeatInfo {
        /**
         * 测站编码
         */
        private String stationCode;

        /**
         * 时间戳
         */
        private long timestamp;
    }

    /**
     * 升级状态信息
     */
    @Data
    public static class UpgradeStatusInfo {
        /**
         * 测站编码
         */
        private String stationCode;

        /**
         * 状态类型
         */
        private ChanghuiMessageType statusType;

        /**
         * 升级进度（0-100）
         */
        private Integer progress;

        /**
         * 时间戳
         */
        private long timestamp;

        /**
         * 错误信息（升级失败时）
         */
        private String errorMessage;
    }

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
         * 测站编码
         */
        private String stationCode;

        /**
         * 消息类型
         */
        private ChanghuiMessageType messageType;

        /**
         * 数据域内容
         */
        private byte[] data;

        /**
         * 错误信息
         */
        private String errorMessage;

        /**
         * 创建成功结果
         */
        public static ParseResult success(String stationCode, ChanghuiMessageType messageType, byte[] data) {
            ParseResult result = new ParseResult();
            result.setSuccess(true);
            result.setStationCode(stationCode);
            result.setMessageType(messageType);
            result.setData(data);
            return result;
        }

        /**
         * 创建失败结果
         */
        public static ParseResult failure(String errorMessage) {
            ParseResult result = new ParseResult();
            result.setSuccess(false);
            result.setErrorMessage(errorMessage);
            return result;
        }
    }

    /**
     * 完整解析帧
     *
     * @param data 帧数据
     * @return 解析结果
     */
    public ParseResult parseFrame(byte[] data) {
        if (data == null || data.length < MIN_FRAME_LENGTH) {
            return ParseResult.failure("数据长度不足");
        }

        // 验证帧头
        if (!validateFrameHeader(data)) {
            return ParseResult.failure("无效的帧头");
        }

        // 验证结束字符
        if (data[data.length - 1] != END_CHAR) {
            return ParseResult.failure("无效的结束字符");
        }

        // 验证校验和
        if (!validateChecksum(data)) {
            return ParseResult.failure("校验和错误");
        }

        // 解析测站编码
        String stationCode = parseStationCode(data);
        if (stationCode == null) {
            return ParseResult.failure("解析测站编码失败");
        }

        // 解析消息类型
        ChanghuiMessageType messageType = parseMessageType(data);
        if (messageType == null) {
            return ParseResult.failure("解析消息类型失败");
        }

        // 提取数据域
        byte[] frameData = extractData(data);

        return ParseResult.success(stationCode, messageType, frameData);
    }

    /**
     * 提取数据域
     *
     * @param data 完整帧数据
     * @return 数据域内容
     */
    private byte[] extractData(byte[] data) {
        if (data == null || data.length <= MIN_FRAME_LENGTH) {
            return new byte[0];
        }

        // 数据域长度 = 总长度 - 最小帧长度
        int dataLength = data.length - MIN_FRAME_LENGTH;
        if (dataLength <= 0) {
            return new byte[0];
        }

        byte[] result = new byte[dataLength];
        System.arraycopy(data, DATA_OFFSET, result, 0, dataLength);
        return result;
    }

    /**
     * 升级触发响应信息
     * <p>
     * 设备收到升级触发命令后的响应。
     * 状态码：00=成功，01=失败
     * </p>
     */
    @Data
    public static class UpgradeTriggerResponse {
        /**
         * 测站编码
         */
        private String stationCode;

        /**
         * 状态码（00=成功，01=失败）
         */
        private int statusCode;

        /**
         * 是否成功
         */
        private boolean success;

        /**
         * 协议类型（如 0x07DA = 2010 德通协议）
         */
        private int protocolType;

        /**
         * 时间戳
         */
        private long timestamp;
    }

    /**
     * 升级URL响应信息
     * <p>
     * 设备收到升级URL后的响应。
     * 状态码：00=接收成功，01=接收失败，02=下载中，03=下载完成
     * </p>
     */
    @Data
    public static class UpgradeUrlResponse {
        /**
         * 测站编码
         */
        private String stationCode;

        /**
         * 状态码
         * <ul>
         *     <li>00 - 接收成功</li>
         *     <li>01 - 接收失败</li>
         *     <li>02 - 下载中</li>
         *     <li>03 - 下载完成</li>
         * </ul>
         */
        private int statusCode;

        /**
         * 状态描述
         */
        private String statusDescription;

        /**
         * 协议类型（如 0x07DA = 2010 德通协议）
         */
        private int protocolType;

        /**
         * 时间戳
         */
        private long timestamp;

        /**
         * 是否接收成功
         */
        public boolean isReceiveSuccess() {
            return statusCode == 0x00;
        }

        /**
         * 是否下载中
         */
        public boolean isDownloading() {
            return statusCode == 0x02;
        }

        /**
         * 是否下载完成
         */
        public boolean isDownloadComplete() {
            return statusCode == 0x03;
        }

        /**
         * 是否接收失败
         */
        public boolean isReceiveFailed() {
            return statusCode == 0x01;
        }
    }
}
