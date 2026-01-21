package cn.iocoder.yudao.module.iot.enums.access;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;

import java.util.Arrays;

/**
 * 门禁告警级别枚举
 *
 * @author 智能化系统
 */
@Getter
public enum AccessAlarmLevelEnum implements IntArrayValuable {

    NORMAL(1, "一般"),
    IMPORTANT(2, "重要"),
    URGENT(3, "紧急");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(AccessAlarmLevelEnum::getLevel).toArray();

    private final Integer level;
    private final String name;

    AccessAlarmLevelEnum(Integer level, String name) {
        this.level = level;
        this.name = name;
    }

    @Override
    public int[] intArray() {
        return ARRAYS;
    }

}








