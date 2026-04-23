package cn.iocoder.yudao.module.iot.newgateway.plugins.ipc.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IPC 适配器工厂
 * 
 * <p>根据设备信息自动选择合适的厂商适配器：</p>
 * <ul>
 *     <li>根据 vendor、productKey、deviceModel 等字段判断品牌</li>
 *     <li>按优先级排序，优先使用专用适配器</li>
 *     <li>ONVIF 作为兜底方案</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IpcAdapterFactory {

    private static final String LOG_PREFIX = "[IpcAdapterFactory]";

    /** 所有已注册的适配器 */
    private final List<IpcVendorAdapter> adapters;
    
    /** 适配器缓存（厂商代码 -> 适配器） */
    private final Map<String, IpcVendorAdapter> adapterCache = new ConcurrentHashMap<>();
    
    /** 设备适配器映射缓存（设备ID -> 适配器） */
    private final Map<Long, IpcVendorAdapter> deviceAdapterMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("{} 初始化适配器工厂，发现 {} 个适配器", LOG_PREFIX, adapters.size());
        
        // 按优先级排序
        adapters.sort(Comparator.comparingInt(IpcVendorAdapter::getPriority));
        
        // 注册到缓存
        for (IpcVendorAdapter adapter : adapters) {
            adapterCache.put(adapter.getVendorCode(), adapter);
            log.info("{} 注册适配器: {} ({}) - 优先级: {}", 
                    LOG_PREFIX, adapter.getVendorCode(), adapter.getVendorName(), adapter.getPriority());
        }
    }

    /**
     * 根据设备信息选择适配器
     * 
     * @param deviceInfo 设备信息（包含 vendor、productKey、deviceModel 等）
     * @return 适配器实例
     */
    public IpcVendorAdapter getAdapter(Map<String, Object> deviceInfo) {
        if (deviceInfo == null) {
            return getDefaultAdapter();
        }
        
        // 尝试从设备信息中获取设备ID，用于缓存
        Long deviceId = null;
        Object idObj = deviceInfo.get("deviceId");
        if (idObj instanceof Long) {
            deviceId = (Long) idObj;
        } else if (idObj instanceof Number) {
            deviceId = ((Number) idObj).longValue();
        }
        
        // 检查设备缓存
        if (deviceId != null && deviceAdapterMap.containsKey(deviceId)) {
            return deviceAdapterMap.get(deviceId);
        }
        
        // 按优先级遍历，找到第一个支持的适配器
        for (IpcVendorAdapter adapter : adapters) {
            if (adapter.supports(deviceInfo)) {
                log.debug("{} 为设备选择适配器: {} -> {}", LOG_PREFIX, deviceInfo, adapter.getVendorCode());
                
                // 缓存设备与适配器的映射
                if (deviceId != null) {
                    deviceAdapterMap.put(deviceId, adapter);
                }
                
                return adapter;
            }
        }
        
        // 使用默认适配器
        return getDefaultAdapter();
    }

    /**
     * 根据厂商代码获取适配器
     * 
     * @param vendorCode 厂商代码（如 "DAHUA", "HIKVISION", "ONVIF"）
     * @return 适配器实例
     */
    public IpcVendorAdapter getAdapterByVendor(String vendorCode) {
        if (vendorCode == null || vendorCode.isEmpty()) {
            return getDefaultAdapter();
        }
        
        IpcVendorAdapter adapter = adapterCache.get(vendorCode.toUpperCase());
        if (adapter != null) {
            return adapter;
        }
        
        // 尝试模糊匹配
        for (IpcVendorAdapter a : adapters) {
            if (a.getVendorCode().toUpperCase().contains(vendorCode.toUpperCase()) ||
                a.getVendorName().contains(vendorCode)) {
                return a;
            }
        }
        
        return getDefaultAdapter();
    }

    /**
     * 根据设备ID获取已缓存的适配器
     * 
     * @param deviceId 设备ID
     * @return 适配器实例，如果未缓存则返回 null
     */
    public IpcVendorAdapter getAdapterByDeviceId(Long deviceId) {
        return deviceAdapterMap.get(deviceId);
    }

    /**
     * 为设备绑定适配器
     * 
     * @param deviceId   设备ID
     * @param vendorCode 厂商代码
     */
    public void bindAdapter(Long deviceId, String vendorCode) {
        IpcVendorAdapter adapter = getAdapterByVendor(vendorCode);
        if (adapter != null && deviceId != null) {
            deviceAdapterMap.put(deviceId, adapter);
            log.info("{} 绑定设备适配器: deviceId={} -> {}", LOG_PREFIX, deviceId, adapter.getVendorCode());
        }
    }

    /**
     * 解绑设备适配器
     * 
     * @param deviceId 设备ID
     */
    public void unbindAdapter(Long deviceId) {
        if (deviceId != null) {
            deviceAdapterMap.remove(deviceId);
        }
    }

    /**
     * 获取默认适配器（ONVIF）
     * 
     * @return ONVIF 适配器
     */
    public IpcVendorAdapter getDefaultAdapter() {
        IpcVendorAdapter adapter = adapterCache.get(OnvifIpcAdapter.VENDOR_CODE);
        if (adapter != null) {
            return adapter;
        }
        
        // 返回优先级最低的适配器
        if (!adapters.isEmpty()) {
            return adapters.get(adapters.size() - 1);
        }
        
        throw new IllegalStateException("没有可用的 IPC 适配器");
    }

    /**
     * 获取所有已注册的适配器
     * 
     * @return 适配器列表
     */
    public List<IpcVendorAdapter> getAllAdapters() {
        return Collections.unmodifiableList(adapters);
    }

    /**
     * 获取所有支持的厂商代码
     * 
     * @return 厂商代码列表
     */
    public List<String> getSupportedVendors() {
        List<String> vendors = new ArrayList<>();
        for (IpcVendorAdapter adapter : adapters) {
            vendors.add(adapter.getVendorCode());
        }
        return vendors;
    }

    /**
     * 初始化所有适配器的 SDK
     */
    public void initializeAllAdapters() {
        log.info("{} 初始化所有适配器 SDK...", LOG_PREFIX);
        
        for (IpcVendorAdapter adapter : adapters) {
            try {
                if (!adapter.isInitialized()) {
                    boolean success = adapter.initialize();
                    if (success) {
                        log.info("{} ✅ {} SDK 初始化成功", LOG_PREFIX, adapter.getVendorCode());
                    } else {
                        log.warn("{} ⚠️ {} SDK 初始化失败", LOG_PREFIX, adapter.getVendorCode());
                    }
                }
            } catch (Exception e) {
                log.error("{} {} SDK 初始化异常: {}", LOG_PREFIX, adapter.getVendorCode(), e.getMessage(), e);
            }
        }
    }

    /**
     * 清理所有适配器的 SDK 资源
     */
    public void cleanupAllAdapters() {
        log.info("{} 清理所有适配器 SDK 资源...", LOG_PREFIX);
        
        for (IpcVendorAdapter adapter : adapters) {
            try {
                if (adapter.isInitialized()) {
                    adapter.cleanup();
                    log.info("{} {} SDK 资源已清理", LOG_PREFIX, adapter.getVendorCode());
                }
            } catch (Exception e) {
                log.error("{} {} SDK 清理异常: {}", LOG_PREFIX, adapter.getVendorCode(), e.getMessage(), e);
            }
        }
        
        deviceAdapterMap.clear();
    }

    /**
     * 设置所有适配器的断线回调
     * 
     * @param callback 断线回调
     */
    public void setDisconnectCallbackForAll(IpcVendorAdapter.DisconnectCallback callback) {
        for (IpcVendorAdapter adapter : adapters) {
            adapter.setDisconnectCallback(callback);
        }
    }
}
