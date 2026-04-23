package cn.iocoder.yudao.module.iot.core.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 设备能力快照（网关 → Biz，可与定时刷新/插件对齐）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCapabilitySnapshot {

    private Long deviceId;
    private String deviceType;
    private String pluginId;
    private String vendor;

    @Builder.Default
    private String schemaVersion = "1";

    private LocalDateTime collectedAt;

    /**
     * 能力键值，如通道数、PTZ、协议特性等
     */
    private Map<String, Object> capabilities;
}
