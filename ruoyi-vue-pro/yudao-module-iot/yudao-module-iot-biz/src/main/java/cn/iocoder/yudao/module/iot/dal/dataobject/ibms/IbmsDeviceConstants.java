package cn.iocoder.yudao.module.iot.dal.dataobject.ibms;

/**
 * IBMS 设备常量。
 *
 * <p>从历史 legacy 设备 DO 静态常量收敛而来（M2-B-PHASE2 单源化）。</p>
 */
public final class IbmsDeviceConstants {

    /**
     * 设备编号 - 全部设备
     *
     * <p>用于规则引擎、消息广播等"匹配所有设备"场景。</p>
     */
    public static final Long DEVICE_ID_ALL = 0L;

    private IbmsDeviceConstants() {
    }
}
