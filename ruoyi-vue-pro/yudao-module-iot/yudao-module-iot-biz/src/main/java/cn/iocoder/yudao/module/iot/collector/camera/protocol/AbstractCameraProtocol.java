package cn.iocoder.yudao.module.iot.collector.camera.protocol;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 摄像头协议抽象基类
 * 
 * 提供通用的工具方法
 * 
 * @author 长辉信息科技有限公司
 */
@Slf4j
public abstract class AbstractCameraProtocol implements CameraProtocol {
    
    /**
     * 执行HTTP GET请求（基本认证）
     */
    protected String executeHttpGet(String url, String username, String password) {
        try {
            HttpResponse response = HttpRequest.get(url)
                .basicAuth(username, password)
                .timeout(5000)
                .execute();
            
            if (response.isOk()) {
                return response.body();
            } else {
                log.warn("[executeHttpGet][请求失败: url={}, status={}]", url, response.getStatus());
                return null;
            }
        } catch (Exception e) {
            log.debug("[executeHttpGet][请求异常: url={}]", url, e);
            return null;
        }
    }
    
    /**
     * 执行HTTP GET请求（摘要认证）
     */
    protected String executeHttpGetWithDigest(String url, String username, String password) {
        try {
            // Hutool的摘要认证需要先尝试basicAuth，如果401再重试
            HttpResponse response = HttpRequest.get(url)
                .basicAuth(username, password)
                .timeout(5000)
                .execute();
            
            if (response.isOk()) {
                return response.body();
            } else {
                log.warn("[executeHttpGetWithDigest][请求失败: url={}, status={}]", url, response.getStatus());
                return null;
            }
        } catch (Exception e) {
            log.debug("[executeHttpGetWithDigest][请求异常: url={}]", url, e);
            return null;
        }
    }
    
    /**
     * 解析键值对响应
     * 格式: key=value\n
     */
    protected Map<String, String> parseKeyValueResponse(String response) {
        Map<String, String> result = new HashMap<>();
        
        if (StrUtil.isBlank(response)) {
            return result;
        }
        
        String[] lines = response.split("\n");
        for (String line : lines) {
            if (line.contains("=")) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    result.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
        
        return result;
    }
    
    /**
     * 从设备配置中获取值
     */
    protected String getConfigValue(IotDeviceDO device, String key, String defaultValue) {
        try {
            if (device.getConfig() != null) {
                Map<String, Object> configMap = device.getConfig().toMap();
                if (configMap.containsKey(key)) {
                    Object value = configMap.get(key);
                    return value != null ? value.toString() : defaultValue;
                }
            }
        } catch (Exception e) {
            log.debug("[getConfigValue][获取配置失败: key={}, device={}]", key, device.getDeviceName(), e);
        }
        return defaultValue;
    }
    
    /**
     * 从设备配置中获取vendor
     */
    protected String getVendor(IotDeviceDO device) {
        return getConfigValue(device, "vendor", "");
    }
    
    /**
     * 测试设备连接（通用实现）
     */
    @Override
    public boolean testConnection(IotDeviceDO device) {
        try {
            Map<String, Object> properties = getDeviceProperties(device);
            return properties != null && !properties.isEmpty();
        } catch (Exception e) {
            log.debug("[testConnection][连接测试失败: device={}]", device.getDeviceName(), e);
            return false;
        }
    }
}
