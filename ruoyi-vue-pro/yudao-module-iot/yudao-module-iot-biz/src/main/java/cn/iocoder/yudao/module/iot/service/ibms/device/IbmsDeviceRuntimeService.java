package cn.iocoder.yudao.module.iot.service.ibms.device;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceRuntimeDO;

/**
 * IBMS 设备运行态（{@code ibms_device_runtime}）读写。
 */
public interface IbmsDeviceRuntimeService {

    /**
     * 按设备主键查询运行态，无记录返回 null。
     */
    IbmsDeviceRuntimeDO getByDeviceId(Long deviceId);

    /**
     * 设备台账创建后确保存在运行态行（幂等）。
     */
    void ensureRowForDevice(IbmsDeviceDO device);

    /**
     * 设备台账删除时同步逻辑删除运行态。
     */
    void deleteByDeviceId(Long deviceId);

    /**
     * 网关状态变更时写入运行态（与 {@code ibms_device.extra.gatewayRuntimeState} 双写，便于后续收敛查询）。
     */
    void patchGatewayState(Long deviceId, Long tenantId, Integer newState, Long timestampMillis);

    /**
     * 更新运行态固件版本（OTA 成功等场景）；无运行态行时按台账补一行最小记录。
     */
    void updateFirmwareId(Long deviceId, Long firmwareId);

    /**
     * 更新运行态定时任务配置（对齐原 {@code iot_device.job_config}）；无运行态行时按台账补插入。
     */
    void updateJobConfig(Long deviceId, String jobConfig);

    /**
     * 持久化运行态 {@code config}（如门禁能力快照）；无运行态行时按台账补插入。
     */
    void saveRuntimeConfig(Long deviceId, DeviceConfig config);
}
