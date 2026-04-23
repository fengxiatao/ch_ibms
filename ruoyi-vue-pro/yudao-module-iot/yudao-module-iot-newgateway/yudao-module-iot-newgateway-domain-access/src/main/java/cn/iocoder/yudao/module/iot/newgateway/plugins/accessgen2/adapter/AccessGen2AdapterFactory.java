package cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.adapter;

import cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceConnectionInfo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 门禁二代适配器工厂：按设备信息选择厂商实现（与 {@code IpcAdapterFactory} 同级）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccessGen2AdapterFactory {

    private static final String LOG_PREFIX = "[AccessGen2AdapterFactory]";

    private final List<AccessGen2VendorAdapter> adapters;

    private final Map<String, AccessGen2VendorAdapter> adapterCache = new ConcurrentHashMap<>();

    private final Map<Long, AccessGen2VendorAdapter> deviceAdapterMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("{} 初始化，共 {} 个门禁二代厂商适配器", LOG_PREFIX, adapters.size());
        adapters.sort(Comparator.comparingInt(AccessGen2VendorAdapter::getPriority));
        for (AccessGen2VendorAdapter adapter : adapters) {
            adapterCache.put(adapter.getVendorCode(), adapter);
            log.info("{} 注册: {} ({}) priority={}",
                    LOG_PREFIX, adapter.getVendorCode(), adapter.getVendorName(), adapter.getPriority());
        }
    }

    /**
     * 从 {@link DeviceConnectionInfo} 构建用于 {@link #getAdapter(Map)} 的设备信息 Map（扁平化 config）。
     */
    public static Map<String, Object> buildDeviceInfo(DeviceConnectionInfo ci) {
        Map<String, Object> m = new HashMap<>();
        if (ci == null) {
            return m;
        }
        if (ci.getDeviceId() != null) {
            m.put("deviceId", ci.getDeviceId());
        }
        if (ci.getVendor() != null) {
            m.put("vendor", ci.getVendor());
        }
        if (ci.getDeviceType() != null) {
            m.put("deviceType", ci.getDeviceType());
        }
        if (ci.getProductId() != null) {
            m.put("productId", ci.getProductId());
        }
        if (ci.getConfig() != null) {
            for (Map.Entry<String, Object> e : ci.getConfig().entrySet()) {
                m.put(e.getKey(), e.getValue());
            }
        }
        return m;
    }

    public AccessGen2VendorAdapter getAdapter(Map<String, Object> deviceInfo) {
        if (deviceInfo == null) {
            return getDefaultAdapter();
        }
        Long deviceId = null;
        Object idObj = deviceInfo.get("deviceId");
        if (idObj instanceof Long) {
            deviceId = (Long) idObj;
        } else if (idObj instanceof Number) {
            deviceId = ((Number) idObj).longValue();
        }
        if (deviceId != null && deviceAdapterMap.containsKey(deviceId)) {
            return deviceAdapterMap.get(deviceId);
        }
        for (AccessGen2VendorAdapter adapter : adapters) {
            if (adapter.supports(deviceInfo)) {
                log.debug("{} 选择适配器: deviceInfo keys -> {}", LOG_PREFIX, deviceInfo.keySet());
                if (deviceId != null) {
                    deviceAdapterMap.put(deviceId, adapter);
                }
                return adapter;
            }
        }
        return getDefaultAdapter();
    }

    public AccessGen2VendorAdapter getAdapterByVendor(String vendorCode) {
        if (vendorCode == null || vendorCode.isEmpty()) {
            return getDefaultAdapter();
        }
        AccessGen2VendorAdapter adapter = adapterCache.get(vendorCode.toUpperCase());
        if (adapter != null) {
            return adapter;
        }
        for (AccessGen2VendorAdapter a : adapters) {
            if (a.getVendorCode().toUpperCase().contains(vendorCode.toUpperCase())
                    || a.getVendorName().contains(vendorCode)) {
                return a;
            }
        }
        return getDefaultAdapter();
    }

    public AccessGen2VendorAdapter getAdapterByDeviceId(Long deviceId) {
        return deviceAdapterMap.get(deviceId);
    }

    public void bindAdapter(Long deviceId, String vendorCode) {
        AccessGen2VendorAdapter adapter = getAdapterByVendor(vendorCode);
        if (adapter != null && deviceId != null) {
            deviceAdapterMap.put(deviceId, adapter);
            log.info("{} 绑定设备适配器: deviceId={} -> {}", LOG_PREFIX, deviceId, adapter.getVendorCode());
        }
    }

    public void unbindAdapter(Long deviceId) {
        if (deviceId != null) {
            deviceAdapterMap.remove(deviceId);
        }
    }

    public AccessGen2VendorAdapter getDefaultAdapter() {
        AccessGen2VendorAdapter adapter = adapterCache.get(
                cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.AccessGen2SdkWrapper.VENDOR_CODE);
        if (adapter != null) {
            return adapter;
        }
        if (!adapters.isEmpty()) {
            return adapters.get(adapters.size() - 1);
        }
        throw new IllegalStateException("没有可用的门禁二代适配器");
    }

    public List<AccessGen2VendorAdapter> getAllAdapters() {
        return Collections.unmodifiableList(adapters);
    }

    public List<String> getSupportedVendors() {
        List<String> vendors = new ArrayList<>();
        for (AccessGen2VendorAdapter adapter : adapters) {
            vendors.add(adapter.getVendorCode());
        }
        return vendors;
    }
}
