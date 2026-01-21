package cn.iocoder.yudao.module.iot.enums.changhui;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 长辉设备升级模式枚举
 * 
 * <p>定义设备固件升级的传输方式
 * <p>基于全渠道量测水设施数据传输规约V2.3（IP9500_OPC协议）
 *
 * @author 长辉信息科技有限公司
 */
@Getter
@AllArgsConstructor
public enum ChanghuiUpgradeModeEnum {

    /**
     * TCP帧传输模式
     * <p>通过TCP连接逐帧传输固件数据
     */
    TCP_FRAME(0, "TCP帧传输"),

    /**
     * HTTP URL下载模式
     * <p>设备通过HTTP下载固件文件
     */
    HTTP_URL(1, "HTTP URL下载");

    /**
     * 升级模式编码
     */
    private final Integer code;

    /**
     * 升级模式描述
     */
    private final String description;

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     * @return 枚举，如果未找到返回null
     */
    public static ChanghuiUpgradeModeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ChanghuiUpgradeModeEnum mode : values()) {
            if (mode.getCode().equals(code)) {
                return mode;
            }
        }
        return null;
    }

}
