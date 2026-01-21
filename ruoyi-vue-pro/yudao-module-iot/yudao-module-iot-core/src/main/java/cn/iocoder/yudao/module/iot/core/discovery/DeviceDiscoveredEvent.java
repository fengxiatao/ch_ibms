package cn.iocoder.yudao.module.iot.core.discovery;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 设备发现事件（共享事件）
 * 
 * @author 长辉信息科技有限公司
 */
@Getter
public class DeviceDiscoveredEvent extends ApplicationEvent {
    
    /**
     * 发现的设备
     */
    private final DiscoveredDevice device;
    
    public DeviceDiscoveredEvent(Object source, DiscoveredDevice device) {
        super(source);
        this.device = device;
    }
}





