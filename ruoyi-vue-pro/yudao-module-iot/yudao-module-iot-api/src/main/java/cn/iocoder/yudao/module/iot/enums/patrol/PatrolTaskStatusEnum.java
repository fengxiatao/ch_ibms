package cn.iocoder.yudao.module.iot.enums.patrol;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;

import java.util.Arrays;

/**
 * 巡更任务状态枚举
 *
 * @author 智能化系统
 */
@Getter
public enum PatrolTaskStatusEnum implements IntArrayValuable {

    PENDING(1, "待执行"),
    IN_PROGRESS(2, "执行中"),
    COMPLETED(3, "已完成"),
    TIMEOUT(4, "已超时");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(PatrolTaskStatusEnum::getStatus).toArray();

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 名称
     */
    private final String name;

    PatrolTaskStatusEnum(Integer status, String name) {
        this.status = status;
        this.name = name;
    }

    @Override
    public int[] intArray() {
        return ARRAYS;
    }

}








