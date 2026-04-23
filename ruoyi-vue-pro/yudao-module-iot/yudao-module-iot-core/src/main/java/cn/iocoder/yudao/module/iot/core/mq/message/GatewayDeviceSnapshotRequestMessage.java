package cn.iocoder.yudao.module.iot.core.mq.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 网关请求 Biz 返回单设备快照（Request-Reply 的请求侧）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayDeviceSnapshotRequestMessage {

    @Builder.Default
    private String schemaVersion = "1";

    private String correlationId;
    private Long deviceId;
    private Long tenantId;

    /**
     * 可选：调用方标识，便于审计
     */
    private String source;
}
