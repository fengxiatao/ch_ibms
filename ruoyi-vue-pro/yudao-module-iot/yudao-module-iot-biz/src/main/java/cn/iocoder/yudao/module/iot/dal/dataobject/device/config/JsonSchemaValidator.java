package cn.iocoder.yudao.module.iot.dal.dataobject.device.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.validation.ValidationException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * JSON Schema 验证器
 * <p>
 * 用于验证设备配置是否符合对应的 JSON Schema 定义。
 * Schema 文件存放在 resources/schemas 目录下,按设备类型命名。
 * <p>
 * 验证器会缓存已加载的 Schema 对象,避免重复解析。
 *
 * @author system
 * @since 2024-12-11
 */
@Slf4j
@Component
public class JsonSchemaValidator {

    /**
     * Schema 缓存
     * Key: 设备类型(小写), Value: JsonSchema 对象
     */
    private final Map<String, JsonSchema> schemaCache = new ConcurrentHashMap<>();

    /**
     * JSON Schema 工厂
     */
    private final JsonSchemaFactory schemaFactory = 
        JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);

    /**
     * JSON 对象映射器
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 验证设备配置是否符合 JSON Schema
     *
     * @param deviceType 设备类型标识(如 "ACCESS", "DETONG")
     * @param data       配置数据 Map
     * @throws ValidationException 如果配置不符合 Schema 定义
     */
    public void validate(String deviceType, Map<String, Object> data) throws ValidationException {
        if (deviceType == null || deviceType.trim().isEmpty()) {
            throw new ValidationException("设备类型不能为空");
        }

        if (data == null || data.isEmpty()) {
            throw new ValidationException("配置数据不能为空");
        }

        try {
            // 获取 Schema
            JsonSchema schema = getSchema(deviceType);

            // 将 Map 转换为 JsonNode
            JsonNode jsonNode = objectMapper.valueToTree(data);

            // 执行验证
            Set<ValidationMessage> errors = schema.validate(jsonNode);

            // 如果有错误,抛出异常
            if (!errors.isEmpty()) {
                String errorMessage = errors.stream()
                        .map(ValidationMessage::getMessage)
                        .collect(Collectors.joining("; "));

                log.warn("设备配置验证失败 - 设备类型: {}, 错误: {}", deviceType, errorMessage);
                throw new ValidationException("设备配置验证失败: " + errorMessage);
            }

            log.debug("设备配置验证成功 - 设备类型: {}", deviceType);

        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("设备配置验证异常 - 设备类型: {}", deviceType, e);
            throw new ValidationException("设备配置验证异常: " + e.getMessage(), e);
        }
    }

    /**
     * 获取指定设备类型的 JSON Schema
     * <p>
     * Schema 文件命名规则: {deviceType}-device-config.json (小写)
     * 例如: access-device-config.json, detong-device-config.json
     *
     * @param deviceType 设备类型标识
     * @return JsonSchema 对象
     * @throws IllegalArgumentException 如果 Schema 文件不存在
     */
    private JsonSchema getSchema(String deviceType) {
        String deviceTypeLower = deviceType.toLowerCase();

        return schemaCache.computeIfAbsent(deviceTypeLower, type -> {
            String schemaPath = "/schemas/" + type + "-device-config.json";

            log.debug("加载 JSON Schema: {}", schemaPath);

            InputStream schemaStream = getClass().getResourceAsStream(schemaPath);
            if (schemaStream == null) {
                log.error("JSON Schema 文件不存在: {}", schemaPath);
                throw new IllegalArgumentException(
                        "未找到设备类型 '" + deviceType + "' 的 Schema 定义文件: " + schemaPath);
            }

            try {
                JsonSchema schema = schemaFactory.getSchema(schemaStream);
                log.info("成功加载 JSON Schema: {}", schemaPath);
                return schema;
            } catch (Exception e) {
                log.error("加载 JSON Schema 失败: {}", schemaPath, e);
                throw new IllegalArgumentException(
                        "加载设备类型 '" + deviceType + "' 的 Schema 定义失败: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 清除 Schema 缓存
     * <p>
     * 用于测试或在 Schema 文件更新后重新加载
     */
    public void clearCache() {
        schemaCache.clear();
        log.info("JSON Schema 缓存已清除");
    }

    /**
     * 清除指定设备类型的 Schema 缓存
     *
     * @param deviceType 设备类型标识
     */
    public void clearCache(String deviceType) {
        if (deviceType != null) {
            schemaCache.remove(deviceType.toLowerCase());
            log.info("已清除设备类型 '{}' 的 JSON Schema 缓存", deviceType);
        }
    }

    /**
     * 获取已缓存的 Schema 数量
     *
     * @return 缓存的 Schema 数量
     */
    public int getCacheSize() {
        return schemaCache.size();
    }

}
