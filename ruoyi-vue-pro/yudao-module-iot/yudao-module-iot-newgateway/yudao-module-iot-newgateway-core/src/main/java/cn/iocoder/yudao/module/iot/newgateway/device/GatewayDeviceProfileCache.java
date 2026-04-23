package cn.iocoder.yudao.module.iot.newgateway.device;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 网关侧设备连接参数缓存（由 Biz 经 {@code DEVICE_PROFILE_CHANGED} 预热/更新）。
 */
@Component
public class GatewayDeviceProfileCache {

    private final ConcurrentHashMap<Long, IotDeviceRespDTO> byDeviceId = new ConcurrentHashMap<>();

    public void upsert(IotDeviceRespDTO dto) {
        if (dto != null && dto.getId() != null) {
            byDeviceId.put(dto.getId(), dto);
        }
    }

    public void remove(Long deviceId) {
        if (deviceId != null) {
            byDeviceId.remove(deviceId);
        }
    }

    public IotDeviceRespDTO get(Long deviceId) {
        return deviceId == null ? null : byDeviceId.get(deviceId);
    }

    public List<IotDeviceRespDTO> listAll() {
        return new ArrayList<>(byDeviceId.values());
    }

    public List<IotDeviceRespDTO> listByTenant(Long tenantId) {
        if (tenantId == null) {
            return listAll();
        }
        List<IotDeviceRespDTO> list = new ArrayList<>();
        for (IotDeviceRespDTO d : byDeviceId.values()) {
            if (tenantId.equals(d.getTenantId())) {
                list.add(d);
            }
        }
        return list;
    }

    public int size() {
        return byDeviceId.size();
    }
}
