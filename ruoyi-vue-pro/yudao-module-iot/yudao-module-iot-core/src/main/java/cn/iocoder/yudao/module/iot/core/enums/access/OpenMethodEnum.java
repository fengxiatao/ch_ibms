package cn.iocoder.yudao.module.iot.core.enums.access;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 开门方式枚举
 * 
 * 基于大华SDK中NET_ALARM_ACCESS_CTL_EVENT事件的openMethod字段定义，
 * 包括单一验证方式和组合验证方式。
 *
 * SDK openMethod值对照：
 * | 值 | 说明 |
 * |----|------|
 * | 0  | 刷卡 |
 * | 1  | 指纹 |
 * | 2  | 人脸 |
 * | 3  | 密码 |
 * | 4  | 二维码 |
 * | 5  | 远程 |
 * | 6  | 按钮 |
 * | 7  | 刷卡+指纹 |
 * | 8  | 刷卡+密码 |
 * | 9  | 刷卡+人脸 |
 * | 10 | 指纹+密码 |
 * | 11 | 指纹+人脸 |
 * | 12 | 人脸+密码 |
 * | 13 | 刷卡+指纹+密码 |
 *
 * @author kiro
 */
@RequiredArgsConstructor
@Getter
public enum OpenMethodEnum implements ArrayValuable<Integer> {

    CARD(0, "刷卡"),
    FINGERPRINT(1, "指纹"),
    FACE(2, "人脸"),
    PASSWORD(3, "密码"),
    QRCODE(4, "二维码"),
    REMOTE(5, "远程"),
    BUTTON(6, "按钮"),
    CARD_FINGERPRINT(7, "刷卡+指纹"),
    CARD_PASSWORD(8, "刷卡+密码"),
    CARD_FACE(9, "刷卡+人脸"),
    FINGERPRINT_PASSWORD(10, "指纹+密码"),
    FINGERPRINT_FACE(11, "指纹+人脸"),
    FACE_PASSWORD(12, "人脸+密码"),
    CARD_FINGERPRINT_PASSWORD(13, "刷卡+指纹+密码"),
    UNKNOWN(-1, "未知");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OpenMethodEnum::getCode)
            .toArray(Integer[]::new);

    /**
     * 开门方式代码
     */
    private final Integer code;

    /**
     * 开门方式名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 开门方式代码
     * @return 开门方式枚举，如果未找到则返回 UNKNOWN
     */
    public static OpenMethodEnum fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (OpenMethodEnum method : values()) {
            if (method.getCode().equals(code)) {
                return method;
            }
        }
        return UNKNOWN;
    }

    /**
     * 根据代码获取开门方式名称
     *
     * @param code 开门方式代码
     * @return 开门方式名称
     */
    public static String getNameByCode(Integer code) {
        return fromCode(code).getName();
    }

    /**
     * 将开门方式转换为对应的事件类型代码
     *
     * @param code 开门方式代码
     * @return 事件类型代码
     */
    public static String toEventTypeCode(Integer code) {
        OpenMethodEnum method = fromCode(code);
        switch (method) {
            case CARD:
            case CARD_FINGERPRINT:
            case CARD_PASSWORD:
            case CARD_FACE:
            case CARD_FINGERPRINT_PASSWORD:
                return AccessEventTypeEnum.CARD_SWIPE.getCode();
            case FINGERPRINT:
            case FINGERPRINT_PASSWORD:
            case FINGERPRINT_FACE:
                return AccessEventTypeEnum.FINGERPRINT.getCode();
            case FACE:
            case FACE_PASSWORD:
                return AccessEventTypeEnum.FACE_RECOGNIZE.getCode();
            case PASSWORD:
                return AccessEventTypeEnum.PASSWORD.getCode();
            case QRCODE:
                return AccessEventTypeEnum.QRCODE.getCode();
            case REMOTE:
                return AccessEventTypeEnum.REMOTE_OPEN.getCode();
            case BUTTON:
                return AccessEventTypeEnum.BUTTON_OPEN.getCode();
            default:
                return AccessEventTypeEnum.UNKNOWN.getCode();
        }
    }
}
