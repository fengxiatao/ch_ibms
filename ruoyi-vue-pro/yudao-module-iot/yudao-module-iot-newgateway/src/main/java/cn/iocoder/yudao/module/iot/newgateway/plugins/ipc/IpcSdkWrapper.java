package cn.iocoder.yudao.module.iot.newgateway.plugins.ipc;

import cn.iocoder.yudao.module.iot.newgateway.plugins.ipc.adapter.IpcAdapterFactory;
import cn.iocoder.yudao.module.iot.newgateway.plugins.ipc.adapter.IpcVendorAdapter;
import cn.iocoder.yudao.module.iot.newgateway.plugins.ipc.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IPC 设备 SDK 封装（多品牌版本）
 * 
 * <p>通过适配器工厂支持多个品牌的 IPC 设备：</p>
 * <ul>
 *     <li>大华（Dahua）- 使用 NetSDK</li>
 *     <li>海康威视（Hikvision）- 使用 HCNetSDK</li>
 *     <li>ONVIF - 通用协议</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 * @see IpcPlugin
 * @see IpcAdapterFactory
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "ipc", havingValue = "true", matchIfMissing = false)
public class IpcSdkWrapper {

    private static final String LOG_PREFIX = "[IpcSdkWrapper]";

    private final IpcAdapterFactory adapterFactory;

    /** 设备登录句柄与适配器的映射 */
    private final Map<Long, IpcVendorAdapter> handleAdapterMap = new ConcurrentHashMap<>();
    
    /** 设备ID与登录句柄的映射 */
    private final Map<Long, Long> deviceHandleMap = new ConcurrentHashMap<>();

    /** 断线监听器列表 */
    private final List<DisconnectListener> disconnectListeners = new ArrayList<>();
    
    /**
     * 断线监听器接口
     */
    public interface DisconnectListener {
        void onDisconnect(long loginHandle, String ip, int port);
    }
    
    /**
     * 注册断线监听器
     */
    public void addDisconnectListener(DisconnectListener listener) {
        if (listener != null && !disconnectListeners.contains(listener)) {
            disconnectListeners.add(listener);
            log.info("{} 注册断线监听器: {}", LOG_PREFIX, listener.getClass().getSimpleName());
        }
    }
    
    /**
     * 移除断线监听器
     */
    public void removeDisconnectListener(DisconnectListener listener) {
        if (listener != null) {
            disconnectListeners.remove(listener);
        }
    }

    // ==================== 生命周期方法 ====================

    @PostConstruct
    public void initialize() {
        log.info("{} 初始化多品牌 IPC SDK 管理器...", LOG_PREFIX);
        
        // 设置统一的断线回调
        adapterFactory.setDisconnectCallbackForAll((loginHandle, ip, port) -> {
            log.warn("{} 设备断线: handle={}, ip={}:{}", LOG_PREFIX, loginHandle, ip, port);
            
            // 通知所有监听器
            for (DisconnectListener listener : disconnectListeners) {
                try {
                    listener.onDisconnect(loginHandle, ip, port);
                } catch (Exception e) {
                    log.error("{} 断线监听器处理异常: {}", LOG_PREFIX, e.getMessage(), e);
                }
            }
        });
        
        // 初始化所有适配器
        adapterFactory.initializeAllAdapters();
        
        log.info("{} ✅ 多品牌 IPC SDK 管理器初始化完成，支持厂商: {}", 
                LOG_PREFIX, adapterFactory.getSupportedVendors());
    }

    @PreDestroy
    public void cleanup() {
        log.info("{} 正在清理所有 SDK 资源...", LOG_PREFIX);
        
        // 清理所有适配器
        adapterFactory.cleanupAllAdapters();
        
        handleAdapterMap.clear();
        deviceHandleMap.clear();
        
        log.info("{} SDK 资源已清理", LOG_PREFIX);
    }

