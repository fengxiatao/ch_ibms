package cn.iocoder.yudao.module.iot.core.enums.access;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Set;

/**
 * 门禁事件类型枚举
 * 
 * 基于大华SDK编程指导手册中定义的门禁报警事件类型，包括：
 * - 正常开门事件：刷卡、指纹、人脸、密码、二维码、远程、按钮、多人组合开门
 * - 报警事件：门未关、闯入、反复进入、恶意开门、胁迫、防拆、本地报警
 * - 状态事件：门禁状态、指纹采集
 *
 * SDK事件类型对照表：
 * | ICommand | 说明 |
 * |----------|------|
 * | NET_ALARM_ACCESS_CTL_EVENT | 门禁事件（刷卡、指纹、人脸、密码等开门事件） |
 * | NET_ALARM_ACCESS_CTL_NOT_CLOSE | 门未关事件 |
 * | NET_ALARM_ACCESS_CTL_BREAK_IN | 闯入事件 |
 * | NET_ALARM_ACCESS_CTL_REPEAT_ENTER | 反复进入事件 |
 * | NET_ALARM_ACCESS_CTL_MALICIOUS | 恶意开门事件 |
 * | NET_ALARM_ACCESS_CTL_DURESS | 胁迫卡刷卡事件 |
 * | NET_ALARM_OPENDOORGROUP | 多人组合开门事件 |
 * | NET_ALARM_CHASSISINTRUDED | 防拆事件 |
 * | NET_ALARM_ALARM_EX2 | 本地报警事件 |
 * | NET_ALARM_ACCESS_CTL_STATUS | 门禁状态事件 |
 * | NET_ALARM_FINGER_PRINT | 获取指纹事件 |
 *
 * @author kiro
 */
@RequiredArgsConstructor
@Getter
public enum AccessEventTypeEnum implements ArrayValuable<String> {

    // ==================== 正常开门事件 ====================
    CARD_SWIPE("CARD_SWIPE", "刷卡开门", EventCategory.NORMAL),
    FINGERPRINT("FINGERPRINT", "指纹开门", EventCategory.NORMAL),
    FACE_RECOGNIZE("FACE_RECOGNIZE", "人脸开门", EventCategory.NORMAL),
    PASSWORD("PASSWORD", "密码开门", EventCategory.NORMAL),
    QRCODE("QRCODE", "二维码开门", EventCategory.NORMAL),
    REMOTE_OPEN("REMOTE_OPEN", "远程开门", EventCategory.NORMAL),
    BUTTON_OPEN("BUTTON_OPEN", "按钮开门", EventCategory.NORMAL),
    MULTI_PERSON_OPEN("MULTI_PERSON_OPEN", "多人组合开门", EventCategory.NORMAL),

    // ==================== 报警事件 ====================
    DOOR_NOT_CLOSED("DOOR_NOT_CLOSED", "门未关报警", EventCategory.ALARM),
    BREAK_IN("BREAK_IN", "闯入报警", EventCategory.ALARM),
    REPEAT_ENTER("REPEAT_ENTER", "反复进入报警", EventCategory.ALARM),
    MALICIOUS_OPEN("MALICIOUS_OPEN", "恶意开门报警", EventCategory.ALARM),
    DURESS("DURESS", "胁迫报警", EventCategory.ALARM),
    TAMPER_ALARM("TAMPER_ALARM", "防拆报警", EventCategory.ALARM),
    LOCAL_ALARM("LOCAL_ALARM", "本地报警", EventCategory.ALARM),

    // ==================== 状态事件 ====================
    ACCESS_STATUS("ACCESS_STATUS", "门禁状态事件", EventCategory.NORMAL),
    FINGERPRINT_CAPTURE("FINGERPRINT_CAPTURE", "指纹采集事件", EventCategory.NORMAL),

    // ==================== 未知事件 ====================
    UNKNOWN("UNKNOWN", "未知事件", EventCategory.NORMAL);

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(AccessEventTypeEnum::getCode)
            .toArray(String[]::new);

    /**
     * 报警事件类型集合
     */
    public static final Set<String> ALARM_TYPES = Set.of(
            DOOR_NOT_CLOSED.getCode(),
            BREAK_IN.getCode(),
            REPEAT_ENTER.getCode(),
            MALICIOUS_OPEN.getCode(),
            DURESS.getCode(),
            TAMPER_ALARM.getCode(),
            LOCAL_ALARM.getCode()
    );

    /**
     * 事件类型代码
     */
    private final String code;

    /**
     * 事件类型名称
     */
    private final String name;

    /**
     * 事件类别
     */
    private final EventCategory category;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 事件类型代码
     * @return 事件类型枚举，如果未找到则返回 UNKNOWN
     */
    public static AccessEventTypeEnum fromCode(String code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (AccessEventTypeEnum eventType : values()) {
            if (eventType.getCode().equals(code)) {
                return eventType;
            }
        }
        return UNKNOWN;
    }

    /**
     * 判断是否为报警事件
     *
     * @param code 事件类型代码
     * @return 是否为报警事件
     */
    public static boolean isAlarmEvent(String code) {
        return ALARM_TYPES.contains(code);
    }

    /**
     * 根据事件类型和验证结果确定事件类别
     * 
     * 规则：
     * - 如果事件类型属于报警类型，返回 ALARM
     * - 如果验证结果为失败(0)，返回 ABNORMAL
     * - 否则返回事件类型定义的类别
     *
     * @param code 事件类型代码
     * @param verifyResult 验证结果（1-成功，0-失败）
     * @return 事件类别
     */
    public static EventCategory determineCategory(String code, Integer verifyResult) {
        // 报警事件优先
        if (isAlarmEvent(code)) {
            return EventCategory.ALARM;
        }
        // 验证失败为异常事件
        if (verifyResult != null && verifyResult == 0) {
            return EventCategory.ABNORMAL;
        }
        // 返回事件类型定义的类别
        AccessEventTypeEnum eventType = fromCode(code);
        return eventType.getCategory();
    }
}
