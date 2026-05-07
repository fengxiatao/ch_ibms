package cn.iocoder.yudao.module.iot.service.camera.dto;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceRuntimeDO;
import cn.iocoder.yudao.module.iot.service.ibms.device.support.IbmsDeviceLedgerRuntimeHelper;
import cn.iocoder.yudao.module.iot.service.video.IbmsDeviceVideoNetworkResolver;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

/**
 * 摄像头设备只读视图（M2-E GAP-011 单源化）
 *
 * <p>包装 ibms_device 台账 + ibms_device_runtime 运行态，对外提供与历史 IotDeviceDO
 * 同名访问器 + Camera 专属字段（vendor / 网络参数），避免下游消费者继续经手
 * {@code IotDeviceDO} 壳类型与冗余 {@code GenericDeviceConfig} 打包/解包 round-trip。</p>
 *
 * <p>该 View 不参与持久化，仅作为 Service / Protocol 层返回类型；写操作仍走
 * {@code IbmsDeviceService} / {@code IbmsDeviceRuntimeService}。</p>
 */
public final class CameraDeviceView {

    private final IbmsDeviceDO ledger;
    private final IbmsDeviceRuntimeDO runtime; // 可空：runtime 行尚未生成时
    private final IbmsDeviceVideoNetworkResolver.NetworkParams net;
    private final String vendor;

    private CameraDeviceView(IbmsDeviceDO ledger, IbmsDeviceRuntimeDO runtime) {
        this.ledger = ledger;
        this.runtime = runtime;
        this.net = IbmsDeviceVideoNetworkResolver.resolve(ledger, runtime);
        this.vendor = resolveVendor(ledger);
    }

    /**
     * 工厂方法。当 {@code ledger} 为空时返回 null，避免下游 NPE。
     */
    public static CameraDeviceView of(IbmsDeviceDO ledger, IbmsDeviceRuntimeDO runtime) {
        if (ledger == null) {
            return null;
        }
        return new CameraDeviceView(ledger, runtime);
    }

    /** 从 ibms_device.brand / ibms_device.extra.vendor 解析 vendor 字符串（与原 helper 等价）。 */
    private static String resolveVendor(IbmsDeviceDO ibms) {
        String brand = ibms.getBrand();
        if (StrUtil.isNotBlank(brand)) {
            String b = brand.trim();
            if (b.equalsIgnoreCase("DAH") || b.equalsIgnoreCase("DAHUA")) {
                return "dahua";
            }
            if (b.equalsIgnoreCase("HIK") || b.equalsIgnoreCase("HIKVISION")) {
                return "hikvision";
            }
        }
        if (StrUtil.isNotBlank(ibms.getExtra())) {
            try {
                JSONObject ex = JSONUtil.parseObj(ibms.getExtra().trim());
                String v = ex.getStr("vendor");
                if (StrUtil.isNotBlank(v)) {
                    return v.trim();
                }
            } catch (Exception ignored) {
                // ignore
            }
        }
        return "";
    }

    // ===== 直通访问器（消费方有需要时直接拿原对象） =====

    @JsonIgnore
    public IbmsDeviceDO getLedger() {
        return ledger;
    }

    @JsonIgnore
    public IbmsDeviceRuntimeDO getRuntime() {
        return runtime;
    }

    @JsonIgnore
    public IbmsDeviceVideoNetworkResolver.NetworkParams getNetworkParams() {
        return net;
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

    public String getSerialNumber() {
        return ledger.getSn();
    }

    public String getPicUrl() {
        return ledger.getPicUrl();
    }

    /**
     * 历史 IotDeviceDO.address 字段在 buildLegacyCameraCollectorShell 中从未被设置，
     * 故现网调用方拿到的始终为 null；此处保持等价行为。如需地址，请改用 {@link #getIp()}
     * 或 {@code getLedger().getExtra()} 自行解析。
     */
    public String getAddress() {
        return null;
    }

    // ===== runtime 透出（state 走 helper 的 ledger+runtime 兜底逻辑） =====

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

    // ===== Camera 专属网络/品牌字段 =====

    public String getVendor() {
        return vendor;
    }

    public String getIp() {
        return net.ip;
    }

    public String getUsername() {
        return StrUtil.blankToDefault(net.username, "admin");
    }

    public String getPassword() {
        return StrUtil.blankToDefault(net.password, "admin123");
    }

    public int getHttpPort() {
        return net.httpPort;
    }

    public int getRtspPort() {
        return net.rtspPort;
    }
}
