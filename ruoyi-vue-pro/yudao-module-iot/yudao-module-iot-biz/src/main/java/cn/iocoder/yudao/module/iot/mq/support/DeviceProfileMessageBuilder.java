package cn.iocoder.yudao.module.iot.mq.support;

import cn.iocoder.yudao.module.iot.api.device.support.IbmsDeviceGatewayDtoMapper;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.DeviceProfileChangedMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;

/**
 * 构造 {@link DeviceProfileChangedMessage}，与网关缓存 DTO 字段对齐。
 */
public final class DeviceProfileMessageBuilder {

    private DeviceProfileMessageBuilder() {
    }

    public static DeviceProfileChangedMessage fromIbms(IbmsDeviceDO d, String op) {
        if (d == null) {
            return null;
        }
        IotDeviceRespDTO dto = IbmsDeviceGatewayDtoMapper.toGatewayDto(d);
        return DeviceProfileChangedMessage.builder()
                .op(op)
                .deviceId(d.getId())
                .tenantId(d.getTenantId())
                .deviceType(dto.getDeviceType())
                .brand(dto.getBrand())
                .productKey(d.getProductKey())
                .deviceName(d.getName())
                .address(dto.getAddress())
                .config(dto.getConfig())
                .updatedAtMillis(System.currentTimeMillis())
                .build();
    }

    public static DeviceProfileChangedMessage deleteIbms(Long deviceId, Long tenantId) {
        return DeviceProfileChangedMessage.builder()
                .op(DeviceProfileChangedMessage.OP_DELETE)
                .deviceId(deviceId)
                .tenantId(tenantId)
                .updatedAtMillis(System.currentTimeMillis())
                .build();
    }
}
