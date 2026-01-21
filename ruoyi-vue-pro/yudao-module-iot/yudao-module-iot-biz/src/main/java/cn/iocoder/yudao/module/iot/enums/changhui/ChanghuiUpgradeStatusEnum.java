package cn.iocoder.yudao.module.iot.enums.changhui;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 长辉设备升级状态枚举
 * 
 * <p>定义设备固件升级任务的状态
 * <p>基于全渠道量测水设施数据传输规约V2.3（IP9500_OPC协议）
 *
 * @author 长辉信息科技有限公司
 */
@Getter
@AllArgsConstructor
public enum ChanghuiUpgradeStatusEnum {

    /**
     * 待执行
     */
    PENDING(0, "待执行"),

    /**
     * 进行中
     */
    IN_PROGRESS(1, "进行中"),

    /**
     * 成功
     */
    SUCCESS(2, "成功"),

    /**
     * 失败
     */
    FAILED(3, "失败"),

    /**
     * 已取消
     */
    CANCELLED(4, "已取消"),

    /**
     * 已拒绝（设备拒绝升级）
     */
    REJECTED(5, "已拒绝");

    /**
     * 状态编码
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     * @return 枚举，如果未找到返回null
     */
    public static ChanghuiUpgradeStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ChanghuiUpgradeStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为终态
     *
     * @return 是否为终态
     */
    public boolean isFinal() {
        return this == SUCCESS || this == FAILED || this == CANCELLED || this == REJECTED;
    }

    /**
     * 判断是否可以取消
     *
     * @return 是否可以取消
     */
    public boolean canCancel() {
        return this == PENDING || this == IN_PROGRESS;
    }

}
