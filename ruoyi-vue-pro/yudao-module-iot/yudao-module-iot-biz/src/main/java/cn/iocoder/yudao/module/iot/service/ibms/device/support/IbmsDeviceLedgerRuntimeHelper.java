package cn.iocoder.yudao.module.iot.service.ibms.device.support;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceRuntimeDO;

/**
 * 台账 {@link IbmsDeviceDO} 与 {@link IbmsDeviceRuntimeDO} 上可读的状态/展示辅助方法。
 */
public final class IbmsDeviceLedgerRuntimeHelper {

    private IbmsDeviceLedgerRuntimeHelper() {
    }

    /**
     * 优先运行态 {@code state}，否则读台账 {@code extra.gatewayRuntimeState}。
     */
    public static Integer resolveDeviceState(IbmsDeviceDO ledger, IbmsDeviceRuntimeDO runtime) {
        if (runtime != null && runtime.getState() != null) {
            return runtime.getState();
        }
        return parseGatewayRuntimeStateFromExtra(ledger != null ? ledger.getExtra() : null);
    }

    public static Integer parseGatewayRuntimeStateFromExtra(String extraJson) {
        if (StrUtil.isBlank(extraJson)) {
            return null;
        }
        try {
            return JSONUtil.parseObj(extraJson.trim()).getInt("gatewayRuntimeState");
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * OTA 推送/进度：仅需 id、租户、在线态、产品、运行态固件等字段的兼容壳。
     */
    public static IotDeviceDO buildLegacyOtaDeviceShell(IbmsDeviceDO ibms, IbmsDeviceRuntimeDO runtime) {
        if (ibms == null) {
            return null;
        }
        IotDeviceDO d = new IotDeviceDO();
        d.setId(ibms.getId());
        d.setTenantId(ibms.getTenantId());
        d.setDeviceName(ibms.getName());
        d.setProductId(ibms.getIbmsProductId());
        d.setProductKey(StrUtil.blankToDefault(ibms.getProductKey(), null));
        d.setState(resolveDeviceState(ibms, runtime));
        if (runtime != null && runtime.getFirmwareId() != null) {
            d.setFirmwareId(runtime.getFirmwareId());
        }
        return d;
    }

}
