package cn.iocoder.yudao.module.iot.enums.patrol;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;

import java.util.Arrays;

/**
 * 巡更计划触发类型枚举
 *
 * @author 智能化系统
 */
@Getter
public enum PatrolTriggerTypeEnum implements IntArrayValuable {

    SCHEDULED(1, "定时执行"),
    MANUAL(2, "手动触发");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(PatrolTriggerTypeEnum::getType).toArray();

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    PatrolTriggerTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public int[] intArray() {
        return ARRAYS;
    }

}








