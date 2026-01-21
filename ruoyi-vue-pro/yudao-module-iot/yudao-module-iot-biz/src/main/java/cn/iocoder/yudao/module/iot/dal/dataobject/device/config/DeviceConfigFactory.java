package cn.iocoder.yudao.module.iot.dal.dataobject.device.config;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 设备配置工厂
 * <p>
 * 负责根据设备类型创建对应的配置对象。支持配置类型的动态注册和创建。
 * 集成 JSON Schema 验证,确保配置数据的有效性。
 *
 * @author system
 * @since 2024-12-11
 */
@Slf4j
@Component
public class DeviceConfigFactory {

    /**
     * 配置类型注册表
     * Key: 设备类型标识(如 "ACCESS", "DETONG")
     * Value: 对应的配置类Class对象
     */
    private final Map<String, Class<? extends DeviceConfig>> configRegistry = new HashMap<>();

    /**
     * JSON Schema 验证器
     */
    private final JsonSchemaValidator schemaValidator;

    /**
     * 默认构造函数,初始化时注册所有已知的配置类型
     * 使用默认的 JsonSchemaValidator
     */
    public DeviceConfigFactory() {
        this.schemaValidator = new JsonSchemaValidator();
        registerConfigTypes();
    }

    /**
     * 构造函数,初始化时注册所有已知的配置类型
     *
     * @param schemaValidator JSON Schema 验证器
     */
    public DeviceConfigFactory(JsonSchemaValidator schemaValidator) {
        this.schemaValidator = schemaValidator != null ? schemaValidator : new JsonSchemaValidator();
        registerConfigTypes();
    }

    /**
     * 注册所有已知的设备配置类型
     */
    private void registerConfigTypes() {
        configRegistry.put("ACCESS", AccessDeviceConfig.class);
        configRegistry.put("CHANGHUI", ChanghuiDeviceConfig.class);
        configRegistry.put("GENERIC", GenericDeviceConfig.class);
        log.info("设备配置工厂初始化完成,已注册配置类型: {}", configRegistry.keySet());
    }

    /**
     * 根据设备类型和配置数据创建配置对象
     * <p>
     * 创建过程包括:
     * 1. JSON Schema 验证
     * 2. 反序列化为配置对象
     * 3. 业务逻辑验证
     *
     * @param deviceType 设备类型标识
     * @param data       配置数据Map
     * @return 创建的配置对象
     * @throws IllegalArgumentException 如果设备类型未注册或配置数据无效
     */
    public DeviceConfig createConfig(String deviceType, Map<String, Object> data) {
        if (deviceType == null || deviceType.trim().isEmpty()) {
            throw new IllegalArgumentException("设备类型不能为空");
        }

        if (data == null) {
            throw new IllegalArgumentException("配置数据不能为空");
        }

        // 获取配置类
        Class<? extends DeviceConfig> configClass = configRegistry.get(deviceType);
        if (configClass == null) {
            throw new IllegalArgumentException("未知的设备类型: " + deviceType + 
                ", 已注册的类型: " + configRegistry.keySet());
        }

        try {
            // 1. JSON Schema 验证
            schemaValidator.validate(deviceType, data);
            
            // 2. 创建配置对象实例
            DeviceConfig config = configClass.getDeclaredConstructor().newInstance();
            
            // 3. 从Map反序列化
            config.fromMap(data);
            
            // 4. 业务逻辑验证
            config.validate();
            
            log.debug("成功创建设备配置: deviceType={}, configClass={}", deviceType, configClass.getSimpleName());
            return config;
            
        } catch (Exception e) {
            log.error("创建设备配置失败: deviceType={}, error={}", deviceType, e.getMessage(), e);
            throw new IllegalArgumentException("创建设备配置失败: " + e.getMessage(), e);
        }
    }

    /**
     * 动态注册新的配置类型
     * <p>
     * 允许在运行时注册新的设备配置类型,支持系统扩展
     *
     * @param deviceType  设备类型标识
     * @param configClass 配置类Class对象
     */
    public void registerConfigType(String deviceType, Class<? extends DeviceConfig> configClass) {
        if (deviceType == null || deviceType.trim().isEmpty()) {
            throw new IllegalArgumentException("设备类型不能为空");
        }
        
        if (configClass == null) {
            throw new IllegalArgumentException("配置类不能为空");
        }

        configRegistry.put(deviceType, configClass);
        log.info("注册新的设备配置类型: deviceType={}, configClass={}", deviceType, configClass.getSimpleName());
    }

    /**
     * 检查设备类型是否已注册
     *
     * @param deviceType 设备类型标识
     * @return true表示已注册,false表示未注册
     */
    public boolean isRegistered(String deviceType) {
        return configRegistry.containsKey(deviceType);
    }

    /**
     * 获取所有已注册的设备类型
     *
     * @return 设备类型集合
     */
    public java.util.Set<String> getRegisteredTypes() {
        return new java.util.HashSet<>(configRegistry.keySet());
    }

}
