package cn.iocoder.yudao.module.iot.service.device.discovery.listener;

import cn.iocoder.yudao.module.iot.core.discovery.DeviceDiscoveredEvent;
import cn.iocoder.yudao.module.iot.core.discovery.DiscoveredDevice;
import cn.iocoder.yudao.module.iot.service.device.discovery.dto.DiscoveredDeviceDTO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.discovery.DiscoveredDeviceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 设备发现事件监听器
 * 
 * @author 长辉信息科技有限公司
 */
@Component
@Slf4j
public class DiscoveryEventListener {
    
    @Autowired(required = false)
    private DiscoveredDeviceService discoveredDeviceService;
    
    @Resource
    private IotDeviceService deviceService;
    
    /**
     * 监听设备发现事件
     */
    @EventListener
    @Async
    public void onDeviceDiscovered(DeviceDiscoveredEvent event) {
        DiscoveredDevice coreDevice = event.getDevice();
        
        log.info("[onDeviceDiscovered][收到设备发现事件: {} ({})]", 
            coreDevice.getIpAddress(), coreDevice.getVendor());
        
        try {
            // 转换为 DTO
            DiscoveredDeviceDTO device = convertToDTO(coreDevice);
            
            // 1. 检查是否已添加到平台
            boolean exists = deviceService.isDeviceExistsByIp(device.getIpAddress());
            
            // 2. 保存发现记录
            if (discoveredDeviceService != null) {
                discoveredDeviceService.saveDiscoveredDevice(device, exists);
            }
            
            // 3. 如果是新设备，发送通知
            if (!exists) {
                notifyNewDevice(device);
            }
            
        } catch (Exception e) {
            log.error("[onDeviceDiscovered][处理设备发现事件失败: {}]", coreDevice.getIpAddress(), e);
        }
    }
    
    /**
     * 转换 core 模型到 DTO
     */
    private DiscoveredDeviceDTO convertToDTO(DiscoveredDevice device) {
        return DiscoveredDeviceDTO.builder()
            .ipAddress(device.getIpAddress())
            .mac(device.getMac())
            .vendor(device.getVendor())
            .model(device.getModel())
            .serialNumber(device.getSerialNumber())
            .deviceName(device.getDeviceName())
            .deviceType(device.getDeviceType())
            .firmwareVersion(device.getFirmwareVersion())
            .httpPort(device.getHttpPort())
            .rtspPort(device.getRtspPort())
            .onvifPort(device.getOnvifPort())
            .onvifSupported(device.getOnvifSupported())
            .discoveryMethod(device.getDiscoveryMethod())
            .discoveryTime(device.getDiscoveryTime())
            .build();
    }
    
    /**
     * 通知新设备发现
     */
    private void notifyNewDevice(DiscoveredDeviceDTO device) {
        // TODO: 实现通知逻辑
        // 1. WebSocket推送给前端
        // 2. 发送系统通知
        // 3. 发送邮件/短信（可选）
        
        log.info("[notifyNewDevice][发现新设备: {} ({})]", 
            device.getIpAddress(), device.getVendor());
    }
}

