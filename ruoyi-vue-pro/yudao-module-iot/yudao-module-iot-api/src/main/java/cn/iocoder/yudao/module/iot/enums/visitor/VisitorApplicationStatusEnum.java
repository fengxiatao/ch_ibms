package cn.iocoder.yudao.module.iot.enums.visitor;

import lombok.Getter;

/**
 * 访客申请状态枚举
 *
 * @author 智能化系统
 */
@Getter
public enum VisitorApplicationStatusEnum {

    PENDING(0, "待审核"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已拒绝"),
    CANCELLED(3, "已取消"),
    EXPIRED(4, "已过期");

    private final Integer status;
    private final String name;

    VisitorApplicationStatusEnum(Integer status, String name) {
        this.status = status;
        this.name = name;
    }

}


























