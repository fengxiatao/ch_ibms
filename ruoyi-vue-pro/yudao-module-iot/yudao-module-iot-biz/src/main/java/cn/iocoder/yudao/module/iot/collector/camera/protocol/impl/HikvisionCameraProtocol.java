package cn.iocoder.yudao.module.iot.collector.camera.protocol.impl;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.collector.camera.protocol.AbstractCameraProtocol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 海康威视摄像头协议实现
 * 
 * @author 长辉信息科技有限公司
 */
@Component
@Slf4j
public class HikvisionCameraProtocol extends AbstractCameraProtocol {
    
    @Override
    public String getProtocolName() {
        return "hikvision";
    }
    
    @Override
    public boolean supports(IotDeviceDO device) {
        String vendor = getVendor(device);
        return "hikvision".equalsIgnoreCase(vendor) || "hik".equalsIgnoreCase(vendor);
    }
    
    @Override
    public Map<String, Object> getDeviceProperties(IotDeviceDO device) {
        Map<String, Object> properties = new HashMap<>();
        
        String ip = cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfigHelper.getIpAddress(device);
        if (StrUtil.isBlank(ip)) {
            log.warn("[getDeviceProperties][设备({}) IP地址为空]", device.getDeviceName());
            return properties;
        }
        
        String username = getConfigValue(device, "username", "admin");
        String password = getConfigValue(device, "password", "admin123");
        
        try {
            // 海康威视使用ISAPI接口
            String url = String.format("http://%s/ISAPI/System/deviceInfo", ip);
            String response = executeHttpGetWithDigest(url, username, password);
            
            if (StrUtil.isNotBlank(response)) {
                properties.put("device_status", 1); // 在线
                
                // TODO: 解析XML响应获取详细信息
                // 海康威视返回的是XML格式，需要解析
                
            } else {
                properties.put("device_status", 0); // 离线
            }
            
            // 设置默认属性
            properties.put("resolution", "1920x1080");
            properties.put("night_vision", true);
            properties.put("audio_enabled", true);
            properties.put("storage_status", "normal");
            properties.put("recording_status", 1);
            properties.put("stream_status", 1);
            properties.put("device_temperature", 45.5f);
            properties.put("cpu_usage", 35.0f);
            properties.put("memory_usage", 60.0f);
            
        } catch (Exception e) {
            log.error("[getDeviceProperties][获取海康摄像头属性失败: device={}]", device.getDeviceName(), e);
            properties.put("device_status", 0);
        }
        
        return properties;
    }
}
