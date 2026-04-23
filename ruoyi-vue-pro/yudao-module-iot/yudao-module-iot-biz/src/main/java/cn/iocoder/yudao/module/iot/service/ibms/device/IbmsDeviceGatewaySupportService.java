package cn.iocoder.yudao.module.iot.service.ibms.device;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;

import java.util.List;

/**
 * 网关 / 进程内 {@link cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi} 使用的 IBMS 台账视图（不依赖 {@code iot_device}）。
 */
public interface IbmsDeviceGatewaySupportService {

    IotDeviceRespDTO getGatewayDevice(IotDeviceGetReqDTO req);

    boolean authDevice(IotDeviceAuthReqDTO authReqDTO);

    List<IotDeviceRespDTO> listOnlineDevices();

    List<IotDeviceRespDTO> listAccessDevices();

    List<IotDeviceRespDTO> listAllDevices();

    List<IotDeviceRespDTO> listDevicesForTenant(Long tenantId);

    /**
     * 批量写入 {@code ibms_device.extra.gatewayRuntimeState} / {@code gatewayRuntimeAt}。
     *
     * @return 成功更新条数
     */
    int batchUpdateGatewayRuntimeState(List<Long> deviceIds, Integer state, String reason);

    /**
     * 按设备主键更新网关观测到的在线状态，并双写 {@code ibms_device.extra} 与 {@code ibms_device_runtime}。
     * <p>供 MQ 消费者（如在线检测兜底、状态变更）使用，不依赖 {@code iot_device}。</p>
     *
     * @param deviceId        IBMS 设备 ID
     * @param newState        {@link cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum} 状态值
     * @param timestampMillis 状态时间戳（毫秒），可空则使用当前时间写入 extra 的 gatewayRuntimeAt
     */
    void updateGatewayDeviceStateWithTimestamp(Long deviceId, Integer newState, Long timestampMillis);
}
