package cn.iocoder.yudao.module.iot.enums.visitor;

import lombok.Getter;

/**
 * 访客类型枚举
 *
 * @author 智能化系统
 */
@Getter
public enum VisitorTypeEnum {

    CUSTOMER(1, "客户"),
    SUPPLIER(2, "供应商"),
    PARTNER(3, "合作伙伴"),
    CANDIDATE(4, "应聘人员"),
    OTHER(5, "其他");

    private final Integer type;
    private final String name;

    VisitorTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

}


























