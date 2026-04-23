package cn.iocoder.yudao.module.iot.newgateway.plugins.changhui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 长辉协议消息模型
 * 
 * <p>封装长辉TCP协议的消息结构，包含测站编码、AFN、数据域、密码和时间戳等字段。</p>
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
 * @author IoT Gateway Team
 * @see ChanghuiProtocolCodec
 * @see ChanghuiMessageType
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChanghuiMessage {

    /**
     * 测站编码（20字符十六进制字符串，对应10字节）
     * <p>
     * 测站编码是设备的唯一标识符，由以下部分组成：
     * <ul>
     *     <li>省编码</li>
     *     <li>管理单位编码</li>
     *     <li>测站编码部分</li>
     *     <li>桩号前</li>
     *     <li>桩号后</li>
     * </ul>
     * </p>
     */
    private String stationCode;

    /**
     * 应用功能码（AFN）
     * <p>
     * 用于标识消息类型，常见值：
     * <ul>
     *     <li>0x3C - 心跳消息</li>
     *     <li>0x02 - 升级触发命令</li>
     *     <li>0x10 - 升级URL下发</li>
     *     <li>0x15 - 升级开始状态</li>
     *     <li>0x66 - 升级进度</li>
     *     <li>0x67 - 升级完成</li>
     *     <li>0x68 - 升级失败</li>
     * </ul>
     * </p>
     */
    private byte afn;

    /**
     * 数据域内容
     * <p>
     * 可变长度的数据域，内容取决于AFN类型。
     * </p>
     */
    private byte[] data;

    /**
     * 密码字段（2字节）
     * <p>
     * 用于设备认证，默认为 0x0000。
     * </p>
     */
    private byte[] password;

    /**
     * 时间标签（5字节）
     * <p>
     * 格式：秒(1) + 分(1) + 时(1) + 日(1) + 月年(1)
     * </p>
     */
    private byte[] timestamp;

    /**
     * 是否为上行消息（设备 -> 服务器）
     * <p>
     * true: 上行消息（设备发送）
     * false: 下行消息（服务器发送）
     * </p>
     */
    private boolean upstream;

    /**
     * 控制域C字段
     * <p>
     * 默认为 0x00。
     * </p>
     */
    private byte controlField;

    /**
     * 消息类型（从AFN解析）
     */
    private ChanghuiMessageType messageType;

    /**
     * 原始帧数据（用于调试）
     */
    private byte[] rawFrame;

    // ==================== 工厂方法 ====================

    /**
     * 创建上行消息（设备 -> 服务器）
     *
     * @param stationCode 测站编码（20字符十六进制字符串）
     * @param afn         应用功能码
     * @param data        数据域内容（可为 null）
     * @return 上行消息
     */
    public static ChanghuiMessage createUpstream(String stationCode, byte afn, byte[] data) {
        return ChanghuiMessage.builder()
                .stationCode(stationCode)
                .afn(afn)
                .data(data)
                .upstream(true)
                .messageType(ChanghuiMessageType.fromAfn(afn))
                .password(new byte[]{0x00, 0x00})
                .controlField((byte) 0x00)
                .build();
    }

    /**
     * 创建下行消息（服务器 -> 设备）
     *
     * @param stationCode 测站编码（20字符十六进制字符串）
     * @param afn         应用功能码
     * @param data        数据域内容（可为 null）
     * @return 下行消息
     */
    public static ChanghuiMessage createDownstream(String stationCode, byte afn, byte[] data) {
        return ChanghuiMessage.builder()
                .stationCode(stationCode)
                .afn(afn)
                .data(data)
                .upstream(false)
                .messageType(ChanghuiMessageType.fromAfn(afn))
                .password(new byte[]{0x00, 0x00})
                .controlField((byte) 0x00)
                .build();
    }

    /**
     * 创建心跳消息
     *
     * @param stationCode 测站编码
     * @param upstream    是否为上行消息
     * @return 心跳消息
     */
    public static ChanghuiMessage createHeartbeat(String stationCode, boolean upstream) {
        return ChanghuiMessage.builder()
                .stationCode(stationCode)
                .afn(ChanghuiMessageType.HEARTBEAT.getAfn())
                .upstream(upstream)
                .messageType(ChanghuiMessageType.HEARTBEAT)
                .password(new byte[]{0x00, 0x00})
                .controlField((byte) 0x00)
                .build();
    }

    /**
     * 创建升级触发消息
     *
     * @param stationCode 测站编码
     * @return 升级触发消息
     */
    public static ChanghuiMessage createUpgradeTrigger(String stationCode) {
        return createDownstream(stationCode, ChanghuiMessageType.UPGRADE_TRIGGER.getAfn(), null);
    }

    /**
     * 创建升级URL消息
     *
     * @param stationCode 测站编码
     * @param url         升级URL
     * @return 升级URL消息
     */
    public static ChanghuiMessage createUpgradeUrl(String stationCode, String url) {
        byte[] urlBytes = url != null ? url.getBytes(java.nio.charset.StandardCharsets.UTF_8) : null;
        return createDownstream(stationCode, ChanghuiMessageType.UPGRADE_URL.getAfn(), urlBytes);
    }

    /**
     * 创建升级状态消息
     *
     * @param stationCode 测站编码
     * @param statusType  状态类型
     * @param progress    进度（0-100，仅对 UPGRADE_STATUS_PROGRESS 有效）
     * @return 升级状态消息
     */
    public static ChanghuiMessage createUpgradeStatus(String stationCode, 
                                                       ChanghuiMessageType statusType, 
                                                       Integer progress) {
        byte[] data = null;
        if (statusType == ChanghuiMessageType.UPGRADE_STATUS_PROGRESS && progress != null) {
            data = new byte[]{progress.byteValue()};
        }
        return createUpstream(stationCode, statusType.getAfn(), data);
    }

    // ==================== 便捷方法 ====================

    /**
     * 获取消息类型
     * <p>
     * 如果 messageType 为 null，则从 AFN 解析。
     * </p>
     *
     * @return 消息类型
     */
    public ChanghuiMessageType getMessageType() {
        if (messageType == null) {
            messageType = ChanghuiMessageType.fromAfn(afn);
        }
        return messageType;
    }

    /**
     * 判断是否为心跳消息
     *
     * @return 是否为心跳消息
     */
    public boolean isHeartbeat() {
        return getMessageType() == ChanghuiMessageType.HEARTBEAT;
    }

    /**
     * 判断是否为升级相关消息
     *
     * @return 是否为升级相关消息
     */
    public boolean isUpgradeRelated() {
        ChanghuiMessageType type = getMessageType();
        return type == ChanghuiMessageType.UPGRADE_TRIGGER
                || type == ChanghuiMessageType.UPGRADE_URL
                || type.isUpgradeStatus();
    }

    /**
     * 判断是否为升级状态消息
     *
     * @return 是否为升级状态消息
     */
    public boolean isUpgradeStatus() {
        return getMessageType().isUpgradeStatus();
    }

    /**
     * 获取数据域长度
     *
     * @return 数据域长度
     */
    public int getDataLength() {
        return data != null ? data.length : 0;
    }

    /**
     * 获取数据域内容的字符串表示（UTF-8编码）
     *
     * @return 数据域字符串，如果数据为空则返回 null
     */
    public String getDataAsString() {
        if (data == null || data.length == 0) {
            return null;
        }
        return new String(data, java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * 设置数据域内容（从字符串）
     *
     * @param dataString 数据字符串
     */
    public void setDataFromString(String dataString) {
        if (dataString != null) {
            this.data = dataString.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        } else {
            this.data = null;
        }
    }

    /**
     * 获取时间戳对应的 LocalDateTime
     *
     * @return LocalDateTime，如果时间戳为空则返回 null
     */
    public LocalDateTime getTimestampAsLocalDateTime() {
        if (timestamp == null || timestamp.length < 5) {
            return null;
        }
        
        int second = timestamp[0] & 0xFF;
        int minute = timestamp[1] & 0xFF;
        int hour = timestamp[2] & 0xFF;
        int day = timestamp[3] & 0xFF;
        int monthYear = timestamp[4] & 0xFF;
        int month = (monthYear >> 4) & 0x0F;
        int year = 2000 + (monthYear & 0x0F);
        
        try {
            return LocalDateTime.of(year, month, day, hour, minute, second);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("ChanghuiMessage{stationCode='%s', afn=0x%02X, type=%s, upstream=%s, dataLen=%d}",
                stationCode, afn, getMessageType(), upstream, getDataLength());
    }
}
