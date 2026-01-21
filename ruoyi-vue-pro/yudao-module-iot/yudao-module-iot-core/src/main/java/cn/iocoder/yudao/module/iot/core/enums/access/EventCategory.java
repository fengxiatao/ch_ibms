package cn.iocoder.yudao.module.iot.core.enums.access;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 门禁事件类别枚举
 * 
 * 定义了门禁事件的三大类别：
 * - ALARM: 报警事件（闯入、门未关、胁迫、防拆等）
 * - ABNORMAL: 异常事件（验证失败的事件）
 * - NORMAL: 正常事件（正常开门事件）
 *
 * @author kiro
 */
@RequiredArgsConstructor
@Getter
public enum EventCategory {

    ALARM("ALARM", "报警事件", "danger"),
    ABNORMAL("ABNORMAL", "异常事件", "warning"),
    NORMAL("NORMAL", "正常事件", "success");

    /**
     * 类别代码
     */
    private final String code;
    
    /**
     * 类别名称
     */
    private final String name;
    
    /**
     * UI样式类型（用于前端展示）
     */
    private final String styleType;

    /**
     * 根据代码获取枚举
     *
     * @param code 类别代码
     * @return 事件类别枚举，如果未找到则返回 null
     */
    public static EventCategory fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (EventCategory category : values()) {
            if (category.getCode().equals(code)) {
                return category;
            }
        }
        return null;
    }
}
