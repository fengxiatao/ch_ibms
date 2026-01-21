package cn.iocoder.yudao.module.iot.enums.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 产品分类所属 IBMS 模块枚举
 *
 * @author 长辉信息科技有限公司
 */
@AllArgsConstructor
@Getter
public enum IotProductCategoryModuleEnum {

    BUILDING("building", "智慧建筑"),
    SECURITY("security", "智慧安防"),
    ACCESS("access", "智慧通行"),
    FIRE("fire", "智慧消防"),
    ENERGY("energy", "智慧能源"),
    INFRASTRUCTURE("infrastructure", "基础设施");

    /**
     * 模块编码
     */
    private final String code;
    /**
     * 模块名称
     */
    private final String name;

    /**
     * 根据编码获取模块
     */
    public static IotProductCategoryModuleEnum getByCode(String code) {
        return Arrays.stream(values())
                .filter(module -> module.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

}

