package cn.iocoder.yudao.module.iot.core.mq.message;

import java.io.Serializable;

/**
 * 网关事件 DTO 标记接口
 * 
 * <p>所有通过 GatewayMessagePublisher 发布的事件消息必须实现此接口。</p>
 * 
 * <p>此接口用于强制类型检查，禁止使用 Map 类型发布事件消息，确保：</p>
 * <ul>
 *   <li>编译时类型安全</li>
 *   <li>运行时类型验证</li>
 *   <li>消息格式一致性</li>
 * </ul>
 * 
 * <p>Requirements: 4.1, 4.2</p>
 *
 * @author 长辉信息科技有限公司
 */
public interface GatewayEventDTO extends Serializable {
    
    /**
     * 获取设备类型
     * 
     * @return 设备类型标识
     */
    String getDeviceType();
    
    /**
     * 获取设备ID
     * 
     * @return 设备ID
     */
    Long getDeviceId();
    
    /**
     * 获取事件时间戳
     * 
     * @return 事件时间戳（毫秒）
     */
    Long getTimestamp();
}
