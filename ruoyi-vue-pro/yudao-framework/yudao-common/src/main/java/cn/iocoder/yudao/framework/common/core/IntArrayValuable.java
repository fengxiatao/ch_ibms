package cn.iocoder.yudao.framework.common.core;

/**
 * 可生成 int 数组的接口
 * 用于枚举类实现，配合 {@link cn.iocoder.yudao.framework.common.validation.InEnum} 注解使用
 *
 * @author 智能化系统
 */
public interface IntArrayValuable extends ArrayValuable<Integer> {

    /**
     * @return int 数组（原始类型）
     */
    int[] intArray();

    /**
     * @return Integer 数组（适配 ArrayValuable 接口）
     */
    @Override
    default Integer[] array() {
        int[] intArray = intArray();
        Integer[] integerArray = new Integer[intArray.length];
        for (int i = 0; i < intArray.length; i++) {
            integerArray[i] = intArray[i];
        }
        return integerArray;
    }

}

