package cn.iocoder.yudao.module.iot.service.device.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IoT 设备服务注册器
 * 
 * 负责管理所有设备服务处理器的注册和查找
 * 
 * ⚠️ 注意：由于 ZLMediaKit 已迁移到 Gateway 层，大部分设备服务处理器已被删除
 * 这些功能现在通过物模型（RocketMQ）调用 Gateway 层实现
 * 参考：docs/ZLMediaKit架构迁移说明.md
 * 
 * @author 长辉信息科技有限公司
 */
@Component
@Slf4j
public class DeviceServiceRegistry {
    
    /**
     * 服务处理器缓存
     * Key: serviceIdentifier
     * Value: IotDeviceServiceHandler
     */
    private final Map<String, IotDeviceServiceHandler> serviceHandlers = new ConcurrentHashMap<>();
    
    // ✅ 改为可选注入，因为大部分处理器已迁移到 Gateway 层
    @Autowired(required = false)
    private List<IotDeviceServiceHandler> handlerList;
    
    /**
     * 初始化：自动注册所有服务处理器
     */
    @PostConstruct
    public void init() {
        if (handlerList != null && !handlerList.isEmpty()) {
            for (IotDeviceServiceHandler handler : handlerList) {
                registerHandler(handler);
            }
        }
        log.info("[DeviceServiceRegistry][初始化完成，已注册服务数量: {}]", serviceHandlers.size());
    }
    
    /**
     * 注册服务处理器
     * 
     * @param handler 服务处理器
     */
    public void registerHandler(IotDeviceServiceHandler handler) {
        String identifier = handler.getServiceIdentifier();
        if (serviceHandlers.containsKey(identifier)) {
            log.warn("[registerHandler][服务标识符({})已存在，将被覆盖]", identifier);
        }
        serviceHandlers.put(identifier, handler);
        log.info("[registerHandler][注册服务成功: {} - {}]", identifier, handler.getServiceName());
    }
    
    /**
     * 获取服务处理器
     * 
     * @param identifier 服务标识符
     * @param productKey 产品Key
     * @return 服务处理器，如果不存在或不支持该产品则返回null
     */
    public IotDeviceServiceHandler getHandler(String identifier, String productKey) {
        IotDeviceServiceHandler handler = serviceHandlers.get(identifier);
        if (handler != null && handler.supportsDevice(productKey)) {
            return handler;
        }
        return null;
    }
    
    /**
     * 获取所有已注册的服务标识符
     * 
     * @return 服务标识符列表
     */
    public List<String> getAllServiceIdentifiers() {
        return List.copyOf(serviceHandlers.keySet());
    }
    
    /**
     * 获取指定产品支持的所有服务
     * 
     * @param productKey 产品Key
     * @return 服务处理器列表
     */
    public List<IotDeviceServiceHandler> getHandlersByProduct(String productKey) {
        return serviceHandlers.values().stream()
                .filter(handler -> handler.supportsDevice(productKey))
                .toList();
    }
    
    /**
     * 检查服务是否已注册
     * 
     * @param identifier 服务标识符
     * @return true=已注册, false=未注册
     */
    public boolean hasHandler(String identifier) {
        return serviceHandlers.containsKey(identifier);
    }
}

