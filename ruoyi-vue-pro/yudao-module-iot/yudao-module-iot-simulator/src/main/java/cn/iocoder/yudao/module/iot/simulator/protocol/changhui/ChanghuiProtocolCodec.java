package cn.iocoder.yudao.module.iot.simulator.protocol.changhui;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * 长辉协议编解码器
 * 
 * <p>实现长辉自研TCP协议的编解码功能</p>
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
 * @author IoT Simulator Team
 */
@Slf4j
public class ChanghuiProtocolCodec {

    private static final String LOG_PREFIX = "[ChanghuiCodec]";

    // ==================== 协议常量 ====================

    public static final byte[] FRAME_HEADER = {(byte) 0xEF, 0x7E, (byte) 0xEF};
    public static final int FRAME_HEADER_LENGTH = 3;
    public static final int LENGTH_FIELD_LENGTH = 4;
    public static final int STATION_CODE_LENGTH = 10;
    public static final byte START_CHAR = 0x68;
    public static final byte END_CHAR = 0x16;
    public static final int PASSWORD_LENGTH = 2;
    public static final int TIME_TAG_LENGTH = 5;
    public static final int CHECKSUM_LENGTH = 1;
    public static final int MIN_FRAME_LENGTH = 31;
    public static final int FIXED_HEADER_LENGTH = FRAME_HEADER_LENGTH + LENGTH_FIELD_LENGTH + STATION_CODE_LENGTH;
    public static final int AFN_OFFSET = 21;
    public static final int DATA_OFFSET = 22;

    // 控制域C常量
    public static final byte CONTROL_UPSTREAM_HEARTBEAT = (byte) 0x8E;    // 上行心跳
    public static final byte CONTROL_UPSTREAM_STATUS = (byte) 0xC0;       // 上行升级状态
    public static final byte CONTROL_UPSTREAM = (byte) 0x8E;              // 上行（兼容，默认心跳）
    public static final byte CONTROL_DOWNSTREAM = (byte) 0x0E;            // 下行（服务器->设备）

    // 协议标识常量（数据域）
    public static final short PROTOCOL_DETONG_2010 = 0x07DA;    // 德通协议2010
    public static final short PROTOCOL_NINGXIA = 0x4010;        // 宁夏协议
    public static final short PROTOCOL_651 = 0x0651;            // 651协议
    public static final short PROTOCOL_427 = 0x0427;            // 427协议

    // ==================== 解析方法 ====================

    /**
     * 验证帧头
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
     */
    public String parseStationCode(byte[] data) {
        if (data == null || data.length < FIXED_HEADER_LENGTH) {
            return null;
        }
        if (!validateFrameHeader(data)) {
            return null;
        }

        int offset = FRAME_HEADER_LENGTH + LENGTH_FIELD_LENGTH;
        byte[] stationCodeBytes = new byte[STATION_CODE_LENGTH];
        System.arraycopy(data, offset, stationCodeBytes, 0, STATION_CODE_LENGTH);
        return bytesToHex(stationCodeBytes);
    }

    /**
     * 解析消息类型
     */
    public ChanghuiMessageType parseMessageType(byte[] data) {
        if (data == null || data.length < AFN_OFFSET + 1) {
            return null;
        }
        if (!validateFrameHeader(data)) {
            return null;
        }
        return ChanghuiMessageType.fromAfn(data[AFN_OFFSET]);
    }

    /**
     * 解析帧长度
     */
    public int parseFrameLength(byte[] data) {
        if (data == null || data.length < FRAME_HEADER_LENGTH + LENGTH_FIELD_LENGTH) {
            return -1;
        }
        if (!validateFrameHeader(data)) {
            return -1;
        }
        int offset = FRAME_HEADER_LENGTH;
        return (data[offset] & 0xFF)
                | ((data[offset + 1] & 0xFF) << 8)
                | ((data[offset + 2] & 0xFF) << 16)
                | ((data[offset + 3] & 0xFF) << 24);
    }

    /**
     * 提取数据域
     */
    public byte[] extractData(byte[] data) {
        if (data == null || data.length <= MIN_FRAME_LENGTH) {
            return new byte[0];
        }
        int dataLength = data.length - MIN_FRAME_LENGTH;
        byte[] result = new byte[dataLength];
        System.arraycopy(data, DATA_OFFSET, result, 0, dataLength);
        return result;
    }

    /**
     * 解析URL（从升级URL帧中）
     */
    public String parseUrl(byte[] data) {
        byte[] urlBytes = extractData(data);
        if (urlBytes.length == 0) {
            return null;
        }
        return new String(urlBytes, StandardCharsets.UTF_8);
    }

    // ==================== 构建方法 ====================

