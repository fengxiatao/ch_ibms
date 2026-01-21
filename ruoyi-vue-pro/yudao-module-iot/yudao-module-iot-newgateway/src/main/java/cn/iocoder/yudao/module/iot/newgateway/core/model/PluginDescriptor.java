package cn.iocoder.yudao.module.iot.newgateway.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 插件描述符
 * <p>
 * 描述已注册插件的元数据信息，由 DevicePluginRegistry 在扫描插件时创建。
 * 包含插件的基本信息和运行时实例引用。
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>
 * {@code
 * PluginDescriptor descriptor = PluginDescriptor.builder()
 *     .id("changhui")
 *     .name("长辉TCP模拟设备")
 *     .deviceType("CHANGHUI")
 *     .vendor("Changhui")
 *     .description("长辉自研TCP协议设备")
 *     .enabled(true)
 *     .instance(changhuiPlugin)
 *     .build();
 * }
 * </pre>
 *
 * @author IoT Gateway Team
 * @see cn.iocoder.yudao.module.iot.newgateway.core.annotation.DevicePlugin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginDescriptor {

    /**
     * 插件ID（唯一标识）
     */
    private String id;

    /**
     * 插件名称
     */
    private String name;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 厂商
     */
    private String vendor;

    /**
     * 插件描述
     */
    private String description;

    /**
     * 是否启用
     */
    private boolean enabled;

    /**
     * 插件实例
     * <p>
     * 插件的 Spring Bean 实例，通常实现了 DeviceHandler 接口。
     * </p>
     */
    private Object instance;

    /**
     * 插件状态
     */
    private PluginStatus status;

    /**
     * 启动时间（毫秒时间戳）
     */
    private Long startTime;

    /**
     * 错误信息（如果启动失败）
     */
    private String errorMessage;

    /**
     * 插件状态枚举
     */
    public enum PluginStatus {
        /**
         * 已注册但未启动
         */
        REGISTERED,

        /**
         * 正在启动
         */
        STARTING,

        /**
         * 运行中
         */
        RUNNING,

        /**
         * 已停止
         */
        STOPPED,

        /**
         * 启动失败
         */
        FAILED
    }

    /**
     * 检查插件是否正在运行
     *
     * @return 是否运行中
     */
    public boolean isRunning() {
        return status == PluginStatus.RUNNING;
    }

    /**
     * 检查插件是否启动失败
     *
     * @return 是否失败
     */
    public boolean isFailed() {
        return status == PluginStatus.FAILED;
    }

    /**
     * 获取运行时长（毫秒）
     *
     * @return 运行时长，如果未启动则返回 0
     */
    public long getUptime() {
        if (startTime == null || status != PluginStatus.RUNNING) {
            return 0;
        }
        return System.currentTimeMillis() - startTime;
    }

    /**
     * 创建简单描述符（用于测试）
     *
     * @param id         插件ID
     * @param deviceType 设备类型
     * @return 插件描述符
     */
    public static PluginDescriptor of(String id, String deviceType) {
        return PluginDescriptor.builder()
                .id(id)
                .deviceType(deviceType)
                .enabled(true)
                .status(PluginStatus.REGISTERED)
                .build();
    }

    /**
     * 创建完整描述符
     *
     * @param id          插件ID
     * @param name        插件名称
     * @param deviceType  设备类型
     * @param vendor      厂商
     * @param description 描述
     * @param instance    插件实例
     * @return 插件描述符
     */
    public static PluginDescriptor of(String id, String name, String deviceType,
                                       String vendor, String description, Object instance) {
        return PluginDescriptor.builder()
                .id(id)
                .name(name)
                .deviceType(deviceType)
                .vendor(vendor)
                .description(description)
                .enabled(true)
                .instance(instance)
                .status(PluginStatus.REGISTERED)
                .build();
    }
}