    public boolean isInitialized() {
        // 至少有一个适配器已初始化
        return adapterFactory.getAllAdapters().stream()
                .anyMatch(IpcVendorAdapter::isInitialized);
    }

    // ==================== 设备登录/登出 ====================

    /**
     * 登录设备（自动选择适配器）
     *
     * @param deviceId   设备ID
     * @param deviceInfo 设备信息（用于选择适配器）
     * @param ip         设备IP
     * @param port       设备端口
     * @param username   用户名
     * @param password   密码
     * @return 登录结果
     */
    public IpcLoginResult login(Long deviceId, Map<String, Object> deviceInfo, 
                                 String ip, int port, String username, String password) {
        // 选择适配器
        IpcVendorAdapter adapter = adapterFactory.getAdapter(deviceInfo);
        
        log.info("{} 使用 {} 适配器登录设备: deviceId={}, ip={}", 
                LOG_PREFIX, adapter.getVendorCode(), deviceId, ip);
        
        // 确保适配器已初始化
        if (!adapter.isInitialized()) {
            boolean initSuccess = adapter.initialize();
            if (!initSuccess) {
                return IpcLoginResult.failure(adapter.getVendorCode() + " SDK 初始化失败");
            }
        }
        
        // 执行登录
        IpcLoginResult result = adapter.login(ip, port, username, password);
        
        if (result.isSuccess()) {
            long loginHandle = result.getLoginHandle();
            
            // 记录映射关系
            handleAdapterMap.put(loginHandle, adapter);
            if (deviceId != null) {
                deviceHandleMap.put(deviceId, loginHandle);
                adapterFactory.bindAdapter(deviceId, adapter.getVendorCode());
            }
            
            log.info("{} ✅ 登录成功: deviceId={}, handle={}, vendor={}", 
                    LOG_PREFIX, deviceId, loginHandle, adapter.getVendorCode());
        }
        
        return result;
    }

    /**
     * 登录设备（使用指定厂商）
     */
    public IpcLoginResult loginWithVendor(Long deviceId, String vendorCode,
                                           String ip, int port, String username, String password) {
        IpcVendorAdapter adapter = adapterFactory.getAdapterByVendor(vendorCode);
        
        log.info("{} 使用指定适配器 {} 登录设备: deviceId={}, ip={}", 
                LOG_PREFIX, adapter.getVendorCode(), deviceId, ip);
        
        // 确保适配器已初始化
        if (!adapter.isInitialized()) {
            boolean initSuccess = adapter.initialize();
            if (!initSuccess) {
                return IpcLoginResult.failure(adapter.getVendorCode() + " SDK 初始化失败");
            }
        }
        
        // 执行登录
        IpcLoginResult result = adapter.login(ip, port, username, password);
        
        if (result.isSuccess()) {
            long loginHandle = result.getLoginHandle();
            
            // 记录映射关系
            handleAdapterMap.put(loginHandle, adapter);
            if (deviceId != null) {
                deviceHandleMap.put(deviceId, loginHandle);
                adapterFactory.bindAdapter(deviceId, adapter.getVendorCode());
            }
        }
        
        return result;
    }

    /**
     * 登出设备
     */
    public boolean logout(long loginHandle) {
        IpcVendorAdapter adapter = handleAdapterMap.get(loginHandle);
        if (adapter == null) {
            log.warn("{} 未找到登录句柄对应的适配器: handle={}", LOG_PREFIX, loginHandle);
            return false;
        }
        
        boolean result = adapter.logout(loginHandle);
        
        // 清理映射
        handleAdapterMap.remove(loginHandle);
        deviceHandleMap.entrySet().removeIf(entry -> entry.getValue().equals(loginHandle));
        
        return result;
    }

    /**
     * 根据设备ID登出
     */
    public boolean logoutByDeviceId(Long deviceId) {
        Long loginHandle = deviceHandleMap.get(deviceId);
        if (loginHandle == null) {
            log.warn("{} 设备未登录: deviceId={}", LOG_PREFIX, deviceId);
            return false;
        }
        
        boolean result = logout(loginHandle);
        adapterFactory.unbindAdapter(deviceId);
        
        return result;
    }