    /**
     * 构建心跳帧
     * 心跳帧数据域包含协议标识（4字节）：前2字节保留(0x0000)，后2字节为协议类型(07DA=德通2010)
     */
    public byte[] buildHeartbeatFrame(String stationCode) {
        // 数据域：0000 07DA（前2字节保留，后2字节协议标识）
        byte[] data = new byte[4];
        data[0] = 0x00;
        data[1] = 0x00;
        data[2] = (byte) ((PROTOCOL_DETONG_2010 >> 8) & 0xFF);  // 07
        data[3] = (byte) (PROTOCOL_DETONG_2010 & 0xFF);         // DA
        return buildFrame(stationCode, ChanghuiMessageType.HEARTBEAT, data);
    }

    /**
     * 构建升级开始状态帧 (AFN=0x15)
     * 控制域C: 0xC0，数据域: 0000 07DA
     */
    public byte[] buildUpgradeStartFrame(String stationCode) {
        byte[] data = buildProtocolIdentifierData();
        return buildFrame(stationCode, ChanghuiMessageType.UPGRADE_STATUS_START, data, CONTROL_UPSTREAM_STATUS);
    }

    /**
     * 构建升级进度帧 (AFN=0x66)
     * 控制域C: 0xC0，数据域: 进度(1字节) + 0000 07DA(4字节)
     *
     * @param stationCode 测站编码
     * @param progress    进度（0-100）
     */
    public byte[] buildUpgradeProgressFrame(String stationCode, int progress) {
        // 数据域：进度(1字节) + 协议标识(4字节)
        byte[] data = new byte[5];
        data[0] = (byte) Math.min(100, Math.max(0, progress));
        data[1] = 0x00;
        data[2] = 0x00;
        data[3] = (byte) ((PROTOCOL_DETONG_2010 >> 8) & 0xFF);  // 07
        data[4] = (byte) (PROTOCOL_DETONG_2010 & 0xFF);         // DA
        return buildFrame(stationCode, ChanghuiMessageType.UPGRADE_STATUS_PROGRESS, data, CONTROL_UPSTREAM_STATUS);
    }

    /**
     * 构建升级完成帧 (AFN=0x67)
     * 控制域C: 0xC0，数据域: 0000 07DA
     */
    public byte[] buildUpgradeCompleteFrame(String stationCode) {
        byte[] data = buildProtocolIdentifierData();
        return buildFrame(stationCode, ChanghuiMessageType.UPGRADE_STATUS_COMPLETE, data, CONTROL_UPSTREAM_STATUS);
    }

    /**
     * 构建升级失败帧 (AFN=0x68)
     * 控制域C: 0xC0，数据域: 0000 07DA + 错误信息
     *
     * @param stationCode  测站编码
     * @param errorMessage 错误信息
     */
    public byte[] buildUpgradeFailedFrame(String stationCode, String errorMessage) {
        byte[] protocolId = buildProtocolIdentifierData();
        byte[] errorBytes = errorMessage != null ? errorMessage.getBytes(StandardCharsets.UTF_8) : new byte[0];
        byte[] data = new byte[protocolId.length + errorBytes.length];
        System.arraycopy(protocolId, 0, data, 0, protocolId.length);
        System.arraycopy(errorBytes, 0, data, protocolId.length, errorBytes.length);
        return buildFrame(stationCode, ChanghuiMessageType.UPGRADE_STATUS_FAILED, data, CONTROL_UPSTREAM_STATUS);
    }

    /**
     * 构建协议标识数据域（4字节）
     * 格式：0000 07DA
     */
    private byte[] buildProtocolIdentifierData() {
        byte[] data = new byte[4];
        data[0] = 0x00;
        data[1] = 0x00;
        data[2] = (byte) ((PROTOCOL_DETONG_2010 >> 8) & 0xFF);  // 07
        data[3] = (byte) (PROTOCOL_DETONG_2010 & 0xFF);         // DA
        return data;
    }

    /**
     * 构建水位数据帧
     *
     * @param stationCode 测站编码
     * @param waterLevel  水位值（米）
     */
    public byte[] buildWaterLevelFrame(String stationCode, float waterLevel) {
        ByteBuf buf = Unpooled.buffer(4);
        buf.writeFloatLE(waterLevel);
        byte[] data = new byte[4];
        buf.readBytes(data);
        buf.release();
        return buildFrame(stationCode, ChanghuiMessageType.DATA_WATER_LEVEL, data);
    }

    /**
     * 构建协议帧（使用默认控制域C）
     */
    public byte[] buildFrame(String stationCode, ChanghuiMessageType messageType, byte[] data) {
        return buildFrame(stationCode, messageType, data, CONTROL_UPSTREAM_HEARTBEAT);
    }

