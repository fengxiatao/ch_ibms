package cn.iocoder.yudao.module.iot.dal.dataobject.device.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.ValidationException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 长辉设备配置
 * <p>
 * 存储长辉TCP设备的特有配置信息，包括测站编码、TEA密钥、密码等。
 * 配置数据以 JSON 格式存储在 iot_device 表的 config 字段中。
 * <p>
 * 基于全渠道量测水设施数据传输规约V2.3（IP9500_OPC协议）
 *
 * @author system
 * @since 2024-12-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChanghuiDeviceConfig implements DeviceConfig {

    /**
     * 测站编码格式验证正则表达式
     * 4-20位字母数字字符
     */
    private static final Pattern STATION_CODE_PATTERN = Pattern.compile("^[A-Za-z0-9]{4,20}$");

    /**
     * 测站编码（唯一标识）
     * <p>
     * 10字节：行政区代码(2B) + 管理处代码(1B) + 站所代码(1B) + 桩号(3B) + 设备类型(1B) + 设备厂家(1B) + 顺序编号(1B)
     */
    private String stationCode;

    /**
     * TEA加密密钥（JSON数组格式）
     * <p>
     * 格式如: [1234567890, 1234567890, 1234567890, 1234567890]
     */
    private String teaKey;

    /**
     * 设备密码
     */
    private String password;

    /**
     * 行政区代码（省编码）
     */
    private String provinceCode;

    /**
     * 管理处代码（管理单位编码）
     */
    private String managementCode;

    /**
     * 站所代码（测站编码部分）
     */
    private String stationCodePart;

    /**
     * 桩号（前）
     */
    private String pileFront;

    /**
     * 桩号（后）
     */
    private String pileBack;

    /**
     * 设备厂家/制造商
     */
    private String manufacturer;

    /**
     * 顺序编号/序列号
     */
    private String sequenceNo;

    /**
     * 长辉设备子类型
     * <p>
     * 1-测控一体化闸门,2-测控分体式闸门,3-退水闸,4-节制闸,5-进水闸,6-水位计,7-流量计,8-流速仪,9-渗压计
     * @see cn.iocoder.yudao.module.iot.enums.device.ChanghuiDeviceTypeConstants
     */
    private Integer changhuiDeviceType;

    @Override
    public String getDeviceType() {
        return "CHANGHUI";
    }

    @Override
    public void validate() throws ValidationException {
        StringBuilder errors = new StringBuilder();

        // 验证测站编码（必填）
        if (stationCode == null || stationCode.trim().isEmpty()) {
            errors.append("测站编码不能为空; ");
        } else if (!STATION_CODE_PATTERN.matcher(stationCode).matches()) {
            errors.append("测站编码格式不正确，应为4-20位字母或数字; ");
        }

        // 验证TEA密钥格式（可选，但如果提供则必须是有效的JSON数组格式）
        if (teaKey != null && !teaKey.trim().isEmpty()) {
            if (!teaKey.startsWith("[") || !teaKey.endsWith("]")) {
                errors.append("TEA密钥格式不正确，应为JSON数组格式; ");
            }
        }

        // 验证长辉设备子类型（可选，但如果提供则必须在有效范围内）
        if (changhuiDeviceType != null) {
            if (changhuiDeviceType < 1 || changhuiDeviceType > 9) {
                errors.append("长辉设备子类型超出范围，应在1-9之间; ");
            }
        }

        // 如果有错误，抛出异常
        if (errors.length() > 0) {
            throw new ValidationException("长辉设备配置验证失败: " + errors.toString());
        }
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceType", getDeviceType());
        map.put("stationCode", stationCode);
        map.put("teaKey", teaKey);
        map.put("password", password);
        map.put("provinceCode", provinceCode);
        map.put("managementCode", managementCode);
        map.put("stationCodePart", stationCodePart);
        map.put("pileFront", pileFront);
        map.put("pileBack", pileBack);
        map.put("manufacturer", manufacturer);
        map.put("sequenceNo", sequenceNo);
        map.put("changhuiDeviceType", changhuiDeviceType);
        return map;
    }

    @Override
    public void fromMap(Map<String, Object> map) {
        if (map == null) {
            return;
        }

        // 提取字段值，处理类型转换
        this.stationCode = (String) map.get("stationCode");
        this.teaKey = (String) map.get("teaKey");
        this.password = (String) map.get("password");
        this.provinceCode = (String) map.get("provinceCode");
        this.managementCode = (String) map.get("managementCode");
        this.stationCodePart = (String) map.get("stationCodePart");
        this.pileFront = (String) map.get("pileFront");
        this.pileBack = (String) map.get("pileBack");
        this.manufacturer = (String) map.get("manufacturer");
        this.sequenceNo = (String) map.get("sequenceNo");

        // 长辉设备子类型可能是 Integer 或 Number
        Object typeObj = map.get("changhuiDeviceType");
        if (typeObj instanceof Integer) {
            this.changhuiDeviceType = (Integer) typeObj;
        } else if (typeObj instanceof Number) {
            this.changhuiDeviceType = ((Number) typeObj).intValue();
        } else if (typeObj instanceof String) {
            try {
                this.changhuiDeviceType = Integer.parseInt((String) typeObj);
            } catch (NumberFormatException e) {
                // 忽略无效值
                this.changhuiDeviceType = null;
            }
        }
    }

}
