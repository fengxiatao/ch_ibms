package cn.iocoder.yudao.module.iot.core.gateway.dto;

import cn.iocoder.yudao.module.iot.core.mq.message.GatewayEventDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 门禁事件消息
 * 
 * <p>用于 Gateway → Biz 的门禁事件上报</p>
 *
 * @author 芋道源码
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessControlEventMessage implements GatewayEventDTO {

    private static final long serialVersionUID = 1L;

    /**
     * 请求ID（链路追踪/幂等关联）
     */
    private String requestId;

    /**
     * 租户ID（多租户上下文）
     */
    private Long tenantId;

    /**
     * 事件ID（幂等键）
     * <p>用于消费端去重；若为空，则由消息总线 Envelope 回退生成</p>
     */
    private String eventId;


    /**
     * 事件发生时间（epoch milli）
     */
    private Long timestamp;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备类型（如 ACCESS_GEN1 / ACCESS_GEN2）
     */
    private String deviceType;


    /**
     * 设备IP
     */
    private String deviceIp;

    /**
     * 通道号
     */
    private Integer channelNo;

    /**
     * 事件类型
     * @see EventType
     */
    private Integer eventType;

    /**
     * 事件时间
     */
    private LocalDateTime eventTime;

    /**
     * 验证方式
     * @see VerifyMode
     */
    private Integer verifyMode;

    /**
     * 验证结果（0-失败，1-成功）
     */
    private Integer verifyResult;

    /**
     * 人员ID（如果能识别）
     */
    private String personId;

    /**
     * 人员姓名
     */
    private String personName;

    /**
     * 卡号
     */
    private String cardNo;

    /**
     * 抓拍图片URL
     */
    private String captureUrl;
    
    /**
     * 抓拍图片数据（Base64编码）
     * <p>用于存储从设备获取的原始图片数据，便于后续保存到文件系统</p>
     * Requirements: 4.3
     */
    private String snapshotBase64;
    
    /**
     * 原始事件数据（JSON格式）
     * <p>用于存储设备上报的原始数据，便于问题排查和数据恢复</p>
     * Requirements: 4.4
     */
    private String rawData;

    /**
     * 扩展数据
     */
    private Map<String, Object> extData;

    /**
     * 事件类型枚举
     * 
     * 对应 AccessEventTypeEnum 中定义的事件类型
     */
    public interface EventType {
        /** 刷卡 */
        int CARD = 1;
        /** 密码 */
        int PASSWORD = 2;
        /** 指纹 */
        int FINGERPRINT = 3;
        /** 人脸 */
        int FACE = 4;
        /** 二维码 */
        int QR_CODE = 5;
        /** 远程开门 */
        int REMOTE_OPEN = 10;
        /** 按钮开门 */
        int BUTTON_OPEN = 11;
        /** 多人组合开门 */
        int MULTI_PERSON_OPEN = 12;
        /** 门磁报警 */
        int DOOR_SENSOR_ALARM = 20;
        /** 强行开门/胁迫报警 */
        int FORCED_OPEN = 21;
        /** 门未关好 */
        int DOOR_NOT_CLOSED = 22;
        /** 闯入报警 */
        int BREAK_IN = 23;
        /** 反复进入报警 */
        int REPEAT_ENTER = 24;
        /** 防拆报警 */
        int TAMPER_ALARM = 25;
        /** 本地报警 */
        int LOCAL_ALARM = 26;
        /** 门禁状态事件 */
        int ACCESS_STATUS = 30;
        /** 指纹采集事件 */
        int FINGERPRINT_CAPTURE = 31;
    }

    /**
     * 验证方式枚举
     */
    public interface VerifyMode {
        /** 刷卡 */
        int CARD = 1;
        /** 密码 */
        int PASSWORD = 2;
        /** 指纹 */
        int FINGERPRINT = 3;
        /** 人脸 */
        int FACE = 4;
        /** 刷卡+密码 */
        int CARD_PASSWORD = 5;
        /** 刷卡+指纹 */
        int CARD_FINGERPRINT = 6;
        /** 刷卡+人脸 */
        int CARD_FACE = 7;
    }

}
