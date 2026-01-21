package cn.iocoder.yudao.module.iot.websocket.message;

import cn.iocoder.yudao.module.iot.core.enums.access.AccessEventTypeEnum;
import cn.iocoder.yudao.module.iot.core.enums.access.EventCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 门禁事件推送消息
 * 
 * 用于 WebSocket 推送门禁事件到前端
 * Requirements: 4.2
 *
 * @author 智能化系统
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessEventPushMessage {

    // ==================== 事件基本信息 ====================

    /**
     * 事件ID
     */
    private Long eventId;

    /**
     * 事件时间
     */
    private LocalDateTime eventTime;

    /**
     * 事件类型：CARD_SWIPE-刷卡，FACE_RECOGNIZE-人脸识别，FINGERPRINT-指纹，PASSWORD-密码，REMOTE_OPEN-远程开门
     * 基于大华SDK事件类型定义
     */
    private String eventType;

    /**
     * 事件类型名称（中文描述）
     * Requirements: 4.2 - 添加事件类型名称
     */
    private String eventTypeName;

    /**
     * 事件类别：ALARM-报警事件，ABNORMAL-异常事件，NORMAL-正常事件
     * Requirements: 4.2 - 添加事件类别
     */
    private String eventCategory;

    /**
     * 事件类别名称（中文描述）
     */
    private String eventCategoryName;

    /**
     * 事件描述
     */
    private String eventDesc;

    // ==================== 人员信息 ====================

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 人员姓名
     */
    private String personName;

    /**
     * 人员编号
     */
    private String personCode;

    /**
     * 卡号
     */
    private String cardNo;

    // ==================== 设备信息 ====================

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

    // ==================== 验证结果 ====================

    /**
     * 验证结果：true-成功，false-失败
     */
    private Boolean success;

    /**
     * 验证结果：0-失败，1-成功
     */
    private Integer verifyResult;

    /**
     * 验证结果描述
     */
    private String verifyResultDesc;

    /**
     * 验证方式
     */
    private String verifyMode;

    /**
     * 失败原因
     */
    private String failReason;

    // ==================== 附加信息 ====================

    /**
     * 抓拍图片URL
     */
    private String captureUrl;

    /**
     * 体温（℃）
     */
    private BigDecimal temperature;

    /**
     * 口罩状态：0-未佩戴，1-已佩戴
     */
    private Integer maskStatus;

    /**
     * 凭证类型
     */
    private String credentialType;

    /**
     * 凭证数据
     */
    private String credentialData;

    // ==================== 时间戳 ====================

    /**
     * 推送时间戳（毫秒）
     */
    private Long timestamp;

    // ==================== 辅助方法 ====================

    /**
     * 获取事件类型的中文名称
     * 使用 AccessEventTypeEnum 进行映射
     * Requirements: 4.2
     *
     * @param eventType 事件类型代码
     * @return 事件类型中文名称
     */
    public static String getEventTypeName(String eventType) {
        if (eventType == null) {
            return "未知事件";
        }
        AccessEventTypeEnum eventTypeEnum = AccessEventTypeEnum.fromCode(eventType);
        return eventTypeEnum.getName();
    }

    /**
     * 根据事件类型和验证结果确定事件类别
     * Requirements: 4.2
     *
     * @param eventType 事件类型代码
     * @param verifyResult 验证结果（1-成功，0-失败）
     * @return 事件类别
     */
    public static EventCategory determineEventCategory(String eventType, Integer verifyResult) {
        return AccessEventTypeEnum.determineCategory(eventType, verifyResult);
    }

    /**
     * 获取事件类别名称
     * Requirements: 4.2
     *
     * @param category 事件类别
     * @return 事件类别中文名称
     */
    public static String getEventCategoryName(EventCategory category) {
        if (category == null) {
            return "未知类别";
        }
        return category.getName();
    }

}
