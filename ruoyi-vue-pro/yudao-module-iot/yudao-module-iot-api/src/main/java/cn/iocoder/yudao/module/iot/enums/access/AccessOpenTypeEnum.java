package cn.iocoder.yudao.module.iot.enums.access;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;

import java.util.Arrays;

/**
 * 门禁开门类型枚举
 *
 * @author 智能化系统
 */
@Getter
public enum AccessOpenTypeEnum implements IntArrayValuable {

    CARD(1, "刷卡"),
    FACE(2, "人脸"),
    PASSWORD(3, "密码"),
    REMOTE(4, "远程");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(AccessOpenTypeEnum::getType).toArray();

    private final Integer type;
    private final String name;

    AccessOpenTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public int[] intArray() {
        return ARRAYS;
    }

}








