package cn.iocoder.yudao.module.iot.service.visitor.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 访客权限回收事件
 * 用于触发异步回收门禁设备权限（松耦合设计）
 *
 * @author 芋道源码
 */
@Getter
public class VisitorAuthRevokeEvent extends ApplicationEvent {

    /**
     * 访客授权ID
     */
    private final Long authId;

    /**
     * 申请ID
     */
    private final Long applyId;

    public VisitorAuthRevokeEvent(Object source, Long authId, Long applyId) {
        super(source);
        this.authId = authId;
        this.applyId = applyId;
    }

}
