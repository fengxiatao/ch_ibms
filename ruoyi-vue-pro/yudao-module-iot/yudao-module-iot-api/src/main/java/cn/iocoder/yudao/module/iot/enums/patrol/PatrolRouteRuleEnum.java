package cn.iocoder.yudao.module.iot.enums.patrol;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;

import java.util.Arrays;

/**
 * 巡更线路规则枚举
 *
 * @author 智能化系统
 */
@Getter
public enum PatrolRouteRuleEnum implements IntArrayValuable {

    UNORDERED(1, "全无序"),
    ORDERED(2, "全有序");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(PatrolRouteRuleEnum::getRule).toArray();

    /**
     * 规则
     */
    private final Integer rule;
    /**
     * 名称
     */
    private final String name;

    PatrolRouteRuleEnum(Integer rule, String name) {
        this.rule = rule;
        this.name = name;
    }

    @Override
    public int[] intArray() {
        return ARRAYS;
    }

}








