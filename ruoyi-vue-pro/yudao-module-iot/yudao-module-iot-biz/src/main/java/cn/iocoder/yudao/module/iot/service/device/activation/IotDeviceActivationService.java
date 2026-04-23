package cn.iocoder.yudao.module.iot.service.device.activation;

import cn.iocoder.yudao.module.iot.service.device.discovery.dto.DiscoveredDeviceDTO;

import java.util.Map;

/**
 * 设备激活服务（台账仅写 IBMS：{@code ibms_device} / {@code ibms_channel}，网关 {@code deviceId} = {@code ibms_device.id}）
 *
 * @author 长辉信息科技有限公司
 */
public interface IotDeviceActivationService {
    
    /**
     * 激活设备
     * 
     * @param discoveredDevice 发现的设备信息
     * @param productId IBMS 产品主键（{@code ibms_product.id}）
     * @param username 设备用户名
     * @param password 设备密码
     * @return 激活请求ID
     */
    String activateDevice(DiscoveredDeviceDTO discoveredDevice, Long productId, String username, String password);
    
    /**
     * 获取激活状态
     * 
     * @param activationId 激活请求ID
     * @return 激活状态（activating, completed, failed, not_found）
     */
    String getActivationStatus(String activationId);
    
    /**
     * 获取激活结果
     * 
     * @param activationId 激活请求ID
     * @return IBMS 设备 ID（{@code ibms_device.id}），如果激活未完成返回 null
     */
    Long getActivationResult(String activationId);
    
    /**
     * 获取激活状态详情（包含错误信息）
     * 
     * @param activationId 激活请求ID
     * @return 激活状态详情，包含 status、deviceId（成功时）、errorMessage（失败时）
     */
    Map<String, Object> getActivationStatusDetail(String activationId);
    
    /**
     * 断开设备连接
     * 
     * @param deviceId 设备ID
     */
    void disconnectDevice(Long deviceId);
}

















