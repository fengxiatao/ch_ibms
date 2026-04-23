package cn.iocoder.yudao.module.iot.newgateway.core.session;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备会话注册表（插件在登录成功/断线时更新，供监控与后续统一门面扩展）。
 */
@Component
public class DeviceSessionRegistry {

    private final ConcurrentHashMap<Long, DeviceSession> sessions = new ConcurrentHashMap<>();

    public void put(DeviceSession session) {
        if (session != null && session.getDeviceId() != null) {
            sessions.put(session.getDeviceId(), session);
        }
    }

    public void remove(Long deviceId) {
        if (deviceId != null) {
            sessions.remove(deviceId);
        }
    }

    public Optional<DeviceSession> get(Long deviceId) {
        return Optional.ofNullable(deviceId == null ? null : sessions.get(deviceId));
    }

    public int size() {
        return sessions.size();
    }
}
