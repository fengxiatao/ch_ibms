package cn.iocoder.yudao.module.iot.enums.patrol;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;

import java.util.Arrays;

/**
 * 巡更计划状态枚举
 *
 * @author 智能化系统
 */
@Getter
public enum PatrolPlanStatusEnum implements IntArrayValuable {

    ENABLED(1, "启用"),
    DISABLED(2, "停用");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(PatrolPlanStatusEnum::getStatus).toArray();

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 名称
     */
    private final String name;

    PatrolPlanStatusEnum(Integer status, String name) {
        this.status = status;
        this.name = name;
    }

    @Override
    public int[] intArray() {
        return ARRAYS;
    }

}








