package cn.iocoder.yudao.module.iot.core.enums.access;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 门禁错误码枚举
 * 
 * 基于大华SDK中NET_ALARM_ACCESS_CTL_EVENT事件的errorCode字段定义，
 * 用于描述门禁验证失败的具体原因。
 *
 * SDK errorCode值对照：
 * | 值 | 说明 |
 * |----|------|
 * | 0  | 成功 |
 * | 1  | 卡号不存在 |
 * | 2  | 卡号已过期 |
 * | 3  | 卡号已挂失 |
 * | 4  | 无此门权限 |
 * | 5  | 不在有效时段 |
 * | 6  | 防潜回失败 |
 * | 7  | 密码错误 |
 * | 8  | 指纹不匹配 |
 * | 9  | 人脸不匹配 |
 * | 10 | 门已锁定 |
 * | 11 | 胁迫报警 |
 * | 12 | 设备离线 |
 *
 * @author kiro
 */
@RequiredArgsConstructor
@Getter
public enum AccessErrorCodeEnum implements ArrayValuable<Integer> {

    SUCCESS(0, "成功"),
    CARD_NOT_EXIST(1, "卡号不存在"),
    CARD_EXPIRED(2, "卡号已过期"),
    CARD_LOST(3, "卡号已挂失"),
    NO_DOOR_PERMISSION(4, "无此门权限"),
    NOT_IN_VALID_TIME(5, "不在有效时段"),
    ANTI_PASSBACK_FAIL(6, "防潜回失败"),
    PASSWORD_ERROR(7, "密码错误"),
    FINGERPRINT_MISMATCH(8, "指纹不匹配"),
    FACE_MISMATCH(9, "人脸不匹配"),
    DOOR_LOCKED(10, "门已锁定"),
    DURESS_ALARM(11, "胁迫报警"),
    DEVICE_OFFLINE(12, "设备离线"),
    UNKNOWN(-1, "未知错误");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(AccessErrorCodeEnum::getCode)
            .toArray(Integer[]::new);

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误描述/失败原因
     */
    private final String reason;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    /**
     * 根据错误码获取枚举
     *
     * @param code 错误码
     * @return 错误码枚举，如果未找到则返回 UNKNOWN
     */
    public static AccessErrorCodeEnum fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (AccessErrorCodeEnum errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return UNKNOWN;
    }

    /**
     * 根据错误码获取失败原因
     *
     * @param code 错误码
     * @return 失败原因描述
     */
    public static String getReasonByCode(Integer code) {
        return fromCode(code).getReason();
    }

    /**
     * 判断是否为成功
     *
     * @param code 错误码
     * @return 是否成功
     */
    public static boolean isSuccess(Integer code) {
        return SUCCESS.getCode().equals(code);
    }

    /**
     * 判断是否为失败
     *
     * @param code 错误码
     * @return 是否失败
     */
    public static boolean isFailed(Integer code) {
        return !isSuccess(code);
    }

    /**
     * 将错误码转换为验证结果
     *
     * @param code 错误码
     * @return 验证结果（1-成功，0-失败）
     */
    public static Integer toVerifyResult(Integer code) {
        return isSuccess(code) ? 1 : 0;
    }
}
