package cn.iocoder.yudao.module.iot.service.device.support;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.websocket.IotWebSocketHandler;
import cn.iocoder.yudao.module.iot.websocket.message.DeviceStatusMessage;
import cn.iocoder.yudao.module.iot.websocket.message.IotMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.DEVICE_NOT_EXISTS;

import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import cn.iocoder.yudao.module.iot.service.ibms.device.IbmsDeviceRuntimeService;
import cn.iocoder.yudao.module.iot.service.ibms.device.IbmsDeviceService;

/**
 * 遗留 {@code iot_device} 表的写路径与 IP 判存收口，供双轨收敛后仍操作 IoT 台账的调用方使用，避免再注入整表 {@link cn.iocoder.yudao.module.iot.service.device.IotDeviceService}。
 */
@Component
@Slf4j
public class IotLegacyIotDeviceSideEffects {

    @Resource
    private IbmsDeviceService ibmsDeviceService;

    @Resource
    private IbmsDeviceRuntimeService ibmsDeviceRuntimeService;

    @Resource
    private IbmsDeviceMapper ibmsDeviceMapper;

    @Resource(name = "iotWebSocketHandler")
    private IotWebSocketHandler iotWebSocketHandler;

    /**
     * 与 {@link cn.iocoder.yudao.module.iot.service.device.IotDeviceServiceImpl#isDeviceExistsByIp} 行为一致：扫全表按 config / DeviceConfigHelper 比对 IP。
     */
    @TenantIgnore
    public boolean isDeviceExistsByIp(String ip) {
        return ibmsDeviceService.isDeviceExistsByIp(ip);
    }

    /**
     * 更新 {@code iot_device} 状态、清理缓存并推送 WebSocket（与 {@code IotDeviceServiceImpl#updateDeviceState(IotDeviceDO, Integer)} 对齐）。
     */
    public void updateDeviceState(IotDeviceDO device, Integer state) {
        if (device == null || device.getId() == null || state == null) {
            return;
        }
        // 仅写 IBMS 运行态，避免任何 iot_device 写副作用
        ibmsDeviceRuntimeService.patchGatewayState(
                device.getId(), device.getTenantId(), state, System.currentTimeMillis());
        pushDeviceStatusChange(device.getId(), device.getDeviceName(), state);
    }

    /**
     * 更新 {@code iot_device.firmware_id} 并清理缓存。
     */
    public void updateDeviceFirmware(Long deviceId, Long firmwareId) {
        if (deviceId == null || firmwareId == null) {
            return;
        }
        IbmsDeviceDO device = ibmsDeviceMapper.selectById(deviceId);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        ibmsDeviceRuntimeService.updateFirmwareId(deviceId, firmwareId);
    }

    private void pushDeviceStatusChange(Long deviceId, String deviceName, Integer state) {
        try {
            DeviceStatusMessage statusMsg = DeviceStatusMessage.builder()
                    .deviceId(deviceId)
                    .deviceName(deviceName)
                    .status(Objects.equals(state, IotDeviceStateEnum.ONLINE.getState()) ? "online" : "offline")
                    .timestamp(System.currentTimeMillis())
                    .build();
            IotMessage message = IotMessage.deviceStatus(statusMsg);
            if (iotWebSocketHandler != null) {
                iotWebSocketHandler.broadcast(message);
                log.info("[设备状态] WebSocket推送成功: deviceId={}, status={}", deviceId, statusMsg.getStatus());
            }
        } catch (Exception e) {
            log.error("[设备状态] WebSocket推送失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
        }
    }
}
