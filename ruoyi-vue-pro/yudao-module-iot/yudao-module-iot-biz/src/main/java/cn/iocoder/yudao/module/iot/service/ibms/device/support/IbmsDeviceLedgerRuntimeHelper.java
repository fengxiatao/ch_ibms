package cn.iocoder.yudao.module.iot.service.ibms.device.support;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
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

}
