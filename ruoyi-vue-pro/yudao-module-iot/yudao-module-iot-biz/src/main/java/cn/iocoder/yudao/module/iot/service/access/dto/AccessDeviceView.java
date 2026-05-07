package cn.iocoder.yudao.module.iot.service.access.dto;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceRuntimeDO;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

/**
 * 门禁设备只读视图（M2-B GAP-011 单源化）
 *
 * <p>包装 ibms_device 台账 + ibms_device_runtime 运行态，对外提供与历史 IotDeviceDO
 * 同名访问器，避免下游消费者继续经手 {@code IotDeviceDO} 壳类型。</p>
 *
 * <p>该 View 不参与持久化，仅作为 Service 层返回类型；写操作仍走
 * {@code IbmsDeviceService} / {@code IbmsDeviceRuntimeService}。</p>
 */
public final class AccessDeviceView {

    private final IbmsDeviceDO ledger;
    private final IbmsDeviceRuntimeDO runtime; // 可空：runtime 行尚未生成时

    private AccessDeviceView(IbmsDeviceDO ledger, IbmsDeviceRuntimeDO runtime) {
        this.ledger = ledger;
        this.runtime = runtime;
    }

    /**
     * 工厂方法。当 {@code ledger} 为空时返回 null，避免下游 NPE。
     */
    public static AccessDeviceView of(IbmsDeviceDO ledger, IbmsDeviceRuntimeDO runtime) {
        if (ledger == null) {
            return null;
        }
        return new AccessDeviceView(ledger, runtime);
    }

    // ===== 直通访问器（消费方有需要时直接拿原对象） =====
    // @JsonIgnore：避免 Jackson 把 ledger/runtime 整体序列化，保持响应体与原
    // IotDeviceDO 平铺结构兼容（业务代码仍可通过 getter 访问内部对象）。

    @JsonIgnore
    public IbmsDeviceDO getLedger() {
        return ledger;
    }

    @JsonIgnore
    public IbmsDeviceRuntimeDO getRuntime() {
        return runtime;
    }

    // ===== 与 IotDeviceDO 同名 getter（最小侵入下游 caller） =====

    public Long getId() {
        return ledger.getId();
    }

    public Long getTenantId() {
        return ledger.getTenantId();
    }

    public LocalDateTime getCreateTime() {
        return ledger.getCreateTime();
    }

    public LocalDateTime getUpdateTime() {
        return ledger.getUpdateTime();
    }

    /** 对应 IotDeviceDO.deviceName，映射自 ibms_device.name。 */
    public String getDeviceName() {
        return ledger.getName();
    }

    public String getNickname() {
        return ledger.getNickname();
    }

    public String getDeviceKey() {
        return ledger.getDeviceKey();
    }

    public String getProductKey() {
        return ledger.getProductKey();
    }

    /** 对应 IotDeviceDO.productId，映射自 ibms_device.ibms_product_id。 */
    public Long getProductId() {
        return ledger.getIbmsProductId();
    }

    public String getSubsystemCode() {
        return ledger.getSubsystemCode();
    }

    public String getDeviceSecret() {
        return ledger.getDeviceSecret();
    }

    public String getAuthType() {
        return ledger.getAuthType();
    }

    /** 数值型设备类型（保留兼容历史 IotDeviceDO.deviceType）。 */
    public Integer getDeviceType() {
        return ledger.getDeviceType();
    }

    public String getSerialNumber() {
        return ledger.getSn();
    }

    public String getPicUrl() {
        return ledger.getPicUrl();
    }

    public String getDxfEntityId() {
        return ledger.getDxfEntityId();
    }

    // ===== runtime 透出 =====

    public Integer getState() {
        return runtime != null ? runtime.getState() : null;
    }

    public DeviceConfig getConfig() {
        return runtime != null ? runtime.getConfig() : null;
    }

    public LocalDateTime getOnlineTime() {
        return runtime != null ? runtime.getOnlineTime() : null;
    }

    public LocalDateTime getOfflineTime() {
        return runtime != null ? runtime.getOfflineTime() : null;
    }

    public LocalDateTime getActiveTime() {
        return runtime != null ? runtime.getActiveTime() : null;
    }

    public Long getFirmwareId() {
        return runtime != null ? runtime.getFirmwareId() : null;
    }

    public Long getGatewayId() {
        return runtime != null ? runtime.getGatewayId() : null;
    }
}
