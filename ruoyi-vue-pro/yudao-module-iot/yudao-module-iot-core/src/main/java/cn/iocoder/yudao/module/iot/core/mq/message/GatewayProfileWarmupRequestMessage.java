package cn.iocoder.yudao.module.iot.core.mq.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 网关冷启动请求 Biz 按租户批量重推设备 Profile（Biz 侧发布多条 {@code DEVICE_PROFILE_CHANGED}）。
 * <p>
 * 与单设备 {@link GatewayDeviceSnapshotRequestMessage} 互补：缓存全空时无法逐台 RR，需先批量灌入缓存。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayProfileWarmupRequestMessage {

    @Builder.Default
    private String schemaVersion = "1";

    /**
     * 目标租户（必填）；Biz 在对应租户上下文中执行与「管理端全量重推」等价的 MQ 发布。
     */
    private Long tenantId;

    /**
     * 调用方标识，便于审计
     */
    private String source;
}