    /**
     * 构建协议帧
     *
     * @param stationCode 测站编码
     * @param messageType 消息类型
     * @param data        数据域
     * @param controlCode 控制域C
     * @return 完整协议帧
     */
    public byte[] buildFrame(String stationCode, ChanghuiMessageType messageType, byte[] data, byte controlCode) {
        if (stationCode == null || stationCode.length() != 20) {
            log.warn("{} 无效的测站编码: {}", LOG_PREFIX, stationCode);
            return null;
        }

        try {
            int dataLength = (data != null) ? data.length : 0;
            int frameLength = MIN_FRAME_LENGTH + dataLength;

            ByteBuf buffer = Unpooled.buffer(frameLength);

            // 1. 帧头 EF7EEF
            buffer.writeBytes(FRAME_HEADER);

            // 2. 长度字段（大端序，与协议文档和服务端保持一致）
            int contentLength = frameLength - FRAME_HEADER_LENGTH - LENGTH_FIELD_LENGTH;
            buffer.writeInt(contentLength);

            // 3. 测站编码
            buffer.writeBytes(hexToBytes(stationCode));

            // 4. 起始字符 68
            buffer.writeByte(START_CHAR);

            // 5. L字段
            int lValue = 9 + dataLength;
            buffer.writeByte(lValue);

            // 6. 起始字符 68
            buffer.writeByte(START_CHAR);

            // 7. 控制域 C
            buffer.writeByte(controlCode);

            // 8. AFN
            buffer.writeByte(messageType.getAfn());

            // 9. 数据域
            if (data != null && data.length > 0) {
                buffer.writeBytes(data);
            }

            // 10. 密码
            buffer.writeShort(0x0000);

            // 11. 时间标签
            buffer.writeBytes(buildTimeTag());

            // 12. 校验和
            int csStartIndex = FIXED_HEADER_LENGTH;
            int csEndIndex = buffer.writerIndex();
            byte cs = calculateChecksum(buffer, csStartIndex, csEndIndex);
            buffer.writeByte(cs);

            // 13. 结束字符
            buffer.writeByte(END_CHAR);

            byte[] result = new byte[buffer.readableBytes()];
            buffer.readBytes(result);
            buffer.release();

            log.debug("{} 构建帧: type={}, controlCode=0x{}, length={}", 
                    LOG_PREFIX, messageType, String.format("%02X", controlCode), result.length);
            return result;
        } catch (Exception e) {
            log.error("{} 构建帧失败: type={}", LOG_PREFIX, messageType, e);
            return null;
        }
    }

    /**
     * 验证校验和
     */
    public boolean validateChecksum(byte[] data) {
        if (data == null || data.length < MIN_FRAME_LENGTH) {
            return false;
        }
        int csIndex = data.length - 2;
        byte expectedCs = data[csIndex];

        int sum = 0;
        for (int i = FIXED_HEADER_LENGTH; i < csIndex; i++) {
            sum += (data[i] & 0xFF);
        }
        return expectedCs == (byte) (sum & 0xFF);
    }

    // ==================== 辅助方法 ====================

    private byte calculateChecksum(ByteBuf buffer, int startIndex, int endIndex) {
        int sum = 0;
        for (int i = startIndex; i < endIndex; i++) {
            sum += (buffer.getByte(i) & 0xFF);
        }
        return (byte) (sum & 0xFF);
    }

    private byte[] buildTimeTag() {
        LocalDateTime now = LocalDateTime.now();
        byte[] timeTag = new byte[TIME_TAG_LENGTH];
        timeTag[0] = (byte) now.getSecond();
        timeTag[1] = (byte) now.getMinute();
        timeTag[2] = (byte) now.getHour();
        timeTag[3] = (byte) now.getDayOfMonth();
        timeTag[4] = (byte) ((now.getMonthValue() << 4) | (now.getYear() % 100 % 16));
        return timeTag;
    }

    public String bytesToHex(byte[] bytes) {
        if (bytes == null) return null;
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public byte[] hexToBytes(String hex) {
        if (hex == null || hex.length() % 2 != 0) return null;
        byte[] data = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    // ==================== 数据类 ====================

    @Data
    public static class ParseResult {
        private boolean success;
        private String stationCode;
        private ChanghuiMessageType messageType;
        private byte[] data;
        private String errorMessage;

        public static ParseResult success(String stationCode, ChanghuiMessageType messageType, byte[] data) {
            ParseResult result = new ParseResult();
            result.success = true;
            result.stationCode = stationCode;
            result.messageType = messageType;
            result.data = data;
            return result;
        }

        public static ParseResult failure(String errorMessage) {
            ParseResult result = new ParseResult();
            result.success = false;
            result.errorMessage = errorMessage;
            return result;
        }
    }

    /**
     * 完整解析帧
     */
    public ParseResult parseFrame(byte[] data) {
        if (data == null || data.length < MIN_FRAME_LENGTH) {
            return ParseResult.failure("数据长度不足");
        }
        if (!validateFrameHeader(data)) {
            return ParseResult.failure("无效的帧头");
        }
        if (data[data.length - 1] != END_CHAR) {
            return ParseResult.failure("无效的结束字符");
        }
        if (!validateChecksum(data)) {
            return ParseResult.failure("校验和错误");
        }

        String stationCode = parseStationCode(data);
        ChanghuiMessageType messageType = parseMessageType(data);
        byte[] frameData = extractData(data);

        return ParseResult.success(stationCode, messageType, frameData);
    }
}

