package cn.iocoder.yudao.module.iot.service.video.nvr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * NVR 网关扫描后、自通道表组装的通道行。
 *
 * <p>替代历史上以 {@code IotDeviceDO} 伪子设备承载通道信息的写法，
 * 直接作为 {@code syncNvrChannelToIbms} 等同步入参。</p>
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

    /** 通道子类型/设备类型（如 IPC / PTZ / DOME，可空） */
    private String deviceType;
}
