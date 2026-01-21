package cn.iocoder.yudao.module.iot.enums.patrol;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;

import java.util.Arrays;

/**
 * 巡更点位类型枚举
 *
 * @author 智能化系统
 */
@Getter
public enum PatrolPointTypeEnum implements IntArrayValuable {

    CUSTOM(1, "自定义"),
    DEVICE(2, "设备巡更");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(PatrolPointTypeEnum::getType).toArray();

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    PatrolPointTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public int[] intArray() {
        return ARRAYS;
    }

}








