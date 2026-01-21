package cn.iocoder.yudao.module.iot.enums;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 防区布防状态枚举
 *
 * @author 长辉信息科技有限公司
 */
@Getter
@AllArgsConstructor
public enum ZoneStatusEnum implements IntArrayValuable {

    /**
     * 撤防
     */
    DISARM(0, "撤防"),

    /**
     * 布防
     */
    ARM(1, "布防"),

    /**
     * 旁路
     */
    BYPASS(2, "旁路");

    /**
     * 状态值
     */
    private final Integer value;

    /**
     * 状态名称
     */
    private final String name;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(ZoneStatusEnum::getValue).toArray();

    @Override
    public int[] intArray() {
        return ARRAYS;
    }

    /**
     * 根据状态值获取枚举
     */
    public static ZoneStatusEnum valueOf(Integer value) {
        if (value == null) {
            return null;
        }
        for (ZoneStatusEnum status : values()) {
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
        ZoneStatusEnum status = valueOf(value);
        return status != null ? status.getName() : null;
    }
}
