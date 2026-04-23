package cn.iocoder.yudao.module.iot.service.ibms.device;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceRuntimeDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceRuntimeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * IBMS 设备运行态 Service 实现
 */
@Service
@Validated
@RequiredArgsConstructor
public class IbmsDeviceRuntimeServiceImpl implements IbmsDeviceRuntimeService {

    private final IbmsDeviceRuntimeMapper runtimeMapper;
    private final IbmsDeviceMapper ibmsDeviceMapper;

    @Override
    public IbmsDeviceRuntimeDO getByDeviceId(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        return runtimeMapper.selectById(deviceId);
    }

    @Override
    public void ensureRowForDevice(IbmsDeviceDO device) {
        if (device == null || device.getId() == null) {
            return;
        }
        if (runtimeMapper.selectById(device.getId()) != null) {
            return;
        }
        IbmsDeviceRuntimeDO row = new IbmsDeviceRuntimeDO();
        row.setDeviceId(device.getId());
        row.setTenantId(device.getTenantId());
        runtimeMapper.insert(row);
    }

    @Override
    public void deleteByDeviceId(Long deviceId) {
        if (deviceId == null) {
            return;
        }
        runtimeMapper.deleteById(deviceId);
    }

    @Override
    public void patchGatewayState(Long deviceId, Long tenantId, Integer newState, Long timestampMillis) {
        if (deviceId == null) {
            return;
        }
        LocalDateTime at = toLocalDateTime(timestampMillis);
        IbmsDeviceRuntimeDO existing = runtimeMapper.selectById(deviceId);
        IbmsDeviceRuntimeDO row;
        if (existing != null) {
            row = existing;
        } else {
            row = new IbmsDeviceRuntimeDO();
            row.setDeviceId(deviceId);
            row.setTenantId(tenantId);
        }
        if (row.getTenantId() == null) {
            row.setTenantId(tenantId);
        }
        row.setState(newState);
        if (IotDeviceStateEnum.isOnline(newState)) {
            row.setOnlineTime(at);
            if (row.getActiveTime() == null) {
                row.setActiveTime(at);
            }
        } else if (newState != null && newState.equals(IotDeviceStateEnum.OFFLINE.getState())) {
            row.setOfflineTime(at);
        }
        if (existing == null) {
            runtimeMapper.insert(row);
        } else {
            runtimeMapper.updateById(row);
        }
    }

    @Override
    public void updateFirmwareId(Long deviceId, Long firmwareId) {
        if (deviceId == null || firmwareId == null) {
            return;
        }
        IbmsDeviceRuntimeDO existing = runtimeMapper.selectById(deviceId);
        if (existing != null) {
            existing.setFirmwareId(firmwareId);
            runtimeMapper.updateById(existing);
            return;
        }
        IbmsDeviceDO ledger = ibmsDeviceMapper.selectById(deviceId);
        if (ledger == null) {
            return;
        }
        IbmsDeviceRuntimeDO row = new IbmsDeviceRuntimeDO();
        row.setDeviceId(deviceId);
        row.setTenantId(ledger.getTenantId());
        row.setFirmwareId(firmwareId);
        runtimeMapper.insert(row);
    }

    @Override
    public void updateJobConfig(Long deviceId, String jobConfig) {
        if (deviceId == null) {
            return;
        }
        IbmsDeviceDO ledger = ibmsDeviceMapper.selectById(deviceId);
        if (ledger == null) {
            return;
        }
        IbmsDeviceRuntimeDO existing = runtimeMapper.selectById(deviceId);
        if (existing != null) {
            existing.setJobConfig(jobConfig);
            runtimeMapper.updateById(existing);
            return;
        }
        IbmsDeviceRuntimeDO row = new IbmsDeviceRuntimeDO();
        row.setDeviceId(deviceId);
        row.setTenantId(ledger.getTenantId());
        row.setJobConfig(jobConfig);
        runtimeMapper.insert(row);
    }

    @Override
    public void saveRuntimeConfig(Long deviceId, DeviceConfig config) {
        if (deviceId == null || config == null) {
            return;
        }
        IbmsDeviceRuntimeDO existing = runtimeMapper.selectById(deviceId);
        if (existing != null) {
            existing.setConfig(config);
            runtimeMapper.updateById(existing);
            return;
        }
        IbmsDeviceDO ledger = ibmsDeviceMapper.selectById(deviceId);
        if (ledger == null) {
            return;
        }
        IbmsDeviceRuntimeDO row = new IbmsDeviceRuntimeDO();
        row.setDeviceId(deviceId);
        row.setTenantId(ledger.getTenantId());
        row.setConfig(config);
        runtimeMapper.insert(row);
    }

    private static LocalDateTime toLocalDateTime(Long timestampMillis) {
        if (timestampMillis == null) {
            return LocalDateTime.now();
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampMillis), ZoneId.systemDefault());
    }
}
