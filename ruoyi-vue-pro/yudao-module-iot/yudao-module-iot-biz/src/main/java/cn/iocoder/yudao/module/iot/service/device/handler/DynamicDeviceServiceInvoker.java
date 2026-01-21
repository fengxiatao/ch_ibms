package cn.iocoder.yudao.module.iot.service.device.handler;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * IoT 设备服务动态调用器
 * 
 * 负责动态调用设备服务处理器
 * 
 * @author 长辉信息科技有限公司
 */
@Service
@Slf4j
public class DynamicDeviceServiceInvoker {
    
    @Resource
    private DeviceServiceRegistry serviceRegistry;
    
    /**
     * 处理设备服务调用
     * 
     * @param message 设备消息
     * @param device 设备信息
     * @return 服务执行结果
     */
    public Object handleDeviceService(IotDeviceMessage message, IotDeviceDO device) {
        String method = message.getMethod();
        Map<String, Object> params = (Map<String, Object>) message.getParams();
        String identifier = (String) params.get("identifier");
        
        log.info("[handleDeviceService][设备({}) 服务调用: {}]", device.getDeviceName(), identifier);
        
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 验证服务标识符
            if (StrUtil.isBlank(identifier)) {
                throw new ServiceException(400, "服务标识符不能为空");
            }
            
            // 2. 获取服务处理器
            IotDeviceServiceHandler handler = serviceRegistry.getHandler(identifier, device.getProductKey());
            if (handler == null) {
                throw new ServiceException(404, 
                    String.format("未找到服务处理器: %s (产品: %s)", identifier, device.getProductKey()));
            }
            
            // 3. 执行服务调用
            Object result = handler.handleService(params, device);
            
            // 4. 记录执行时长
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("[handleDeviceService][设备({}) 服务({}) 调用成功，耗时: {}ms]", 
                device.getDeviceName(), identifier, executionTime);
            
            return result;
            
        } catch (ServiceException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("[handleDeviceService][设备({}) 服务({}) 调用失败，耗时: {}ms，错误: {}]", 
                device.getDeviceName(), identifier, executionTime, e.getMessage());
            throw e;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("[handleDeviceService][设备({}) 服务({}) 调用异常，耗时: {}ms]", 
                device.getDeviceName(), identifier, executionTime, e);
            throw new ServiceException(500, "服务调用失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取设备支持的服务列表
     * 
     * @param productKey 产品Key
     * @return 服务标识符列表
     */
    public Map<String, String> getSupportedServices(String productKey) {
        return serviceRegistry.getHandlersByProduct(productKey).stream()
                .collect(java.util.stream.Collectors.toMap(
                    IotDeviceServiceHandler::getServiceIdentifier,
                    IotDeviceServiceHandler::getServiceName
                ));
    }
}

