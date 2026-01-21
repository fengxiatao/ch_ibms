package cn.iocoder.yudao.module.iot.enums.access;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;

import java.util.Arrays;

/**
 * 门禁下发类型枚举
 *
 * @author 智能化系统
 */
@Getter
public enum AccessDispatchTypeEnum implements IntArrayValuable {

    PERSON(1, "人员下发"),
    CARD(2, "卡片下发"),
    FACE(3, "人脸下发");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(AccessDispatchTypeEnum::getType).toArray();

    private final Integer type;
    private final String name;

    AccessDispatchTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public int[] intArray() {
        return ARRAYS;
    }

}








