package cn.iocoder.yudao.module.iot.collector.camera;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.collector.camera.protocol.CameraProtocol;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用摄像头数据采集器
 * 
 * 功能：
 * 1. 支持多种品牌摄像头（大华、海康、ONVIF等）
 * 2. 自动识别设备协议
 * 3. 定时轮询设备状态
 * 4. 上报属性到IoT平台
 * 
 * 扩展方式：
 * - 添加新品牌：实现 CameraProtocol 接口
 * - 自动注册：Spring会自动注入所有实现类
 * 
 * @author 长辉信息科技有限公司
 */
@Component
@Slf4j
public class UniversalCameraCollector {
    
    @Resource
    private IotDeviceService deviceService;
    
    @Resource
    private IotDeviceMessageProducer messageProducer;
    
    @Resource
    private List<CameraProtocol> cameraProtocols; // Spring自动注入所有协议实现
    
    /**
     * 协议缓存：deviceId -> CameraProtocol
     */
    private final Map<Long, CameraProtocol> protocolCache = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        log.info("[init][通用摄像头采集器初始化，支持的协议: {}]", 
            cameraProtocols.stream()
                .map(CameraProtocol::getProtocolName)
                .toList());
    }
    
    /**
     * 定时轮询摄像头状态（每30秒）
     * 
     * 支持的产品：
     * - 产品ID=62: 大华摄像头
     * - 产品ID=63: 海康摄像头
     * - 产品ID=64: 大华NVR
     * - 产品ID=65: 海康NVR
     * - 其他摄像头产品...
     */
    @Scheduled(fixedRate = 30000)
    public void pollCameraStatus() {
        // 获取所有摄像头类型的设备
        List<Long> cameraProductIds = List.of(62L, 63L, 64L, 65L); // 可配置化
        
        for (Long productId : cameraProductIds) {
            // 使用 TenantUtils 执行，自动处理租户上下文
            TenantUtils.execute(1L, () -> {
                List<IotDeviceDO> devices = deviceService.getDeviceListByProductId(productId);
                
                if (devices.isEmpty()) {
                    return;
                }
                
                log.info("[pollCameraStatus][开始轮询产品({}) 的 {} 个设备]", productId, devices.size());
                
                for (IotDeviceDO device : devices) {
                    try {
                        pollSingleDevice(device);
                    } catch (Exception e) {
                        log.error("[pollCameraStatus][设备({}) 轮询失败]", device.getDeviceName(), e);
                        // 上报离线状态
                        reportOfflineStatus(device);
                    }
                }
            });
        }
    }
    
    /**
     * 轮询单个设备
     */
    private void pollSingleDevice(IotDeviceDO device) {
        log.debug("[pollSingleDevice][开始轮询设备: {}]", device.getDeviceName());
        
        // 1. 获取或选择协议
        CameraProtocol protocol = getProtocol(device);
        if (protocol == null) {
            log.warn("[pollSingleDevice][设备({}) 没有匹配的协议]", device.getDeviceName());
            reportOfflineStatus(device);
            return;
        }
        
        // 2. 获取设备属性
        Map<String, Object> properties = protocol.getDeviceProperties(device);
        if (properties.isEmpty()) {
            log.warn("[pollSingleDevice][设备({}) 未获取到属性]", device.getDeviceName());
            reportOfflineStatus(device);
            return;
        }
        
        // 3. 构建上报消息
        IotDeviceMessage message = IotDeviceMessage.builder()
            .deviceId(device.getId())
            .tenantId(device.getTenantId())  // 添加租户ID
            .method("thing.property.post")
            .params(properties)
            .reportTime(LocalDateTime.now())
            .build();
        
        // 4. 发送到消息总线
        messageProducer.sendDeviceMessage(message);
        
        log.info("[pollSingleDevice][设备({}) 属性上报成功，协议: {}]", 
            device.getDeviceName(), protocol.getProtocolName());
    }
    
    /**
     * 获取设备对应的协议
     */
    private CameraProtocol getProtocol(IotDeviceDO device) {
        // 1. 从缓存获取
        CameraProtocol cached = protocolCache.get(device.getId());
        if (cached != null && cached.supports(device)) {
            return cached;
        }
        
        // 2. 遍历所有协议，找到第一个支持的
        for (CameraProtocol protocol : cameraProtocols) {
            if (protocol.supports(device)) {
                protocolCache.put(device.getId(), protocol);
                log.info("[getProtocol][设备({}) 使用协议: {}]", 
                    device.getDeviceName(), protocol.getProtocolName());
                return protocol;
            }
        }
        
        log.warn("[getProtocol][设备({}) 没有匹配的协议]", device.getDeviceName());
        return null;
    }
    
    /**
     * 上报离线状态
     */
    private void reportOfflineStatus(IotDeviceDO device) {
        Map<String, Object> properties = Map.of("device_status", 0);
        
        IotDeviceMessage message = IotDeviceMessage.builder()
            .deviceId(device.getId())
            .tenantId(device.getTenantId())  // 添加租户ID
            .method("thing.property.post")
            .params(properties)
            .reportTime(LocalDateTime.now())
            .build();
        
        messageProducer.sendDeviceMessage(message);
        
        log.warn("[reportOfflineStatus][设备({}) 已标记为离线]", device.getDeviceName());
    }
    
    /**
     * 清除协议缓存（用于设备配置变更时）
     */
    public void clearProtocolCache(Long deviceId) {
        protocolCache.remove(deviceId);
        log.info("[clearProtocolCache][已清除设备({}) 的协议缓存]", deviceId);
    }
    
    /**
     * 清除所有协议缓存
     */
    public void clearAllProtocolCache() {
        protocolCache.clear();
        log.info("[clearAllProtocolCache][已清除所有协议缓存]");
    }
}
