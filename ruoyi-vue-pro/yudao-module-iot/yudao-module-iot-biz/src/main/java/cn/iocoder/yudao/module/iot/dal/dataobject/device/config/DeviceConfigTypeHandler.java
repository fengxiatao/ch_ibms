package cn.iocoder.yudao.module.iot.dal.dataobject.device.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * 设备配置类型处理器
 * <p>
 * MyBatis TypeHandler,用于自动处理 DeviceConfig 对象与数据库 JSON 字段之间的转换。
 * 在查询时将 JSON 字符串反序列化为 DeviceConfig 对象,
 * 在插入/更新时将 DeviceConfig 对象序列化为 JSON 字符串。
 *
 * @author system
 * @since 2024-12-11
 */
@Slf4j
@MappedTypes(DeviceConfig.class)
public class DeviceConfigTypeHandler extends BaseTypeHandler<DeviceConfig> {

    /**
     * JSON 序列化/反序列化工具
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 配置工厂,用于创建配置对象
     */
    private final DeviceConfigFactory configFactory;

    /**
     * 默认构造函数
     * 注意: MyBatis会使用无参构造函数创建TypeHandler实例
     */
    public DeviceConfigTypeHandler() {
        this.configFactory = new DeviceConfigFactory();
    }

    /**
     * 设置非空参数到PreparedStatement
     *
     * @param ps        PreparedStatement对象
     * @param i         参数索引
     * @param parameter DeviceConfig参数
     * @param jdbcType  JDBC类型
     * @throws SQLException 如果序列化失败
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, 
                                     DeviceConfig parameter, JdbcType jdbcType) 
            throws SQLException {
        try {
            // 将配置对象转换为Map
            Map<String, Object> map = parameter.toMap();
            
            // 序列化为JSON字符串
            String json = OBJECT_MAPPER.writeValueAsString(map);
            
            // 设置到PreparedStatement
            ps.setString(i, json);
            
            log.debug("序列化设备配置: deviceType={}, json={}", parameter.getDeviceType(), json);
            
        } catch (JsonProcessingException e) {
            log.error("序列化设备配置失败: {}", e.getMessage(), e);
            throw new SQLException("Failed to serialize DeviceConfig: " + e.getMessage(), e);
        }
    }

    /**
     * 从ResultSet获取可空结果
     *
     * @param rs         ResultSet对象
     * @param columnName 列名
     * @return DeviceConfig对象,如果为null则返回null
     * @throws SQLException 如果反序列化失败
     */
    @Override
    public DeviceConfig getNullableResult(ResultSet rs, String columnName) 
            throws SQLException {
        String json = rs.getString(columnName);
        return parseConfig(json);
    }

    /**
     * 从ResultSet获取可空结果
     *
     * @param rs          ResultSet对象
     * @param columnIndex 列索引
     * @return DeviceConfig对象,如果为null则返回null
     * @throws SQLException 如果反序列化失败
     */
    @Override
    public DeviceConfig getNullableResult(ResultSet rs, int columnIndex) 
            throws SQLException {
        String json = rs.getString(columnIndex);
        return parseConfig(json);
    }

    /**
     * 从CallableStatement获取可空结果
     *
     * @param cs          CallableStatement对象
     * @param columnIndex 列索引
     * @return DeviceConfig对象,如果为null则返回null
     * @throws SQLException 如果反序列化失败
     */
    @Override
    public DeviceConfig getNullableResult(CallableStatement cs, int columnIndex) 
            throws SQLException {
        String json = cs.getString(columnIndex);
        return parseConfig(json);
    }

    /**
     * 解析JSON字符串为DeviceConfig对象
     *
     * @param json JSON字符串
     * @return DeviceConfig对象,如果json为null或空则返回null
     * @throws SQLException 如果解析失败
     */
    private DeviceConfig parseConfig(String json) throws SQLException {
        // 处理null和空字符串
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            // 反序列化为Map
            Map<String, Object> map = OBJECT_MAPPER.readValue(json, 
                new TypeReference<Map<String, Object>>() {});
            
            // 获取设备类型
            String deviceType = (String) map.get("deviceType");
            
            // 如果没有deviceType或类型未注册或是GENERIC类型，直接使用GenericDeviceConfig（跳过Schema验证）
            if (deviceType == null || deviceType.trim().isEmpty() 
                    || !configFactory.isRegistered(deviceType)
                    || "GENERIC".equalsIgnoreCase(deviceType)) {
                log.debug("使用GenericDeviceConfig处理配置: deviceType={}", deviceType);
                GenericDeviceConfig genericConfig = new GenericDeviceConfig();
                genericConfig.fromMap(map);
                return genericConfig;
            }
            
            // 使用工厂创建配置对象（会进行Schema验证）
            DeviceConfig config = configFactory.createConfig(deviceType, map);
            
            log.debug("反序列化设备配置: deviceType={}", deviceType);
            return config;
            
        } catch (JsonProcessingException e) {
            log.error("反序列化设备配置失败: json={}, error={}", json, e.getMessage(), e);
            throw new SQLException("Failed to deserialize DeviceConfig: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            // 如果创建失败，尝试使用GenericDeviceConfig
            log.warn("创建特定设备配置失败，使用GenericDeviceConfig: error={}", e.getMessage());
            try {
                Map<String, Object> map = OBJECT_MAPPER.readValue(json, 
                    new TypeReference<Map<String, Object>>() {});
                GenericDeviceConfig genericConfig = new GenericDeviceConfig();
                genericConfig.fromMap(map);
                return genericConfig;
            } catch (JsonProcessingException ex) {
                throw new SQLException("Failed to create DeviceConfig: " + e.getMessage(), e);
            }
        }
    }

}
