package cn.iocoder.yudao.module.iot.newgateway.core.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 设备插件注解
 * <p>
 * 标记一个类为设备插件入口，用于自动发现和注册。
 * 被标记的类会被 Spring 容器管理，并被 DevicePluginRegistry 自动扫描和注册。
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>
 * {@code
 * @DevicePlugin(
 *     id = "changhui",
 *     name = "长辉TCP模拟设备",
 *     deviceType = "CHANGHUI",
 *     vendor = "Changhui",
 *     description = "长辉自研TCP协议设备，支持心跳、远程升级等功能"
 * )
 * public class ChanghuiPlugin implements PassiveDeviceHandler {
 *     // ...
 * }
 * }
 * </pre>
 *
 * @author IoT Gateway Team
 * @see cn.iocoder.yudao.module.iot.newgateway.core.registry.DevicePluginRegistry
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface DevicePlugin {

    /**
     * 插件ID（唯一标识）
     * <p>
     * 用于在配置文件中启用/禁用插件，以及在日志中标识插件。
     * 建议使用小写字母和连字符，如 "alarm", "changhui", "access-gen1"。
     * </p>
     *
     * @return 插件唯一标识
     */
    String id();

    /**
     * 插件名称
     * <p>
     * 用于显示的友好名称，如 "报警主机插件"、"长辉TCP模拟设备"。
     * </p>
     *
     * @return 插件显示名称
     */
    String name();

    /**
     * 设备类型
     * <p>
     * 用于匹配设备信息中的设备类型字段，如 "ALARM", "CHANGHUI", "NVR"。
     * 建议使用大写字母。
     * </p>
     *
     * @return 设备类型标识
     */
    String deviceType();

    /**
     * 厂商（可选）
     * <p>
     * 设备厂商名称，如 "Dahua", "Changhui"。
     * 用于进一步区分同类型但不同厂商的设备。
     * </p>
     *
     * @return 厂商名称，默认为空字符串
     */
    String vendor() default "";

    /**
     * 插件描述（可选）
     * <p>
     * 插件的详细描述信息，用于文档和管理界面展示。
     * </p>
     *
     * @return 插件描述，默认为空字符串
     */
    String description() default "";

    /**
     * 是否默认启用
     * <p>
     * 当配置文件中未明确指定插件启用状态时，使用此默认值。
     * 设置为 false 可以让插件默认禁用，需要在配置中显式启用。
     * </p>
     *
     * @return 是否默认启用，默认为 true
     */
    boolean enabledByDefault() default true;

    /**
     * 是否需要周期性刷新设备能力
     *
     * <p>用于区分“有能力概念”的设备（如门禁一/二代、NVR），以及无需能力刷新
     * 的设备（如报警主机、长辉 TCP）。</p>
     *
     * <p>注意：该标识用于插件自描述，业务侧可根据 deviceType 采用对应刷新策略。</p>
     *
     * @return 是否需要刷新能力，默认 false
     */
    boolean capabilityRefreshEnabled() default false;
}
