package cn.iocoder.yudao.module.iot.service.ibms.device.support;

import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceRuntimeDO;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

/**
 * OTA / 场景规则 / 下行消息 / 设备 Job 等仍消费 legacy 设备 DO 字段子集的统一只读视图（M2-D 阶段二 GAP-011 收口）。
 *
 * <p>包装 {@link IbmsDeviceDO} 台账 + {@link IbmsDeviceRuntimeDO} 运行态，对外暴露与 legacy 设备 DO 同名 getter
 * （id / tenantId / productId / productKey / deviceName / deviceType / state / firmwareId / jobConfig），
 * 取代 {@code IbmsDeviceLedgerRuntimeHelper#buildLegacyOtaDeviceShell} 的"造壳 + 解壳" round-trip。</p>
 *
 * <p>该 View 不参与持久化；写操作仍走 {@code IbmsDeviceService} / {@code IbmsDeviceRuntimeService}。</p>
 */
public final class OtaDeviceView {

    private final IbmsDeviceDO ledger;
    private final IbmsDeviceRuntimeDO runtime; // 可空：runtime 行尚未生成时

    private OtaDeviceView(IbmsDeviceDO ledger, IbmsDeviceRuntimeDO runtime) {
        this.ledger = ledger;
        this.runtime = runtime;
    }

    /** 工厂方法。{@code ledger} 为空时返回 null，避免下游 NPE。 */
    public static OtaDeviceView of(IbmsDeviceDO ledger, IbmsDeviceRuntimeDO runtime) {
        if (ledger == null) {
            return null;
        }
        return new OtaDeviceView(ledger, runtime);
    }

    @JsonIgnore
    public IbmsDeviceDO getLedger() {
        return ledger;
    }

    @JsonIgnore
    public IbmsDeviceRuntimeDO getRuntime() {
        return runtime;
    }

    // ===== legacy 设备 DO 同名访问器 =====

    public Long getId() {
        return ledger.getId();
    }

    public Long getTenantId() {
        return ledger.getTenantId();
    }

    /** 对应 legacy deviceName，映射自 ibms_device.name。 */
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

    /** 对应 legacy productId，映射自 ibms_device.ibms_product_id。 */
    public Long getProductId() {
        return ledger.getIbmsProductId();
    }

    /**
     * 设备类型（整型枚举），与 legacy deviceType 同型；映射自 {@code ibms_device.device_type}。
     */
    public Integer getDeviceType() {
        return ledger.getDeviceType();
    }

    // ===== runtime 透出 =====

    public Integer getState() {
        return IbmsDeviceLedgerRuntimeHelper.resolveDeviceState(ledger, runtime);
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

    /** 设备级 jobConfig 覆盖（null 表示走产品级）。 */
    public String getJobConfig() {
        return runtime != null ? runtime.getJobConfig() : null;
    }
}
