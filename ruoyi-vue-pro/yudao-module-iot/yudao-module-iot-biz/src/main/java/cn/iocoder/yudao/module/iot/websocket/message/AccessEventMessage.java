package cn.iocoder.yudao.module.iot.websocket.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 门禁事件消息
 *
 * @author 智能化系统
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessEventMessage {

    /**
     * 事件ID
     */
    private Long eventId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 通道名称
     */
    private String channelName;

    /**
     * 事件类型：card-刷卡，face-人脸，fingerprint-指纹，password-密码，qrcode-二维码，alarm-报警
     */
    private String eventType;

    /**
     * 事件名称
     */
    private String eventName;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 人员姓名
     */
    private String personName;

    /**
     * 卡号
     */
    private String cardNo;

    /**
     * 验证结果：0-失败，1-成功
     */
    private Integer verifyResult;

    /**
     * 门状态：0-关闭，1-打开
     */
    private Integer doorStatus;

    /**
     * 抓拍图片URL
     */
    private String captureUrl;

    /**
     * 事件时间
     */
    private LocalDateTime eventTime;

    /**
     * 时间戳
     */
    private Long timestamp;

}
