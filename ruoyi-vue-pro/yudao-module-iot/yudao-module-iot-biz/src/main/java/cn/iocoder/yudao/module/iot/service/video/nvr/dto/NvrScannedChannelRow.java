package cn.iocoder.yudao.module.iot.service.video.nvr.dto;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.GenericDeviceConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * NVR 网关扫描后、自通道表组装的通道行（替代历史上 {@link IotDeviceDO} 伪子设备）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NvrScannedChannelRow {

    /** 兼容旧列表展示用的合成 ID（非库主键） */
    private Long syntheticId;

    private Integer channelNo;

    private String channelName;

    /** 与 {@link cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum} 取值一致时用于在线等 */
    private Integer state;

    private Long productId;

    private String ipAddress;

    private Boolean ptzSupport;

    private Boolean audioSupport;

    private String resolution;

    /**
     * 转为仍依赖 {@link IotDeviceDO#config} 的 {@code syncNvrChannel} 入参形态。
     */
    public IotDeviceDO toLegacyChannelDeviceForSync(Long nvrId) {
        IotDeviceDO d = new IotDeviceDO();
        long sid = syntheticId != null ? syntheticId
                : nvrId * 1000L + (channelNo != null ? channelNo : 0);
        d.setId(sid);
        d.setDeviceName(channelName);
        d.setState(state);
        d.setGatewayId(nvrId);
        d.setProductId(productId);

        GenericDeviceConfig cfg = new GenericDeviceConfig();
        cfg.set("channel", channelNo);
        cfg.set("channelName", channelName);
        cfg.set("online", state != null && state == 1);
        if (ipAddress != null) {
            cfg.set("ipAddress", ipAddress);
        }
        if (ptzSupport != null) {
            cfg.set("ptzSupport", ptzSupport);
        }
        if (audioSupport != null) {
            cfg.set("audioSupport", audioSupport);
        }
        if (resolution != null) {
            cfg.set("resolution", resolution);
        }
        d.setConfig(cfg);
        return d;
    }
}
