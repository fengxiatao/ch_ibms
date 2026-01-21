package cn.iocoder.yudao.module.iot.enums.access;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;

import java.util.Arrays;

/**
 * 门禁下发状态枚举
 *
 * @author 智能化系统
 */
@Getter
public enum AccessDispatchStatusEnum implements IntArrayValuable {

    PENDING(0, "待下发"),
    DISPATCHING(1, "下发中"),
    SUCCESS(2, "成功"),
    FAILED(3, "失败");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(AccessDispatchStatusEnum::getStatus).toArray();

    private final Integer status;
    private final String name;

    AccessDispatchStatusEnum(Integer status, String name) {
        this.status = status;
        this.name = name;
    }

    @Override
    public int[] intArray() {
        return ARRAYS;
    }

}








