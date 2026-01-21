package cn.iocoder.yudao.module.iot.dal.dataobject.device.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.ValidationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 门禁设备配置
 * <p>
 * 存储门禁设备的特有配置信息，包括网络连接、认证信息、固件版本等。
 * 配置数据以 JSON 格式存储在 iot_device 表的 config 字段中。
 *
 * @author system
 * @since 2024-12-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessDeviceConfig implements DeviceConfig {

    /**
     * IP 地址格式验证正则表达式
     */
    private static final Pattern IP_PATTERN = Pattern.compile(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );

    /**
     * 日期时间格式化器
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * 设备 IP 地址
     */
    private String ipAddress;

    /**
     * 设备端口号
     */
    private Integer port;

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 登录密码（加密存储）
     */
    private String password;

    /**
     * 固件版本号
     */
    private String firmwareVersion;

    /**
     * SDK 版本号
     */
    private String sdkVersion;

    /**
     * 是否已激活
     */
    private Boolean activated;

    /**
     * 激活时间
     */
    private LocalDateTime activationTime;

    /**
     * 是否支持视频预览
     * <p>
     * 人脸一体机通常都支持视频预览功能
     */
    private Boolean supportVideo;

    /**
     * HTTP 端口（用于 RTSP over WebSocket）
     * <p>
     * 大华设备默认为 80
     */
    private Integer httpPort;

    /**
     * RTSP 端口
     * <p>
     * 大华设备默认为 554
     */
    private Integer rtspPort;

    /**
     * 门禁能力快照（来自网关能力查询）
     *
     * <p>用于业务侧进行“按能力下发/撤销”，避免默认所有设备都支持人脸/指纹等能力导致误操作。</p>
     *
     * <p>建议结构（与 newgateway 返回字段保持一致）：</p>
     * <pre>
     * {
     *   "hasCard": true,
     *   "hasFace": false,
     *   "hasFingerprint": false,
     *   "maxCardCount": 10000,
     *   "maxFaceCount": 0,
     *   "maxFingerprintCount": 0,
     *   "maxDoorCount": 4,
     *   "updatedAt": "2026-01-14T20:02:35"
     * }
     * </pre>
     */
    private Map<String, Object> accessCapabilities;

    /**
     * 能力快照更新时间
     * <p>用于判断能力信息是否需要刷新</p>
     */
    private LocalDateTime capabilityTime;

    @Override
    public String getDeviceType() {
        return "ACCESS";
    }

    @Override
    public void validate() throws ValidationException {
        StringBuilder errors = new StringBuilder();

        // 验证 IP 地址
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            errors.append("IP地址不能为空; ");
        } else if (!IP_PATTERN.matcher(ipAddress).matches()) {
            errors.append("IP地址格式不正确，应为有效的IPv4地址; ");
        }

        // 验证端口号
        if (port == null) {
            errors.append("端口号不能为空; ");
        } else if (port < 1 || port > 65535) {
            errors.append("端口号超出范围，应在1-65535之间; ");
        }

        // 验证用户名（可选，但如果提供则不能为空字符串）
        if (username != null && username.trim().isEmpty()) {
            errors.append("用户名不能为空字符串; ");
        }

        // 验证密码（可选，但如果提供则不能为空字符串）
        if (password != null && password.trim().isEmpty()) {
            errors.append("密码不能为空字符串; ");
        }

        // 验证固件版本格式（可选，但如果提供则应符合版本号格式）
        if (firmwareVersion != null && !firmwareVersion.trim().isEmpty()) {
            if (!firmwareVersion.matches("^\\d+\\.\\d+\\.\\d+.*$")) {
                errors.append("固件版本号格式不正确，应为 x.y.z 格式; ");
            }
        }

        // 如果有错误，抛出异常
        if (errors.length() > 0) {
            throw new ValidationException("门禁设备配置验证失败: " + errors.toString());
        }
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceType", getDeviceType());
        map.put("ipAddress", ipAddress);
        map.put("port", port);
        map.put("username", username);
        map.put("password", password);
        map.put("firmwareVersion", firmwareVersion);
        map.put("sdkVersion", sdkVersion);
        map.put("activated", activated);
        map.put("supportVideo", supportVideo);
        map.put("httpPort", httpPort);
        map.put("rtspPort", rtspPort);
        if (accessCapabilities != null) {
            map.put("accessCapabilities", accessCapabilities);
        }
        
        // LocalDateTime 转换为字符串
        if (activationTime != null) {
            map.put("activationTime", activationTime.format(DATE_TIME_FORMATTER));
        }
        if (capabilityTime != null) {
            map.put("capabilityTime", capabilityTime.format(DATE_TIME_FORMATTER));
        }
        
        return map;
    }

    @Override
    public void fromMap(Map<String, Object> map) {
        if (map == null) {
            return;
        }

        // 提取字段值，处理类型转换
        this.ipAddress = (String) map.get("ipAddress");
        
        // 端口号可能是 Integer 或 String
        Object portObj = map.get("port");
        if (portObj instanceof Integer) {
            this.port = (Integer) portObj;
        } else if (portObj instanceof String) {
            try {
                this.port = Integer.parseInt((String) portObj);
            } catch (NumberFormatException e) {
                // 忽略无效值
            }
        }
        
        this.username = (String) map.get("username");
        this.password = (String) map.get("password");
        this.firmwareVersion = (String) map.get("firmwareVersion");
        this.sdkVersion = (String) map.get("sdkVersion");
        
        // 布尔值可能是 Boolean 或 String
        Object activatedObj = map.get("activated");
        if (activatedObj instanceof Boolean) {
            this.activated = (Boolean) activatedObj;
        } else if (activatedObj instanceof String) {
            this.activated = Boolean.parseBoolean((String) activatedObj);
        }

        // 视频支持标记
        Object supportVideoObj = map.get("supportVideo");
        if (supportVideoObj instanceof Boolean) {
            this.supportVideo = (Boolean) supportVideoObj;
        } else if (supportVideoObj instanceof String) {
            this.supportVideo = Boolean.parseBoolean((String) supportVideoObj);
        }

        // HTTP 端口
        Object httpPortObj = map.get("httpPort");
        if (httpPortObj instanceof Integer) {
            this.httpPort = (Integer) httpPortObj;
        } else if (httpPortObj instanceof Number) {
            this.httpPort = ((Number) httpPortObj).intValue();
        } else if (httpPortObj instanceof String) {
            try {
                this.httpPort = Integer.parseInt((String) httpPortObj);
            } catch (NumberFormatException e) {
                // 忽略无效值
            }
        }

        // RTSP 端口
        Object rtspPortObj = map.get("rtspPort");
        if (rtspPortObj instanceof Integer) {
            this.rtspPort = (Integer) rtspPortObj;
        } else if (rtspPortObj instanceof Number) {
            this.rtspPort = ((Number) rtspPortObj).intValue();
        } else if (rtspPortObj instanceof String) {
            try {
                this.rtspPort = Integer.parseInt((String) rtspPortObj);
            } catch (NumberFormatException e) {
                // 忽略无效值
            }
        }
        
        // 激活时间可能是字符串或 LocalDateTime
        Object activationTimeObj = map.get("activationTime");
        if (activationTimeObj instanceof String) {
            try {
                this.activationTime = LocalDateTime.parse((String) activationTimeObj, DATE_TIME_FORMATTER);
            } catch (Exception e) {
                // 忽略无效值
            }
        } else if (activationTimeObj instanceof LocalDateTime) {
            this.activationTime = (LocalDateTime) activationTimeObj;
        }

        // 能力快照
        Object capObj = map.get("accessCapabilities");
        if (capObj instanceof Map<?, ?>) {
            // 保守转换：JSON 反序列化后 key/value 类型不保证严格为 String/Object
            Map<String, Object> caps = new HashMap<>();
            ((Map<?, ?>) capObj).forEach((k, v) -> {
                if (k != null) {
                    caps.put(String.valueOf(k), v);
                }
            });
            this.accessCapabilities = caps;
        }
        Object capabilityTimeObj = map.get("capabilityTime");
        if (capabilityTimeObj instanceof String) {
            try {
                this.capabilityTime = LocalDateTime.parse((String) capabilityTimeObj, DATE_TIME_FORMATTER);
            } catch (Exception e) {
                // ignore
            }
        } else if (capabilityTimeObj instanceof LocalDateTime) {
            this.capabilityTime = (LocalDateTime) capabilityTimeObj;
        }
    }

}
