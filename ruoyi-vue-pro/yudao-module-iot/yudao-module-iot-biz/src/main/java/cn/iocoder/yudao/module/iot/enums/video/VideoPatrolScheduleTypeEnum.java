package cn.iocoder.yudao.module.iot.enums.video;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 视频定时轮巡计划类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum VideoPatrolScheduleTypeEnum implements IntArrayValuable {

    DAILY(1, "日计划"),
    WEEKLY(2, "周计划");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(VideoPatrolScheduleTypeEnum::getType).toArray();

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    @Override
    public int[] intArray() {
        return ARRAYS;
    }

}
