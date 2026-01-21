package cn.iocoder.yudao.module.iot.util;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.DeviceConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 设备厂商提取工具类（biz 模块版本）
 * <p>
 * 此类委托给 core 模块的 {@link cn.iocoder.yudao.module.iot.core.util.VendorExtractor} 实现，
 * 并额外支持从 DeviceConfig 对象提取厂商信息。
 * <p>
 * 用于从设备配置中提取厂商信息，支持：
 * <ul>
 *   <li>从 DeviceConfig 对象提取</li>
 *   <li>从 JSON 字符串提取</li>
 *   <li>从 Map 提取（支持嵌套结构如 GenericDeviceConfig.data）</li>
 *   <li>根据端口号推断厂商</li>
 * </ul>
 *
 * @author 长辉信息科技有限公司
 * @since 2024-12-17
 * @see cn.iocoder.yudao.module.iot.core.util.VendorExtractor
 */
@Slf4j
public class VendorExtractor {

    /** 大华设备默认端口 */
    public static final int DAHUA_DEFAULT_PORT = cn.iocoder.yudao.module.iot.core.util.VendorExtractor.DAHUA_DEFAULT_PORT;
    /** 海康设备默认端口 */
    public static final int HIKVISION_DEFAULT_PORT = cn.iocoder.yudao.module.iot.core.util.VendorExtractor.HIKVISION_DEFAULT_PORT;
    /** ONVIF 默认端口 */
    public static final int ONVIF_DEFAULT_PORT = cn.iocoder.yudao.module.iot.core.util.VendorExtractor.ONVIF_DEFAULT_PORT;

    /** 厂商标识：大华 */
    public static final String VENDOR_DAHUA = cn.iocoder.yudao.module.iot.core.util.VendorExtractor.VENDOR_DAHUA;
    /** 厂商标识：海康 */
    public static final String VENDOR_HIKVISION = cn.iocoder.yudao.module.iot.core.util.VendorExtractor.VENDOR_HIKVISION;
    /** 厂商标识：ONVIF */
    public static final String VENDOR_ONVIF = cn.iocoder.yudao.module.iot.core.util.VendorExtractor.VENDOR_ONVIF;

    private VendorExtractor() {
        // 工具类，禁止实例化
    }

    /**
     * 从设备配置中提取厂商信息
     * <p>
     * 支持以下类型的配置：
     * <ul>
     *   <li>DeviceConfig 对象</li>
     *   <li>JSON 字符串</li>
     *   <li>Map 对象</li>
     * </ul>
     *
     * @param config 设备配置（可能是 JSON 字符串、DeviceConfig 对象或 Map）
     * @return 厂商标识，如 "Dahua"、"Hikvision"，或 null
     */
    @SuppressWarnings("unchecked")
    public static String extractVendor(Object config) {
        if (config == null) {
            return null;
        }

        // 如果是 DeviceConfig 对象，转换为 Map 后委托给 core 模块
        if (config instanceof DeviceConfig) {
            Map<String, Object> configMap = ((DeviceConfig) config).toMap();
            return cn.iocoder.yudao.module.iot.core.util.VendorExtractor.extractVendorFromMap(configMap);
        }

        // 其他类型委托给 core 模块处理
        return cn.iocoder.yudao.module.iot.core.util.VendorExtractor.extractVendor(config);
    }

    /**
     * 从 Map 中提取厂商信息
     *
     * @param configMap 配置 Map
     * @return 厂商标识，或 null
     */
    public static String extractVendorFromMap(Map<String, Object> configMap) {
        return cn.iocoder.yudao.module.iot.core.util.VendorExtractor.extractVendorFromMap(configMap);
    }

    /**
     * 根据其他配置字段推断厂商
     *
     * @param configMap 配置 Map
     * @return 推断的厂商标识，或 null
     */
    public static String inferVendor(Map<String, Object> configMap) {
        return cn.iocoder.yudao.module.iot.core.util.VendorExtractor.inferVendor(configMap);
    }

    /**
     * 标准化厂商名称
     *
     * @param vendor 原始厂商名称
     * @return 标准化后的厂商名称
     */
    public static String normalizeVendor(String vendor) {
        return cn.iocoder.yudao.module.iot.core.util.VendorExtractor.normalizeVendor(vendor);
    }
}
