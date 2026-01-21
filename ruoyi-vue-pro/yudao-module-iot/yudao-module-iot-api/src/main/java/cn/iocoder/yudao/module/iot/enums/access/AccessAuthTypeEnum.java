package cn.iocoder.yudao.module.iot.enums.access;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;

import java.util.Arrays;

/**
 * 门禁授权类型枚举
 *
 * @author 智能化系统
 */
@Getter
public enum AccessAuthTypeEnum implements IntArrayValuable {

    PASSWORD(1, "密码"),
    CARD(2, "卡片");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(AccessAuthTypeEnum::getType).toArray();

    private final Integer type;
    private final String name;

    AccessAuthTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public int[] intArray() {
        return ARRAYS;
    }

}