    // ==================== PTZ 控制 ====================

    public IpcOperationResult ptzControl(long loginHandle, int channelNo, String ptzCommand, int speed) {
        IpcVendorAdapter adapter = handleAdapterMap.get(loginHandle);
        if (adapter == null) {
            return IpcOperationResult.failure("设备未登录或适配器未找到");
        }
        
        return adapter.ptzControl(loginHandle, channelNo, ptzCommand, speed);
    }

    public IpcOperationResult ptzControlByDeviceId(Long deviceId, int channelNo, String ptzCommand, int speed) {
        Long loginHandle = deviceHandleMap.get(deviceId);
        if (loginHandle == null) {
            return IpcOperationResult.failure("设备未登录");
        }
        
        return ptzControl(loginHandle, channelNo, ptzCommand, speed);
    }

    // ==================== 抓图 ====================

    public IpcOperationResult capturePicture(long loginHandle, int channelNo) {
        IpcVendorAdapter adapter = handleAdapterMap.get(loginHandle);
        if (adapter == null) {
            return IpcOperationResult.failure("设备未登录或适配器未找到");
        }
        
        return adapter.capturePicture(loginHandle, channelNo);
    }

    public IpcOperationResult capturePictureByDeviceId(Long deviceId, int channelNo) {
        Long loginHandle = deviceHandleMap.get(deviceId);
        if (loginHandle == null) {
            return IpcOperationResult.failure("设备未登录");
        }
        
        return capturePicture(loginHandle, channelNo);
    }

    // ==================== RTSP 地址构建 ====================

    /**
     * 构建 RTSP 流地址
     */
    public String buildRtspUrl(Long deviceId, String ip, int port, 
                                String username, String password, int channelNo, int subtype) {
        IpcVendorAdapter adapter = adapterFactory.getAdapterByDeviceId(deviceId);
        if (adapter == null) {
            // 使用默认适配器
            adapter = adapterFactory.getDefaultAdapter();
        }
        
        return adapter.buildRtspUrl(ip, port, username, password, channelNo, subtype);
    }

    /**
     * 根据厂商代码构建 RTSP 地址
     */
    public String buildRtspUrlByVendor(String vendorCode, String ip, int port, 
                                        String username, String password, int channelNo, int subtype) {
        IpcVendorAdapter adapter = adapterFactory.getAdapterByVendor(vendorCode);
        return adapter.buildRtspUrl(ip, port, username, password, channelNo, subtype);
    }

    // ==================== 设备信息查询 ====================

    public IpcOperationResult queryDeviceInfo(long loginHandle) {
        IpcVendorAdapter adapter = handleAdapterMap.get(loginHandle);
        if (adapter == null) {
            return IpcOperationResult.failure("设备未登录或适配器未找到");
        }
        
        return adapter.queryDeviceInfo(loginHandle);
    }

    public IpcOperationResult queryCapabilities(long loginHandle) {
        IpcVendorAdapter adapter = handleAdapterMap.get(loginHandle);
        if (adapter == null) {
            return IpcOperationResult.failure("设备未登录或适配器未找到");
        }
        
        return adapter.queryCapabilities(loginHandle);
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取设备登录句柄
     */
    public Long getLoginHandle(Long deviceId) {
        return deviceHandleMap.get(deviceId);
    }

    /**
     * 检查设备是否已登录
     */
    public boolean isDeviceLoggedIn(Long deviceId) {
        return deviceHandleMap.containsKey(deviceId);
    }

    /**
     * 获取设备使用的适配器
     */
    public IpcVendorAdapter getDeviceAdapter(Long deviceId) {
        return adapterFactory.getAdapterByDeviceId(deviceId);
    }

    /**
     * 获取所有支持的厂商
     */
    public List<String> getSupportedVendors() {
        return adapterFactory.getSupportedVendors();
    }
}
