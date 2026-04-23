package cn.iocoder.yudao.module.iot.core.mq.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备台账/连接参数变更通知（Biz → Gateway）。
 * <p>
 * 网关仅消费并更新本地缓存，不写业务库。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceProfileChangedMessage {

    public static final String OP_UPSERT = "UPSERT";
    public static final String OP_DELETE = "DELETE";

    /**
     * 契约版本，便于双读兼容
     */
    @Builder.Default
    private String schemaVersion = "1";

    /**
     * {@link #OP_UPSERT} 或 {@link #OP_DELETE}
     */
    private String op;

    private Long deviceId;
    private Long tenantId;

    /**
     * 网关插件 deviceType，如 NVR、ACCESS_GEN1
     */
    private String deviceType;

    /**
     * IBMS 品牌码等，如 HIK、DAH
     */
    private String brand;

    private String vendorKey;

    private String pluginId;

    private String productKey;
    private Long productId;
    private String deviceName;
    private String address;

    /**
     * 连接用 JSON（与 {@link cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO#getConfig()} 语义一致）
     */
    private String config;

    private Long updatedAtMillis;
}
