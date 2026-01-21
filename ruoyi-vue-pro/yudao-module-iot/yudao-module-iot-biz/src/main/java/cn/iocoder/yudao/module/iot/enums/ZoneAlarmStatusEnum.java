package cn.iocoder.yudao.module.iot.enums;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 防区报警状态枚举
 *
 * @author 长辉信息科技有限公司
 */
@Getter
@AllArgsConstructor
public enum ZoneAlarmStatusEnum implements IntArrayValuable {

    /**
     * 正常（无报警）
     */
    NORMAL(0, "正常"),

    /**
     * 报警中
     */
    ALARMING(1, "报警中"),

    /**
     * 剪断报警
     */
    CUT_ALARM(11, "剪断报警"),

    /**
     * 短路报警
     */
    SHORT_CIRCUIT_ALARM(12, "短路报警"),

    /**
     * 触网报警
     */
    TOUCH_NET_ALARM(13, "触网报警"),

    /**
     * 松弛报警
     */
    SLACK_ALARM(14, "松弛报警"),

    /**
     * 拉紧报警
     */
    TIGHT_ALARM(15, "拉紧报警"),

    /**
     * 攀爬报警
     */
    CLIMB_ALARM(16, "攀爬报警"),

    /**
     * 开路报警
     */
    OPEN_CIRCUIT_ALARM(17, "开路报警");

    /**
     * 状态值
     */
    private final Integer value;

    /**
     * 状态名称
     */
    private final String name;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(ZoneAlarmStatusEnum::getValue).toArray();

    @Override
    public int[] intArray() {
        return ARRAYS;
    }

    /**
     * 根据状态值获取枚举
     */
    public static ZoneAlarmStatusEnum valueOf(Integer value) {
        if (value == null) {
            return null;
        }
        for (ZoneAlarmStatusEnum status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据状态值获取状态名称
     */
    public static String getNameByValue(Integer value) {
        ZoneAlarmStatusEnum status = valueOf(value);
        return status != null ? status.getName() : null;
    }

    /**
     * 判断是否为报警状态
     */
    public boolean isAlarming() {
        return this.value > 0;
    }
}
