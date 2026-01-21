package cn.iocoder.yudao.module.iot.dal.dataobject.device.config;

import jakarta.validation.ValidationException;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用设备配置
 * <p>
 * 用于处理没有特定配置类型的设备，或者临时创建的设备对象。
 * 支持任意键值对的配置数据。
 *
 * @author system
 * @since 2024-12-11
 */
@Data
@NoArgsConstructor
public class GenericDeviceConfig implements DeviceConfig {

    /**
     * 设备类型标识
     */
    private String deviceType = "GENERIC";

    /**
     * 设备 IP 地址（可选）
     */
    private String ipAddress;

    /**
     * 设备端口号（可选）
     */
    private Integer port;

    /**
     * 配置数据
     */
    private Map<String, Object> data = new HashMap<>();

    /**
     * 从 Map 创建配置
     *
     * @param data 配置数据
     * @return 配置对象
     */
    public static GenericDeviceConfig fromData(Map<String, Object> data) {
        GenericDeviceConfig config = new GenericDeviceConfig();
        if (data != null) {
            config.fromMap(data);
        }
        return config;
    }

    @Override
    public String getDeviceType() {
        return deviceType;
    }

    @Override
    public void validate() throws ValidationException {
        // 通用配置不做特殊验证
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>(data);
        map.put("deviceType", deviceType);
        if (ipAddress != null) {
            map.put("ipAddress", ipAddress);
        }
        if (port != null) {
            map.put("port", port);
        }
        return map;
    }

    @Override
    public void fromMap(Map<String, Object> map) {
        if (map != null) {
            this.data = new HashMap<>(map);
            Object type = map.get("deviceType");
            if (type != null) {
                this.deviceType = type.toString();
            }
            // 提取 ipAddress
            Object ipObj = map.get("ipAddress");
            if (ipObj instanceof String) {
                this.ipAddress = (String) ipObj;
            }
            // 提取 port
            Object portObj = map.get("port");
            if (portObj instanceof Integer) {
                this.port = (Integer) portObj;
            } else if (portObj instanceof Number) {
                this.port = ((Number) portObj).intValue();
            } else if (portObj instanceof String) {
                try {
                    this.port = Integer.parseInt((String) portObj);
                } catch (NumberFormatException e) {
                    // 忽略无效值
                }
            }
        }
    }

    /**
     * 获取配置值
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return data.get(key);
    }

    /**
     * 设置配置值
     *
     * @param key   键
     * @param value 值
     */
    public void set(String key, Object value) {
        data.put(key, value);
    }
}
