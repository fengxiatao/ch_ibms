package cn.iocoder.yudao.module.iot.collector.camera.protocol.impl;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.collector.camera.protocol.AbstractCameraProtocol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 大华摄像头协议实现
 * 
 * @author 长辉信息科技有限公司
 */
@Component
@Slf4j
public class DahuaCameraProtocol extends AbstractCameraProtocol {
    
    @Override
    public String getProtocolName() {
        return "dahua";
    }
    
    @Override
    public boolean supports(IotDeviceDO device) {
        String vendor = getVendor(device);
        return "dahua".equalsIgnoreCase(vendor);
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
            // 获取系统信息
            String url = String.format("http://%s/cgi-bin/magicBox.cgi?action=getSystemInfo", ip);
            String response = executeHttpGet(url, username, password);
            
            if (StrUtil.isNotBlank(response)) {
                properties.put("device_status", 1); // 在线
                
                // 解析系统信息
                Map<String, String> systemInfo = parseKeyValueResponse(response);
                properties.put("device_type", systemInfo.getOrDefault("deviceType", "Unknown"));
                properties.put("serial_number", systemInfo.getOrDefault("serialNumber", ""));
                properties.put("hardware_version", systemInfo.getOrDefault("hardwareVersion", ""));
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
            log.error("[getDeviceProperties][获取大华摄像头属性失败: device={}]", device.getDeviceName(), e);
            properties.put("device_status", 0);
        }
        
        return properties;
    }
}
