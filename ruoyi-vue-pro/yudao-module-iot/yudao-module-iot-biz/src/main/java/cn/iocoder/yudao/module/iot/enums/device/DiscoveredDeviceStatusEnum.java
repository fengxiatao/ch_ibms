package cn.iocoder.yudao.module.iot.enums.device;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 发现设备状态枚举
 *
 * @author 长辉信息科技有限公司
 */
@Getter
@AllArgsConstructor
public enum DiscoveredDeviceStatusEnum implements ArrayValuable<Integer> {

    DISCOVERED(1, "已发现"),
    NOTIFIED(2, "已通知"),
    IGNORED(3, "已忽略"),
    PENDING(4, "待处理"),
    REGISTERED(5, "已注册"),
    ACTIVATED(6, "已激活");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(DiscoveredDeviceStatusEnum::getStatus)
            .toArray(Integer[]::new);

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static DiscoveredDeviceStatusEnum valueOf(Integer status) {
        return Arrays.stream(values())
                .filter(type -> type.getStatus().equals(status))
                .findFirst()
                .orElse(null);
    }
}


