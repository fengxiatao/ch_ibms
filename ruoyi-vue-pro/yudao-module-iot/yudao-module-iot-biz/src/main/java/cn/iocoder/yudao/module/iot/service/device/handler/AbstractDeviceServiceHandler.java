package cn.iocoder.yudao.module.iot.service.device.handler;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * IoT 设备服务处理器抽象基类
 * 
 * 提供通用的工具方法和默认实现
 * 
 * @author 长辉信息科技有限公司
 */
@Slf4j
public abstract class AbstractDeviceServiceHandler implements IotDeviceServiceHandler {
    
    // ✅ ZLMediaKit 已迁移到 Gateway 层，通过物模型调用
    // 参考：docs/ZLMediaKit架构迁移说明.md
    
    /**
     * 验证必需参数
     * 
     * @param params 参数Map
     * @param requiredParams 必需参数列表
     */
    protected void validateRequiredParams(Map<String, Object> params, String... requiredParams) {
        for (String param : requiredParams) {
            if (!params.containsKey(param) || params.get(param) == null) {
                throw new ServiceException(400, "缺少必需参数: " + param);
            }
        }
    }
    
    /**
     * 获取参数值
     * 
     * @param params 参数Map
     * @param key 参数键
     * @param defaultValue 默认值
     * @return 参数值
     */
    protected <T> T getParam(Map<String, Object> params, String key, T defaultValue) {
        Object value = params.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return (T) value;
        } catch (ClassCastException e) {
            log.warn("[getParam][参数类型转换失败: key={}, value={}]", key, value);
            return defaultValue;
        }
    }
    
    /**
     * 构建 RTSP 地址
     * 
     * @param deviceIp 设备IP
     * @param username 用户名
     * @param password 密码
     * @param rtspPath RTSP路径
     * @return RTSP地址
     */
    protected String buildRtspUrl(String deviceIp, String username, String password, String rtspPath) {
        if (StrUtil.isBlank(deviceIp) || StrUtil.isBlank(rtspPath)) {
            throw new ServiceException(400, "设备IP或RTSP路径不能为空");
        }
        
        if (StrUtil.isNotBlank(username) && StrUtil.isNotBlank(password)) {
            return String.format("rtsp://%s:%s@%s:554%s", username, password, deviceIp, rtspPath);
        } else {
            return String.format("rtsp://%s:554%s", deviceIp, rtspPath);
        }
    }
    
    /**
     * 记录服务执行日志
     * 
     * @param deviceName 设备名称
     * @param action 动作
     * @param message 消息
     */
    protected void logServiceExecution(String deviceName, String action, String message) {
        log.info("[{}][设备: {}, 动作: {}, 消息: {}]", 
            getServiceIdentifier(), deviceName, action, message);
    }
    
    /**
     * 记录服务执行错误
     * 
     * @param deviceName 设备名称
     * @param action 动作
     * @param e 异常
     */
    protected void logServiceError(String deviceName, String action, Exception e) {
        log.error("[{}][设备: {}, 动作: {}, 错误: {}]", 
            getServiceIdentifier(), deviceName, action, e.getMessage(), e);
    }
}

