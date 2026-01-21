package cn.iocoder.yudao.module.iot.newgateway.core.registry;

import cn.iocoder.yudao.module.iot.newgateway.core.annotation.DevicePlugin;
import cn.iocoder.yudao.module.iot.newgateway.core.config.GatewayPluginProperties;
import cn.iocoder.yudao.module.iot.newgateway.core.handler.DeviceHandler;
import cn.iocoder.yudao.module.iot.newgateway.core.model.PluginDescriptor;
import cn.iocoder.yudao.module.iot.newgateway.core.model.PluginDescriptor.PluginStatus;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备插件注册表
 * <p>
 * 负责发现、注册和管理所有设备插件。在应用启动时自动扫描所有带 {@link DevicePlugin} 注解的类，
 * 并根据配置决定是否启用。
 * </p>
 *
 * <p>核心功能：</p>
 * <ul>
 *     <li>自动扫描和注册带 @DevicePlugin 注解的 Bean</li>
 *     <li>根据配置启用/禁用插件</li>
 *     <li>提供按设备类型查找处理器的能力</li>
 *     <li>提供插件生命周期管理</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>
 * {@code
 * @Resource
 * private DevicePluginRegistry pluginRegistry;
 *
 * // 获取处理器
 * DeviceHandler handler = pluginRegistry.getHandler("ALARM");
 *
 * // 获取所有插件
 * Collection<PluginDescriptor> plugins = pluginRegistry.getAllPlugins();
 * }
 * </pre>
 *
 * @author IoT Gateway Team
 * @see DevicePlugin
 * @see PluginDescriptor
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DevicePluginRegistry {

    /**
     * 插件ID -> 插件描述符
     */
    private final Map<String, PluginDescriptor> plugins = new ConcurrentHashMap<>();

    /**
     * 设备类型 -> 设备处理器
     */
    private final Map<String, DeviceHandler> handlers = new ConcurrentHashMap<>();

    private final ApplicationContext applicationContext;
    private final GatewayPluginProperties pluginProperties;

    /**
     * 初始化：扫描并注册所有插件
     * <p>
     * 在 Spring 容器初始化完成后自动执行，扫描所有带 @DevicePlugin 注解的 Bean。
     * </p>
     */
    @PostConstruct
    public void init() {
        log.info("[PluginRegistry] 开始扫描设备插件...");

        // 扫描所有带 @DevicePlugin 注解的类
        Map<String, Object> pluginBeans = applicationContext.getBeansWithAnnotation(DevicePlugin.class);

        for (Map.Entry<String, Object> entry : pluginBeans.entrySet()) {
            String beanName = entry.getKey();
            Object bean = entry.getValue();

            try {
                registerPlugin(beanName, bean);
            } catch (Exception e) {
                log.error("[PluginRegistry] 注册插件失败: beanName={}", beanName, e);
            }
        }

        log.info("[PluginRegistry] 插件扫描完成，共注册 {} 个插件，{} 个处理器",
                plugins.size(), handlers.size());
    }

    /**
     * 注册单个插件
     *
     * @param beanName Bean 名称
     * @param bean     Bean 实例
     */
    private void registerPlugin(String beanName, Object bean) {
        // 获取注解信息
        Class<?> beanClass = bean.getClass();
        DevicePlugin annotation = beanClass.getAnnotation(DevicePlugin.class);

        if (annotation == null) {
            log.warn("[PluginRegistry] Bean {} 没有 @DevicePlugin 注解，跳过", beanName);
            return;
        }

        String pluginId = annotation.id();
        String deviceType = annotation.deviceType();

        // 检查是否启用
        boolean enabled = pluginProperties.isPluginEnabled(pluginId, annotation.enabledByDefault());

        if (!enabled) {
            log.info("[PluginRegistry] 插件已禁用: id={}, name={}", pluginId, annotation.name());

            // 仍然记录禁用的插件，但标记为禁用状态
            PluginDescriptor descriptor = PluginDescriptor.builder()
                    .id(pluginId)
                    .name(annotation.name())
                    .deviceType(deviceType)
                    .vendor(annotation.vendor())
                    .description(annotation.description())
                    .enabled(false)
                    .instance(bean)
                    .status(PluginStatus.STOPPED)
                    .build();
            plugins.put(pluginId, descriptor);
            return;
        }

        // 创建插件描述符
        PluginDescriptor descriptor = PluginDescriptor.builder()
                .id(pluginId)
                .name(annotation.name())
                .deviceType(deviceType)
                .vendor(annotation.vendor())
                .description(annotation.description())
                .enabled(true)
                .instance(bean)
                .status(PluginStatus.REGISTERED)
                .startTime(System.currentTimeMillis())
                .build();

        // 注册插件
        plugins.put(pluginId, descriptor);

        // 如果是 DeviceHandler，注册到处理器映射
        if (bean instanceof DeviceHandler) {
            DeviceHandler handler = (DeviceHandler) bean;
            handlers.put(deviceType, handler);
            log.info("[PluginRegistry] 插件注册成功: id={}, name={}, deviceType={}, vendor={}",
                    pluginId, annotation.name(), deviceType, annotation.vendor());
        } else {
            log.warn("[PluginRegistry] 插件 {} 未实现 DeviceHandler 接口，不会注册为处理器", pluginId);
        }
    }

    /**
     * 获取设备处理器
     * <p>
     * 根据设备类型获取对应的处理器。
     * </p>
     *
     * @param deviceType 设备类型
     * @return 设备处理器，如果未找到则返回 null
     */
    public DeviceHandler getHandler(String deviceType) {
        return handlers.get(deviceType);
    }

    /**
     * 获取设备处理器（Optional 版本）
     *
     * @param deviceType 设备类型
     * @return 设备处理器的 Optional 包装
     */
    public Optional<DeviceHandler> findHandler(String deviceType) {
        return Optional.ofNullable(handlers.get(deviceType));
    }

    /**
     * 获取所有已注册的插件
     * <p>
     * 返回所有插件的描述符，包括已禁用的插件。
     * </p>
     *
     * @return 插件描述符集合（不可修改）
     */
    public Collection<PluginDescriptor> getAllPlugins() {
        return Collections.unmodifiableCollection(plugins.values());
    }

    /**
     * 获取所有已启用的插件
     *
     * @return 已启用的插件描述符集合
     */
    public Collection<PluginDescriptor> getEnabledPlugins() {
        return plugins.values().stream()
                .filter(PluginDescriptor::isEnabled)
                .toList();
    }

    /**
     * 获取插件描述符
     *
     * @param pluginId 插件ID
     * @return 插件描述符，如果未找到则返回 null
     */
    public PluginDescriptor getPlugin(String pluginId) {
        return plugins.get(pluginId);
    }

    /**
     * 获取插件描述符（Optional 版本）
     *
     * @param pluginId 插件ID
     * @return 插件描述符的 Optional 包装
     */
    public Optional<PluginDescriptor> findPlugin(String pluginId) {
        return Optional.ofNullable(plugins.get(pluginId));
    }

    /**
     * 检查插件是否已注册
     *
     * @param pluginId 插件ID
     * @return 是否已注册
     */
    public boolean isPluginRegistered(String pluginId) {
        return plugins.containsKey(pluginId);
    }

    /**
     * 检查插件是否已启用
     *
     * @param pluginId 插件ID
     * @return 是否已启用
     */
    public boolean isPluginEnabled(String pluginId) {
        PluginDescriptor descriptor = plugins.get(pluginId);
        return descriptor != null && descriptor.isEnabled();
    }

    /**
     * 检查设备类型是否有对应的处理器
     *
     * @param deviceType 设备类型
     * @return 是否有处理器
     */
    public boolean hasHandler(String deviceType) {
        return handlers.containsKey(deviceType);
    }

    /**
     * 获取已注册的插件数量
     *
     * @return 插件数量
     */
    public int getPluginCount() {
        return plugins.size();
    }

    /**
     * 获取已启用的插件数量
     *
     * @return 已启用的插件数量
     */
    public int getEnabledPluginCount() {
        return (int) plugins.values().stream()
                .filter(PluginDescriptor::isEnabled)
                .count();
    }

    /**
     * 获取已注册的处理器数量
     *
     * @return 处理器数量
     */
    public int getHandlerCount() {
        return handlers.size();
    }

    /**
     * 获取所有支持的设备类型
     *
     * @return 设备类型集合
     */
    public Collection<String> getSupportedDeviceTypes() {
        return Collections.unmodifiableCollection(handlers.keySet());
    }
}
